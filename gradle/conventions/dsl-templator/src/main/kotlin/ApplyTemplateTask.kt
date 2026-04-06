/*
 * Copyright (c) 2026, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.ktmongo.build

import org.antlr.v4.runtime.CommonTokenStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

/**
 * Copies the `dsl-template` sources into this project, adding convenience overloads in the process.
 *
 * To learn more, see [the :dsl-template README](../dsl-template/README.md).
 */
abstract class ApplyTemplateTask : DefaultTask() {

	@get:Incremental
	@get:InputDirectory
	abstract val sourceDir: DirectoryProperty

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	@get:Input
	abstract val projectRootDir: Property<File>

	@TaskAction
	fun generate(changes: InputChanges) {
		val src = sourceDir.get().asFile
		val dest = outputDir.get().asFile

		changes.getFileChanges(sourceDir).forEach { change ->
			val relative = change.file.relativeTo(src)
			val destFile = dest.resolve(relative)

			when (change.changeType) {
				ChangeType.REMOVED -> destFile.delete()
				ChangeType.ADDED, ChangeType.MODIFIED -> {
					destFile.parentFile.mkdirs()
					logger.info("Templating file $destFile")
					destFile.writeText(change.file.readText().applyTemplate(change.file.relativeTo(projectRootDir.get())))
				}
			}
		}
	}

	private fun String.applyTemplate(sourceFile: File): String {
		val source = this
		// ANTLRInputStream uses Java char indices (not Unicode code-point indices), which is required
		// so that token startIndex/stopIndex values are consistent with String.substring() calls below.
		// CharStreams.fromString() uses code-point indices, which diverge from char indices when the
		// source contains characters outside the BMP (e.g. emoji in KDoc comments), causing wrong text
		// extraction for any token that appears after such a character.
		@Suppress("DEPRECATION")
		val stream = org.antlr.v4.runtime.ANTLRInputStream(source)
		val lexer = opensavvy.ktmongo.build.kotlin.KotlinLexer(stream)
		val tokens = CommonTokenStream(lexer)
		tokens.fill()
		val parser = opensavvy.ktmongo.build.kotlin.KotlinParser(tokens)
		val tree = parser.kotlinFile()

		val rewriter = org.antlr.v4.runtime.TokenStreamRewriter(tokens)

		val sourceFilePath = sourceFile.path.replace('\\', '/')
		val isValueOverloadTarget = sourceFilePath.endsWith("aggregation/operators/ArithmeticValueOperators.kt") ||
			sourceFilePath.endsWith("aggregation/operators/ArrayValueOperators.kt") ||
			sourceFilePath.endsWith("aggregation/operators/ComparisonValueOperators.kt") ||
			sourceFilePath.endsWith("aggregation/operators/ConditionalValueOperators.kt") ||
			sourceFilePath.endsWith("aggregation/operators/StringValueOperators.kt") ||
			sourceFilePath.endsWith("aggregation/operators/TrigonometryValueOperators.kt")

		// Pre-scan: collect existing KProperty1 receiver functions/properties to avoid duplicates
		val existingKPropFunctions = mutableSetOf<Pair<String, Int>>() // (name, paramCount)
		val existingKPropProperties = mutableSetOf<String>() // property names
		val preScanWalker = org.antlr.v4.runtime.tree.ParseTreeWalker()
		val preScanListener = object : opensavvy.ktmongo.build.kotlin.KotlinParserBaseListener() {
			override fun exitFunctionDeclaration(ctx: opensavvy.ktmongo.build.kotlin.KotlinParser.FunctionDeclarationContext) {
				val receiverText = getReceiverText(ctx, source) ?: return
				if (!receiverText.startsWith("KProperty1<") && !receiverText.startsWith("kotlin.reflect.KProperty1<")) return
				val funcName = ctx.identifier()?.text ?: return
				val paramCount = ctx.functionValueParameters()?.functionValueParameter()?.size ?: 0
				existingKPropFunctions.add(Pair(funcName, paramCount))
			}

			override fun exitPropertyDeclaration(ctx: opensavvy.ktmongo.build.kotlin.KotlinParser.PropertyDeclarationContext) {
				val receiverTypeCtx = ctx.receiverType() ?: return
				if (ctx.DOT() == null) return
				val receiverText = source.substring(receiverTypeCtx.start.startIndex, receiverTypeCtx.stop.stopIndex + 1)
				if (!receiverText.startsWith("KProperty1<") && !receiverText.startsWith("kotlin.reflect.KProperty1<")) return
				val propName = ctx.variableDeclaration()?.simpleIdentifier()?.text ?: return
				existingKPropProperties.add(propName)
			}
		}
		preScanWalker.walk(preScanListener, tree)

		val walker = org.antlr.v4.runtime.tree.ParseTreeWalker()
		val listener = object : opensavvy.ktmongo.build.kotlin.KotlinParserBaseListener() {
			override fun exitFunctionDeclaration(ctx: opensavvy.ktmongo.build.kotlin.KotlinParser.FunctionDeclarationContext) {
				// region Value<...> overload generation (combinatorial: receiver × params)
				if (isValueOverloadTarget) {
					val vFuncStart0 = ctx.start.startIndex
					val vFunKeywordStart = ctx.FUN().symbol.startIndex
					if (!source.substring(vFuncStart0, vFunKeywordStart).contains("override")) {
						val vFuncName = ctx.identifier()?.text
						if (vFuncName != null) {
							val vFuncStart = ctx.start.startIndex
							val vFuncBody = ctx.functionBody()
							// Use the body start to determine the signature extent; if ANTLR misparses
							// due to constructs like 'T & Any' in type arguments, ctx.stop may be wrong,
							// so we bracket-count in the token stream to find the true end.
							val bodyStartInSource = vFuncBody?.start?.startIndex ?: -1
							val bodyStartInFunc = if (bodyStartInSource >= 0) bodyStartInSource - vFuncStart else -1
							val vFuncText = source.substring(
								vFuncStart,
								if (bodyStartInFunc >= 0) vFuncStart + bodyStartInFunc else ctx.stop.stopIndex + 1,
							)
							val vParamCtxList = ctx.functionValueParameters()?.functionValueParameter() ?: emptyList()

							// Collect all "Value<...>" positions: receiver first, then each param
							data class ValuePos(
								val isReceiver: Boolean,
								val paramIdx: Int, // -1 for receiver
								val contextType: String,
								val resultType: String,
								val typeStartInFunc: Int,
								val typeEndInFunc: Int,
								val isVararg: Boolean = false,
								// True when the original type was Value<...>? with a default of null.
								// Overloads use the non-null type without a default value.
								val isNullableWithNullDefault: Boolean = false,
								// Exclusive end of the default value text in vFuncText (covers " = null").
								// Only meaningful when isNullableWithNullDefault is true.
								val defaultValueEndInFunc: Int = -1,
							)

							val valuePositions = mutableListOf<ValuePos>()

							val receiverCtx = ctx.receiverType()
							if (receiverCtx != null) {
								val recText = source.substring(receiverCtx.start.startIndex, receiverCtx.stop.stopIndex + 1)
								extractValueTypeArgs(recText)?.let { (c, r, _) ->
									valuePositions.add(ValuePos(true, -1, c, r,
										receiverCtx.start.startIndex - vFuncStart,
										receiverCtx.stop.stopIndex + 1 - vFuncStart))
								}
							}

							for ((idx, paramCtx) in vParamCtxList.withIndex()) {
								val paramType = paramCtx.parameter()?.type() ?: continue
								val paramTypeText = source.substring(paramType.start.startIndex, paramType.stop.stopIndex + 1)
								val isVararg = paramCtx.modifierList()?.text?.contains("vararg") == true
								val hasNullDefault = paramCtx.expression()?.text == "null"
								extractValueTypeArgs(paramTypeText)?.let { (c, r, isNullable) ->
									// Nullable Value without a null default cannot be faithfully substituted.
									if (isNullable && !hasNullDefault) return@let
									val isNullableWithNullDefault = isNullable && hasNullDefault
									val defaultValueEndInFunc = if (isNullableWithNullDefault)
										paramCtx.expression()!!.stop.stopIndex + 1 - vFuncStart
									else -1
									valuePositions.add(ValuePos(false, idx, c, r,
										paramType.start.startIndex - vFuncStart,
										paramType.stop.stopIndex + 1 - vFuncStart,
										isVararg, isNullableWithNullDefault, defaultValueEndInFunc))
								}
							}

							if (valuePositions.isNotEmpty()) {
								// Alternatives per position:
								// - Receiver: [null=keep, Field, KProperty1, Result]
								// - Params:   [null=keep, Field, KProperty1, Result]
								// For vararg params, skip the raw Result alternative: 'of(array)' would wrap the
								// entire array as a single Value rather than mapping each element individually,
								// and Context cannot be inferred from a raw element type.
								val alternatives: List<List<String?>> = valuePositions.map { pos ->
									buildList {
										// null = keep original type (including "? = null" default for nullable-with-null-default positions)
										add(null)
										add("opensavvy.ktmongo.dsl.path.Field<${pos.contextType}, ${pos.resultType}>")
										add("kotlin.reflect.KProperty1<${pos.contextType}, ${pos.resultType}>")
										if (!pos.isVararg) add(pos.resultType)
									}
								}

								// Cartesian product of alternatives
								val combinations = alternatives.fold(listOf(listOf<String?>())) { acc, alts ->
									acc.flatMap { prev -> alts.map { prev + it } }
								}

								val recPosIdx = valuePositions.indexOfFirst { it.isReceiver }
								val hasValueReceiver = recPosIdx >= 0

								val valueOverloadBuilder = StringBuilder()

								for (combination in combinations) {
									if (combination.all { it == null }) continue // skip original

									// For 'div', skip KProperty1 overloads: they conflict with the
									// navigation operator KProperty1<Root,Parent>.div(KProperty1<Parent,Child>).
									if (vFuncName == "div" && combination.any { it != null && it.startsWith("kotlin.reflect.KProperty1") }) continue

									// Build the new signature by replacing positions in reverse order
									val replacements = valuePositions.zip(combination)
										.filter { (_, t) -> t != null }
										.sortedByDescending { (pos, _) -> pos.typeStartInFunc }
									val sigEnd = if (bodyStartInFunc >= 0) bodyStartInFunc else vFuncText.length
									var newSignature = vFuncText.substring(0, sigEnd)
									for ((pos, newType) in replacements) {
										val replaceEnd = if (pos.isNullableWithNullDefault && pos.defaultValueEndInFunc >= 0)
											pos.defaultValueEndInFunc
										else
											pos.typeEndInFunc
										newSignature = newSignature.substring(0, pos.typeStartInFunc) +
											newType!! +
											newSignature.substring(replaceEnd)
									}

									// When all Value<> positions are replaced by raw types, contextType no longer
									// appears in any receiver or parameter — only in the return type. The type
									// parameter cannot be inferred, so remove its declaration and fix the return
									// type to use Any instead.
									val contextTypeForFix = valuePositions.first().contextType
									val contextAbsentFromSignature = valuePositions.zip(combination).all { (_, t) ->
										t != null && !t.startsWith("opensavvy") && !t.startsWith("kotlin.reflect.KProperty1")
									}
									if (contextAbsentFromSignature && newSignature.contains(" <$contextTypeForFix : Any>")) {
										newSignature = newSignature.replace(" <$contextTypeForFix : Any>", "")
										val lastParenIdx = newSignature.lastIndexOf(')')
										if (lastParenIdx >= 0) {
											newSignature = newSignature.substring(0, lastParenIdx + 1) +
												newSignature.substring(lastParenIdx + 1).replace(contextTypeForFix, "Any")
										}
									}

									// Build the delegation call
									val receiverReplaced = hasValueReceiver && combination[recPosIdx] != null
									val receiverPrefix = when {
										receiverReplaced -> "of(this)."
										hasValueReceiver -> "this."
										else -> ""
									}
									// Skip params that ANTLR created during error recovery (they have no type
									// in the parse tree, e.g. a spurious 'Boolean' from 'Value<T & Any, Boolean>').
									// For vararg replaced params, map each element through of() individually
									// rather than wrapping the whole array.
									var seenVararg = false
									val delegationArgs = vParamCtxList.mapIndexedNotNull { idx, fp ->
										if (fp.parameter()?.type() == null) return@mapIndexedNotNull null
										val name = fp.parameter()?.simpleIdentifier()?.text
											?: return@mapIndexedNotNull null
										val isVararg = fp.modifierList()?.text?.contains("vararg") == true
										val afterVararg = seenVararg
										if (isVararg) seenVararg = true
										val paramPosIdx = valuePositions.indexOfFirst { !it.isReceiver && it.paramIdx == idx }
										val replaced = paramPosIdx >= 0 && combination[paramPosIdx] != null
										if (replaced) {
											val pos = valuePositions[paramPosIdx]
											if (pos.isVararg) "$name = $name.map { of(it) }.toTypedArray()" else if (afterVararg) "$name = of($name)" else "of($name)"
										} else {
											if (isVararg) "*$name" else if (afterVararg) "$name = $name" else name
										}
									}.joinToString(", ")
									val delegationCall = "$receiverPrefix$vFuncName($delegationArgs)"

									val newFuncText = if (bodyStartInFunc >= 0) {
										newSignature + "=\n\t\t$delegationCall"
									} else {
										newSignature + " =\n\t\t$delegationCall"
									}

									// Build @JvmName suffix — added when field/kprop/result types are involved
									// to avoid JVM signature clashes from erasure
									val receiverSuffix = if (hasValueReceiver) {
										when {
											combination[recPosIdx] == null -> ""
											combination[recPosIdx]!!.startsWith("opensavvy") -> "FieldReceiver"
											combination[recPosIdx]!!.startsWith("kotlin.reflect.KProperty1") -> "PropertyReceiver"
											else -> "ResultReceiver"
										}
									} else ""
									val paramSuffix = valuePositions.zip(combination)
										.filter { (pos, _) -> !pos.isReceiver }
										.joinToString("") { (_, t) ->
											when {
												t == null -> "ByValue"
												t.startsWith("opensavvy") -> "ByField"
												t.startsWith("kotlin.reflect.KProperty1") -> "ByProperty"
												else -> "ByResult"
											}
										}

									val needsJvmName = receiverReplaced ||
										combination.any { it != null && (it.startsWith("opensavvy") || it.startsWith("kotlin.reflect.KProperty1")) }

									// Overloads where Result (raw type parameter) appears in any position
									// are given low priority so navigation operators win on ambiguity.
									// Exception: when the receiver is replaced by Field<> or KProperty1<>, the
									// annotation is omitted. Kotlin's specificity rules already make Field.ne(Field)
									// beat Field.ne(Result) within the aggregation context. Adding the annotation
									// would cause an outer FilterQuery.ne(Field, V) to win over the aggregation
									// overload despite the aggregation context being the closer implicit receiver.
									val receiverIsSpecificType = recPosIdx >= 0 && combination[recPosIdx] != null &&
										(combination[recPosIdx]!!.startsWith("opensavvy") || combination[recPosIdx]!!.startsWith("kotlin.reflect.KProperty1"))
									val hasResultAlternative = !receiverIsSpecificType && valuePositions.zip(combination).any { (_, t) ->
										t != null && !t.startsWith("opensavvy") && !t.startsWith("kotlin.reflect.KProperty1")
									}

									// Merge INAPPLICABLE_JVM_NAME into the existing @Suppress rather than
									// adding a second (non-repeatable) @Suppress annotation.
									val afterJvmName = if (needsJvmName) {
										if (newFuncText.contains("@Suppress(\"")) {
											newFuncText.replaceFirst("@Suppress(\"", "@Suppress(\"INAPPLICABLE_JVM_NAME\", \"")
										} else {
											"@Suppress(\"INAPPLICABLE_JVM_NAME\")\n\t" + newFuncText
										}
									} else newFuncText
									// @kotlin.internal.LowPriorityInOverloadResolution is itself an internal API,
									// so INVISIBLE_REFERENCE must be suppressed wherever it appears.
									val annotatedFuncText = if (hasResultAlternative && !afterJvmName.contains("INVISIBLE_REFERENCE")) {
										if (afterJvmName.contains("@Suppress(\"")) {
											afterJvmName.replaceFirst("@Suppress(\"", "@Suppress(\"INVISIBLE_REFERENCE\", \"")
										} else {
											"@Suppress(\"INVISIBLE_REFERENCE\")\n\t" + afterJvmName
										}
									} else afterJvmName
									val jvmNameAnnotation = if (needsJvmName) "@JvmName(\"$vFuncName$receiverSuffix$paramSuffix\")\n\t" else ""
									val lowPriorityAnnotation = if (hasResultAlternative) "@kotlin.internal.LowPriorityInOverloadResolution\n\t" else ""

									val docComment = findDocCommentBefore(source, vFuncStart)
									val docPart = if (docComment.isNotEmpty()) "\t$docComment\n" else ""
									valueOverloadBuilder.append("\n\n").append(docPart).append("\t").append(lowPriorityAnnotation).append(jvmNameAnnotation).append(annotatedFuncText)
								}

								if (valueOverloadBuilder.isNotEmpty()) {
									// Use bracket-counting to find the true end of the function body,
									// because ANTLR may misparse functions whose bodies contain constructs
									// like 'T & Any' in type arguments (which the grammar doesn't support),
									// resulting in a wrong ctx.stop token.
									val insertAfterIdx = if (vFuncBody != null) {
										findExpressionBodyEndTokenIndex(tokens, vFuncBody)
									} else {
										ctx.stop.tokenIndex
									}
									rewriter.insertAfter(insertAfterIdx, valueOverloadBuilder.toString())
								}
							}
						}
					}
				}
				// endregion

				// The receiver type is stored in different places depending on whether type parameters are present:
				// - With type params (e.g. fun <V> Field<T,V>.foo()): receiverType() after typeParameters()
				// - Without type params (e.g. fun Field<T,*>.foo()): type() before typeParameters() (grammar quirk)
				val receiverStart: Int
				val receiverEnd: Int
				if (ctx.receiverType() != null) {
					receiverStart = ctx.receiverType().start.startIndex
					receiverEnd = ctx.receiverType().stop.stopIndex
				} else if (ctx.typeParameters() == null && ctx.DOT().isNotEmpty()) {
					val dispatchReceiverType = ctx.type().firstOrNull() ?: return
					receiverStart = dispatchReceiverType.start.startIndex
					receiverEnd = dispatchReceiverType.stop.stopIndex
				} else {
					return
				}
				val receiverText = source.substring(receiverStart, receiverEnd + 1)
				if (!receiverText.startsWith("Field<T")) return

				// Don't generate overloads for 'override' functions: the parent interface already has them
				val funcStart0 = ctx.start.startIndex
				val funKeywordStart = ctx.FUN().symbol.startIndex
				if (source.substring(funcStart0, funKeywordStart).contains("override")) return

				val funcName = ctx.identifier()?.text ?: return
				val paramCount = ctx.functionValueParameters()?.functionValueParameter()?.size ?: 0
				if (Pair(funcName, paramCount) in existingKPropFunctions) return

				val kpropReceiver = receiverText.replaceFirst("Field<T", "kotlin.reflect.KProperty1<T")

				val paramCallParts = ctx.functionValueParameters()
					?.functionValueParameter()
					?.mapNotNull { fp ->
						val name = fp.parameter()?.simpleIdentifier()?.text ?: return@mapNotNull null
						val isVararg = fp.modifierList()?.text?.contains("vararg") == true
						if (isVararg) "*$name" else name
					} ?: emptyList()
				val paramCall = paramCallParts.joinToString(", ")

				val funcStart = ctx.start.startIndex
				val funcEnd = ctx.stop.stopIndex
				val funcText = source.substring(funcStart, funcEnd + 1)

				val receiverOffsetInFunc = receiverStart - funcStart
				val receiverEndOffsetInFunc = receiverEnd + 1 - funcStart

				val delegationBody = "{\n\t\treturn this.field.${funcName}($paramCall)\n\t}"

				val kpropFuncText = if (ctx.functionBody() != null) {
					val bodyStartInFunc = ctx.functionBody()!!.start.startIndex - funcStart
					funcText.substring(0, receiverOffsetInFunc) +
						kpropReceiver +
						funcText.substring(receiverEndOffsetInFunc, bodyStartInFunc) +
						delegationBody
				} else {
					funcText.substring(0, receiverOffsetInFunc) +
						kpropReceiver +
						funcText.substring(receiverEndOffsetInFunc) +
						" " + delegationBody
				}

				val docComment = findDocCommentBefore(source, funcStart)
				val docPart = if (docComment.isNotEmpty()) "\t$docComment\n" else ""

				// Build all insertions in one string to avoid multiple insertions at same position
				val insertionBuilder = StringBuilder()
				insertionBuilder.append("\n\n").append(docPart).append("\t").append(kpropFuncText)

				// For each parameter whose type starts with Field<, also generate overloads with KProperty1 for that param
				val paramCtxList = ctx.functionValueParameters()?.functionValueParameter() ?: emptyList()
				for (fieldParamCtx in paramCtxList) {
					val paramType = fieldParamCtx.parameter()?.type() ?: continue
					val paramTypeText = source.substring(paramType.start.startIndex, paramType.stop.stopIndex + 1)
					if (!paramTypeText.startsWith("Field<")) continue

					val kpropParamType = paramTypeText.replaceFirst("Field<", "kotlin.reflect.KProperty1<")
					val paramTypeStart = paramType.start.startIndex - funcStart
					val paramTypeEnd = paramType.stop.stopIndex + 1 - funcStart

					// Overload: original Field receiver + KProperty1 param
					val fieldParamCallParts = paramCtxList.mapNotNull { fp ->
						val name = fp.parameter()?.simpleIdentifier()?.text ?: return@mapNotNull null
						val isVararg = fp.modifierList()?.text?.contains("vararg") == true
						val callName = if (fp == fieldParamCtx) "$name.field" else name
						if (isVararg) "*$callName" else callName
					}
					val fieldParamCall = fieldParamCallParts.joinToString(", ")
					val delegationBodyForFieldParam = "{\n\t\treturn this.${funcName}($fieldParamCall)\n\t}"

					val baseFieldReceiverKpropParamText = funcText.substring(0, paramTypeStart) +
						kpropParamType +
						funcText.substring(paramTypeEnd)
					val fieldReceiverKpropParamText = if (ctx.functionBody() != null) {
						baseFieldReceiverKpropParamText
					} else {
						baseFieldReceiverKpropParamText + " " + delegationBodyForFieldParam
					}
					insertionBuilder.append("\n\n").append(docPart).append("\t").append(fieldReceiverKpropParamText)

					// Overload: KProperty1 receiver + KProperty1 param
					val kpropParamOffset = receiverOffsetInFunc + kpropReceiver.length + (paramTypeStart - receiverEndOffsetInFunc)
					val kpropReceiverKpropParamText = kpropFuncText.substring(0, kpropParamOffset) +
						kpropParamType +
						kpropFuncText.substring(kpropParamOffset + paramTypeText.length)
					insertionBuilder.append("\n\n").append(docPart).append("\t").append(kpropReceiverKpropParamText)
				}

				rewriter.insertAfter(ctx.stop, insertionBuilder.toString())
			}

			override fun exitPropertyDeclaration(ctx: opensavvy.ktmongo.build.kotlin.KotlinParser.PropertyDeclarationContext) {
				// For extension properties, the receiver type is accessed via ctx.receiverType() with a DOT following
				val receiverTypeCtx = ctx.receiverType() ?: return
				if (ctx.DOT() == null) return
				val receiverStart = receiverTypeCtx.start.startIndex
				val receiverEnd = receiverTypeCtx.stop.stopIndex
				val receiverText = source.substring(receiverStart, receiverEnd + 1)
				// region Value<> overloads for extension properties
				if (isValueOverloadTarget) {
					extractValueTypeArgs(receiverText)?.let { (contextType, resultType, _) ->
						val propName = ctx.variableDeclaration()?.simpleIdentifier()?.text ?: return@let
						val propStart = ctx.start.startIndex
						val propText = source.substring(propStart, ctx.stop.stopIndex + 1)
						val receiverOffsetInProp = receiverStart - propStart
						val receiverEndOffsetInProp = receiverEnd + 1 - propStart
						val afterReceiver = propText.substring(receiverEndOffsetInProp)
						val getterPattern = "get() = "
						val getterIdx = afterReceiver.indexOf(getterPattern)
						val valueOverloadBuilder = StringBuilder()

						data class PropReceiverCandidate(val type: String, val isRawType: Boolean)
						for (candidate in listOf(
							PropReceiverCandidate("opensavvy.ktmongo.dsl.path.Field<$contextType, $resultType>", false),
							PropReceiverCandidate("kotlin.reflect.KProperty1<$contextType, $resultType>", false),
							PropReceiverCandidate(resultType, true),
						)) {
							// For raw-type receivers, contextType is no longer in the receiver — remove the
							// type parameter declaration and replace contextType with Any in the return type.
							val beforeReceiver = if (candidate.isRawType) {
								propText.substring(0, receiverOffsetInProp).replace(" <$contextType : Any>", "")
							} else {
								propText.substring(0, receiverOffsetInProp)
							}
							val newPropText = if (getterIdx >= 0) {
								val afterReceiverSig = afterReceiver.substring(0, getterIdx + getterPattern.length)
								val cleanedSig = if (candidate.isRawType) afterReceiverSig.replace(contextType, "Any") else afterReceiverSig
								beforeReceiver + candidate.type + cleanedSig + "of(this).$propName"
							} else {
								val cleanedAfterReceiver = if (candidate.isRawType) afterReceiver.replace(contextType, "Any") else afterReceiver
								beforeReceiver + candidate.type + cleanedAfterReceiver + "\n\t\tget() = of(this).$propName"
							}
							val lowPriorityAnnotation = if (candidate.isRawType) "@kotlin.internal.LowPriorityInOverloadResolution\n\t" else ""
							val annotatedPropText = if (candidate.isRawType) {
								if (newPropText.contains("@Suppress(\"")) {
									newPropText.replaceFirst("@Suppress(\"", "@Suppress(\"INVISIBLE_REFERENCE\", \"")
								} else {
									"@Suppress(\"INVISIBLE_REFERENCE\")\n\t" + newPropText
								}
							} else newPropText
							val docComment = findDocCommentBefore(source, propStart)
							val docPart = if (docComment.isNotEmpty()) "\t$docComment\n" else ""
							valueOverloadBuilder.append("\n\n").append(docPart).append("\t").append(lowPriorityAnnotation).append(annotatedPropText)
						}
						if (valueOverloadBuilder.isNotEmpty()) {
							rewriter.insertAfter(ctx.stop, valueOverloadBuilder.toString())
						}
					}
				}
				// endregion
				if (!receiverText.startsWith("Field<T")) return

				val propName = ctx.variableDeclaration()?.simpleIdentifier()?.text ?: return
				if (propName in existingKPropProperties) return

				val kpropReceiver = receiverText.replaceFirst("Field<T", "kotlin.reflect.KProperty1<T")

				val propStart = ctx.start.startIndex
				val propEnd = ctx.stop.stopIndex
				val propText = source.substring(propStart, propEnd + 1)
				val receiverOffsetInProp = receiverStart - propStart
				val receiverEndOffsetInProp = receiverEnd + 1 - propStart

				val afterReceiver = propText.substring(receiverEndOffsetInProp)
				val getterPattern = "get() = "
				val getterIdx = afterReceiver.indexOf(getterPattern)

				val newPropText = if (getterIdx >= 0) {
					propText.substring(0, receiverOffsetInProp) +
						kpropReceiver +
						afterReceiver.substring(0, getterIdx + getterPattern.length) +
						"this.field.$propName"
				} else {
					propText.substring(0, receiverOffsetInProp) +
						kpropReceiver +
						afterReceiver +
						"\n\t\tget() = this.field.$propName"
				}

				val docComment = findDocCommentBefore(source, propStart)
				val docPart = if (docComment.isNotEmpty()) "\t$docComment\n" else ""
				rewriter.insertAfter(ctx.stop, "\n\n" + docPart + "\t" + newPropText)
			}
		}
		walker.walk(listener, tree)

		return buildString {
			val text = rewriter.text
			val endOfCopyright = "limitations under the License.\n */\n"
			val indexEndOfCopyright = text.indexOf(endOfCopyright) + endOfCopyright.length

			appendLine(text.substring(0, indexEndOfCopyright))

			appendLine("// This file is generated from $sourceFile")
			appendLine("// DO NOT EDIT THIS FILE DIRECTLY. To learn more, read dsl-template/README.md.")

			append(text.substring(indexEndOfCopyright))
		}
	}

	/**
	 * For expression bodies of functions, ANTLR may misparse constructs like `T & Any` in type
	 * arguments (definitely non-null syntax), causing [ctx.stop][opensavvy.ktmongo.build.kotlin.KotlinParser.FunctionDeclarationContext.stop]
	 * to point to the wrong token. This method finds the true last token of a [functionBody] by
	 * bracket-counting in the raw token stream, which is immune to parser-level misparsing.
	 *
	 * For block bodies (`{ ... }`), the ANTLR stop token is reliable and is used directly.
	 */
	private fun findExpressionBodyEndTokenIndex(
		tokens: CommonTokenStream,
		funcBody: opensavvy.ktmongo.build.kotlin.KotlinParser.FunctionBodyContext,
	): Int {
		// Block bodies: ANTLR reliably identifies the closing '}' even with grammar quirks
		if (funcBody.block() != null) return funcBody.block().stop.tokenIndex

		// Expression body ('= expression'): bracket-count in the token stream from after '='
		val allTokens = tokens.tokens
		val equalsTokenIdx = funcBody.start.tokenIndex

		// Skip the '=' token and any whitespace/NL immediately following it
		var i = equalsTokenIdx + 1
		while (i < allTokens.size) {
			val t = allTokens[i]
			if (t.channel != 0 || t.type == opensavvy.ktmongo.build.kotlin.KotlinLexer.NL) i++ else break
		}
		if (i >= allTokens.size) return equalsTokenIdx

		var depth = 0
		var lastRealIdx = i

		while (i < allTokens.size) {
			val tok = allTokens[i]
			if (tok.channel != 0) {
				i++; continue
			} // skip hidden channel (WS, comments)
			when (tok.type) {
				opensavvy.ktmongo.build.kotlin.KotlinLexer.LPAREN,
				opensavvy.ktmongo.build.kotlin.KotlinLexer.LCURL,
				opensavvy.ktmongo.build.kotlin.KotlinLexer.LSQUARE,
					-> {
					depth++
					lastRealIdx = i
				}

				opensavvy.ktmongo.build.kotlin.KotlinLexer.RPAREN,
				opensavvy.ktmongo.build.kotlin.KotlinLexer.RCURL,
				opensavvy.ktmongo.build.kotlin.KotlinLexer.RSQUARE,
					-> {
					if (depth == 0) return lastRealIdx // closing bracket we didn't open → stop before it
					depth--
					lastRealIdx = i
				}

				opensavvy.ktmongo.build.kotlin.KotlinLexer.NL,
				opensavvy.ktmongo.build.kotlin.KotlinLexer.SEMICOLON,
					-> {
					if (depth == 0) return lastRealIdx // expression finished at previous token
				}

				-1 -> return lastRealIdx // EOF
				else -> lastRealIdx = i
			}
			i++
		}
		return lastRealIdx
	}

	/**
	 * Returns the receiver type text for a function declaration, or null if there is none.
	 * Handles both the with-type-params and without-type-params ANTLR grammar positions.
	 */
	private fun getReceiverText(ctx: opensavvy.ktmongo.build.kotlin.KotlinParser.FunctionDeclarationContext, source: String): String? {
		return if (ctx.receiverType() != null) {
			val start = ctx.receiverType().start.startIndex
			val end = ctx.receiverType().stop.stopIndex
			source.substring(start, end + 1)
		} else if (ctx.typeParameters() == null && ctx.DOT().isNotEmpty()) {
			val dispatchReceiverType = ctx.type().firstOrNull() ?: return null
			val start = dispatchReceiverType.start.startIndex
			val end = dispatchReceiverType.stop.stopIndex
			source.substring(start, end + 1)
		} else {
			null
		}
	}

	/**
	 * Extracts the two type arguments from a `Value<Context, Result>` type string.
	 * Returns a triple `(contextType, resultType, isNullable)`, or null if the input doesn't match.
	 *
	 * `isNullable` is true when the outer type was `Value<...>?`. Callers decide whether to act on
	 * such positions; the `?` is stripped before the type arguments are parsed.
	 */
	private fun extractValueTypeArgs(valueTypeText: String): Triple<String, String, Boolean>? {
		if (!valueTypeText.startsWith("Value<")) return null
		val isNullable = valueTypeText.endsWith("?")
		val typeText = if (isNullable) valueTypeText.dropLast(1) else valueTypeText
		var depth = 0
		var openIdx = -1
		var closeIdx = -1
		for (i in typeText.indices) {
			when (typeText[i]) {
				'<' -> {
					if (depth == 0) openIdx = i
					depth++
				}

				'>' -> {
					depth--
					if (depth == 0) {
						closeIdx = i
						break
					}
				}
			}
		}
		if (openIdx == -1 || closeIdx == -1) return null
		val content = typeText.substring(openIdx + 1, closeIdx)
		var innerDepth = 0
		for (i in content.indices) {
			when (content[i]) {
				'<' -> innerDepth++
				'>' -> innerDepth--
				',' -> if (innerDepth == 0) {
					return Triple(content.substring(0, i).trim(), content.substring(i + 1).trim(), isNullable)
				}
			}
		}
		return null
	}

	/**
	 * Scans backward from [funcStart] in [source] to find a KDoc comment (`/** ... */`) immediately
	 * preceding the function (skipping whitespace and line comments). Returns the comment text, or empty string.
	 */
	private fun findDocCommentBefore(source: String, funcStart: Int): String {
		var i = funcStart - 1
		while (i >= 0) {
			val c = source[i]
			if (c.isWhitespace()) {
				i--; continue
			}
			// Check if we're at the end of a block comment: '...*/'
			if (c == '/' && i >= 1 && source[i - 1] == '*') {
				val commentEnd = i + 1
				i -= 2
				while (i >= 2) {
					if (source[i] == '/' && source[i + 1] == '*' && source[i + 2] == '*') {
						return source.substring(i, commentEnd)
					}
					i--
				}
				return ""
			}
			// Check if we're at the end of a line comment: the current line starts with '//'
			var lineStart = i
			while (lineStart > 0 && source[lineStart - 1] != '\n') lineStart--
			val lineContent = source.substring(lineStart, i + 1).trimStart()
			if (lineContent.startsWith("//")) {
				// Skip this whole line comment and continue scanning
				i = lineStart - 1
				continue
			}
			// Something else (e.g. end of a previous function body), stop
			return ""
		}
		return ""
	}
}

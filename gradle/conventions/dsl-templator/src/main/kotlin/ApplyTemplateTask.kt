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

import org.antlr.v4.runtime.CharStreams
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
		val stream = CharStreams.fromString(source)
		val lexer = opensavvy.ktmongo.build.kotlin.KotlinLexer(stream)
		val tokens = CommonTokenStream(lexer)
		tokens.fill()
		val parser = opensavvy.ktmongo.build.kotlin.KotlinParser(tokens)
		val tree = parser.kotlinFile()

		val rewriter = org.antlr.v4.runtime.TokenStreamRewriter(tokens)

		val isValueOverloadTarget = sourceFile.path.replace('\\', '/').endsWith("aggregation/operators/ArithmeticValueOperators.kt")

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
				val receiverTypeCtx = ctx.type() ?: return
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
							val vFuncText = source.substring(vFuncStart, ctx.stop.stopIndex + 1)
							val vParamCtxList = ctx.functionValueParameters()?.functionValueParameter() ?: emptyList()

							// Collect all "Value<...>" positions: receiver first, then each param
							data class ValuePos(
								val isReceiver: Boolean,
								val paramIdx: Int, // -1 for receiver
								val contextType: String,
								val resultType: String,
								val typeStartInFunc: Int,
								val typeEndInFunc: Int,
							)

							val valuePositions = mutableListOf<ValuePos>()

							val receiverCtx = ctx.receiverType()
							if (receiverCtx != null) {
								val recText = source.substring(receiverCtx.start.startIndex, receiverCtx.stop.stopIndex + 1)
								extractValueTypeArgs(recText)?.let { (c, r) ->
									valuePositions.add(ValuePos(true, -1, c, r,
										receiverCtx.start.startIndex - vFuncStart,
										receiverCtx.stop.stopIndex + 1 - vFuncStart))
								}
							}

							for ((idx, paramCtx) in vParamCtxList.withIndex()) {
								val paramType = paramCtx.parameter()?.type() ?: continue
								val paramTypeText = source.substring(paramType.start.startIndex, paramType.stop.stopIndex + 1)
								extractValueTypeArgs(paramTypeText)?.let { (c, r) ->
									valuePositions.add(ValuePos(false, idx, c, r,
										paramType.start.startIndex - vFuncStart,
										paramType.stop.stopIndex + 1 - vFuncStart))
								}
							}

							if (valuePositions.isNotEmpty()) {
								// Alternatives per position:
								// - Receiver: [null=keep, Field, KProperty1, Result]
								// - Params:   [null=keep, Field, KProperty1, Result]
								val alternatives: List<List<String?>> = valuePositions.map { pos ->
									listOf(
										null,
										"opensavvy.ktmongo.dsl.path.Field<${pos.contextType}, ${pos.resultType}>",
										"kotlin.reflect.KProperty1<${pos.contextType}, ${pos.resultType}>",
										pos.resultType,
									)
								}

								// Cartesian product of alternatives
								val combinations = alternatives.fold(listOf(listOf<String?>())) { acc, alts ->
									acc.flatMap { prev -> alts.map { prev + it } }
								}

								val bodyStartInFunc = ctx.functionBody()?.start?.startIndex?.minus(vFuncStart) ?: -1
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
										newSignature = newSignature.substring(0, pos.typeStartInFunc) +
											newType!! +
											newSignature.substring(pos.typeEndInFunc)
									}

									// Build the delegation call
									val receiverReplaced = hasValueReceiver && combination[recPosIdx] != null
									val receiverPrefix = when {
										receiverReplaced -> "of(this)."
										hasValueReceiver -> "this."
										else -> ""
									}
									val delegationArgs = vParamCtxList.mapIndexed { idx, fp ->
										val name = fp.parameter()?.simpleIdentifier()?.text ?: return@mapIndexed ""
										val paramPosIdx = valuePositions.indexOfFirst { !it.isReceiver && it.paramIdx == idx }
										val replaced = paramPosIdx >= 0 && combination[paramPosIdx] != null
										if (replaced) "of($name)" else name
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
									val hasResultAlternative = valuePositions.zip(combination).any { (_, t) ->
										t != null && !t.startsWith("opensavvy") && !t.startsWith("kotlin.reflect.KProperty1")
									}

									// Merge INAPPLICABLE_JVM_NAME into the existing @Suppress rather than
									// adding a second (non-repeatable) @Suppress annotation.
									val annotatedFuncText = if (needsJvmName) {
										if (newFuncText.contains("@Suppress(\"")) {
											newFuncText.replaceFirst("@Suppress(\"", "@Suppress(\"INAPPLICABLE_JVM_NAME\", \"")
										} else {
											"@Suppress(\"INAPPLICABLE_JVM_NAME\")\n\t" + newFuncText
										}
									} else newFuncText
									val jvmNameAnnotation = if (needsJvmName) "@JvmName(\"$vFuncName$receiverSuffix$paramSuffix\")\n\t" else ""
									val lowPriorityAnnotation = if (hasResultAlternative) "@kotlin.internal.LowPriorityInOverloadResolution\n\t" else ""

									val docComment = findDocCommentBefore(source, vFuncStart)
									val docPart = if (docComment.isNotEmpty()) "\t$docComment\n" else ""
									valueOverloadBuilder.append("\n\n").append(docPart).append("\t").append(lowPriorityAnnotation).append(jvmNameAnnotation).append(annotatedFuncText)
								}

								if (valueOverloadBuilder.isNotEmpty()) {
									rewriter.insertAfter(ctx.stop, valueOverloadBuilder.toString())
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
				// For extension properties, the receiver type is accessed via ctx.type() with a DOT following
				val receiverTypeCtx = ctx.type() ?: return
				if (ctx.DOT() == null) return

				val receiverStart = receiverTypeCtx.start.startIndex
				val receiverEnd = receiverTypeCtx.stop.stopIndex
				val receiverText = source.substring(receiverStart, receiverEnd + 1)
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
	 * Returns a pair `(contextType, resultType)`, or null if the input doesn't match.
	 */
	private fun extractValueTypeArgs(valueTypeText: String): Pair<String, String>? {
		if (!valueTypeText.startsWith("Value<")) return null
		var depth = 0
		var openIdx = -1
		var closeIdx = -1
		for (i in valueTypeText.indices) {
			when (valueTypeText[i]) {
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
		val content = valueTypeText.substring(openIdx + 1, closeIdx)
		var innerDepth = 0
		for (i in content.indices) {
			when (content[i]) {
				'<' -> innerDepth++
				'>' -> innerDepth--
				',' -> if (innerDepth == 0) {
					return Pair(content.substring(0, i).trim(), content.substring(i + 1).trim())
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

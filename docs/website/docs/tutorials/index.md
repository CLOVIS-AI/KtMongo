# Getting started

KtMongo is a modern DSL for interacting with MongoDB in Kotlin. On the JVM, KtMongo is built on top of the official Kotlin driver.

This guide shows you how to create an application that uses the KtMongo driver to connect to a MongoDB instance and interact with data.

If you are a KMongo user, read the [KMongo migration guide](from-kmongo/setup.md) instead.

??? tip "TL;DR"
    If you're used to Kotlin projects using Gradle, here are the important steps:
    
    - Add a dependency on `dev.opensavvy.ktmongo:driver-coroutines` or `dev.opensavvy.ktmongo:driver-sync`
    - Add a dependency on any serialization library supported by the official Kotlin driver
    - Use `MongoCollection.asKtMongo()` to access the classes of the driver

## Creating a Kotlin project

First, create a regular Kotlin project, using your build tool of choice. We recommend using an IDE, such as IntelliJ IDEA (Community or Ultimate) or Eclipse.

To do so, follow one of these tutorials:

- [Creating a project with Gradle with IntelliJ](https://kotlinlang.org/docs/get-started-with-jvm-gradle-project.html) (recommended)
- [Creating a project with Gradle in the CLI](https://docs.gradle.org/current/samples/sample_building_kotlin_applications.html)
- [Creating a project with Maven](https://kotlinlang.org/docs/maven.html)

### Adding KtMongo

KtMongo is available in two variants: the coroutines-aware variant, and the synchronous variant.
The coroutines driver is recommended for modern Kotlin projects, especially if you're using Ktor or another coroutines-first library.

=== "Gradle"

    Open the `build.gradle.kts` file and edit the `dependencies {}` block:
    ```kotlin
    dependencies {
        implementation("dev.opensavvy.ktmongo:driver-coroutines:VERSION_HERE")
    }
    ```

    Replace `VERSION_HERE` by one of [the available versions](https://search.maven.org/artifact/dev.opensavvy.ktmongo/driver-coroutines).

    After editing the file, synchronize your project to ensure the dependencies are downloaded:
    
    - In IntelliJ, press 'shift' twice then type "Sync all Gradle projects", then press enter.
    - In the CLI, run `./gradlew build`.

=== "Maven"
 
    Open the `pom.xml` file and edit the `<dependencies>` tag:
    ```xml
    <dependencies>
        <dependency>
            <groupId>dev.opensavvy.ktmongo</groupId>
            <artifactId>driver-coroutines-jvm</artifactId>
            <version>VERSION_HERE</version>
        </dependency>
    </dependencies>
    ```

    Replace `VERSION_HERE` by one of [the available versions](https://search.maven.org/artifact/dev.opensavvy.ktmongo/driver-coroutines-jvm).

    After editing the file, refresh the project in your IDE.

### Adding a serialization library

MongoDB requires a serialization library, which is responsible for translating your objects to and from the database.
The official driver supports two serialization library.

=== "KotlinX.Serialization (recommended)"
    [KotlinX.Serialization](https://kotlinlang.org/docs/serialization.html) is a first-party library adding compile-time serialization to Kotlin. It is extensively used in the ecosystem, including by Ktor. 

    Since the analysis is done at compile-time, it is much faster than reflection-based libraries, and greatly reduces the risk of serialization-born security flaws. Each class we want to serialize must be annotated with `@Serializable`.

    Open your `build.gradle.kts` file and add the KotlinX.Serialization plugin:
    ```kotlin
    plugins {
        kotlin("jvm") version "…"
        kotlin("plugin.serialization") version "…"
    }
    ```
    The KotlinX.Serialization plugin should use the **same** version as the Kotlin plugin itself.

    Following the same steps as previously, add a dependency on:
    
    - `org.jetbrains.kotlinx:kotlinx-serialization-core` ([available versions](https://search.maven.org/artifact/org.jetbrains.kotlinx/kotlinx-serialization-core))
    - `org.mongodb:bson-kotlinx` ([available versions](https://search.maven.org/artifact/org.mongodb/bson-kotlinx))

=== "Data class reflection"
    MongoDB provides a reflection-based serialization library, that is able to serialize basic Kotlin types (primitives, collections, `data class` instances).

    Following the same steps as previously, add a dependency on:

    - `org.mongodb:bson-kotlin` ([available versions](https://search.maven.org/artifact/org.mongodb/bson-kotlin))

=== "Other"
    It is possible to add support for any serialization library you prefer, directly through the Kotlin driver's `Codec` system.

    - [Learn more in the Coroutines driver documentation](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/data-formats/codecs/)
    - [Learn more in the Synchronous driver documentation](https://www.mongodb.com/docs/languages/kotlin/kotlin-sync-driver/current/data-formats/codecs/)

## Running MongoDB

=== "Docker Compose"
    Docker is a popular way to encapsulate programs without impacting the user's system. Docker is particularly common to create development environment on the developer's machine.

    If you haven't already, start by [installing Docker](https://docs.docker.com/engine/install/).

    Create a file `docker-compose.yml`:
    ```yaml
    version: "3.6"
    
    services:
      mongo:
        image: "mongo:8.0.0"
        ports:
          - "27017:27017"
    ```
    See the [available versions](https://hub.docker.com/_/mongo/tags).

    Then, run `docker compose up -d` or click the green triangle in IntelliJ.

    **Connection URI: `mongodb://localhost:27017`**. It will be required in the next steps.

=== "MongoDB Atlas"
    MongoDB inc. offers MongoDB Atlas, a hosted MongoDB solution. You can create a MongoDB Atlas instance by following [this tutorial](https://www.mongodb.com/docs/atlas/getting-started/).

    Once you have done so, obtain your **connection URI** by following the steps outlined [here](https://www.mongodb.com/docs/languages/kotlin/kotlin-sync-driver/current/get-started/create-a-connection-string/). It will be required in the next steps.

=== "Helm"
    Helm charts allow easy deployment onto Kubernetes.

    - [Official Helm charts](https://github.com/mongodb/helm-charts)
    - [Bitnami Helm charts](https://artifacthub.io/packages/helm/bitnami/mongodb)

## Make a request

We can now start writing some code.
Depending on how you created your project, you may already have a sample code file (in `src/main/kotlin`) with a `main` function. If you don't have it, create it.

### Connecting to the database

We first need to create a `MongoClient` (low-level representation of the connection between our program and MongoDB) and select a database (namespace of collections):
```kotlin
val client = MongoClient.create("CONNECTION_URI") //(1)!
val database = client.getDatabase("my_first_project")
```

1. Replace `CONNECTION_URI` by the connection URI you obtained in the previous step.

If the database doesn't exist, it will be created automatically.

### Representing data

Then, we create a class that represents the schema of the data we want to store:
```kotlin
data class Counter(
	val name: String,
	val value: Int,
)
```

Note that additional configuration may be necessary depending on your serialization library. For example, when using KotlinX.Serialization, you should annotate this class with `@Serializable`.

With KotlinX.Serialization, collection classes are not necessarily declared as `data class`, regular classes, sealed classes and other Kotlin types can be used as well.

We can now obtain a collection of that data:
```kotlin
val counters = database.getCollection<Counter>("counters").asKtMongo()
```

Notice the call to `asKtMongo()` which converts the `MongoCollection` from the Kotlin driver into KtMongo's representation.

If the collection doesn't exist, it will be created automatically.

### Reading and writing data

We can now make simple requests to read the data (for example with `find()` or `count()`) or modify it (for example with `updateOne`).

In this tutorial, we will make an application that prints a new number each time it is run. To do this, we can write the query:
```kotlin
counters.upsertOne( //(1)!
	filter = {
		Counter::name eq "simple-counter" //(2)!
	},
	update = {
		Counter::value inc 1 //(3)!
	}
)
```

1.  `upsertOne` finds a document matching the filter and updates it. If no documents are found, a new one is created.
2.  We want to upsert a document with a `name` of `"simple-counter"`. We use [Kotlin's property references](https://kotlinlang.org/docs/reflection.html#property-references) to reference a field, and the `eq` infix function to use MongoDB's, `$eq` operator.
3.  Similarly as how the filter uses the `eq` infix function to insert an `$eq` operator, we use the `inc` infix function to insert an `$inc` operator.

If a document already exists with the name `simple-counter`, its value is increased by one. If none exist, a new document is created with the name `simple-counter` and the value 1.

```kotlin
val counter = counters.find {
	Counter::name eq "simple-counter"
}

println("Current counter: $counter")
```

## Final code

At this stage, your file should look like:
```kotlin
import kotlinx.serialization.Serializable
import com.mongodb.kotlin.client.MongoClient
import opensavvy.ktmongo.coroutines.asKtMongo

@Serializable
data class Counter(
	val name: String,
	val value: Int,
)

suspend fun main() {
	val client = MongoClient.create("CONNECTION_URI")
	val database = client.getDatabase("my_first_project")
	val counters = database.getCollection<Counter>("counters").asKtMongo()

	counters.upsertOne(
		filter = {
			Counter::name eq "simple-counter"
		},
		update = {
			Counter::value inc 1
		}
	)

	val counter = counters.find {
		Counter::name eq "simple-counter"
	}

	println("Current counter: $counter")
}
```

If you run this program, you will see that each time it is run, the counter is incremented once.

Don't hesitate to edit the requests to try different things. You may also be interested in:

- Putting a breakpoint in the `filter` or `update` lambdas: notice how the debugger shows the current JSON for the request.
- The JSON representation can also be obtained by inserting `println(this)` in any lambda of the DSL.
- Notice how all operations and operators have an example of usage, and link to the MongoDB website, if you look at their documentation (CTRL Q by default in IntelliJ).

??? failure "Troubleshooting: no documentation in IntelliJ"
    In `File | Settings | Advanced Settings | Build Tools. Gradle`, enable "Download sources" and re-sync the project.

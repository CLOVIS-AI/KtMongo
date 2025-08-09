# KtMongo: the next MongoDB driver for Kotlin

> _Pronounced "cat-mongo"._

In 2016, Julien Buret created [KMongo](https://litote.org/kmongo/), a Kotlin driver for MongoDB, based on the official Java driver. KMongo added a lot of syntax sugar, making complex queries much more readable and less error-prone thanks to improved type safety:
```java
// Official Java driver
Bson filter = and(eq("user.gender", "female"), gt("user.age", 29));
collection.find(filter);
```

```kotlin
class User(
	val gender: String,
	val age: Int,
)

class Document(
	val user: User,
)

// KMongo
collection.find(
	and(
		Document::user / User::gender eq "female",
		Document::user / User::age gt 29
	)
)
```

In 2023, MongoDB released an official Kotlin driver. Development of KMongo stopped, but the official driver lacked much of the syntax niceties of KMongo, as well as requiring major migration efforts. As a result, many projects decided to keep using KMongo for the foreseeable future.

We decided to take it upon ourselves to birth the future of MongoDB drivers for Kotlin. KtMongo is based on the official Kotlin driver to ensure we profit from security fixes and new features, and reimplements a DSL inspired by KMongo, taking the occasion to change the decisions we didn't like.

```kotlin
// KtMongo
collection.find {
	Document::user / User::gender eq "female"
	Document::user / User::age gt 29
}
```

This project is for **everyone who works with KMongo and is worried about the future after the deprecation notice**, as well as for **everyone dissatisfied with the official Kotlin driver**.

**Read more in the [documentation](https://ktmongo.opensavvy.dev).**

## License

This project is licensed under the [Apache 2.0 license](LICENSE).
Individual files may have additional copyright holders.

The initial prototype was developed by 4SH, and is also licensed under the [Apache 2.0 license](prototype/LICENSE), though with different copyright holders.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
- To learn more about our coding conventions and workflow, see the [OpenSavvy website](https://opensavvy.dev/open-source/index.html).
- This project is based on the [OpenSavvy Playground](docs/playground/README.md), a collection of preconfigured project templates.

If you don't want to clone this project on your machine, it is also available using [DevContainer](https://containers.dev/) (open in [VS Code](https://code.visualstudio.com/docs/devcontainers/containers) • [IntelliJ & JetBrains IDEs](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)). Don't hesitate to create issues if you have problems getting the project up and running.

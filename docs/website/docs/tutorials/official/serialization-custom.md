# Writing custom serializers for Kotlin types in MongoDB and the official Kotlin driver

The official Java & Kotlin drivers provide a `CodecRegistry` system for registering custom serialization strategies.

KtMongo entirely delegates serialization to the official driver, so it is automatically compatible with your existing configuration.

Learn more in the official driver's documentation:

- [Learn more in the Coroutines driver documentation](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/data-formats/codecs/)
- [Learn more in the Synchronous driver documentation](https://www.mongodb.com/docs/languages/kotlin/kotlin-sync-driver/current/data-formats/codecs/)

!!! warning
    If your custom serializer renames fields (serializes fields using another name than written in the Kotlin source code) you must configure KtMongo to be aware of that fact. Otherwise, the generated requests may use incorrect field names. See [`PropertyNameStrategy`](../../api/dsl/opensavvy.ktmongo.dsl.path/-property-name-strategy/index.md).

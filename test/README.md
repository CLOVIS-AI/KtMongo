# Integration tests

This folder contains integration tests using the driver. They are run in CI against the last 3 stable versions of MongoDB, so we can ensure new versions don't break anything.

We highly welcome contributions adding simple scenarii or complex requests that are important to you. This way, we will ensure they continue to work and use them as the basis for future performance or syntax improvements.

This could be as simple as a request using some strange combination of options, or as complex as massive aggregations that impact each other.

If you'd like to contribute, but don't know how to, please read our [contribution guide](https://opensavvy.dev/open-source/index.html#contributing-to-open-source-projects). The rest of this document describes how to edit this project but doesn't explain our general workflow regarding contributions.

## Requirements

Before starting, use this command to check if you have a valid Docker installation:
```shell
docker run -it hello-world
```

You will also need a valid Java installation, use this command to check if you have one:
```shell
java -version
```

The tests require a running MongoDB instance. To start one, click here: `MongoDB`. If you're not using IntelliJ, open [docker-compose.yml](../docker/docker-compose.yml) and run the MongoDB service (from the root of the repository):
```shell
cd docker
docker compose up -d
```

Finally, run the existing tests before editing anything, to ensure they all pass: `Check`. If you're not using IntelliJ, run (from the root of the repository):
```shell
./gradlew check
```

## Add your scenario

Open the folder [src/commonTest/kotlin](src/commonTest/kotlin) and create a new Kotlin file. Take inspiration from other existing tests, in particular [BasicReadWriteTest](src/commonTest/kotlin/BasicReadWriteTest.kt).

These tests use the [Prepared test framework](https://opensavvy.gitlab.io/groundwork/prepared/docs/). The most important things to know are:
- Tests are declared in a class that implements `PreparedSpec`.
- `by testCollection("collection-name")` is used to dynamically generate a new collection on each execution (to ensure parallel tests don't impact each other). The name of the collection is printed in the standard output of the test so you can go observe it afterward.
- The `check` function is used to make assertions. Assertions should not be split into multiple lines nor intermediary variables.
- Most functions should have a quick documentation (CTRL Q by default in IntelliJ).

Once you're done writing your test, execute it with `Check`. If you're not using IntelliJ, run (from the root of the repository):
```shell
./gradlew check
```

Each test should automatically print details on which collections it modified. If a test fails, the collections are kept so you can observe them yourself. In IntelliJ, you can open the "Database" tool window which should already be configured to read from the database. Otherwise, you can connect with any other tool using the URL:
```text
mongodb://localhost:27017
```

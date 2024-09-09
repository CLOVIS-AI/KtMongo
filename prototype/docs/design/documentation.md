# Documentation guidelines

One of the main pain points with KMongo was the structure of the documentation. KMongo was documented in two parts: the [website](https://litote.org/kmongo/) (Markdown articles) and the [API reference](https://litote.org/kmongo/dokka/kmongo/index.html) (generated with Dokka).

Operators as they appear in Kotlin and in the Mongo shell are sometimes named differently (for example because of name conflicts). Understanding "which Mongo operator does this function create" and "which function should I call to get this Mongo operator" are common pain points for beginners.

This document describes guidelines any feature of this project must follow to, hopefully, avoid these issues.

## Guides

Guides are articles that focus on a single feature of the library and describe how to use it idiomatically. They are aimed towards users who are using the library for the first time, or towards users who know other parts of the library but are using a feature for the first time.

Guides are meant to provide the user with an overview of what is possible, the recommended approach and major pitfalls to consider. Guides are **not** meant to be exhaustive and mention every possible feature—that would be the API reference. Instead, guides are meant to provide enough information to users that they know where to search in the API reference. 

## Migration guides

Migration guides are similar to guides in intent and structure, but not in content. Instead of focusing on KtMongo features, they focus on features from other libraries. Migration guides provide short descriptions of the differences, a checklist of changes to make, as well as examples using both libraries.

## The API reference

The API reference is an exhaustive description of all KtMongo features, no matter how trivial. Unlike other documentation sources, it is not composed of Markdown files, but is directly extracted from the codebase by Dokka.

By nature, the API reference is structured around symbols from the project. It may be hard to navigate for beginners, because it requires knowing where things are. However, the API reference is the main way users experience the library (because it is directly available in autocomplete popups in IDEs). It is also the documentation section that is less likely to become outdated because it is directly in the code.

For these reasons, users should be able to know at a glance if a function they are looking at is what they are searching for, and help them find similar features in case this isn't exactly what they wanted.

Thus, all operators and queries must:
- Have a short description of its usage,
- Describe pitfalls and edge cases,
- Have at least one example of usage,
- Link to the official MongoDB documentation,
- Link to any other similar operators in KtMongo,
- (Optionally) Describe how the feature relates to other MongoDB features (e.g. what kind of indexes are supported by an operator).

Additionally, the module-level README should list all MongoDB operators and queries implemented—in regular MongoDB syntax—with a link to the corresponding functions in the DSL, to ensure users who are searching for a specific operator can find it even if it is named differently. This list only needs to be high-level (just the name), users can learn about the various parameters and subtlety by following the links.

## Design documents

Unlike other sections of the documentation, design documents are not written for users of the project. They are written for contributors, to ensure the reason why a decision was made is not lost. Their goal is to ensure new contributors can understand the vision behind the project, in the goal of keeping the project coherent in the long-term.

Design documents are also useful for future contributors who may want to reconsider some past decisions: as the initial reasoning is described, they can know easily if their new idea was already known and discarded, or if it is indeed new.

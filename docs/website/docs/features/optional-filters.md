# Optional criteria

When developing user interfaces, we often expose ways to query data that the user can control. In some cases, we know exactly what the user is searching for and can thus write a dedicated query. In some other cases, we provide multiple filters to the user that they can enable or disable.

These optional criteria make queries harder to write, and more importantly harder to read. Since they appear often and can grow quite complex, KtMongo provides operators specifically for this pattern.

## Example

As an example, we will query a list of songs based on its author and release date. For now, we assume all criteria are mandatory, and thus we receive a request that looks like:
```kotlin
class SongCriteria(
	val authorName: String,
	val releasedAfter: Instant,
	val releasedBefore: Instant,
)
```

The request can be written:
```kotlin hl_lines="5"
val criteria: SongCriteria = getCriteriaFromUser()

songs.find {
	Song::author / Author::name eq criteria.authorName
	Song::releaseDate gt criteria.releasedAfter
	Song::releaseDate lt criteria.releasedBefore
}
```

However, in the real world, we may be interested in allowing users to search for songs without specifying a release date range. We'd rather make the last two criteria optional:
```kotlin
class SongCriteria(
	val authorName: String,
	val releasedAfter: Instant? = null,
	val releasedBefore: Instant? = null,
)
```

## Taking advantage of the DSL

Since all KtMongo operators are written using DSLs, we can use any constructs from Kotlin directly within requests:
```kotlin hl_lines="6 7"
val criteria: SongCriteria = getCriteriaFromUser()

songs.find {
	Song::author / Author::name eq criteria.authorName
	
	if (criteria.releasedAfter != null)
		Song::releaseDate gt criteria.releasedAfter
	
	if (criteria.releasedBefore != null)
		Song::releaseDate lt criteria.releasedBefore
}
```

This is simple to read, but is a bit verbose especially when there are many such criteria.

## Optional filter operators

KtMongo provides variants of filter operators that do nothing if their argument is `null`. Using them, we can rewrite the request as:
```kotlin hl_lines="5"
val criteria: SongCriteria = getCriteriaFromUser()

songs.find {
	Song::author / Author::name eq criteria.authorName
	Song::releaseDate gtNotNull criteria.releasedAfter
	Song::releaseDate ltNotNull criteria.releasedBefore
}
```

/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.ktmongo.sync;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import kotlin.Unit;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static opensavvy.ktmongo.sync.Query.filter;
import static opensavvy.ktmongo.sync.Query.options;

public class SimpleJavaTest {

	MongoClient testClient(MongoClient client) {
		client.getDatabase("java-test").getCollection("fake").countDocuments();
		return client;
	}

	MongoClient connect() {
		try {
			return testClient(MongoClients.create("mongodb://localhost:27017"));
		} catch (MongoTimeoutException timeout) {
			return testClient(MongoClients.create("mongodb://mongo:27017")); // In CI, the localhost doesn't work
		}
	}

	@Test
	void test() {
		try (var client = connect()) {
			var javaCollection = client.getDatabase("java-test")
				.getCollection("test-basic", Utilisateur.class);

			var collection = KtMongo.from(javaCollection);

			System.out.println("truc " + collection);

			collection.insertOne(new Utilisateur(new ObjectId(), "foo", new Utilisateur.Enfant("Bob", 3)), options());
			collection.insertOne(new Utilisateur(new ObjectId(), "bar", new Utilisateur.Enfant("Fred", 38)), options());

			System.out.println("Elements: " + collection.find().toList());

			// Calling the lambdas directly
			collection.find(options -> Unit.INSTANCE, filters -> {
				filters.eq(JavaField.of(Utilisateur::enfant).child(Utilisateur.Enfant::name), "foo");

				filters.and((it) -> {
					it.gt(JavaField.of(Utilisateur::enfant).child(Utilisateur.Enfant::age), 5);
					it.eq(JavaField.of(Utilisateur::name), "bar");
					return Unit.INSTANCE;
				});

				return Unit.INSTANCE;
			}).toList();

			// Using the Java helpers
			collection.find(options(), filter(filter -> {
				filter.eq(JavaField.of(Utilisateur::enfant).child(Utilisateur.Enfant::name), "Bob");

				filter.and(filter(and -> {
					and.gt(JavaField.of(Utilisateur::enfant).child(Utilisateur.Enfant::age), 5);
					and.eq(JavaField.of(Utilisateur::name), "bar");
				}));
			})).toList();

			collection.drop(options());
		}
	}

}

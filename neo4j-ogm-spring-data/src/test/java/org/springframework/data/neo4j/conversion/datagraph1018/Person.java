/*
 * Copyright 2011-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.conversion.datagraph1018;

import java.util.UUID;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Michael J. Simons
 * @soundtrack Metallica - S&M
 */
@NodeEntity
public class Person {

	@Id
	@Convert(value = UuidStringConverter.class)
	private UUID id = UUID.randomUUID();

	private String name;

	@Convert(EmailStringConverter.class)
	private Email email;

	public Person(String name, Email email) {
		this.name = name;
		this.email = email;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Email getEmail() {
		return email;
	}
}

/*
 * Copyright 2011-2024 the original author or authors.
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Michael J. Simons
 * @soundtrack Metallica - S&M2
 */
public interface PersonRepository extends Neo4jRepository<Person, UUID> {

	Optional<Person> findByEmail(Email email);

	List<Person> findAllByEmail(Email email);

	List<Person> findAllByEmailIn(List<Email> email);
}

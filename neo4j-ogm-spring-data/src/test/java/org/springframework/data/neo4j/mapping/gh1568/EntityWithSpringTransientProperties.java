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
package org.springframework.data.neo4j.mapping.gh1568;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.data.annotation.Transient;

/**
 * @author Michael J. Simons
 * @soundtrack Evanescence - Fallen
 */
@NodeEntity
public class EntityWithSpringTransientProperties {

	@Id @GeneratedValue
	private Long id;

	private String name;

	@Transient private String shouldNotBeThere;

	public EntityWithSpringTransientProperties(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getShouldNotBeThere() {
		return shouldNotBeThere;
	}

	public void setShouldNotBeThere(String shouldNotBeThere) {
		this.shouldNotBeThere = shouldNotBeThere;
	}
}

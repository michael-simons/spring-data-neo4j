/*
 * Copyright 2011-2023 the original author or authors.
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
package org.springframework.data.neo4j.transaction.domain;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.Properties;

/**
 * @author Michael J. Simons
 */
public class ThreadInfo {
	@Id
	@GeneratedValue
	private Long id;
	@Index
	private String name;
	@Index(unique = true)
	private String method;
	@Properties
	private Map<String,Long> serviceRelationship = new HashMap<>();

	public ThreadInfo() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, Long> getServiceRelationship() {
		return serviceRelationship;
	}

	public void setServiceRelationship(Map<String, Long> serviceRelationship) {
		this.serviceRelationship = serviceRelationship;
	}
}

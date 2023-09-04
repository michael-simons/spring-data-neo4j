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
package org.springframework.data.neo4j.repository.sample.relcentric;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
public class Movie {

	private Long id;
	private String name;

	@Relationship("HAS")
	private Genre genre;

	public Movie() {
	}

	public Movie(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}
}

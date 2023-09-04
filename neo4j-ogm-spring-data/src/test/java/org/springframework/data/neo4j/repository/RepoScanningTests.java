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
package org.springframework.data.neo4j.repository;

import static org.springframework.data.neo4j.test.GraphDatabaseServiceAssert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.domain.sample.User;
import org.springframework.data.neo4j.repository.sample.repo.UserRepository;
import org.springframework.data.neo4j.test.Neo4jIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Michal Bachman
 * @author Mark Angrish
 * @author Michael J. Simons
 */
@ContextConfiguration(classes = RepoScanningTests.PersistenceContextInTheSamePackage.class)
@RunWith(SpringRunner.class)
public class RepoScanningTests {

	@Autowired private GraphDatabaseService graphDatabaseService;

	@Autowired private UserRepository userRepository;

	@Before
	public void clearDatabase() {
		graphDatabaseService.executeTransactionally("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
	}

	@Test
	public void enableNeo4jRepositoriesShouldScanSelfPackageByDefault() {

		User user = new User("Michal");
		userRepository.save(user);

		Map<String, Object> params = new HashMap<>();
		params.put("name", user.getLastname());

		assertThat(graphDatabaseService)
				.containsNode("MATCH (n:User {lastname: $name}) RETURN n", params)
				.withId(user.getId());
	}

	@Configuration
	@Neo4jIntegrationTest(domainPackages = "org.springframework.data.neo4j.domain.sample")
	static class PersistenceContextInTheSamePackage {}

}

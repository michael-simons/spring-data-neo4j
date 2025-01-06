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
package org.springframework.data.neo4j.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.neo4j.test.GraphDatabaseServiceAssert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.data.neo4j.domain.sample.User;
import org.springframework.data.neo4j.domain.sample.Movie;
import org.springframework.data.neo4j.repository.sample.repo.MovieRepository;
import org.springframework.data.neo4j.repository.sample.repo.UserRepository;
import org.springframework.data.neo4j.repository.support.Neo4jRepositoryFactory;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Michal Bachman
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class ProgrammaticRepositoryTests {

	private static Neo4j serverControls;
	private static SessionFactory sessionFactory;
	private static TransactionTemplate transactionTemplate;

	private MovieRepository movieRepository;
	private Session session;

	@BeforeClass
	public static void oneTimeSetUp() {
		serverControls = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();

		Configuration configuration = new Configuration.Builder() //
				.uri(serverControls.boltURI().toString()) //
				.build();
		sessionFactory = new SessionFactory(configuration, "org.springframework.data.neo4j.domain.sample");

		PlatformTransactionManager platformTransactionManager = new Neo4jTransactionManager(sessionFactory);
		transactionTemplate = new TransactionTemplate(platformTransactionManager);
	}

	@AfterClass
	public static void shutdownTestServer() {

		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}

		if (serverControls != null) {
			serverControls.close();
			serverControls = null;
		}
	}

	@Before
	public void init() {
		session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	@Test
	public void canInstantiateRepositoryProgrammatically() {

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status) {
				RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

				movieRepository = factory.getRepository(MovieRepository.class);

				Movie movie = new Movie("PF");
				movieRepository.save(movie);
			}
		});

		Map<String, Object> params = new HashMap<>();
		params.put("title", "PF");
		assertThat(serverControls.defaultDatabaseService()).containsNode("MATCH (n:Movie) WHERE n.title = $title RETURN n", params);

		assertThat(StreamSupport.stream(movieRepository.findAll().spliterator(), false)
				.count()).isEqualTo(1);
	}

	@Test // DATAGRAPH-847
	public void shouldBeAbleToDeleteAllViaRepository() {

		RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

		UserRepository userRepository = factory.getRepository(UserRepository.class);

		User userA = new User("A");
		User userB = new User("B");
		userRepository.save(userA);
		userRepository.save(userB);

		assertThat(userRepository.count()).isEqualTo(2);

		userRepository.deleteAll();
		assertThat(userRepository.count()).isEqualTo(0);
	}

	@Test // DATAGRAPH-813
	public void shouldDeleteUserByNameAndReturnCountOfDeletedUsers() {

		RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

		UserRepository userRepository = factory.getRepository(UserRepository.class);

		User userA = new User("A");

		userRepository.save(userA);
		assertThat(userRepository.count()).isEqualTo(1);

		assertThat(userRepository.deleteByLastname("A")).isEqualTo(new Long(1));
		assertThat(userRepository.count()).isEqualTo(0);
	}

	@Test // DATAGRAPH-813
	public void shouldDeleteUserByNameAndReturnListOfDeletedUserIds() {

		RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

		UserRepository userRepository = factory.getRepository(UserRepository.class);

		User userA = new User("A");
		User userAClone = new User("A");

		userRepository.save(userA);
		userRepository.save(userAClone);

		assertThat(userRepository.count()).isEqualTo(2);

		List<Long> deletedUserIds = userRepository.removeByLastname("A");
		assertThat(deletedUserIds.size()).isEqualTo(2);

		Assertions.assertThat(deletedUserIds).containsExactlyInAnyOrder(userA.getId(), userAClone.getId());

		assertThat(userRepository.count()).isEqualTo(0);
	}

	@Test
	public void shouldBeAbleToDeleteUserWithRelationships() {

		RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

		UserRepository userRepository = factory.getRepository(UserRepository.class);

		User userA = new User("A");
		User userB = new User("B");

		userA.getFriends().add(userB);
		userB.getFriends().add(userA);

		userRepository.save(userA);

		assertThat(userRepository.count()).isEqualTo(2);

		userRepository.deleteByLastname("A");

		assertThat(userRepository.count()).isEqualTo(1);
	}

	@Test // DATAGRAPH-813
	public void shouldCountUserByName() {

		RepositoryFactorySupport factory = new Neo4jRepositoryFactory(session);

		UserRepository userRepository = factory.getRepository(UserRepository.class);

		User userA = new User("A");

		userRepository.save(userA);

		assertThat(userRepository.countByLastname("A")).isEqualTo(new Long(1));
	}
}

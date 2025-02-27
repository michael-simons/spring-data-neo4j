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
package org.springframework.data.neo4j.examples.galaxy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.examples.galaxy.domain.World;
import org.springframework.data.neo4j.examples.galaxy.repo.WorldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Mark Paluch
 * @author Jens Schauder
 */
@Service
@Transactional
public class GalaxyService {

	@Autowired private WorldRepository worldRepository;

	@Autowired Session session;

	public long getNumberOfWorlds() {
		return worldRepository.count();
	}

	public World createWorld(String name, int moons) {
		return worldRepository.save(new World(name, moons));
	}

	public World create(World world) {
		return worldRepository.save(world);
	}

	public Iterable<World> getAllWorlds() {
		return worldRepository.findAll();
	}

	public Optional<World> findWorldById(Long id) {
		return worldRepository.findById(id);
	}

	public World findWorldByName(String name) {
		Iterable<World> worlds = findByProperty("name", name);
		if (worlds.iterator().hasNext()) {
			return worlds.iterator().next();
		} else {
			return null;
		}
	}

	public Iterable<World> findAllByNumberOfMoons(int numberOfMoons) {
		return findByProperty("moons", numberOfMoons);
	}

	public Collection<World> makeSomeWorlds() {

		Collection<World> worlds = new ArrayList<World>();

		// Solar worlds
		worlds.add(createWorld("Mercury", 0));
		worlds.add(createWorld("Venus", 0));

		World earth = createWorld("Earth", 1);
		World mars = createWorld("Mars", 2);

		mars.addRocketRouteTo(earth);

		// todo: handle bi-directional automatically
		// earth.addRocketRouteTo(mars);

		// this is a bit silly
		worldRepository.save(mars);

		// todo: handle-bidirectional automatically
		// worldRepository.save(earth);

		worlds.add(earth);
		worlds.add(mars);

		worlds.add(createWorld("Jupiter", 63));
		worlds.add(createWorld("Saturn", 62));
		worlds.add(createWorld("Uranus", 27));
		worlds.add(createWorld("Neptune", 13));

		// Norse worlds
		worlds.add(createWorld("Alfheimr", 0));
		worlds.add(createWorld("Midgard", 1));
		worlds.add(createWorld("Muspellheim", 2));
		worlds.add(createWorld("Asgard", 63));
		worlds.add(createWorld("Hel", 62));

		return worlds;
	}

	// not in original
	public Collection<World> makeAllWorldsAtOnce() {

		Collection<World> worlds = new ArrayList<World>();

		// Solar worlds

		worlds.add(new World("Mercury", 0));
		worlds.add(new World("Venus", 0));

		World earth = new World("Earth", 1);
		World mars = new World("Mars", 2);

		mars.addRocketRouteTo(earth);
		earth.addRocketRouteTo(mars);

		worlds.add(earth);
		worlds.add(mars);

		worlds.add(new World("Jupiter", 63));
		worlds.add(new World("Saturn", 62));
		worlds.add(new World("Uranus", 27));
		worlds.add(new World("Neptune", 13));

		// Norse worlds
		worlds.add(new World("Alfheimr", 0));
		worlds.add(new World("Midgard", 1));
		worlds.add(new World("Muspellheim", 2));
		worlds.add(new World("Asgard", 63));
		worlds.add(new World("Hel", 62));

		worldRepository.saveAll(worlds);

		return worlds;
	}

	public void deleteAll() {
		worldRepository.deleteAll();
	}

	private Iterable<World> findByProperty(String propertyName, Object propertyValue) {
		return session.loadAll(World.class, new Filter(propertyName, ComparisonOperator.EQUALS, propertyValue));
	}

	public Iterable<World> findByProperty(String propertyName, Object propertyValue, int depth) {
		return session.loadAll(World.class, new Filter(propertyName, ComparisonOperator.EQUALS, propertyValue), depth);
	}

	public Iterable<World> findAllWorlds(Pagination paging) {
		return session.loadAll(World.class, paging, 0);
	}

	public Iterable<World> findAllWorlds(Sort sort) {
		return worldRepository.findAll(sort, 0);
	}

	public Page<World> findAllWorlds(Pageable pageable) {
		return worldRepository.findAll(pageable, 0);
	}

	public Iterable<World> findAllWorlds(Sort sort, int depth) {
		return worldRepository.findAll(sort, depth);
	}

	public World findByName(String name) {
		return worldRepository.findByName(name);
	}
}

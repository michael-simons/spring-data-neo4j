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
package org.springframework.data.neo4j.repository.query;

import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.util.PagingAndSortingUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Represents an OGM query. Can hold either cypher queries or filter definitions. Also in charge of adding pagination /
 * sort to the string based queries as OGM does not support pagination and sort on those.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 * @author Ihor Dziuba
 */
public class Query {

	private static final String SKIP_PARAM = "sdnSkip";
	private static final String LIMIT_PARAM = "sdnLimit";
	private static final String SKIP_LIMIT = " SKIP $" + SKIP_PARAM + " LIMIT $" + LIMIT_PARAM;
	private static final String LIMIT = "LIMIT $" + LIMIT_PARAM;
	private static final String ORDER_BY_CLAUSE = " ORDER BY %s";

	private Filters filters;
	private String cypherQuery;
	private Map<String, Object> parameters;
	private @Nullable String countQuery;
	private @Nullable Integer optionalLimit;
	private @Nullable Sort optionalSort;

	public Query(Filters filters, @Nullable Integer optionalLimit, Sort optionalSort) {

		Assert.notNull(filters, "Filters must not be null.");
		this.filters = filters;
		this.optionalLimit = optionalLimit;
		this.optionalSort = optionalSort;
	}

	public Query(String cypherQuery, @Nullable String countQuery, Map<String, Object> parameters) {
		Assert.notNull(cypherQuery, "Query must not be null.");
		Assert.notNull(parameters, "Parameters must not be null.");
		this.cypherQuery = sanitize(cypherQuery);
		this.countQuery = countQuery == null ? null : sanitize(countQuery);
		this.parameters = parameters;
	}

	public boolean isFilterQuery() {
		return filters != null;
	}

	public Filters getFilters() {
		return filters;
	}

	public String getCypherQuery() {
		return cypherQuery;
	}

	public Map<String, ?> getParameters() {
		return parameters;
	}

	public String getCountQuery() {
		return countQuery;
	}

	public String getCypherQuery(Pageable pageable, boolean forSlicing) {
		String result = cypherQuery;
		Sort sort = null;
		if (pageable.isPaged() && pageable.getSort() != Sort.unsorted()) {
			sort = pageable.getSort();
		}
		if (sort != Sort.unsorted()) {
			// Custom queries in the OGM do not support pageable
			result = addSorting(result, sort);
		}
		result = addPaging(result, pageable, forSlicing);
		return result;
	}

	public String getCypherQuery(Sort sort) {
		// Custom queries in the OGM do not support pageable
		String result = cypherQuery;
		if (sort != Sort.unsorted()) {
			result = addSorting(cypherQuery, sort);
		}
		return result;
	}

	private String addPaging(String cypherQuery, Pageable pageable, boolean forSlicing) {
		// Custom queries in the OGM do not support pageable
		cypherQuery = formatBaseQuery(cypherQuery);
		cypherQuery = cypherQuery + SKIP_LIMIT;
		parameters.put(SKIP_PARAM, pageable.getOffset());
		if (forSlicing) {
			parameters.put(LIMIT_PARAM, pageable.getPageSize() + 1);
		} else {
			parameters.put(LIMIT_PARAM, pageable.getPageSize());
		}
		return cypherQuery;
	}

	private String addSorting(String baseQuery, Sort sort) {
		baseQuery = formatBaseQuery(baseQuery);
		if (sort == null) {
			return baseQuery;
		}
		final String sortOrder = getSortOrder(sort);
		if (sortOrder.isEmpty()) {
			return baseQuery;
		}
		return baseQuery + String.format(ORDER_BY_CLAUSE, sortOrder);
	}

	private String getSortOrder(Sort sort) {
		return sort.stream()
				.map(Query::getPropertySortOrder)
				.collect(Collectors.joining(", "));
	}

	private static String getPropertySortOrder(Sort.Order order) {
		StringBuilder sb = new StringBuilder();
		if(order.isIgnoreCase()){
			sb.append("toLower(").append(order.getProperty()).append(")");
		} else {
			sb.append(order.getProperty());
		}
		sb.append(" ").append(order.getDirection());
		return sb.toString();
	}

	private String formatBaseQuery(String cypherQuery) {
		cypherQuery = cypherQuery.trim();
		if (cypherQuery.endsWith(";")) {
			cypherQuery = cypherQuery.substring(0, cypherQuery.length() - 1);
		}
		return cypherQuery;
	}

	@Nullable Pagination getOptionalPagination(@Nullable Pageable pageable, boolean forSlicing) {

		if(pageable != null) {
			Pagination pagination = new Pagination(pageable.getPageNumber(),pageable.getPageSize() + ((forSlicing) ? 1 : 0));
			pagination.setOffset(pageable.getPageNumber() * pageable.getPageSize());
			return pagination;
		}

		if(this.optionalLimit == null) {
			return null;
		}

		Pagination pagination = new Pagination(0, optionalLimit);
		pagination.setOffset(0);
		return pagination;
	}

	@Nullable Sort getOptionalSort() {
		return optionalSort;
	}

	private String sanitize(String cypherQuery) {
		cypherQuery = cypherQuery.trim();
		if (cypherQuery.endsWith(";")) {
			cypherQuery = cypherQuery.substring(0, cypherQuery.length() - 1);
		}
		return cypherQuery;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Query{");
		if (filters != null) {
			sb.append("filters=").append(filters);
		} else {
			sb.append("cypherQuery='").append(cypherQuery).append('\'');
		}
		sb.append('}');
		return sb.toString();
	}
}

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
package org.springframework.data.neo4j.web.support;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.AsyncWebRequestInterceptor;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;

/**
 * Spring web request interceptor that binds a Neo4j OGM Session to the thread for the entire processing of the request.
 * Intended for the "Open Session in View" pattern, i.e. to allow for lazy loading in web views despite the original
 * transactions already being completed.
 * <p>
 * This interceptor makes Neo4j OGM Sessions available via the current thread, which will be autodetected by transaction
 * managers. It is suitable for service layer transactions via {@link Neo4jTransactionManager}.
 * <p>
 * In contrast to {@link OpenSessionInViewFilter}, this interceptor is set up in a Spring application context and can
 * thus take advantage of bean wiring.
 *
 * @see OpenSessionInViewFilter
 * @see Neo4jTransactionManager
 * @see TransactionSynchronizationManager
 *
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class OpenSessionInViewInterceptor implements BeanFactoryAware, AsyncWebRequestInterceptor {

	/**
	 * Logger available to subclasses
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Suffix that gets appended to the SessionFactory toString representation for the "participate in existing session
	 * handling" request attribute.
	 *
	 * @see #getParticipateAttributeName
	 */
	public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";

	private SessionFactory sessionFactory;

	/**
	 * Set the Neo4j OGM SessionFactory that should be used to create Sessions.
	 *
	 * @see SessionFactory#openSession
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the Neo4j OGM SessionFactory that should be used to create Sessions.
	 */
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	/**
	 * Retrieves the default SessionFactory bean.
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (getSessionFactory() == null) {
			setSessionFactory(beanFactory.getBean(SessionFactory.class));
		}
	}

	@Override
	public void preHandle(WebRequest request) throws DataAccessException {
		String participateAttributeName = getParticipateAttributeName();

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		if (asyncManager.hasConcurrentResult()) {
			if (applyCallableInterceptor(asyncManager, participateAttributeName)) {
				return;
			}
		}

		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			// Do not modify the Session: just mark the request accordingly.
			Integer count = (Integer) request.getAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
			int newCount = (count != null ? count + 1 : 1);
			request.setAttribute(getParticipateAttributeName(), newCount, WebRequest.SCOPE_REQUEST);
		} else {
			logger.debug("Opening Neo4j OGM Session in OpenSessionInViewInterceptor");
			Session session = sessionFactory.openSession();
			SessionHolder sessionHolder = new SessionHolder(session);
			TransactionSynchronizationManager.bindResource(getSessionFactory(), sessionHolder);

			AsyncRequestInterceptor interceptor = new AsyncRequestInterceptor(getSessionFactory(), sessionHolder);
			asyncManager.registerCallableInterceptor(participateAttributeName, interceptor);
			asyncManager.registerDeferredResultInterceptor(participateAttributeName, interceptor);
		}
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) {}

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws DataAccessException {
		if (!decrementParticipateCount(request)) {
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
			logger.debug("Closed Neo4j OGM Session in OpenSessionInViewInterceptor");
		}
	}

	private boolean decrementParticipateCount(WebRequest request) {
		String participateAttributeName = getParticipateAttributeName();
		Integer count = (Integer) request.getAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
		if (count == null) {
			return false;
		}
		// Do not modify the Session: just clear the marker.
		if (count > 1) {
			request.setAttribute(participateAttributeName, count - 1, WebRequest.SCOPE_REQUEST);
		} else {
			request.removeAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
		}
		return true;
	}

	@Override
	public void afterConcurrentHandlingStarted(WebRequest request) {
		if (!decrementParticipateCount(request)) {
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request is already filtered. Default implementation
	 * takes the toString representation of the SessionFactory instance and appends ".FILTERED".
	 *
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
	}

	private boolean applyCallableInterceptor(WebAsyncManager asyncManager, String key) {

		CallableProcessingInterceptor callableInterceptor = asyncManager.getCallableInterceptor(key);
		if (callableInterceptor == null) {
			return false;
		}
		((AsyncRequestInterceptor) callableInterceptor).bindSession();
		return true;
	}
}

/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ruleengineservices.rule.evaluation;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * This interface represents the context for actions applied during rule evaluation.
 *
 */
public interface RuleActionContext
{
	/**
	 * Returns the rule's meta-data for the given key (or {@code null}). Calls {@code toString()} on the meta-data
	 * object.
	 *
	 * @param key
	 *
	 * @return the string representation of the meta-data (or null)
	 */
	String getMetaData(String key);

	/**
	 * schedules the given facts for an update. Note: The actual fact update is invoked via
	 * {@link #updateScheduledFacts()}
	 *
	 * @param facts
	 *           the facts to schedule for update
	 */
	void scheduleForUpdate(Object... facts);

	/**
	 * updates any previously scheduled facts (see {@link #scheduleForUpdate(Object...)}
	 */
	void updateScheduledFacts();

	/**
	 * Returns the value for a specific type from the default variables container. If more than one value is found, the
	 * first one is returned.
	 *
	 * @param type
	 *           - the type
	 *
	 * @return the value or null if no value is found for the given type
	 */
	<T> T getValue(Class<T> type);

	/**
	 * Returns the values for a specific type from the default variables container.
	 *
	 * @param type
	 *           - the type
	 *
	 * @return the values or an empty set if no value is found for the given type
	 */
	<T> Set<T> getValues(Class<T> type);

	/**
	 * Returns the value for a specific type and variables container. If more than one value is found, the first one is
	 * returned.
	 *
	 * @param type
	 *           - the type
	 * @param path
	 *           - the path for the variables container
	 *
	 * @return the value or null if no value is found for the given type
	 */
	<T> T getValue(Class<T> type, String... path);

	/**
	 * Returns the values for a specific type and variables container.
	 *
	 * @param type
	 *           - the type
	 * @param path
	 *           - the path for the variables container
	 *
	 * @return the values or an empty set if no value is found for the given type
	 */
	<T> Set<T> getValues(Class<T> type, String... path);

	/**
	 * Insert the facts in an array to a context
	 *
	 * @param facts
	 *           - the array of facts
	 */
	void insertFacts(Object... facts);

	/**
	 * Insert the facts in a collection to a context
	 *
	 * @param facts
	 *           - the collection of facts
	 */
	void insertFacts(Collection facts);

	/**
	 * Update the facts in a context
	 *
	 * @param facts
	 *           - the array of facts
	 */
	void updateFacts(final Object... facts);

	/**
	 * Get cart RAO
	 */
	CartRAO getCartRao();

	/**
	 * Get rule engine result RAO
	 */
	RuleEngineResultRAO getRuleEngineResultRao();

	/**
	 * Get the name of the rule
	 */
	String getRuleName();

	/**
	 * get rule metadata
	 */
	Map<String, Object> getRuleMetadata();


	/**
	 * Returns a rule engine specific context object.
	 */
	Object getDelegate();

	/**
	 * Get the name of the rules module.
	 */
	Optional<String> getRulesModuleName();

	/**
	 * Returns parameters of the running rule.
	 */
	Map<String, Object> getParameters();

	/**
	 * Returns parameter of the running rule by it name or null if not found.
	 */
	Object getParameter(String parameterName);

	/**
	 * Returns parameter of the running rule by it name and type or throws {@link IllegalArgumentException} otherwise.
	 */
	<T> T getParameter(String parameterName, Class<T> type);

	/**
	 * Sets parameters to the running rule
	 *
	 * @param parameters
	 *           - map of named objects, creating the execution context for an action
	 */
	void setParameters(final Map<String, Object> parameters);

	/**
	 * Is the rule stackable?
	 * @deprecated since 6.7
	 */
	@Deprecated
	boolean isStackable();

	/**
	 * Stops evaluation of following rules
	 */
	void halt();
}

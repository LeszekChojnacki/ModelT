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
package de.hybris.platform.ruleengineservices.compiler;

/**
 * Implementations of this interface are responsible for generating variables.
 */
@SuppressWarnings({"squid:S1170","squid:S1214"})
public interface RuleIrVariablesGenerator
{
	String DEFAULT_VARIABLES_CONTAINER_ID = "default";
	String CONTAINER_PATH_SEPARATOR = "/";

	/**
	 * Returns the root container.
	 *
	 * @return the root container
	 */
	RuleIrVariablesContainer getRootContainer();

	/**
	 * Returns the current container.
	 *
	 * @return the current container
	 */
	RuleIrVariablesContainer getCurrentContainer();

	/**
	 * Creates a new container and uses the current one as parent. The new container is set as the current one.
	 *
	 * @param id
	 *           - the id for the new container
	 *
	 * @return the current container
	 */
	RuleIrVariablesContainer createContainer(String id);

	/**
	 * Closes the current container. The parent container is set as the current one.
	 */
	void closeContainer();

	/**
	 * Generates a new variable for the given type. If a variable already exists for the given type in the current
	 * container or any of the parent containers, it will be used. If not, a new one will be created in the current
	 * container.
	 *
	 * @param type
	 *           - the type
	 *
	 * @return the name of the variable
	 */
	String generateVariable(Class<?> type);

	/**
	 * Creates a new local container.
	 *
	 * @return the current container
	 */
	RuleIrLocalVariablesContainer createLocalContainer();

	/**
	 * Generates a new variable for the given local container and type.
	 *
	 * @param type
	 *           - the type
	 *
	 * @return the name of the variable
	 */
	String generateLocalVariable(RuleIrLocalVariablesContainer container, Class<?> type);

}

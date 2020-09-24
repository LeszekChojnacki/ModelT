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
package de.hybris.platform.ruleengine.cache;

/**
 * Interface for retrieving of rule globals beans
 */
@FunctionalInterface
public interface RuleGlobalsBeanProvider
{

	/**
	 * The method to be used to retrieve the rule globals by key
	 * @param key
	 * 		key of rule globals entry
	 * @return
	 * 		the globals bean
	 */
	Object getRuleGlobals(String key);

}

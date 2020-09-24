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
package de.hybris.platform.ruleengine.init.tasks;

import de.hybris.platform.ruleengine.RuleEngineActionResult;

import java.util.List;
import java.util.function.Supplier;


/**
 * Interface providing the abstraction for post rules module swapping task sequences
 */
public interface PostRulesModuleSwappingTasksProvider
{
	/**
	 * Get list of suppliers for the post rules module swapping tasks
	 *
	 * @param result
	 * 		instance of {@link RuleEngineActionResult}
	 * @return list of suppliers executing the tasks
	 */
	List<Supplier<Object>> getTasks(RuleEngineActionResult result);
}

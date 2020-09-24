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
package de.hybris.platform.ruleengine.init.tasks.impl;

import static java.util.stream.Collectors.toList;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTask;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTasksProvider;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the {@link PostRulesModuleSwappingTasksProvider}
 */
public class DefaultPostRulesModuleSwappingTasksProvider implements PostRulesModuleSwappingTasksProvider
{
	private List<PostRulesModuleSwappingTask> postRulesModuleSwappingTasks;

	@Override
	public List<Supplier<Object>> getTasks(final RuleEngineActionResult result)
	{
		return getPostRulesModuleSwappingTasks().stream().map(task -> (Supplier<Object>) () -> task.execute(result))
				.collect(toList());
	}

	protected List<PostRulesModuleSwappingTask> getPostRulesModuleSwappingTasks()
	{
		return postRulesModuleSwappingTasks;
	}

	@Required
	public void setPostRulesModuleSwappingTasks(final List<PostRulesModuleSwappingTask> postRulesModuleSwappingTasks)
	{
		this.postRulesModuleSwappingTasks = postRulesModuleSwappingTasks;
	}
}

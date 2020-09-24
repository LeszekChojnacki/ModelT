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
package de.hybris.platform.ruleengine.init;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

import de.hybris.platform.ruleengine.RuleEngineActionResult;

import java.util.List;


/**
 * The future object used to synchronize on multiple modules initialization
 */
public class InitializationFuture
{

	private RuleEngineKieModuleSwapper moduleSwapper;
	private List<RuleEngineActionResult> results;

	private InitializationFuture(final RuleEngineKieModuleSwapper moduleSwapper)
	{
		this.moduleSwapper = moduleSwapper;
		this.results = newCopyOnWriteArrayList();
	}

	public static InitializationFuture of(final RuleEngineKieModuleSwapper moduleSwapper)
	{
		return new InitializationFuture(moduleSwapper);
	}

	public InitializationFuture waitForInitializationToFinish()
	{
		moduleSwapper.waitForSwappingToFinish();
		return this;
	}

	public List<RuleEngineActionResult> getResults()
	{
		return results;
	}
}

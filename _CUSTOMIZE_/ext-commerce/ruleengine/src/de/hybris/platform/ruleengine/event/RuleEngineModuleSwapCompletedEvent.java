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
package de.hybris.platform.ruleengine.event;

import de.hybris.platform.ruleengine.ResultItem;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import java.util.Collection;


/**
 * The Event is fired on Rule Engine initialization when the kie module swap is completed or on error
 *
 */
public class RuleEngineModuleSwapCompletedEvent extends AbstractEvent
{

	private final String rulesModuleName;
	private final String previousRulesModuleVersion;
	private final String rulesModuleVersion;
	private boolean failed;
	private String failureReason;
	private Collection<ResultItem> results;

	private RuleEngineModuleSwapCompletedEvent(final String rulesModuleName, final String previousRulesModuleVersion,
			final String rulesModuleVersion, final Collection<ResultItem> results)
	{
		this.rulesModuleName = rulesModuleName;
		this.previousRulesModuleVersion = previousRulesModuleVersion;
		this.rulesModuleVersion = rulesModuleVersion;
		this.results = results;
	}

	public static RuleEngineModuleSwapCompletedEvent ofSuccess(final String rulesModuleName, final String previousRulesModuleVersion,
			final String rulesModuleVersion, final Collection<ResultItem> results)
	{
		return new RuleEngineModuleSwapCompletedEvent(rulesModuleName, previousRulesModuleVersion, rulesModuleVersion, results);
	}

	public static RuleEngineModuleSwapCompletedEvent ofFailure(final String rulesModuleName, final String previousRulesModuleVersion,
			final String failureReason, final Collection<ResultItem> results)
	{
		final RuleEngineModuleSwapCompletedEvent completedEvent = new RuleEngineModuleSwapCompletedEvent(
				rulesModuleName, previousRulesModuleVersion, null, results);
		completedEvent.setFailed(true);
		completedEvent.setFailureReason(failureReason);
		return completedEvent;
	}

	@Override
	public String toString()
	{
		return "RuleEngineModuleSwapCompletedEvent{" +
				"rulesModuleName='" + rulesModuleName + '\'' +
				", previousRulesModuleVersion='" + previousRulesModuleVersion + '\'' +
				", rulesModuleVersion='" + rulesModuleVersion + '\'' +
				", failed=" + failed +
				", failureReason='" + failureReason + '\'' +
				'}';
	}

	public String getRulesModuleName()
	{
		return rulesModuleName;
	}

	public String getPreviousRulesModuleVersion()
	{
		return previousRulesModuleVersion;
	}

	public String getRulesModuleVersion()
	{
		return rulesModuleVersion;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(final boolean failed)
	{
		this.failed = failed;
	}

	public String getFailureReason()
	{
		return failureReason;
	}

	public void setFailureReason(final String failureReason)
	{
		this.failureReason = failureReason;
	}

	public Collection<ResultItem> getResults()
	{
		return results;
	}
}

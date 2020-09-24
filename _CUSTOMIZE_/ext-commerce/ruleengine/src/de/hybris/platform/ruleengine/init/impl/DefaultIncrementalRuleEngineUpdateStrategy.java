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
package de.hybris.platform.ruleengine.init.impl;

import de.hybris.platform.ruleengine.init.IncrementalRuleEngineUpdateStrategy;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;

import java.util.Collection;

import org.kie.api.builder.ReleaseId;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link IncrementalRuleEngineUpdateStrategy} interface
 */
public class DefaultIncrementalRuleEngineUpdateStrategy implements IncrementalRuleEngineUpdateStrategy
{
	private EngineRulesRepository engineRulesRepository;
	private int totalNumOfRulesThreshold;
	private float fractionOfRulesThreshold;

	@Override
	public boolean shouldUpdateIncrementally(final ReleaseId releaseId, final String moduleName,
			final Collection<DroolsRuleModel> rulesToAdd, final Collection<DroolsRuleModel> rulesToRemove)
	{
		boolean updateIncrementally;
		final long totalNumberOfDeployedRules = getEngineRulesRepository().countDeployedEngineRulesForModule(moduleName);
		int numberOfRulesToUpdate = rulesToAdd.size() + rulesToRemove.size();
		updateIncrementally =
				Math.sqrt((double)totalNumberOfDeployedRules * totalNumberOfDeployedRules + numberOfRulesToUpdate * numberOfRulesToUpdate)
						> totalNumOfRulesThreshold;
		if (updateIncrementally && numberOfRulesToUpdate > 0)
		{
			updateIncrementally = totalNumberOfDeployedRules > 0
					&& ((double) numberOfRulesToUpdate) / ((double) totalNumberOfDeployedRules) < fractionOfRulesThreshold;
		}
		return updateIncrementally;
	}

	protected EngineRulesRepository getEngineRulesRepository()
	{
		return engineRulesRepository;
	}

	@Required
	public void setEngineRulesRepository(final EngineRulesRepository engineRulesRepository)
	{
		this.engineRulesRepository = engineRulesRepository;
	}

	protected int getTotalNumOfRulesThreshold()
	{
		return totalNumOfRulesThreshold;
	}

	@Required
	public void setTotalNumOfRulesThreshold(final int totalNumOfRulesThreshold)
	{
		this.totalNumOfRulesThreshold = totalNumOfRulesThreshold;
	}

	protected float getFractionOfRulesThreshold()
	{
		return fractionOfRulesThreshold;
	}

	@Required
	public void setFractionOfRulesThreshold(final float fractionOfRulesThreshold)
	{
		this.fractionOfRulesThreshold = fractionOfRulesThreshold;
	}
}

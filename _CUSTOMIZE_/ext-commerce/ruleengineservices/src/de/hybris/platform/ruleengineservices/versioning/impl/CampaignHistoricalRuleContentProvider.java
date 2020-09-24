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
package de.hybris.platform.ruleengineservices.versioning.impl;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.versioning.HistoricalRuleContentProvider;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.internal.model.impl.DefaultModelServiceInterceptorContext;
import de.hybris.platform.servicelayer.internal.model.impl.RegisteredElements;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * implementation of {@link HistoricalRuleContentProvider} copying the Campaign association types
 */
public class CampaignHistoricalRuleContentProvider implements HistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final SourceRuleModel sourceRule,
			final SourceRuleModel historicalSourceRule, final InterceptorContext ctx)
	{
		if(wasSourceRuleEverPublished(sourceRule) && ctx instanceof DefaultModelServiceInterceptorContext)
		{
			final RegisteredElements registeredElements = ((DefaultModelServiceInterceptorContext) ctx).getInitialElements();
			final Set<CampaignModel> registeredCampaignSet = registeredElements.unmodifiableSet().stream()
					.filter(CampaignModel.class::isInstance).map(CampaignModel.class::cast).collect(toSet());
			if(isNotEmpty(registeredCampaignSet))
			{
				final Set<CampaignModel> campaignSet = registeredCampaignSet.stream().filter(c -> c.getSourceRules().contains(sourceRule))
						.collect(Collectors.toSet());
				if(isNotEmpty(campaignSet))
				{
					campaignSet.forEach(c -> substituteAssociatedSourceRule(c, sourceRule, historicalSourceRule));
					campaignSet.forEach(c -> ctx.registerElementFor(c, PersistenceOperation.SAVE));
				}
			}
		}
	}

	protected void substituteAssociatedSourceRule(final CampaignModel campaign, final SourceRuleModel ruleToRemove, final SourceRuleModel ruleToAdd)
	{
		final Set<SourceRuleModel> sourceRules = campaign.getSourceRules();
		sourceRules.remove(ruleToRemove);
		sourceRules.add(ruleToAdd);
	}

	protected boolean wasSourceRuleEverPublished(final AbstractRuleModel rule)
	{
		return rule instanceof SourceRuleModel && !rule.getStatus().equals(RuleStatus.UNPUBLISHED);
	}
}

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

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.internal.model.impl.DefaultModelServiceInterceptorContext;
import de.hybris.platform.servicelayer.internal.model.impl.RegisteredElements;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Sets;


/**
 * Extension of {@link SourceRuleModelHistoricalContentCreator} adding on the control over associated rules campaigns
 */
public class CampaignSourceRuleModelHistoricalContentCreator extends SourceRuleModelHistoricalContentCreator
{
	@Override
	protected boolean associatedTypesChanged(final AbstractRuleModel ruleModel, final InterceptorContext ctx)
	{
		boolean changed = super.associatedTypesChanged(ruleModel, ctx);
		if (!changed)
		{
			changed = associatedCampaignsChanged(ruleModel, ctx);
		}
		return changed;
	}

	protected boolean associatedCampaignsChanged(final AbstractRuleModel rule, final InterceptorContext ctx)
	{
		if (wasSourceRuleEverPublished(rule) && ctx instanceof DefaultModelServiceInterceptorContext)
		{
			final SourceRuleModel sourceRule = (SourceRuleModel)rule;
			final RegisteredElements registeredElements = ((DefaultModelServiceInterceptorContext) ctx).getInitialElements();
			final Set<CampaignModel> registeredCampaignSet = registeredElements.unmodifiableSet().stream()
					.filter(CampaignModel.class::isInstance).map(CampaignModel.class::cast).collect(toSet());
			// if the campaign was not involved in modification, just skip any further analysis
			if (CollectionUtils.isNotEmpty(registeredCampaignSet))
			{
				final Set<CampaignModel> associatedCampaignSet = registeredCampaignSet.stream()
						.filter(c -> c.getSourceRules().contains(sourceRule)).collect(toSet());
				final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(sourceRule);
				final Set<CampaignModel> origCampaigns = modelContext.getOriginalValue(SourceRuleModel.CAMPAIGNS);
				return !Sets.symmetricDifference(associatedCampaignSet, origCampaigns).isEmpty();
			}
		}
		return false;
	}

	protected boolean wasSourceRuleEverPublished(final AbstractRuleModel rule)
	{
		return rule instanceof SourceRuleModel && !rule.getStatus().equals(RuleStatus.UNPUBLISHED);
	}

}

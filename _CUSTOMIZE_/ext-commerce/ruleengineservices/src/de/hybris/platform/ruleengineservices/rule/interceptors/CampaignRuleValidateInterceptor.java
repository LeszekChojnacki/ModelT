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
package de.hybris.platform.ruleengineservices.rule.interceptors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Instance of {@link ValidateInterceptor} monitoring the campaign rule removal
 */
public class CampaignRuleValidateInterceptor implements ValidateInterceptor<CampaignModel>
{

	private static final String PUBLISHED_RULES_ERROR_MESSAGE =
			"Only rules with " + RuleStatus.UNPUBLISHED + " status could be removed";

	@Override
	public void onValidate(final CampaignModel campaign, final InterceptorContext ctx)
			throws InterceptorException
	{
		if (!ctx.isNew(campaign))
		{
			final Set<SourceRuleModel> sourceRules = campaign.getSourceRules();
			final Set<SourceRuleModel> frozenAssociatedSourceRules = getFrozenAssociatedSourceRules(campaign);
			if (isNotEmpty(frozenAssociatedSourceRules) && !sourceRules.containsAll(frozenAssociatedSourceRules))
			{
				throw new InterceptorException(PUBLISHED_RULES_ERROR_MESSAGE, this);
			}
		}
	}

	protected Set<SourceRuleModel> getFrozenAssociatedSourceRules(final CampaignModel campaign)
	{
		final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(campaign);
		final Set<SourceRuleModel> allAssociatedSourceRules = modelContext.getOriginalValue(CampaignModel.SOURCERULES);
		return allAssociatedSourceRules.stream().filter(r -> !r.getStatus().equals(RuleStatus.UNPUBLISHED)).collect(
				Collectors.toSet());
	}
}

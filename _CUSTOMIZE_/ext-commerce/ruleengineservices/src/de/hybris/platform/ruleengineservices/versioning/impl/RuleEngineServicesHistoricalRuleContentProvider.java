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

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.impl.AbstractHistoricalRuleContentProvider;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


/**
 * The HistoricalRuleContentProvider addon implementation, specific for ruleengine extension
 */
public class RuleEngineServicesHistoricalRuleContentProvider extends AbstractHistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel ruleModel,
			final AbstractRuleEngineRuleModel historicalRuleModel, final InterceptorContext ctx)
	{
		if (historicalRuleModel instanceof DroolsRuleModel)
		{
			final DroolsRuleModel historicalDroolModel = (DroolsRuleModel) historicalRuleModel;
			historicalDroolModel.setSourceRule(this.<SourceRuleModel> getOriginal(ruleModel, ctx, DroolsRuleModel.SOURCERULE));
		}
	}


}

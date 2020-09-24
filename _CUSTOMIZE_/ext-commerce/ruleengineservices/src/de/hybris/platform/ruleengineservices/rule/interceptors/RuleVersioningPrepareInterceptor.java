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

import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.DEFAULT_RULE_VERSION;
import static java.util.Objects.isNull;

import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.versioning.RuleModelHistoricalContentCreator;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import org.springframework.beans.factory.annotation.Required;


/**
 * Versioning-specific prepare interceptor for AbstractRuleModel.
 */
public class RuleVersioningPrepareInterceptor implements PrepareInterceptor<AbstractRuleModel>
{
	private RuleModelHistoricalContentCreator historicalContentCreator;

	@Override
	public void onPrepare(final AbstractRuleModel model, final InterceptorContext context) throws InterceptorException
	{
		if (!(model instanceof SourceRuleModel))
		{
			return;
		}
		final SourceRuleModel sourceRule = (SourceRuleModel) model;

		if (isNull(sourceRule.getVersion()))
		{
			sourceRule.setVersion(DEFAULT_RULE_VERSION);
		}
		if (!context.isNew(sourceRule))
		{
			getHistoricalContentCreator().createHistoricalVersion(sourceRule, context);
		}
	}

	protected RuleModelHistoricalContentCreator getHistoricalContentCreator()
	{
		return historicalContentCreator;
	}

	@Required
	public void setHistoricalContentCreator(final RuleModelHistoricalContentCreator historicalContentCreator)
	{
		this.historicalContentCreator = historicalContentCreator;
	}


}

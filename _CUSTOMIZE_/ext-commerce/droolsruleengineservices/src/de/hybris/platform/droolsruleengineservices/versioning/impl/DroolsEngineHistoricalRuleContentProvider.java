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
package de.hybris.platform.droolsruleengineservices.versioning.impl;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.impl.AbstractHistoricalRuleContentProvider;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


public class DroolsEngineHistoricalRuleContentProvider extends AbstractHistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel ruleModel,
			final AbstractRuleEngineRuleModel historicalRuleModel, final InterceptorContext ctx)
	{
		if (historicalRuleModel instanceof DroolsRuleModel)
		{
			final DroolsRuleModel historicalDroolModel = (DroolsRuleModel) historicalRuleModel;
			historicalDroolModel.setGlobals(this.getOriginal(ruleModel, ctx, DroolsRuleModel.GLOBALS));
			historicalDroolModel.setKieBase(this.getOriginal(ruleModel, ctx, DroolsRuleModel.KIEBASE));
			historicalDroolModel.setRulePackage(this.getOriginal(ruleModel, ctx, DroolsRuleModel.RULEPACKAGE));
		}
	}


}

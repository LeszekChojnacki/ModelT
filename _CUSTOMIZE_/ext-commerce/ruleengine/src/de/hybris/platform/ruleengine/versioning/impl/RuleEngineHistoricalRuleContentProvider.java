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
package de.hybris.platform.ruleengine.versioning.impl;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


public class RuleEngineHistoricalRuleContentProvider extends AbstractHistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel ruleModel,
			final AbstractRuleEngineRuleModel historicalRuleModel, final InterceptorContext ctx)
	{
		historicalRuleModel.setActive(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.ACTIVE));
		historicalRuleModel.setUuid(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.UUID));
		historicalRuleModel.setChecksum(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.CHECKSUM));
		historicalRuleModel.setRuleContent(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.RULECONTENT));
		historicalRuleModel.setVersion(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.VERSION));
		historicalRuleModel.setModifiedtime(this.getOriginal(ruleModel, ctx, DroolsRuleModel.MODIFIEDTIME));
		historicalRuleModel.setCreationtime(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.CREATIONTIME));
		historicalRuleModel
				.setComments(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.COMMENTS));
		historicalRuleModel
				.setRuleParameters(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.RULEPARAMETERS));
		historicalRuleModel.setMessageFired(this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.MESSAGEFIRED));
	}

}

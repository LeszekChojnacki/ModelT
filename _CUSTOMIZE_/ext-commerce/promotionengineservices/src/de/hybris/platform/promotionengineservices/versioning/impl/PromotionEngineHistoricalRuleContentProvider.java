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
package de.hybris.platform.promotionengineservices.versioning.impl;

import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.versioning.impl.AbstractHistoricalRuleContentProvider;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;

import java.util.Optional;
import java.util.function.Consumer;


public class PromotionEngineHistoricalRuleContentProvider extends AbstractHistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel ruleModel,
			final AbstractRuleEngineRuleModel historicalRuleModel, final InterceptorContext ctx)
	{
		final Optional<RuleBasedPromotionModel> ruleBasedPromotion = Optional
				.ofNullable(this.<RuleBasedPromotionModel> getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.PROMOTION));
		ruleBasedPromotion.ifPresent(backupOriginalValues(historicalRuleModel));
	}

	protected Consumer<RuleBasedPromotionModel> backupOriginalValues(final AbstractRuleEngineRuleModel historicalRuleModel)
	{
		return p -> {
			p.setRule(historicalRuleModel);
			historicalRuleModel.setPromotion(p);
		};
	}

}

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
package de.hybris.platform.promotionengineservices.promotionengine.report.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;


import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.promotionengineservices.constants.PromotionEngineServicesConstants.PromotionCertainty;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResult;
import de.hybris.platform.promotions.PromotionResultService;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;


/**
 * Populator responsible for populating data from {@link PromotionResultModel} to {@link PromotionEngineResult}
 */
public class PromotionEngineResultPopulator implements Populator<PromotionResultModel, PromotionEngineResult>
{
	private PromotionResultService promotionResultService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(final PromotionResultModel source, final PromotionEngineResult target)
	{
		checkArgument(nonNull(source),"Source cannot be null");
		checkArgument(nonNull(target),"Target cannot be null");
		final RuleBasedPromotionModel promotion = (RuleBasedPromotionModel) source.getPromotion();
		final AbstractRuleEngineRuleModel rule = promotion.getRule();
		final AbstractRuleModel sourceRule = rule.getSourceRule();
		target.setCode(sourceRule.getCode());
		target.setName(sourceRule.getName());
		String messageFired = source.getMessageFired();
		if(StringUtils.isEmpty(messageFired))
		{
			messageFired = getPromotionResultService().getDescription(source);
		}
		target.setDescription(messageFired);
		target.setPromotionResult(source);
		target.setFired(PromotionCertainty.FIRED.around(source.getCertainty()));
	}

	protected PromotionResultService getPromotionResultService()
	{
		return promotionResultService;
	}

	@Required
	public void setPromotionResultService(final PromotionResultService promotionResultService)
	{
		this.promotionResultService = promotionResultService;
	}
}

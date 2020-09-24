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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.dao.RuleBasedPromotionActionDao;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResult;
import de.hybris.platform.promotions.PromotionResultService;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.util.DiscountValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populator responsible for populating {@link PromotionResultModel} associated with provided {@link DiscountValue} data
 * into {@link PromotionEngineResult}
 */
public class DiscountValuePromotionEngineResultPopulator implements Populator<DiscountValue, PromotionEngineResult>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscountValuePromotionEngineResultPopulator.class);
	private RuleBasedPromotionActionDao ruleBasedPromotionActionDao;
	private PromotionResultService promotionResultService;
	private Populator<PromotionResultModel, PromotionEngineResult> promotionResultPopulator;

	@Override
	public void populate(final DiscountValue source, final PromotionEngineResult target)
	{
		checkArgument(nonNull(source),"Source cannot be null");
		checkArgument(nonNull(target),"Target cannot be null");
		target.setDiscountValue(source);                                                           							// NOSONAR
		try
		{
			final AbstractRuleBasedPromotionActionModel action = getRuleBasedPromotionActionDao()
						 .findRuleBasedPromotionByGuid(source.getCode());
			getPromotionResultPopulator().populate(action.getPromotionResult(), target);
		}
		catch( AmbiguousIdentifierException | ModelNotFoundException e)
		{
			LOGGER.warn("Cannot find an action corresponding to discount value",e);
			target.setCode("Unable to find corresponding action");
		}
	}

	protected RuleBasedPromotionActionDao getRuleBasedPromotionActionDao()
	{
		return ruleBasedPromotionActionDao;
	}

	@Required
	public void setRuleBasedPromotionActionDao(final RuleBasedPromotionActionDao ruleBasedPromotionActionDao)
	{
		this.ruleBasedPromotionActionDao = ruleBasedPromotionActionDao;
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

	protected Populator<PromotionResultModel, PromotionEngineResult> getPromotionResultPopulator()
	{
		return promotionResultPopulator;
	}
	@Required
	public void setPromotionResultPopulator(
				 final Populator<PromotionResultModel, PromotionEngineResult> promotionResultPopulator)
	{
		this.promotionResultPopulator = promotionResultPopulator;
	}
}

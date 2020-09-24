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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.dao.RuleBasedPromotionActionDao;
import de.hybris.platform.promotionengineservices.util.ActionUtils;
import de.hybris.platform.promotions.model.PromotionResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.AbstractPromotionEngineResults;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResult;
import de.hybris.platform.util.DiscountValue;


/**
 * Populator responsible for populating {@link DiscountValue} data into {@link PromotionEngineResult}
 */
public abstract class AbstractPromotionEngineResultPopulator<S, R extends AbstractPromotionEngineResults> implements Populator<S, R>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPromotionEngineResultPopulator.class);

	private RuleBasedPromotionActionDao ruleBasedPromotionActionDao;
	private Populator<PromotionResultModel, PromotionEngineResult> promotionResultPopulator;
	private ActionUtils actionUtils;

	/**
	 * Populates {@link AbstractPromotionEngineResults} based on discounts supplied by {@link DiscountValue} and
	 * {@link de.hybris.platform.core.model.order.OrderModel}

	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final S source, final R target)
	{
		checkArgument(nonNull(source),"Source cannot be null");
		checkArgument(nonNull(target),"Target cannot be null");
		final List<PromotionEngineResult> promotionEngineResult = getPromotionEngineResults(source);
		target.setPromotionEngineResults(promotionEngineResult);
	}

	protected List<PromotionEngineResult> getPromotionEngineResults(final S source)
	{
		final Collection<DiscountValue> discountValues = getDiscountValues(source).stream()
				.filter(dv -> getActionUtils().isActionUUID(dv.getCode()))
				.collect(Collectors.toList());
		final List<AbstractRuleBasedPromotionActionModel> promotions = getRuleBasedPromotionActionDao()
				.findRuleBasedPromotions(getOrder(source), discountValues);
		final Map<DiscountValue, List<PromotionResultModel>> discountValue2PromotionResults = discountValues.stream()

				.collect(Collectors.toMap(
						Function.identity(),
						dv -> promotions.stream().filter(p -> p.getGuid().equals(dv.getCode()))
								.map(AbstractRuleBasedPromotionActionModel::getPromotionResult).collect(Collectors.toList())
				));

		return discountValue2PromotionResults.entrySet().stream()
				.map(e -> createPromotionEngineResult(e.getKey(), e.getValue())).collect(Collectors.toList());
	}

	protected PromotionEngineResult createPromotionEngineResult(
			final DiscountValue discountValue, final List<PromotionResultModel> promotionResults)
	{
		checkArgument(nonNull(discountValue),"Source cannot be null");
		checkArgument(nonNull(promotionResults),"Target cannot be null");

		final PromotionEngineResult promotionEngineResult = new PromotionEngineResult();
		if (promotionResults.size() == 1) //NOSONAR
		{
			getPromotionResultPopulator().populate(promotionResults.iterator().next(), promotionEngineResult);
			promotionEngineResult.setDiscountValue(discountValue);
		}
		else
		{
			LOGGER.warn("Cannot find an action corresponding to discount value {}", discountValue);
			promotionEngineResult.setCode("Unable to find corresponding action");
		}

		return promotionEngineResult;
	}

	/**
	 * Provides list of discount values that will be processed by the populator
	 * @param source
	 */
	protected abstract Collection<DiscountValue> getDiscountValues(final S source);

	/**
	 * Provides order that will be processed by the populator
	 * @param source
	 * @return order
	 */
	protected abstract AbstractOrderModel getOrder(final S source);

	protected RuleBasedPromotionActionDao getRuleBasedPromotionActionDao()
	{
		return ruleBasedPromotionActionDao;
	}

	@Required
	public void setRuleBasedPromotionActionDao(
			final RuleBasedPromotionActionDao ruleBasedPromotionActionDao)
	{
		this.ruleBasedPromotionActionDao = ruleBasedPromotionActionDao;
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

	protected ActionUtils getActionUtils()
	{
		return actionUtils;
	}

	@Required
	public void setActionUtils(final ActionUtils actionUtils)
	{
		this.actionUtils = actionUtils;
	}
}

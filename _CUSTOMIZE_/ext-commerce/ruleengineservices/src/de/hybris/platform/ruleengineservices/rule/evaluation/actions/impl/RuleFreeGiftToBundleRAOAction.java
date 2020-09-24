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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.RAOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;


/**
 * @deprecated since 18.11
 */
@Deprecated
public class RuleFreeGiftToBundleRAOAction extends AbstractRuleExecutableSupport
{
	private static final Logger LOG = LoggerFactory.getLogger(RuleFreeGiftToBundleRAOAction.class);

	private ProductService productService;
	private RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction;

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final String productCode = context.getParameter(RAOConstants.PRODUCT_PARAM, String.class);
		final Integer quantity = context.getParameter(RAOConstants.QUANTITY_PARAM, Integer.class);
		final List<EntriesSelectionStrategyRPD> strategies = (List<EntriesSelectionStrategyRPD>) context
				.getParameter(RAOConstants.SELECTION_STRATEGY_RPDS_PARAM);

		if (nonNull(strategies))
		{
			this.validateSelectionStrategy(strategies, context);
		}

		return performAction(context, productCode, strategies, quantity);
	}

	protected boolean performAction(final RuleActionContext context, final String productCode,
			final Collection<EntriesSelectionStrategyRPD> strategies, final Integer quantity)
	{
		boolean isPerformed = false;
		final Optional<ProductModel> product = findProduct(productCode, context);
		if (hasEnoughQuantity(context, strategies) && product.isPresent())
		{
			isPerformed = true;
			final CartRAO cartRao = context.getCartRao();
			int count = quantity;
			if (!isConsumptionEnabled())
			{
				// if order entry consumption is turned off, the container
				// based actions need to consume "all in one go" like in previous versions
				count = adjustStrategyQuantity(strategies, -1);
			}
			final FreeProductRAO freeProductRAO = getRuleEngineCalculationService().addFreeProductsToCart(cartRao, product.get(),
					count);
			setRAOMetaData(context, freeProductRAO);
			consumeOrderEntries(context, strategies, freeProductRAO);

			final RuleEngineResultRAO result = context.getRuleEngineResultRao();
			result.getActions().add(freeProductRAO);
			context.scheduleForUpdate(cartRao, result);
			context.insertFacts(freeProductRAO, freeProductRAO.getAddedOrderEntry());
			context.insertFacts(freeProductRAO.getConsumedEntries());
			getRuleOrderEntryPercentageDiscountRAOAction().processOrderEntry(context, freeProductRAO.getAddedOrderEntry(),
					valueOf(100d));
		}
		return isPerformed;
	}

	protected Optional<ProductModel> findProduct(final String productCode, final RuleActionContext context)
	{
		Optional<ProductModel> product = empty();
		try
		{
			product = ofNullable(getProductService().getProductForCode(productCode));
		}
		catch (final Exception e)
		{
			LOG.error("no product found for code{} in rule {}, cannot apply rule action.", productCode, getRuleCode(context), e);
		}
		return product;
	}

	protected ProductService getProductService()
	{
		return productService;
	}

	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	@Required
	public void setRuleOrderEntryPercentageDiscountRAOAction(
			final RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction)
	{
		this.ruleOrderEntryPercentageDiscountRAOAction = ruleOrderEntryPercentageDiscountRAOAction;
	}

	protected RuleOrderEntryPercentageDiscountRAOAction getRuleOrderEntryPercentageDiscountRAOAction()
	{
		return ruleOrderEntryPercentageDiscountRAOAction;
	}

}

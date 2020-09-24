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

import static java.math.BigDecimal.valueOf;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengineservices.enums.OrderEntrySelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRulePartnerProductAction;
import de.hybris.platform.ruleengineservices.util.RAOConstants;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class RuleFreeGiftRAOAction extends AbstractRulePartnerProductAction
{
	public static final String QUALIFYING_CONTAINERS_PARAM = "qualifying_containers";
	private static final Logger LOG = LoggerFactory.getLogger(RuleFreeGiftRAOAction.class);
	private ProductService productService;
	private RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction;

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final String productCode = context.getParameter(RAOConstants.PRODUCT_PARAM, String.class);
		final Integer quantity = context.getParameter(RAOConstants.QUANTITY_PARAM, Integer.class);
		final Map<String, Integer> qualifyingProductsContainers = (Map<String, Integer>) context
				.getParameter(QUALIFYING_CONTAINERS_PARAM);

		return performAction(context, qualifyingProductsContainers, productCode, quantity);
	}

	protected boolean performAction(final RuleActionContext context,
			final Map<String, Integer> qualifyingProductsContainers, final String productCode,
			final Integer quantity)
	{
		int count = quantity;

		final List<EntriesSelectionStrategyRPD> triggeringSelectionStrategyRPDs = createSelectionStrategyRPDsQualifyingProducts(
			context, OrderEntrySelectionStrategy.DEFAULT, qualifyingProductsContainers);

		final boolean isQualifyingContainersAvailable = MapUtils.isNotEmpty(qualifyingProductsContainers);
		if (isQualifyingContainersAvailable)
		{
			validateSelectionStrategy(triggeringSelectionStrategyRPDs, context);

			if (!hasEnoughQuantity(context, triggeringSelectionStrategyRPDs))
			{
				return false;
			}
			if (!isConsumptionEnabled())
			{
				// if order entry consumption is turned off, the container
				// based actions need to consume "all in one go" like in previous versions
				count = adjustStrategyQuantity(triggeringSelectionStrategyRPDs, -1);
			}
		}

		final CartRAO cartRao = context.getCartRao();
		final ProductModel product = findProduct(productCode, context);
		if (nonNull(product))
		{
			final FreeProductRAO freeProductRAO = getRuleEngineCalculationService()
					.addFreeProductsToCart(cartRao, product, count);

			final int availableFreeProducts = freeProductRAO.getAddedOrderEntry().getQuantity()
					- getRuleEngineCalculationService().getConsumedQuantityForOrderEntry(freeProductRAO.getAddedOrderEntry());

			if (availableFreeProducts >= count)
			{
				setRAOMetaData(context, freeProductRAO);

				final RuleEngineResultRAO result = context.getRuleEngineResultRao();

				result.getActions().add(freeProductRAO);
				context.scheduleForUpdate(cartRao, result);
				context.insertFacts(freeProductRAO, freeProductRAO.getAddedOrderEntry());

				final boolean isPerformed = getRuleOrderEntryPercentageDiscountRAOAction()
						.processOrderEntry(context, freeProductRAO.getAddedOrderEntry(), valueOf(100d));

				if (isPerformed && isConsumptionEnabled())
				{
					context.insertFacts(createProductConsumedRAO(freeProductRAO.getAddedOrderEntry()));

					if (isQualifyingContainersAvailable)
					{
						final AbstractRuleActionRAO discount = result.getActions().stream().filter(DiscountRAO.class::isInstance)
							.filter(d -> getMetaDataFromRule(context, RuleEngineConstants.RULEMETADATA_RULECODE)
								.equals(d.getFiredRuleCode())).findFirst().orElse(null);

						consumeOrderEntries(context, triggeringSelectionStrategyRPDs, discount);
						updateFactsWithOrderEntries(context, triggeringSelectionStrategyRPDs);
					}
				}
				return isPerformed;
			}

		}

		return false;
	}

	protected void updateFactsWithOrderEntries(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDS)
	{
		for (final EntriesSelectionStrategyRPD selectionStrategyRPDForTriggering : entriesSelectionStrategyRPDS)
		{
			for (final OrderEntryRAO orderEntryRao : selectionStrategyRPDForTriggering.getOrderEntries())
			{
				context.scheduleForUpdate(orderEntryRao);
			}
		}
	}

	protected ProductModel findProduct(final String productCode, final RuleActionContext context)
	{
		ProductModel product = null;
		try
		{
			product = getProductService().getProductForCode(productCode);
		}
		catch (final Exception e)
		{
			LOG.error("no product found for code{} in rule {}, cannot apply rule action.", productCode, getRuleCode(context), e);
		}
		return product;
	}

	protected ProductConsumedRAO createProductConsumedRAO(final OrderEntryRAO orderEntryRAO)
	{
		final ProductConsumedRAO productConsumedRAO = new ProductConsumedRAO();
		productConsumedRAO.setOrderEntry(orderEntryRAO);
		productConsumedRAO
				.setAvailableQuantity(getRuleEngineCalculationService().getProductAvailableQuantityInOrderEntry(orderEntryRAO));
		return productConsumedRAO;
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

	protected RuleOrderEntryPercentageDiscountRAOAction getRuleOrderEntryPercentageDiscountRAOAction()
	{
		return ruleOrderEntryPercentageDiscountRAOAction;
	}

	@Required
	public void setRuleOrderEntryPercentageDiscountRAOAction(
			final RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction)
	{
		this.ruleOrderEntryPercentageDiscountRAOAction = ruleOrderEntryPercentageDiscountRAOAction;
	}

}

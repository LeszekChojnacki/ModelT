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

import static de.hybris.platform.ruleengineservices.util.RAOConstants.EXCLUDED_PRODUCTS_PARAM;
import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.RAOConstants;

import java.math.BigDecimal;
import java.util.List;

/**
 * @deprecated since 18.11
 */
@Deprecated
public class RuleOrderPercentageDiscountExcludedProductsRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final List<String> excludedProducts = (List<String>) context.getParameter(EXCLUDED_PRODUCTS_PARAM);
		final BigDecimal threshold = (BigDecimal) context.getParameter(RAOConstants.SUB_TOTALS_THRESHOLD_PARAM);
		return extractAmountForCurrency(context, context.getParameter(VALUE_PARAM)).map(
				amount -> performAction(context, excludedProducts, threshold, amount)).orElse(false);
	}

	protected boolean performAction(final RuleActionContext context, final List<String> excludedProducts,
			final BigDecimal threshold, final BigDecimal amount)
	{
		boolean isPerformed = false;
		final CartRAO cartRao = context.getCartRao();

		final BigDecimal applicableTotal = calculateSubTotals(cartRao, excludedProducts);
		isPerformed = nonNull(threshold) && applicableTotal.doubleValue() <= threshold.doubleValue();

		if (isPerformed)
		{
			final BigDecimal absoluteAmount = amount.multiply(applicableTotal).divide(BigDecimal.valueOf(100.00));
			final DiscountRAO discount = getRuleEngineCalculationService().addOrderLevelDiscount(cartRao, true, absoluteAmount);

			final RuleEngineResultRAO result = context.getRuleEngineResultRao();
			result.getActions().add(discount);
			setRAOMetaData(context, discount);
			context.scheduleForUpdate(cartRao, result);
			context.insertFacts(discount);
		}
		return isPerformed;
	}

	protected BigDecimal calculateSubTotals(final CartRAO cartRao, final List<String> excludedProductCodes)
	{
		final List<ProductRAO> list = excludedProductCodes.stream().map(this::createProduct).collect(toList());
		return getRuleEngineCalculationService().calculateSubTotals(cartRao, list);
	}

	protected ProductRAO createProduct(final String code)
	{
		final ProductRAO product = new ProductRAO();
		product.setCode(code);
		return product;
	}

}

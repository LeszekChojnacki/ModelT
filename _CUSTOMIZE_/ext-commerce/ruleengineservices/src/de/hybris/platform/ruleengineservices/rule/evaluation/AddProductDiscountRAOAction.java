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
package de.hybris.platform.ruleengineservices.rule.evaluation;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;



/**
 * AddProductDiscountRAOAction adds a discount on a product.
 * @deprecated since 6.6
 */
@Deprecated
public interface AddProductDiscountRAOAction
{

	/**
	 * Adds a discount to the given {@code productRao} and returns the discount. The {@code absolute} flag determines
	 * whether the discount is absolute or percentage based. For absolute values, the given {@code currencyIsoCode} is
	 * used. The {@code amount} specifies the amount used, e.g. $20 or 10%. The {@code ruleContext} can be used to
	 * enhance the returned DiscountRAO.
	 *
	 * @param productRao
	 *           the productRao to apply the discount to
	 * @param absolute
	 *           the type of discount
	 * @param amount
	 *           the amount of the discount
	 * @param currencyIsoCode
	 *           the currency (only needed if {@code absolute} is set to {@code true}
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	DiscountRAO addProductDiscount(ProductRAO productRao, boolean absolute, String currencyIsoCode, BigDecimal amount,
			RuleEngineResultRAO result, Object ruleContext);

}

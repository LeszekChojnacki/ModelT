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
package de.hybris.order.calculation.strategies;

/**
 * Holder with lists of calculation strategies, which will be used to execute the order and line item specific
 * calculation.
 * 
 * @spring.bean calculationStrategies
 */
public class CalculationStrategies
{
	// REVIEW: We may need different rounding for taxes and the rest of calc.

	private RoundingStrategy roundingStrategy;
	private RoundingStrategy taxRondingStrategy;

	public void setRoundingStrategy(final RoundingStrategy roundingStrategy)
	{
		this.roundingStrategy = roundingStrategy;
	}

	public RoundingStrategy getRoundingStrategy()
	{
		return roundingStrategy;
	}

	public void setTaxRoundingStrategy(final RoundingStrategy taxRondingStrategy)
	{
		this.taxRondingStrategy = taxRondingStrategy;
	}

	public RoundingStrategy getTaxRondingStrategy()
	{
		return taxRondingStrategy;
	}

}
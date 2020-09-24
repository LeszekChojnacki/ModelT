/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.sourcing.strategy;

import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract sourcing strategy matcher. This will hold a reference to the sourcing strategy the mapper maps to.
 */
public abstract class AbstractSourcingStrategyMapper implements SourcingStrategyMapper
{
	private SourcingStrategy strategy;

	@Override
	public SourcingStrategy getStrategy()
	{
		return strategy;
	}

	@Required
	public void setStrategy(final SourcingStrategy strategy)
	{
		this.strategy = strategy;
	}

}

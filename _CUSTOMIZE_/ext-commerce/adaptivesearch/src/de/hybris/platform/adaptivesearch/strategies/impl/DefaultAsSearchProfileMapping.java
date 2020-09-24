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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.strategies.AsSearchConfigurationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileMapping;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileMapping}.
 */
public class DefaultAsSearchProfileMapping implements AsSearchProfileMapping
{
	private String type;
	private AsSearchProfileLoadStrategy loadStrategy;
	private AsSearchProfileCalculationStrategy calculationStrategy;
	private AsSearchConfigurationStrategy searchConfigurationStrategy;


	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		this.type = type;
	}

	@Override
	public AsSearchProfileLoadStrategy getLoadStrategy()
	{
		return loadStrategy;
	}

	@Required
	public void setLoadStrategy(final AsSearchProfileLoadStrategy loadStrategy)
	{
		this.loadStrategy = loadStrategy;
	}

	@Override
	public AsSearchProfileCalculationStrategy getCalculationStrategy()
	{
		return calculationStrategy;
	}

	@Required
	public void setCalculationStrategy(final AsSearchProfileCalculationStrategy calculationStrategy)
	{
		this.calculationStrategy = calculationStrategy;
	}

	@Override
	public AsSearchConfigurationStrategy getSearchConfigurationStrategy()
	{
		return searchConfigurationStrategy;
	}

	@Required
	public void setSearchConfigurationStrategy(final AsSearchConfigurationStrategy searchConfigurationStrategy)
	{
		this.searchConfigurationStrategy = searchConfigurationStrategy;
	}

}

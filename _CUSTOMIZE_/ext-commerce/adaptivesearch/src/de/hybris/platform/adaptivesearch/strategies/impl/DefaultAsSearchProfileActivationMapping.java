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

import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationStrategy;


/**
 * Default implementation of {@link AsSearchProfileActivationMapping}.
 */
public class DefaultAsSearchProfileActivationMapping implements AsSearchProfileActivationMapping
{
	private int priority;
	private AsSearchProfileActivationStrategy activationStrategy;

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(final int priority)
	{
		this.priority = priority;
	}

	@Override
	public AsSearchProfileActivationStrategy getActivationStrategy()
	{
		return activationStrategy;
	}

	public void setActivationStrategy(final AsSearchProfileActivationStrategy activationStrategy)
	{
		this.activationStrategy = activationStrategy;
	}
}

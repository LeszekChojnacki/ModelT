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

import de.hybris.platform.adaptivesearch.strategies.AsBoostRulesMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;

import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for implementations of {@link AsBoostRulesMergeStrategy}.
 */
public abstract class AbstractAsBoostRulesMergeStrategy implements AsBoostRulesMergeStrategy
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;

	public AsSearchProfileResultFactory getAsSearchProfileResultFactory()
	{
		return asSearchProfileResultFactory;
	}

	@Required
	public void setAsSearchProfileResultFactory(final AsSearchProfileResultFactory asSearchProfileResultFactory)
	{
		this.asSearchProfileResultFactory = asSearchProfileResultFactory;
	}
}
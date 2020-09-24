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



import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSearchProfile;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;

import java.io.Serializable;


/**
 * Base class for implementations of {@link AsSearchProfileCalculationStrategy}.
 *
 * @param <T>
 *           - the type of search profile data
 */
public abstract class AbstractAsSearchProfileCalculationStrategy<T extends AbstractAsSearchProfile>
		implements AsSearchProfileCalculationStrategy<T>
{
	@Override
	public Serializable getCacheKeyFragment(final AsSearchProfileContext context, final T searchProfile)
	{
		return null;
	}
}

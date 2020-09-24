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
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSimpleSearchProfile;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;

import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileCalculationStrategy} for simple search profiles.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericSearchProfileCalculationStrategy}
 */
@Deprecated
public class AsSimpleSearchProfileCalculationStrategy extends AbstractAsSearchProfileCalculationStrategy<AsSimpleSearchProfile>
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;

	@Override
	public AsSearchProfileResult calculate(final AsSearchProfileContext context, final AsSimpleSearchProfile searchProfile)
	{
		if (searchProfile.getSearchConfiguration() == null)
		{
			return asSearchProfileResultFactory.createResult();
		}

		return asSearchProfileResultFactory.createResultFromSearchConfiguration(searchProfile.getSearchConfiguration());
	}

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

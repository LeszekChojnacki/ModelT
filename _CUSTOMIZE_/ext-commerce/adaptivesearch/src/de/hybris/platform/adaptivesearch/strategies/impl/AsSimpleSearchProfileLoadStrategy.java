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
import de.hybris.platform.adaptivesearch.data.AsSimpleSearchProfile;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileLoadStrategy} for simple search profiles.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericSimpleSearchProfileLoadStrategy}
 */
@Deprecated
public class AsSimpleSearchProfileLoadStrategy
		extends AbstractAsSearchProfileLoadStrategy<AsSimpleSearchProfileModel, AsSimpleSearchProfile>
{
	private Converter<AsSimpleSearchProfileModel, AsSimpleSearchProfile> asSimpleSearchProfileConverter;

	@Override
	public AsSimpleSearchProfile load(final AsSearchProfileContext context, final AsSimpleSearchProfileModel searchProfile)
	{
		return asSimpleSearchProfileConverter.convert(searchProfile);
	}

	public Converter<AsSimpleSearchProfileModel, AsSimpleSearchProfile> getAsSimpleSearchProfileConverter()
	{
		return asSimpleSearchProfileConverter;
	}

	@Required
	public void setAsSimpleSearchProfileConverter(
			final Converter<AsSimpleSearchProfileModel, AsSimpleSearchProfile> asSimpleSearchProfileConverter)
	{
		this.asSimpleSearchProfileConverter = asSimpleSearchProfileConverter;
	}
}

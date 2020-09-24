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
import de.hybris.platform.adaptivesearch.data.AsCategoryAwareSearchProfile;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileLoadStrategy} for category aware search profiles.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericCategoryAwareSearchProfileLoadStrategy}
 */
@Deprecated
public class AsCategoryAwareSearchProfileLoadStrategy
		extends AbstractAsSearchProfileLoadStrategy<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchProfile>
{
	private Converter<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchProfile> asCategoryAwareSearchProfileConverter;

	@Override
	public AsCategoryAwareSearchProfile load(final AsSearchProfileContext context,
			final AsCategoryAwareSearchProfileModel searchProfile)
	{
		return asCategoryAwareSearchProfileConverter.convert(searchProfile);
	}

	public Converter<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchProfile> getAsCategoryAwareSearchProfileConverter()
	{
		return asCategoryAwareSearchProfileConverter;
	}

	@Required
	public void setAsCategoryAwareSearchProfileConverter(
			final Converter<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchProfile> asCategoryAwareSearchProfileConverter)
	{
		this.asCategoryAwareSearchProfileConverter = asCategoryAwareSearchProfileConverter;
	}
}

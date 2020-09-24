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
package de.hybris.platform.adaptivesearch.converters.populators;

import de.hybris.platform.adaptivesearch.data.AbstractAsSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsCatalogVersion;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AbstractAsSearchProfile} from {@link AbstractAsSearchProfileModel}.
 */
public class AsSearchProfilePopulator implements Populator<AbstractAsSearchProfileModel, AbstractAsSearchProfile>
{
	private Converter<CatalogVersionModel, AsCatalogVersion> asCatalogVersionConverter;

	@Override
	public void populate(final AbstractAsSearchProfileModel source, final AbstractAsSearchProfile target)
	{
		if (source.getCatalogVersion() != null)
		{
			target.setCatalogVersion(asCatalogVersionConverter.convert(source.getCatalogVersion()));
		}

		target.setCode(source.getCode());
		target.setIndexType(source.getIndexType());
	}

	public Converter<CatalogVersionModel, AsCatalogVersion> getAsCatalogVersionConverter()
	{
		return asCatalogVersionConverter;
	}

	@Required
	public void setAsCatalogVersionConverter(final Converter<CatalogVersionModel, AsCatalogVersion> asCatalogVersionConverter)
	{
		this.asCatalogVersionConverter = asCatalogVersionConverter;
	}
}

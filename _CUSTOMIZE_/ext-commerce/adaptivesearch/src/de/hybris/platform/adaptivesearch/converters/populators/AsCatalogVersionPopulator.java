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

import de.hybris.platform.adaptivesearch.data.AsCatalogVersion;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.converters.Populator;


/**
 * Populates {@link AsCatalogVersion} from {@link CatalogVersionModel}.
 */
public class AsCatalogVersionPopulator implements Populator<CatalogVersionModel, AsCatalogVersion>
{
	@Override
	public void populate(final CatalogVersionModel source, final AsCatalogVersion target)
	{
		target.setCatalogId(source.getCatalog().getId());
		target.setVersion(source.getVersion());
	}
}
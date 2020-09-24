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
package com.hybris.backoffice.solrsearch.resolvers;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;


public class CatalogLabelValueResolver extends ItemModelLabelValueResolver
{

	protected static final String CATALOG_VERSION_ATTRIBUTE_NAME = "catalogVersion";


	@Override
	protected ItemModel provideModel(final ItemModel model)
	{
		final CatalogVersionModel catalogVersion = getModelService().getAttributeValue(model, CATALOG_VERSION_ATTRIBUTE_NAME);
		if (catalogVersion != null)
		{
			return catalogVersion.getCatalog();
		}
		else
		{

			LOG.warn("{} couldn't resolve target CatalogVersionModel for {}", this.getClass().getSimpleName(), model);
			return null;
		}
	}
}

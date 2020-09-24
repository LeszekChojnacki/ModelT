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

import de.hybris.platform.core.model.ItemModel;

import java.util.Optional;


public class CatalogVersionPKValueResolver extends ItemModelPKValueResolver
{

	protected static final String CATALOG_VERSION_ATTRIBUTE = "catalogVersion";


	@Override
	protected Optional<ItemModel> getTargetModel(final ItemModel sourceModel)
	{
		final Optional<ItemModel> catalogVersion = Optional.ofNullable(getModelService().getAttributeValue(sourceModel, CATALOG_VERSION_ATTRIBUTE));
		if (!catalogVersion.isPresent())
		{
			LOG.warn("{} couldn't resolve target CatalogVersionModel for {}", this.getClass().getSimpleName(), sourceModel);
		}
		return catalogVersion;
	}

}

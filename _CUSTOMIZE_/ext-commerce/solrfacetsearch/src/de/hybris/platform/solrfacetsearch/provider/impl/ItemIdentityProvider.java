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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.provider.IdentityProvider;

import java.util.Iterator;
import java.util.Set;



/**
 * Resolves unique Item's identity. Respects multi-catalog versions and items with no catalogs
 *
 *
 *
 */
public class ItemIdentityProvider implements IdentityProvider<ItemModel>
{
	private static final long serialVersionUID = 1L;
	private CatalogTypeService catalogTypeService;
	private ModelService modelService;


	@Override
	public String getIdentifier(final IndexConfig indexConfig, final ItemModel item)
	{
		String identifier;
		if (item instanceof ProductModel)
		{
			final ProductModel product = (ProductModel) item;
			final CatalogVersionModel catalogVersion = product.getCatalogVersion();
			final String code = product.getCode();
			identifier = catalogVersion.getCatalog().getId() + "/" + catalogVersion.getVersion() + "/" + code;
		}
		else if (catalogTypeService.isCatalogVersionAwareModel(item))
		{
			identifier = prepareCatalogAwareItemIdentifier(item);
		}
		else
		{
			identifier = item.getPk().getLongValueAsString();
		}

		return identifier;
	}

	protected String prepareCatalogAwareItemIdentifier(final ItemModel item)
	{
		final Set<String> catalogVersionUniqueKeyAttributes = catalogTypeService
				.getCatalogVersionUniqueKeyAttribute(item.getItemtype());
		final String catalogVersionContainerAttribute = catalogTypeService.getCatalogVersionContainerAttribute(item.getItemtype());

		final CatalogVersionModel catalogVersion = modelService.getAttributeValue(item, catalogVersionContainerAttribute);

		final Iterator<String> catalogVersionUniqueKeyIterator = catalogVersionUniqueKeyAttributes.iterator();
		final StringBuilder itemKey = new StringBuilder("");
		while (catalogVersionUniqueKeyIterator.hasNext())
		{
			final String codePart = catalogVersionUniqueKeyIterator.next();
			itemKey.append("/").append(modelService.getAttributeValue(item, codePart).toString());
		}

		return catalogVersion.getCatalog().getId() + "/" + catalogVersion.getVersion() + itemKey.toString();
	}

	/**
	 * @return the catalogTypeService
	 */
	public CatalogTypeService getCatalogTypeService()
	{
		return catalogTypeService;
	}

	/**
	 * @param catalogTypeService
	 *           the catalogTypeService to set
	 */
	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}

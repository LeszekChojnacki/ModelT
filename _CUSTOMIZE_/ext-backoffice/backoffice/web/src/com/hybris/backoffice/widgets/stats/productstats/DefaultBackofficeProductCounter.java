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
package com.hybris.backoffice.widgets.stats.productstats;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;


public class DefaultBackofficeProductCounter implements BackofficeProductCounter
{
	private ProductService productService;
	private CatalogService catalogService;

	@Override
	public long countProducts()
	{
		return getAllProductsStream().count();
	}

	@Override
	public long countProducts(@Nonnull final ArticleApprovalStatus status)
	{
		return getAllProductsStream() //
				.filter(productModel -> productModel.getApprovalStatus().equals(status)) //
				.count();
	}

	protected Stream<ProductModel> getAllProductsStream()
	{
		return getCatalogService() //
				.getAllCatalogs() //
				.stream() //
				.map(CatalogModel::getCatalogVersions) //
				.flatMap(Collection::stream) //
				.map(catalogVersion -> getProductService().getAllProductsForCatalogVersion(catalogVersion)) //
				.flatMap(Collection::stream);
	}

	public ProductService getProductService()
	{
		return productService;
	}

	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	public CatalogService getCatalogService()
	{
		return catalogService;
	}

	@Required
	public void setCatalogService(final CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}
}

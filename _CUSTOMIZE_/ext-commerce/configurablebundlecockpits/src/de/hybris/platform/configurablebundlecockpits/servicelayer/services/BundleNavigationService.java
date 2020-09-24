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

package de.hybris.platform.configurablebundlecockpits.servicelayer.services;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.Collection;
import java.util.List;


/**
 * The Interface BundleNavigationService for managing bundle navigation nodes.
 * 
 * @spring.bean bundleNavigationService
 */
public interface BundleNavigationService
{
	/**
	 * Returns all root navigation nodes for given catalog version.
	 * 
	 * @param catalogVersion
	 *           catalog version
	 * @return root navigation nodes that have been found
	 */
	List<BundleTemplateModel> getRootNavigationNodes(CatalogVersionModel catalogVersion);

	/**
	 * Associates the list of {@link ProductModel}s to the {@link BundleTemplateModel}, i.e. adds it to the end of the
	 * products list.
	 * 
	 * @param bundleTemplateModel
	 *           the {@link BundleTemplateModel} to which the {@link ProductModel} is association with
	 * @param productModels
	 *           the list of {@link ProductModel}s which should be associated with the {@link BundleTemplateModel}
	 */
	void add(final BundleTemplateModel bundleTemplateModel, final Collection<ProductModel> productModels);

	/**
	 * Associates the {@link ProductModel} to the {@link BundleTemplateModel}, i.e. adds it to the end of the products
	 * list.
	 * 
	 * @param bundleTemplateModel
	 *           the {@link BundleTemplateModel} to which the {@link ProductModel} is association with
	 * @param productModel
	 *           the {@link ProductModel} which should be associated with the {@link BundleTemplateModel}
	 */
	void add(final BundleTemplateModel bundleTemplateModel, final ProductModel productModel);

	/**
	 * Removes the {@link ProductModel} association from the {@link BundleTemplateModel}.
	 * 
	 * @param bundleTemplateModel
	 *           the {@link BundleTemplateModel} to remove the {@link ProductModel} association from.
	 * @param productModel
	 *           the {@link ProductModel} which should be removed from the {@link BundleTemplateModel} association.
	 */
	void remove(final BundleTemplateModel bundleTemplateModel, final ProductModel productModel);

	/**
	 * Moves the order within a {@link BundleTemplateModel} products list. The source {@link ProductModel} is moved to
	 * the place of the target {@link ProductModel}.
	 * 
	 * @param bundleTemplateModel
	 *           the {@link BundleTemplateModel} in which the products list is modified
	 * @param sourceProductModel
	 *           the source position of the move, this product is removed from this position
	 * @param targetProductModel
	 *           the target position, so the source product is moved to the position of the target product
	 */
	void move(final BundleTemplateModel bundleTemplateModel, final ProductModel sourceProductModel,
			final ProductModel targetProductModel);

	/**
	 * Moves a {@link ProductModel} from a source {@link BundleTemplateModel} to a target {@link BundleTemplateModel}.
	 * The {@link ProductModel} is removed from the source {@link BundleTemplateModel} and added to the target
	 * {@link BundleTemplateModel} i.e. at the end of the list of associated products.
	 * 
	 * @param sourceBundleTemplateModel
	 *           the source {@link BundleTemplateModel} from which the {@link ProductModel} is removed
	 * @param sourceProductModel
	 *           the {@link ProductModel} which is moved
	 * @param targetBundleTemplateModel
	 *           the target {@link BundleTemplateModel} to which the {@link ProductModel} is moved
	 */
	void move(final BundleTemplateModel sourceBundleTemplateModel, final ProductModel sourceProductModel,
			final BundleTemplateModel targetBundleTemplateModel);
}

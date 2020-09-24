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

package de.hybris.platform.configurablebundlecockpits.servicelayer.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree.ParentBundleTemplateComparator;
import de.hybris.platform.configurablebundlecockpits.servicelayer.services.BundleNavigationService;
import de.hybris.platform.configurablebundleservices.bundle.BundleTemplateService;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;


/**
 * Default implementation of <code>BundleNavigationService</code> interface.
 */
public class DefaultBundleNavigationService extends AbstractBusinessService implements BundleNavigationService
{
	private BundleTemplateService bundleTemplateService;

	@Override
	public List<BundleTemplateModel> getRootNavigationNodes(final CatalogVersionModel catVer)
	{
		final List<BundleTemplateModel> rootBundles = getBundleTemplateService().getAllRootBundleTemplates(catVer);
		final List<BundleTemplateModel> bundleList = new ArrayList<BundleTemplateModel>(rootBundles);
		Collections.sort(bundleList, new ParentBundleTemplateComparator());
		return bundleList;
	}

	@Override
	public void add(final BundleTemplateModel bundleTemplateModel, final Collection<ProductModel> productModels)
	{
		validateParameterNotNullStandardMessage("bundleTemplateModel", bundleTemplateModel);
		validateParameterNotNullStandardMessage("productModels", productModels);

		final List<ProductModel> existingProducts = new ArrayList<ProductModel>(bundleTemplateModel.getProducts());

		for (final ProductModel productToAdd : productModels)
		{
			if (!existingProducts.contains(productToAdd))
			{
				existingProducts.add(productToAdd);
			}
		}

		bundleTemplateModel.setProducts(existingProducts);
		getModelService().save(bundleTemplateModel);
	}

	@Override
	public void add(final BundleTemplateModel bundleTemplateModel, final ProductModel productModel)
	{
		validateParameterNotNullStandardMessage("bundleTemplateModel", bundleTemplateModel);
		validateParameterNotNullStandardMessage("productModel", productModel);

		add(bundleTemplateModel, Arrays.asList(productModel));
	}

	@Override
	public void remove(final BundleTemplateModel bundleTemplateModel, final ProductModel productModel)
	{
		validateParameterNotNullStandardMessage("bundleTemplateModel", bundleTemplateModel);
		validateParameterNotNullStandardMessage("productModel", productModel);

		final List<ProductModel> items = new ArrayList<ProductModel>(bundleTemplateModel.getProducts());
		items.remove(productModel);
		bundleTemplateModel.setProducts(items);
		getModelService().save(bundleTemplateModel);
	}

	@Override
	public void move(final BundleTemplateModel bundleTemplateModel, final ProductModel sourceEntry, final ProductModel targetEntry)
	{
		validateParameterNotNullStandardMessage("bundleTemplateModel", bundleTemplateModel);
		validateParameterNotNullStandardMessage("sourceEntry", sourceEntry);
		validateParameterNotNullStandardMessage("targetEntry", targetEntry);

		final List<ProductModel> items = new ArrayList<ProductModel>(bundleTemplateModel.getProducts());
		final int sourceIndex = items.indexOf(sourceEntry);
		final int targetIndex = items.indexOf(targetEntry);

		if (sourceIndex == -1 || targetIndex == -1)
		{
			return;
		}

		if (sourceIndex < targetIndex)
		{
			items.add(targetIndex, sourceEntry);
			items.remove(sourceIndex);
		}
		else
		{
			items.add(targetIndex, sourceEntry);
			items.remove(items.lastIndexOf(sourceEntry));
		}
		bundleTemplateModel.setProducts(items);
		getModelService().save(bundleTemplateModel);
	}

	@Override
	public void move(final BundleTemplateModel sourceBundleTemplateModel, final ProductModel productModel,
			final BundleTemplateModel targetBundleTemplateModel)
	{
		validateParameterNotNullStandardMessage("sourceBundleTemplateModel", sourceBundleTemplateModel);
		validateParameterNotNullStandardMessage("productModel", productModel);
		validateParameterNotNullStandardMessage("targetBundleTemplateModel", targetBundleTemplateModel);

		add(targetBundleTemplateModel, productModel);
		remove(sourceBundleTemplateModel, productModel);
	}

	protected BundleTemplateService getBundleTemplateService()
	{
		return bundleTemplateService;
	}

	@Resource
	public void setBundleTemplateService(final BundleTemplateService bundleTemplateService)
	{
		this.bundleTemplateService = bundleTemplateService;
	}
}

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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengineservices.rao.CategoryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.util.ProductUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Converter implementation for {@link ProductModel} as source and {@link ProductRAO} as target type.
 */
public class ProductRaoPopulator implements Populator<ProductModel, ProductRAO>
{
	private Converter<CategoryModel, CategoryRAO> categoryConverter;
	private CategoryService categoryService;
	private ProductUtils productUtils;

	protected Collection<CategoryRAO> getAllCategories(final Collection<CategoryModel> categories)
	{
		if (CollectionUtils.isNotEmpty(categories))
		{
			final Set<CategoryModel> allCategories = new HashSet<>(categories);
			for (final CategoryModel category : categories)
			{
				allCategories.addAll(getCategoryService().getAllSupercategoriesForCategory(category));
			}
			return Converters.convertAll(allCategories, getCategoryConverter());
		}
		return Collections.emptySet();
	}

	@Override
	public void populate(final ProductModel source, final ProductRAO target)
	{
		target.setCode(source.getCode());
		final HashSet<CategoryRAO> categories = new HashSet<>(getAllCategories(source.getSupercategories()));
		final Set<ProductModel> baseProducts = getProductUtils().getAllBaseProducts(source);
		baseProducts.stream().forEach(bp -> categories.addAll(getAllCategories(bp.getSupercategories())));
		target.setCategories(categories);
		target.setBaseProductCodes(baseProducts.stream().map(ProductModel::getCode).collect(Collectors.toSet()));
	}

	protected Converter<CategoryModel, CategoryRAO> getCategoryConverter()
	{
		return categoryConverter;
	}

	@Required
	public void setCategoryConverter(final Converter<CategoryModel, CategoryRAO> categoryConverter)
	{
		this.categoryConverter = categoryConverter;
	}

	protected CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}

	protected ProductUtils getProductUtils()
	{
		return productUtils;
	}

	@Required
	public void setProductUtils(final ProductUtils productUtils)
	{
		this.productUtils = productUtils;
	}
}

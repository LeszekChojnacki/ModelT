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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Joiner;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default excel translator for supercategories of product.
 */
public class ExcelProductSupercategoriesTypeTranslator extends AbstractCatalogVersionAwareTranslator<Collection<CategoryModel>>
{

	private static final String PATTERN = "%s:%s";
	public static final String CATEGORY_TOKEN = "category";
	private TypeService typeService;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor instanceof RelationDescriptorModel
				&& ProductModel._CATEGORYPRODUCTRELATION
						.equals((((RelationDescriptorModel) attributeDescriptor).getRelationType()).getCode())
				&& typeService.isAssignableFrom(ProductModel._TYPECODE, attributeDescriptor.getEnclosingType().getCode());
	}

	@Override
	public Optional<Object> exportData(final Collection<CategoryModel> objectToExport)
	{
		return CollectionUtils.emptyIfNull(objectToExport).stream()//
				.map(this::exportCategory)//
				.reduce(Joiner.on(',')::join)//
				.map(Object.class::cast);
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final List<String> categories = new ArrayList<>();
		for (final Map<String, String> params : importParameters.getMultiValueParameters())
		{
			final String catalogVersion = catalogVersionData(params);
			if (StringUtils.isNotBlank(catalogVersion) && StringUtils.isNotBlank(params.get(CATEGORY_TOKEN)))
			{
				final String category = String.format(PATTERN, params.get(CATEGORY_TOKEN), catalogVersion);
				categories.add(category);
			}
		}
		final ImpexHeaderValue categoryHeader = new ImpexHeaderValue.Builder(String.format("%s(%s, %s)",
				ProductModel.SUPERCATEGORIES, CategoryModel.CODE, catalogVersionHeader(CategoryModel._TYPECODE)))
						.withQualifier(attributeDescriptor.getQualifier()).build();
		return new ImpexValue(String.join(",", categories), categoryHeader);
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return String.format(PATTERN, CATEGORY_TOKEN, referenceCatalogVersionFormat());
	}

	protected String exportCategory(final CategoryModel category)
	{
		final CatalogVersionModel catalogVersion = category.getCatalogVersion();
		return String.format(PATTERN, category.getCode(), exportCatalogVersionData(catalogVersion));
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}

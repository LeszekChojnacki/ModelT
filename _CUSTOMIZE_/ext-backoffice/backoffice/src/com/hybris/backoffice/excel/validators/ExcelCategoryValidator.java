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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.ExcelProductSupercategoriesTypeTranslator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for product's supercategories. The validator checks whether import parameters contains
 * "category" key, whether category exists and the category belongs to given catalog and version. This validator doesn't
 * check whether catalog and version exist. To do this, add {@link ExcelCatalogVersionValidator} to a list of validators.
 */
public class ExcelCategoryValidator implements ExcelValidator
{

	protected static final String CATEGORY_PATTERN = "%s:%s:%s";
	protected static final String VALIDATION_CATEGORY_DOESNT_MATCH = "excel.import.validation.category.doesntmatch";
	protected static final String VALIDATION_CATEGORY_EMPTY = "excel.import.validation.category.empty";
	private static final Logger LOG = LoggerFactory.getLogger(ExcelCategoryValidator.class);

	private CatalogVersionService catalogVersionService;
	private CategoryService categoryService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> ctx)
	{
		final List<ValidationMessage> validationMessages = new ArrayList<>();

		for (final Map<String, String> parameters : importParameters.getMultiValueParameters())
		{
			validateSingleValue(ctx, parameters).ifPresent(validationMessages::add);
		}
		return new ExcelValidationResult(validationMessages);
	}

	protected Optional<ValidationMessage> validateSingleValue(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		if (parameters.get(ExcelProductSupercategoriesTypeTranslator.CATEGORY_TOKEN) == null)
		{
			return Optional.of(new ValidationMessage(VALIDATION_CATEGORY_EMPTY));
		}
		else
		{
			final Optional<CategoryModel> categoryFromCache = findValueInCache(ctx, parameters);
			if (!categoryFromCache.isPresent() && parameters.get(CatalogVersionModel.CATALOG) != null
					&& parameters.get(CatalogVersionModel.VERSION) != null)
			{
				return Optional.of(new ValidationMessage(VALIDATION_CATEGORY_DOESNT_MATCH,
						parameters.get(ExcelProductSupercategoriesTypeTranslator.CATEGORY_TOKEN),
						parameters.get(CatalogVersionModel.VERSION), parameters.get(CatalogVersionModel.CATALOG)));
			}
		}
		return Optional.empty();
	}

	protected Optional<CategoryModel> findValueInCache(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		final String formattedCategory = getFormattedCategory(parameters);

		if (ctx.containsKey(formattedCategory))
		{
			return (Optional) ctx.get(formattedCategory);
		}
		try
		{
			final CatalogVersionModel catalogVersion = getCatalogVersionService()
					.getCatalogVersion(parameters.get(CatalogVersionModel.CATALOG), parameters.get(CatalogVersionModel.VERSION));
			final CategoryModel categoryModel = getCategoryService().getCategoryForCode(catalogVersion,
					parameters.get(ExcelProductSupercategoriesTypeTranslator.CATEGORY_TOKEN));
			final Optional<CategoryModel> result = Optional.ofNullable(categoryModel);
			ctx.put(formattedCategory, result);
			return result;
		}
		catch (final UnknownIdentifierException | IllegalArgumentException ex)
		{
			LOG.debug("Value not found in cache", ex);
			ctx.put(formattedCategory, Optional.empty());
			return Optional.empty();
		}
	}

	protected String getFormattedCategory(final Map<String, String> parameters)
	{
		return String.format(CATEGORY_PATTERN, parameters.get(CatalogVersionModel.CATALOG),
				parameters.get(CatalogVersionModel.VERSION),
				parameters.get(ExcelProductSupercategoriesTypeTranslator.CATEGORY_TOKEN));
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank() && attributeDescriptor instanceof RelationDescriptorModel
				&& ProductModel._CATEGORYPRODUCTRELATION
						.equals((((RelationDescriptorModel) attributeDescriptor).getRelationType()).getCode());
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}
}

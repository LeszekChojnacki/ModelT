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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default validator for base product. The validator checks whether base product code is not empty and base product for
 * given catalog version exist.
 */
public class ExcelBaseProductValidator implements ExcelValidator
{

	protected static final String BASE_PRODUCT_PATTERN = "%s:%s:%s";
	protected static final String VALIDATION_BASE_PRODUCT_DOESNT_MATCH = "excel.import.validation.baseproduct.doesntmatch";
	protected static final String VALIDATION_BASE_PRODUCT_EMPTY = "excel.import.validation.baseproduct.empty";
	protected static final String VALIDATION_BASE_PRODUCT_DOESNT_EXIST = "excel.import.validation.baseproduct.doesntexists";
	private static final Logger LOG = LoggerFactory.getLogger(ExcelBaseProductValidator.class);

	private CatalogVersionService catalogVersionService;
	private ProductService productService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> ctx)
	{
		final List<ValidationMessage> validationMessages = new ArrayList<>();

		final Map<String, String> parameters = importParameters.getSingleValueParameters();

		if (parameters.get(VariantProductModel.BASEPRODUCT) == null)
		{
			validationMessages.add(new ValidationMessage(VALIDATION_BASE_PRODUCT_EMPTY));
		}
		else
		{
			final Optional<ProductModel> product = findValueInCache(parameters, ctx);
			if (!product.isPresent())
			{
				if (parameters.get(CatalogVersionModel.CATALOG) != null && parameters.get(CatalogVersionModel.VERSION) != null)
				{
					validationMessages.add(
							new ValidationMessage(VALIDATION_BASE_PRODUCT_DOESNT_MATCH, parameters.get(VariantProductModel.BASEPRODUCT),
									parameters.get(CatalogVersionModel.VERSION), parameters.get(CatalogVersionModel.CATALOG)));
				}
				else
				{
					validationMessages.add((new ValidationMessage(VALIDATION_BASE_PRODUCT_DOESNT_EXIST,
							parameters.get(VariantProductModel.BASEPRODUCT))));
				}
			}
		}
		return new ExcelValidationResult(validationMessages);
	}

	protected Optional<ProductModel> findValueInCache(final Map<String, String> parameters, final Map<String, Object> ctx)
	{
		final String formattedKey = getFormattedBaseProduct(parameters);
		if (!ctx.containsKey(formattedKey))
		{
			try
			{
				final CatalogVersionModel catalogVersion = getCatalogVersionService()
						.getCatalogVersion(parameters.get(CatalogVersionModel.CATALOG), parameters.get(CatalogVersionModel.VERSION));
				final ProductModel foundProduct = productService.getProductForCode(catalogVersion,
						parameters.get(VariantProductModel.BASEPRODUCT));
				final Optional<ProductModel> result = Optional.ofNullable(foundProduct);
				ctx.put(formattedKey, result);
			}
			catch (final UnknownIdentifierException | IllegalArgumentException ex)
			{
				ctx.put(formattedKey, Optional.empty());
				LOG.debug("Cannot find value in cache", ex);
			}

		}
		return (Optional<ProductModel>) ctx.get(formattedKey);
	}

	protected String getFormattedBaseProduct(final Map<String, String> parameters)
	{
		return String.format(BASE_PRODUCT_PATTERN, parameters.get(CatalogVersionModel.CATALOG),
				parameters.get(CatalogVersionModel.VERSION), parameters.get(VariantProductModel.BASEPRODUCT));
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank() && attributeDescriptor instanceof RelationDescriptorModel
				&& attributeDescriptor.getAttributeType() instanceof ComposedTypeModel && VariantProductModel._PRODUCT2VARIANTRELATION
						.equals(((RelationDescriptorModel) attributeDescriptor).getRelationType().getCode());
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

	public ProductService getProductService()
	{
		return productService;
	}

	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}
}

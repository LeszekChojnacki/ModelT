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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * DefaultProductResolutionStrategy resolves the given {@link RuleParameterData#getValue()} into a product code, looks
 * up the product via and invokes {@link #getProductRepresentation(ProductModel)} to display the product.
 */
public class DefaultProductResolutionStrategy implements PromotionMessageParameterResolutionStrategy
{

	private ProductService productService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultProductResolutionStrategy.class);

	@Override
	public String getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale)
	{
		final String productCode = data.getValue();
		final ProductModel product = getProduct(productCode);
		if (product != null)
		{
			return getProductRepresentation(product);
		}
		// if not resolved, just return the input
		return productCode;
	}

	/**
	 * retrieves a Product based on the given {@code productCode}. This method uses the
	 * {@link ProductService#getProductForCode(String)} method.
	 *
	 * @param productCode
	 *           the product's code
	 * @return the product or null if none (or multiple) are found.
	 */
	protected ProductModel getProduct(final String productCode)
	{
		if (productCode != null)
		{
			try
			{
				return getProductService().getProductForCode(productCode);
			}
			catch (UnknownIdentifierException | AmbiguousIdentifierException e)
			{
				LOG.error("cannot resolve product code: " + productCode + " to a product.", e);
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * returns the {@link ProductModel#getName()} for the given {@code product}.
	 *
	 * @param product
	 *           the product
	 * @return the name of the product
	 */
	protected String getProductRepresentation(final ProductModel product)
	{
		return product.getName();
	}

	protected ProductService getProductService()
	{
		return productService;
	}

	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}
}

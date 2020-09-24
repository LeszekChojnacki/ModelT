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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;


/**
 * The default implementation for the product {@code RAO} which allows customization of how and which product-based
 * {@code RAO}s will be created based on the defined options. Specializations of this class can overwrite the
 * {@link #expandRAOs(ProductModel, Collection)} method and define its own {@code option} strings.
 */
public class DefaultProductRAOProvider extends AbstractExpandedRAOProvider<ProductModel, ProductRAO>
{
	public static final String INCLUDE_PRODUCT = "INCLUDE_PRODUCT";
	public static final String EXPAND_CATEGORIES = "EXPAND_CATEGORIES";

	private Converter<ProductModel, ProductRAO> productRaoConverter;

	public DefaultProductRAOProvider()
	{
		validOptions = asList(INCLUDE_PRODUCT, EXPAND_CATEGORIES);
		defaultOptions = singleton(INCLUDE_PRODUCT);
		minOptions = singleton(INCLUDE_PRODUCT);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		final Map<String, BiConsumer<Set<Object>, ProductRAO>> myConsumerMap = getConsumerMap();
		myConsumerMap.putAll(ImmutableMap.of(INCLUDE_PRODUCT, Set::add, EXPAND_CATEGORIES,
				(f, r) -> f.addAll(r.getCategories())));
	}

	protected Set<Object> expandRAOs(final ProductModel modelFact, final Collection<String> options)
	{
		return expandRAO(createRAO(modelFact), options);
	}

	@Override
	protected ProductRAO createRAO(final ProductModel modelFact)
	{
		return getProductRaoConverter().convert(modelFact);
	}

	public void setDefaultOptions(final Collection<String> defaultOptions)
	{
		this.defaultOptions = defaultOptions;
	}

	protected Converter<ProductModel, ProductRAO> getProductRaoConverter()
	{
		return productRaoConverter;
	}

	@Required
	public void setProductRaoConverter(final Converter<ProductModel, ProductRAO> productRaoConverter)
	{
		this.productRaoConverter = productRaoConverter;
	}

	void setValidOptions(final Collection<String> validOptions)
	{
		this.validOptions = validOptions;
	}

	public void setMinOptions(final Collection<String> minOptions)
	{
		this.minOptions = minOptions;
	}
}

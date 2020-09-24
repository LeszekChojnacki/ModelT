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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;


public class ProductRuleParameterValueMapper implements RuleParameterValueMapper<ProductModel>
{
	private ProductDao productDao;

	private CatalogVersionService catalogVersionService;

	private RuleParameterValueMapper<CatalogModel> catalogRuleParameterValueMapper;

	private String delimiter;

	private String catalogVersionName;

	@Override
	public String toString(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNull(product, "Object cannot be null");
		return String.join(getDelimiter(), product.getCode(),
				getCatalogRuleParameterValueMapper().toString(product.getCatalogVersion().getCatalog()));
	}

	@Override
	public ProductModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");

		String productCode = value;
		final List<ProductModel> products;

		if (value.contains(getDelimiter()))
		{
			final String catalogIdentifier = substringAfter(value, getDelimiter());
			final CatalogModel catalog = getCatalogRuleParameterValueMapper().fromString(catalogIdentifier);
			final CatalogVersionModel catalogVersion =
					getCatalogVersionService().getCatalogVersion(catalog.getId(), getCatalogVersionName());

			productCode = substringBefore(value, getDelimiter());

			products = getProductDao().findProductsByCode(catalogVersion, productCode);
		}
		else
		{
			products = getProductDao().findProductsByCode(value);
		}

		if (CollectionUtils.isEmpty(products))
		{
			throw new RuleParameterValueMapperException("Cannot find product with the code: " + productCode);
		}

		return products.iterator().next();
	}

	protected ProductDao getProductDao()
	{
		return productDao;
	}

	@Required
	public void setProductDao(final ProductDao productDao)
	{
		this.productDao = productDao;
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected RuleParameterValueMapper<CatalogModel> getCatalogRuleParameterValueMapper()
	{
		return catalogRuleParameterValueMapper;
	}

	@Required
	public void setCatalogRuleParameterValueMapper(
			final RuleParameterValueMapper<CatalogModel> catalogRuleParameterValueMapper)
	{
		this.catalogRuleParameterValueMapper = catalogRuleParameterValueMapper;
	}

	protected String getDelimiter()
	{
		return delimiter;
	}

	@Required
	public void setDelimiter(final String delimiter)
	{
		this.delimiter = delimiter;
	}

	protected String getCatalogVersionName()
	{
		return catalogVersionName;
	}

	@Required
	public void setCatalogVersionName(final String catalogVersionName)
	{
		this.catalogVersionName = catalogVersionName;
	}
}

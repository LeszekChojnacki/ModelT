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
package de.hybris.platform.externaltax.impl;

import de.hybris.platform.basecommerce.model.externaltax.ProductTaxCodeModel;
import de.hybris.platform.externaltax.dao.ProductTaxCodeDao;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;


public class DefaultProductTaxCodeDao extends AbstractItemDao implements ProductTaxCodeDao
{
	private static final Logger LOG = Logger.getLogger(DefaultProductTaxCodeDao.class.getName());

	private static final List<Class> SINGLE_CODE_SIG = (List) Arrays.asList(String.class);

	private static final List<Class> BULK_CODE_SIG = (List) Arrays.asList(String.class, String.class);

	@Override
	public String getTaxCodeForProductAndAreaDirect(final String productCode, final String taxArea)
	{
		final FlexibleSearchQuery query = createUniqueLookupQuery(productCode, taxArea, "{" + ProductTaxCodeModel.TAXCODE + "}");
		query.setResultClassList(SINGLE_CODE_SIG);
		final SearchResult<String> result = search(query);
		if (result.getCount() == 0)
		{
			return null;
		}
		else
		{
			if (result.getCount() > 1)
			{
				LOG.warn("Multiple tax codes found for product " + productCode + " and area " + taxArea + " : " + result.getResult()
						+ "! (Choosing first one)");
			}
			return result.getResult().get(0);
		}
	}

	protected FlexibleSearchQuery createUniqueLookupQuery(final String productCode, final String taxArea,
			final String selectClause)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT " + selectClause + " " + //
						"FROM {" + ProductTaxCodeModel._TYPECODE + "} " + // NOSONAR
						"WHERE {" + ProductTaxCodeModel.PRODUCTCODE + "}=?product " + // NOSONAR
						"AND {" + ProductTaxCodeModel.TAXAREA + "}=?area" //
		);
		query.addQueryParameter("product", productCode);
		query.addQueryParameter("area", taxArea);
		return query;
	}

	@Override
	public List<List<String>> getTaxCodesForProductsAndAreaDirect(final Collection<String> productCodes, final String taxArea)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {" + ProductTaxCodeModel.PRODUCTCODE + "}, {" + ProductTaxCodeModel.TAXCODE + "} " + //
						"FROM {" + ProductTaxCodeModel._TYPECODE + "} " + //
						"WHERE {" + ProductTaxCodeModel.PRODUCTCODE + "} IN (?products) " + //
						"AND {" + ProductTaxCodeModel.TAXAREA + "}=?area" //
		);
		query.addQueryParameter("products", productCodes);
		query.addQueryParameter("area", taxArea);
		query.setResultClassList(BULK_CODE_SIG);

		return (List) search(query).getResult();
	}

	@Override
	public ProductTaxCodeModel getTaxCodeForProductAndArea(final String productCode, final String taxArea)
	{
		final SearchResult<ProductTaxCodeModel> result = search(
				createUniqueLookupQuery(productCode, taxArea, "{" + ProductTaxCodeModel.PK + "}"));
		if (result.getCount() == 0)
		{
			return null;
		}
		else
		{
			if (result.getCount() > 1)
			{
				LOG.warn("Multiple tax codes found for product " + productCode + " and area " + taxArea + " : " + result.getResult()
						+ "! (Choosing first one)");
			}
			return result.getResult().get(0);
		}
	}

	@Override
	public List<ProductTaxCodeModel> getTaxCodesForProduct(final String productCode)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(//
				"SELECT {" + ProductTaxCodeModel.PK + "} " + //
						"FROM {" + ProductTaxCodeModel._TYPECODE + "} " + //
						"WHERE {" + ProductTaxCodeModel.PRODUCTCODE + "}=?product " //
		);
		query.addQueryParameter("product", productCode);

		final SearchResult<ProductTaxCodeModel> result = search(query);
		return result.getResult();
	}
}

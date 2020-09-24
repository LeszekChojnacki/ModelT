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
package de.hybris.platform.solrfacetsearch.search.product;

import de.hybris.platform.solrfacetsearch.search.AbstractSolrConverter;
import de.hybris.platform.solrfacetsearch.search.impl.SolrResult;

import java.io.Serializable;
import java.util.Collection;


/**
 * Converts SolrResult to ProductData.
 *
 * @see de.hybris.platform.solrfacetsearch.search.impl.SolrResult
 * @see de.hybris.platform.solrfacetsearch.search.product.SolrProductData
 *
 *
 */
public class DefaultSolrProductConverter extends AbstractSolrConverter<SolrProductData> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String CATALOG = "catalog";
	private static final String PK = "pk";
	private static final String DESCRIPTION = "description";
	private static final String NAME = "name";
	private static final String CATALOG_VERSION = "catalogVersion";
	private static final String CATEGORIES = "category";
	private static final String PRICE = "priceValue";
	private static final String CODE = "code";
	private static final String EAN = "ean";



	@Override
	public SolrProductData convert(final SolrResult solrResult, final SolrProductData target)
	{
		target.setCatalog(this.<String> getValue(solrResult, CATALOG));
		target.setPk(this.<Long> getValue(solrResult, PK));
		target.setName(this.<String> getValue(solrResult, NAME));
		target.setDescription(this.<String> getValue(solrResult, DESCRIPTION));
		target.setCatalogVersion(this.<String> getValue(solrResult, CATALOG_VERSION));
		target.setCategories((Collection<String>) this.getValue(solrResult, CATEGORIES));
		target.setPrice(this.<Double> getValue(solrResult, PRICE));
		target.setCode(this.<String> getValue(solrResult, CODE));
		target.setEan(this.<String> getValue(solrResult, EAN));

		return target;
	}


	@Override
	protected SolrProductData createDataObject()
	{
		return new SolrProductData();
	}
}

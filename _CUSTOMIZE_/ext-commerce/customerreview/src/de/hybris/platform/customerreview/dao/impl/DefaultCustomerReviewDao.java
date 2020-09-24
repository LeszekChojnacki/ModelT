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
package de.hybris.platform.customerreview.dao.impl;


import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.dao.CustomerReviewDao;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Collections;
import java.util.List;


/**
 * Default implementation of {@link CustomerReviewDao}
 */
public class DefaultCustomerReviewDao extends AbstractItemDao implements CustomerReviewDao
{

	private static final String FIND_REVIEWS_BY_USER = "SELECT {" + Item.PK + "} FROM {" + CustomerReviewModel._TYPECODE + "}"
			+ " WHERE {" + CustomerReviewModel.USER + "}=?user ORDER BY {" + CustomerReviewModel.CREATIONTIME + "} DESC";

	@Override
	public List<CustomerReviewModel> getReviewsForProduct(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("product", product);
		final String query = "SELECT {" + Item.PK + "} FROM {" + CustomerReviewModel._TYPECODE + "} WHERE {"
				+ CustomerReviewModel.PRODUCT + "}=?product ORDER BY {" + CustomerReviewModel.CREATIONTIME + "} DESC";
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);
		fsQuery.addQueryParameter("product", product);
		fsQuery.setResultClassList(Collections.singletonList(CustomerReviewModel.class));

		final SearchResult<CustomerReviewModel> searchResult = getFlexibleSearchService().search(fsQuery);
		return searchResult.getResult();
	}

	@Override
	public List<CustomerReviewModel> getReviewsForProductAndLanguage(final ProductModel product, final LanguageModel language)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("product", product);
		ServicesUtil.validateParameterNotNullStandardMessage("language", language);
		final String query = "SELECT {" + Item.PK + "} FROM {" + CustomerReviewModel._TYPECODE + "} WHERE {"
				+ CustomerReviewModel.PRODUCT + "}=?product AND {" + CustomerReviewModel.LANGUAGE + "}=?language ORDER BY {"
				+ CustomerReviewModel.CREATIONTIME + "} DESC";
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);
		fsQuery.addQueryParameter("product", product);
		fsQuery.addQueryParameter("language", language);
		fsQuery.setResultClassList(Collections.singletonList(CustomerReviewModel.class));

		final SearchResult<CustomerReviewModel> searchResult = getFlexibleSearchService().search(fsQuery);
		return searchResult.getResult();
	}

	@Override
	public List<CustomerReviewModel> getReviewsForCustomer(final UserModel customer)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("customer", customer);
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(FIND_REVIEWS_BY_USER);
		fsQuery.addQueryParameter("user", customer);
		fsQuery.setResultClassList(Collections.singletonList(CustomerReviewModel.class));

		final SearchResult<CustomerReviewModel> searchResult = getFlexibleSearchService().search(fsQuery);
		return searchResult.getResult();
	}

	@Override
	public Integer getNumberOfReviews(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("product", product);
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery("SELECT count(*) FROM {" + CustomerReviewModel._TYPECODE
				+ "} WHERE {" + CustomerReviewModel.PRODUCT
				+ "} = ?product");
		fsQuery.addQueryParameter("product", product);
		fsQuery.setResultClassList(Collections.singletonList(Integer.class));

		final SearchResult<Integer> searchResult = getFlexibleSearchService().search(fsQuery);
		return searchResult.getResult().iterator().next();
	}

	@Override
	public Double getAverageRating(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("product", product);
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery("SELECT avg({" + CustomerReviewModel.RATING + "}) FROM {"
				+ CustomerReviewModel._TYPECODE + "} WHERE {"
				+ CustomerReviewModel.PRODUCT + "} = ?product");
		fsQuery.addQueryParameter("product", product);
		fsQuery.setResultClassList(Collections.singletonList(Double.class));

		final SearchResult<Double> searchResult = getFlexibleSearchService().search(fsQuery);
		return searchResult.getResult().iterator().next();
	}
}

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
package de.hybris.platform.customerreview.impl;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.CustomerReviewService;
import de.hybris.platform.customerreview.dao.CustomerReviewDao;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for CustomerReviewService.
 */
public class DefaultCustomerReviewService implements CustomerReviewService
{
	private CustomerReviewDao customerReviewDao;
	private ModelService modelService;
	private static final String PRODUCT = "product";
	private static final String USERMODEL = "userModel";
	private static final String LANGUAGE = "language";

	@Override
	public CustomerReviewModel createCustomerReview(final Double rating, final String headline, final String comment,
			final UserModel user, final ProductModel product)
	{
		final CustomerReviewModel review = getModelService().create(CustomerReviewModel.class);
		review.setUser(user);
		review.setProduct(product);
		review.setRating(rating);
		review.setHeadline(headline);
		review.setComment(comment);
		getModelService().save(review);
		return review;
	}

	/**
	 * @deprecated Since 6.4 not required
	 */
	@Override
	@Deprecated
	public void updateCustomerReview(final CustomerReviewModel model, final UserModel user, final ProductModel product)
	{
		model.setProduct(product);
		model.setUser(user);
		getModelService().save(model);
	}

	/**
	 * @deprecated Since 6.4 use instead {@link CustomerReviewService#getReviewsForProduct(ProductModel)}
	 */
	@Override
	@Deprecated
	public List<CustomerReviewModel> getAllReviews(final ProductModel product)
	{
		return getReviewsForProduct(product);
	}

	@Override
	public Double getAverageRating(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PRODUCT, product);
		return getCustomerReviewDao().getAverageRating(product);
	}

	@Override
	public Integer getNumberOfReviews(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PRODUCT, product);
		return getCustomerReviewDao().getNumberOfReviews(product);
	}

	@Override
	public List<CustomerReviewModel> getReviewsForProduct(final ProductModel product)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PRODUCT, product);
		return getCustomerReviewDao().getReviewsForProduct(product);
	}

	@Override
	public List<CustomerReviewModel> getReviewsForCustomer(final UserModel userModel)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(USERMODEL, userModel);
		return getCustomerReviewDao().getReviewsForCustomer(userModel);
	}

	@Override
	public List<CustomerReviewModel> getReviewsForProductAndLanguage(final ProductModel product, final LanguageModel language)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PRODUCT, product);
		ServicesUtil.validateParameterNotNullStandardMessage(LANGUAGE, language);
		return getCustomerReviewDao().getReviewsForProductAndLanguage(product, language);
	}

	protected CustomerReviewDao getCustomerReviewDao()
	{
		return customerReviewDao;
	}

	@Required
	public void setCustomerReviewDao(final CustomerReviewDao customerReviewDao)
	{
		this.customerReviewDao = customerReviewDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}

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
package de.hybris.platform.customerreview.dao;


import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;

import java.util.List;


/**
 * Data Access Object for {@link CustomerReviewModel}
 */
public interface CustomerReviewDao
{
	/**
	 * Get the reviews for the specified product.
	 * Gets all the reviews in any language.
	 * The reviews are ordered by creation date with the most recent first.
	 *
	 * @param product the product
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForProduct(ProductModel product);

	/**
	 * Get the reviews for the specified product in the specified language.
	 * The reviews are ordered by creation date with the most recent first.
	 *
	 * @param product the product
	 * @param language the language
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForProductAndLanguage(ProductModel product, LanguageModel language);

	/**
	 * Get the reviews for the specified customer.
	 * Gets all the reviews in any language.
	 * The reviews are ordered by created date with the most recent first.
	 *
	 * @param userModel the customer
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForCustomer(UserModel userModel);

	/**
	 * Get number of all reviews in any language for the specified product.
	 *
	 * @param product the product
	 * @return the number of reviews
	 */
	Integer getNumberOfReviews(ProductModel product);

	/**
	 * Calculates the average rating for a product.
	 *
	 * @param product the Product for which the average rating shall be calculated
	 * @return the average of all ratings for the given product
	 */
	Double getAverageRating(ProductModel product);
}

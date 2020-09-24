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
package de.hybris.platform.customerreview;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;

import java.util.List;


/**
 * Service managing customer reviews.
 *
 * @spring.bean customerReviewService
 */
public interface CustomerReviewService
{
	/**
	 * Creates a new customer review based on the given parameters
	 *
	 * @param rating
	 * 		the rating
	 * @param headline
	 * 		the headline
	 * @param comment
	 * 		the comments
	 * @param user
	 * 		the user who created the review
	 * @param product
	 * 		the product reviewed
	 * @return the newly created review
	 */
	CustomerReviewModel createCustomerReview(Double rating, String headline, String comment, UserModel user, ProductModel product);

	/**
	 * Updates the given review based on the given parameters.
	 *
	 * @param model
	 * 		the review to update
	 * @param user
	 * 		the user
	 * @param product
	 * 		the product reviewed
	 * @deprecated since 6.4 not required
	 */
	@Deprecated
	void updateCustomerReview(CustomerReviewModel model, UserModel user, ProductModel product);   // NOSONAR

	/**
	 * Returns all reviews for the given product
	 *
	 * @param product
	 * 		the product for which to return all reviews
	 * @return a list of reviews for the given product
	 * @deprecated since 6.4 use instead {@link CustomerReviewService#getReviewsForProduct(ProductModel)}
	 */
	@Deprecated
	List<CustomerReviewModel> getAllReviews(ProductModel product);            // NOSONAR

	/**
	 * Returns the average rating of all reviews for the given product.
	 *
	 * @param product
	 * 		the product
	 * @return the average rating
	 */
	Double getAverageRating(ProductModel product);

	/**
	 * Returns the number of reviews for the given product
	 *
	 * @param product
	 * 		instance of {@link ProductModel} to find the number of reviews for
	 * @return the number of reviews
	 */
	Integer getNumberOfReviews(ProductModel product);

	/**
	 * Get the reviews for the specified product. Gets all the reviews in any language. (Reviews are restricted by
	 * approval status) The reviews are ordered by creation date with the most recent first.
	 *
	 * @param product
	 * 		the product
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForProduct(ProductModel product);

	/**
	 * Get the reviews for the specified customer. Gets all the reviews in any language. (Reviews are restricted by
	 * approval status) The reviews are ordered by created date with the most recent first.
	 *
	 * @param userModel
	 * 		the customer
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForCustomer(UserModel userModel);

	/**
	 * Get the reviews for the specified product in the specified language. The reviews are ordered by creation date with
	 * the most recent first.
	 *
	 * @param product
	 * 		the product
	 * @param language
	 * 		the language
	 * @return the reviews
	 */
	List<CustomerReviewModel> getReviewsForProductAndLanguage(ProductModel product, LanguageModel language);
}

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
package de.hybris.platform.customerreview.jalo;

import de.hybris.platform.core.Constants;
import de.hybris.platform.customerreview.constants.CustomerReviewConstants;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserGroup;
import de.hybris.platform.util.JspContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * This is the extension manager of the CustomerReview extension.
 */
public class CustomerReviewManager extends GeneratedCustomerReviewManager
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CustomerReviewManager.class.getName());

	/**
	 * Gets the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static CustomerReviewManager getInstance()
	{
		final ExtensionManager extensionManager = JaloSession.getCurrentSession().getExtensionManager();
		return (CustomerReviewManager) extensionManager.getExtension(CustomerReviewConstants.EXTENSIONNAME);
	}

	/**
	 * Creates the essential data for CustomerReview. The essential data are restrictions on customerReview item, so that
	 * they are not selected and shown for customer users.
	 *
	 * @param params
	 *           Ignored
	 * @param jspc
	 *           Ignored
	 */
	@Override
	public void createEssentialData(final Map params, final JspContext jspc)
	{
		// ---------------------------------------------------------------------
		// --- create restrictions for customer user group
		// ---------------------------------------------------------------------
		final TypeManager typeManager = getSession().getTypeManager(); //NOSONAR
		final UserGroup customers = getSession().getUserManager().getUserGroupByGroupID(Constants.USER.CUSTOMER_USERGROUP);

		if (typeManager.getSearchRestriction(typeManager.getComposedType(CustomerReview.class), "CustomerReviewRestrictions") == null) //NOSONAR
		{
			typeManager.createRestriction("CustomerReviewRestrictions", customers, //NOSONAR
					typeManager.getComposedType(CustomerReview.class), "{blocked} = 0 OR {blocked} IS NULL" // null for backward compatibility //NOSONAR
			);
		}
	}

	/**
	 * Creates a new CustomerReview item
	 *
	 * @param rating
	 *           The rating for this review
	 * @param headline
	 *           The optional headline for this review
	 * @param comment
	 *           The optional comment for this review
	 * @param user
	 *           The user, who created the review
	 * @param product
	 *           The product for which the review is given
	 * @return A new CustomerReview item
	 */
	public CustomerReview createCustomerReview(final Double rating, final String headline, final String comment, final User user,
			final Product product) //NOSONAR
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(CustomerReview.COMMENT, comment);
		params.put(CustomerReview.HEADLINE, headline);
		params.put(CustomerReview.RATING, rating);
		params.put(CustomerReview.USER, user);
		params.put(CustomerReview.PRODUCT, product);
		return createCustomerReview(getSession().getSessionContext(), params);
	}

	/**
	 * Calculates the average rating for a product
	 *
	 * @param ctx
	 *           The context
	 * @param item
	 *           The Product for which the average rating shall be calculated
	 * @return The average of all ratings for the given product.
	 */
	@Override
	public Double getAverageRating(final SessionContext ctx, final Product item) // NOSONAR
	{
		final String query = "SELECT avg({" + CustomerReviewModel.RATING + "}) FROM {" + CustomerReviewModel._TYPECODE + "} WHERE {" //NOSONAR
				+ CustomerReviewModel.PRODUCT + "} = ?product";
		final Map<String, Product> values = Collections.singletonMap("product", item); // NOSONAR
		final List<Double> result = FlexibleSearch.getInstance() // NOSONAR
				.search(query, values, Collections.singletonList(Double.class), true, // failOnUnknownFields,
						true, // don't need total
						0, -1).getResult();
		return result.iterator().next();
	}



	/**
	 * Get the number of reviews for the given product
	 *
	 * @param ctx
	 *           The context
	 * @param item
	 *           The Product for which the number of review shall be retrieved
	 * @return The number of reviews for the given product.
	 */
	@Override
	public Integer getNumberOfReviews(final SessionContext ctx, final Product item) // NOSONAR
	{
		final String query = "SELECT count(*) FROM {" + CustomerReviewModel._TYPECODE + "} WHERE {" + CustomerReviewModel.PRODUCT
				+ "} = ?product";
		final Map<String, Product> values = Collections.singletonMap("product", item); //NOSONAR
		final List<Integer> result = FlexibleSearch.getInstance() //NOSONAR
				.search(query, values, Collections.singletonList(Integer.class), true, // failOnUnknownFields
						true, // dontNeedTotal
						0, -1).getResult();
		return result.iterator().next();
	}

	/**
	 * Get a list of all reviews for the given product using the current session context
	 *
	 * @param item
	 *           The Product for which the list of review shall be retrieved
	 * @return The list of all reviews for the given product.
	 */
	public List<CustomerReview> getAllReviews(final Product item) // NOSONAR
	{
		return getAllReviews(JaloSession.getCurrentSession().getSessionContext(), item);
	}

	/**
	 * Get a list of all reviews for the given product
	 *
	 * @param ctx
	 *           The context
	 * @param item
	 *           The Product for which the list of review shall be retrieved
	 * @return The list of all reviews for the given product.
	 */
	public List<CustomerReview> getAllReviews(final SessionContext ctx, final Product item) // NOSONAR
	{
		final String query = "SELECT {" + CustomerReviewModel.PK + "} FROM {" + CustomerReviewModel._TYPECODE + "} WHERE {"
				+ CustomerReviewModel.PRODUCT + "} = ?product ORDER BY {" + CustomerReviewModel.CREATIONTIME + "} DESC";
		final Map<String, Product> values = Collections.singletonMap("product", item); //NOSONAR
		final List<CustomerReview> result = FlexibleSearch.getInstance() //NOSONAR
				.search(ctx, query, values, Collections.singletonList(CustomerReview.class), true, true, 0, -1).getResult();
		return result;
	}

}

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

import de.hybris.platform.customerreview.constants.CustomerReviewConstants;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.util.localization.Localization;

import java.util.HashSet;
import java.util.Set;


/**
 * This class extends the GeneratedCustomerReview and contains business logic for customerreviews.
 * 
 * 
 */
public class CustomerReview extends GeneratedCustomerReview
{

	/**
	 * Creates a new customer review item.
	 * 
	 * Hook in method for the creation of a new customer review item Do not use this method directly, always use
	 * ComposedType.newInstance() or CustomerReviewManager.createCustomerReview(..)
	 * 
	 * @param ctx
	 *           The session
	 * @param type
	 *           The item type
	 * @param allAttributes
	 *           A map containing initial values
	 * @return The new CustomerReview
	 * 
	 */
	@Override
	public CustomerReview createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{

		final Set missing = new HashSet();
		checkMandatoryAttribute(CustomerReview.PRODUCT, allAttributes, missing);
		checkMandatoryAttribute(CustomerReview.USER, allAttributes, missing);
		checkMandatoryAttribute(CustomerReview.RATING, allAttributes, missing);
		if (!missing.isEmpty())
		{
			throw new JaloInvalidParameterException("missing " + missing + " for creating a new CustomerReview", 0);
		}
		return (CustomerReview) super.createItem(ctx, type, allAttributes);
	}

	/**
	 * 
	 * Setter for rating.
	 * 
	 * @param ctx
	 *           The session
	 * @param rating
	 *           The rating
	 * @throws JaloInvalidParameterException
	 *            Thrown when the rating is not valid.
	 * 
	 */
	@Override
	public void setRating(final SessionContext ctx, final Double rating)
	{
		if (rating == null)
		{
			throw new JaloInvalidParameterException(Localization.getLocalizedString("error.customerreview.invalidrating",
					new Object[]
					{ "null", CustomerReviewConstants.getInstance().MINRATING,
							CustomerReviewConstants.getInstance().MAXRATING}), 0);
		}
		if (rating < CustomerReviewConstants.getInstance().MINRATING
				|| rating > CustomerReviewConstants.getInstance().MAXRATING)
		{
			throw new JaloInvalidParameterException(Localization.getLocalizedString("error.customerreview.invalidrating",
					new Object[]
					{ rating, CustomerReviewConstants.getInstance().MINRATING,
							CustomerReviewConstants.getInstance().MAXRATING}), 0);
		}
		super.setRating(ctx, rating);
	}
}

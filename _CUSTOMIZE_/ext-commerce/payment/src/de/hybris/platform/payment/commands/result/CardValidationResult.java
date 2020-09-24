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
package de.hybris.platform.payment.commands.result;

import de.hybris.platform.core.enums.CreditCardType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 */
public class CardValidationResult
{
	private Set<CardValidationError> validationErrors;
	private String issuer;
	private String issueCountryCode;
	private CreditCardType cardType;

	public void setIssuer(final String issuer)
	{
		this.issuer = issuer;
	}

	/**
	 * The issuing bank if known.
	 */
	public String getIssuer()
	{
		return issuer;
	}

	public void setIssueCountryCode(final String issueCountryCode)
	{
		this.issueCountryCode = issueCountryCode;
	}

	/**
	 * The country of issue if known.
	 */
	public String getIssueCountryCode()
	{
		return this.issueCountryCode;
	}

	public void setValidationErrors(final Set<CardValidationError> validationErrors)
	{
		this.validationErrors = validationErrors;
	}

	/**
	 * The validation process will not stop at the first error, so there may be more than one validation error attached
	 * to this object.
	 */
	public Set<CardValidationError> getValidationErrors()
	{
		return this.validationErrors;
	}

	public void addValidationError(final CardValidationError validationError)
	{
		if (this.validationErrors == null)
		{
			this.validationErrors = new HashSet<CardValidationError>(1);
		}
		this.validationErrors.add(validationError);
	}

	public void addValidationErrors(final Collection<CardValidationError> validationErrors)
	{
		for (final CardValidationError error : validationErrors)
		{
			addValidationError(error);
		}
	}

	/**
	 * Its success, if there are no validation errors.
	 */
	public boolean isSuccess()
	{
		return this.validationErrors == null || this.validationErrors.isEmpty();
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @param cardType
	 *           the cardType to set
	 */
	public void setCardType(final CreditCardType cardType)
	{
		this.cardType = cardType;
	}

	/**
	 * @return the cardType
	 */
	public CreditCardType getCardType()
	{
		return cardType;
	}
}

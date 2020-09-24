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
package de.hybris.platform.ruleengine.versioning;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;


public abstract class AbstractValidationResult
{

	public static final AbstractValidationResult error(final String errorMessage)
	{
		checkArgument(nonNull(errorMessage), "message must not be empty");
		return new Error(errorMessage); //NOPMD
	}

	public abstract boolean succeeded();

	public abstract String getErrorMessage();

	protected static final class Success extends AbstractValidationResult
	{
		@Override
		public boolean succeeded()
		{
			return true;
		}

		@Override
		public String getErrorMessage()
		{
			throw new IllegalStateException("There has been no error.");
		}
	}

	private static final class Error extends AbstractValidationResult
	{
		private final String errorMessage;

		public Error(final String errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		@Override
		public boolean succeeded()
		{
			return false;
		}

		@Override
		public String getErrorMessage()
		{
			return errorMessage;
		}
	}
}

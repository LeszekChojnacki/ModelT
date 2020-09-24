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

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;


public class ComposableValidationResult extends AbstractValidationResult
{
	public static final ComposableValidationResult SUCCESS = new ComposableValidationResult(new Success());

	private final AbstractValidationResult target;

	public ComposableValidationResult(final AbstractValidationResult target)
	{
		this.target = requireNonNull(target);
	}

	public static final ComposableValidationResult makeError(final String errorMessage)
	{
		return new ComposableValidationResult(error(requireNonNull(errorMessage)));
	}

	@Override
	public boolean succeeded()
	{
		return target.succeeded();
	}

	@Override
	public String getErrorMessage()
	{
		return target.getErrorMessage();
	}

	public ComposableValidationResult and(final Supplier<ComposableValidationResult> supplier)
	{
		if (!succeeded())
		{
			return this;
		}
		return requireNonNull(requireNonNull(supplier).get());
	}
}

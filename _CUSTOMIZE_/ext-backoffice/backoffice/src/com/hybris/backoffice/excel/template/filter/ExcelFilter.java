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
package com.hybris.backoffice.excel.template.filter;

import java.util.Objects;
import java.util.function.Predicate;


/**
 * Abstraction which can filter results computed in {@link com.hybris.backoffice.excel.template.mapper.ExcelMapper}s
 */
@FunctionalInterface
public interface ExcelFilter<T> extends Predicate<T>
{

	@Override
	default ExcelFilter<T> negate()
	{
		return t -> !test(t);
	}

	default ExcelFilter<T> or(final ExcelFilter<T> other)
	{
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}
}

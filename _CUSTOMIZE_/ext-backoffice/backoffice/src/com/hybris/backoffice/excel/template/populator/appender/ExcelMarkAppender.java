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
package com.hybris.backoffice.excel.template.populator.appender;

import java.util.function.BiFunction;

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * Allows to append special character to given input
 *
 * @param <T>
 */
public interface ExcelMarkAppender<T extends ExcelAttribute> extends Ordered, BiFunction<String, T, String>
{
}

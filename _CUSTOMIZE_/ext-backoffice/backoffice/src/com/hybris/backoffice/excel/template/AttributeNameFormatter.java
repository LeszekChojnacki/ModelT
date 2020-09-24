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
package com.hybris.backoffice.excel.template;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;


/**
 * Allows to format given {@link ExcelAttribute} to readable value
 * 
 * @param <ATTRIBUTE>
 *           {@link ExcelAttribute} subtype
 */
@FunctionalInterface
public interface AttributeNameFormatter<ATTRIBUTE extends ExcelAttribute>
{
	/**
	 * Formats given input to readable value
	 *
	 * @param excelAttributeContext
	 *           a context which contains excel attribute
	 * @return readable value
	 */
	String format(@Nonnull ExcelAttributeContext<ATTRIBUTE> excelAttributeContext);
}

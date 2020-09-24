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
package com.hybris.backoffice.excel.template.populator;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * @param <ATTRIBUTE>
 *           type of {@link ExcelAttribute}
 */
public interface ExcelAttributeContext<ATTRIBUTE extends ExcelAttribute>
{
	/**
	 * Retrieves desired attribute from context
	 *
	 * @param <TYPE>
	 *           type of desired attribute
	 * @param name
	 *           name of the key of desired attribute
	 * @return attribute
	 */
	<TYPE> TYPE getAttribute(@Nonnull String name, @Nonnull Class<TYPE> type);

	/**
	 * Retrieves ExcelAttribute
	 *
	 * @return ExcelAttribute which is a mandatory attribute in the context
	 * @param type
	 */
	ATTRIBUTE getExcelAttribute(@Nonnull Class<ATTRIBUTE> type);
}

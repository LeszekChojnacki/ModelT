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
package com.hybris.backoffice.excel.translators.classification;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.importing.ExcelImportContext;


public interface ClassificationAttributeHeaderValueCreator
{
	String create(@Nonnull ExcelClassificationAttribute attribute, @Nonnull ExcelImportContext excelImportContext);
}

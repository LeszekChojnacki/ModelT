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
package com.hybris.backoffice.excel.exporting.data.filter;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.function.Predicate;


/**
 * Predicate which tests if given {@link AttributeDescriptorModel} should be allowed to be exported by Excel. You can
 * easily limit the attributes that you would like to be exportable to Excel files.
 *
 * @see com.hybris.backoffice.excel.exporting.ExcelExportService
 * @see com.hybris.backoffice.excel.exporting.DefaultExcelExportService
 */
@FunctionalInterface
public interface ExcelExportAttributePredicate extends Predicate<AttributeDescriptorModel>
{
	@Override
	boolean test(AttributeDescriptorModel attributeDescriptorModel);
}

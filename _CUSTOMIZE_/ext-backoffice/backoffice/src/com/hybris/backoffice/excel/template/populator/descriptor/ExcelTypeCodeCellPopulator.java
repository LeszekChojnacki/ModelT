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
package com.hybris.backoffice.excel.template.populator.descriptor;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.ExcelCellPopulator;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;


class ExcelTypeCodeCellPopulator implements ExcelCellPopulator<ExcelAttributeDescriptorAttribute>
{

	private ExcelSheetService excelSheetService;

	@Override
	public String apply(final ExcelAttributeContext<ExcelAttributeDescriptorAttribute> populatorContext)
	{
		return excelSheetService.findSheetNameForTypeCode(
				populatorContext.getAttribute(Workbook.class.getSimpleName(), Workbook.class),
				populatorContext.getExcelAttribute(ExcelAttributeDescriptorAttribute.class).getAttributeDescriptorModel()
						.getEnclosingType().getCode());
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}
}

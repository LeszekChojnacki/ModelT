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
package com.hybris.backoffice.excel.importing;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.importing.data.ExcelImportResult;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


public class DefaultExcelImportWorkbookPostProcessor implements ExcelImportWorkbookPostProcessor
{

	private List<ExcelImportWorkbookDecorator> decorators;

	@Override
	public void process(final ExcelImportResult excelImportResult)
	{
		getDecorators().forEach(decorator -> decorator.decorate(excelImportResult));
	}

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		return getDecorators().stream().filter(ExcelImportWorkbookValidationAwareDecorator.class::isInstance)
				.map(ExcelImportWorkbookValidationAwareDecorator.class::cast).map(decorator -> decorator.validate(workbook))
				.flatMap(Collection::stream).collect(Collectors.toList());
	}

	public List<ExcelImportWorkbookDecorator> getDecorators()
	{
		return decorators;
	}

	@Required
	public void setDecorators(final List<ExcelImportWorkbookDecorator> decorators)
	{
		this.decorators = decorators;
	}
}

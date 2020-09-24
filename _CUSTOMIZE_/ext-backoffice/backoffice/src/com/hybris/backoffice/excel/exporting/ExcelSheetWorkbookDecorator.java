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
package com.hybris.backoffice.excel.exporting;

import java.util.Collection;
import java.util.LinkedList;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.populator.ExcelSheetPopulator;


/**
 * Implementation of {@link ExcelExportWorkbookDecorator}. The decorator creates additional sheets for TypeSystem and
 * ClassificationTypeSystem. You can use {@link #setPopulators(Collection)} to add your own sheet-creating mechanism.
 *
 * @see ExcelSheetPopulator
 */
public class ExcelSheetWorkbookDecorator implements ExcelExportWorkbookDecorator
{
	private int order = 0;
	private Collection<ExcelSheetPopulator> populators = new LinkedList<>();

	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		populators.forEach(populator -> populator.populate(excelExportResult));
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	// optional
	public void setOrder(final int order)
	{
		this.order = order;
	}

	// optional
	public void setPopulators(final Collection<ExcelSheetPopulator> populators)
	{
		this.populators = populators;
	}
}

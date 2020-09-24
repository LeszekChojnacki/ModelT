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
package com.hybris.backoffice.excel.template.populator.typesheet;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.exporting.ExcelExportDivider;
import com.hybris.backoffice.excel.template.populator.ExcelSheetPopulator;


/**
 * Wraps {@link ExcelSheetPopulator} to limit {@link ExcelExportResult} to data that the user has access to.
 *
 * @see PermissionCRUDService#canReadType(String)
 */
public class PermissionAwareTypeSystemSheetPopulator implements ExcelSheetPopulator
{
	private ExcelExportDivider excelExportDivider;
	private PermissionCRUDService permissionCRUDService;
	private ExcelSheetPopulator populator;

	@Override
	public void populate(@Nonnull final ExcelExportResult excelExportResult)
	{
		final Collection<ItemModel> filteredSelectedItems = filterOutInaccessibleItems(excelExportResult.getSelectedItems());
		populator.populate(copyExcelExportResultWithNewSelectedItems(excelExportResult, filteredSelectedItems));
	}

	protected Collection<ItemModel> filterOutInaccessibleItems(final Collection<ItemModel> selectedItems)
	{
		return excelExportDivider.groupItemsByType(selectedItems).entrySet().stream() //
				.filter(entry -> permissionCRUDService.canReadType(entry.getKey())) //
				.map(Map.Entry::getValue) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toList());
	}

	protected ExcelExportResult copyExcelExportResultWithNewSelectedItems(final ExcelExportResult excelExportResult,
			final Collection<ItemModel> filteredSelectedItems)
	{
		return new ExcelExportResult(excelExportResult.getWorkbook(), filteredSelectedItems,
				excelExportResult.getSelectedAttributes(), excelExportResult.getSelectedAdditionalAttributes(),
				excelExportResult.getAvailableAdditionalAttributes());
	}

	@Required
	public void setExcelExportDivider(final ExcelExportDivider excelExportDivider)
	{
		this.excelExportDivider = excelExportDivider;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}

	@Required
	public void setPopulator(final ExcelSheetPopulator populator)
	{
		this.populator = populator;
	}
}

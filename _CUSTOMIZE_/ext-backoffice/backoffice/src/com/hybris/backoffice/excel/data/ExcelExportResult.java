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
package com.hybris.backoffice.excel.data;

import de.hybris.platform.core.model.ItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Workbook;


/**
 * Represents result of exporting process. This object is used by
 * {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookDecorator}
 */
public class ExcelExportResult
{

	/**
	 * Exported and populated workbook
	 */
	private final Workbook workbook;

	/**
	 * All item models exported into excel workbook
	 */
	private final Collection<ItemModel> selectedItems;

	/**
	 * All exported attributes connected with {@link de.hybris.platform.core.model.type.AttributeDescriptorModel}
	 */
	private final Collection<SelectedAttribute> selectedAttributes;

	/**
	 * All additional attributes, not connected within
	 * {@link de.hybris.platform.core.model.type.AttributeDescriptorModel}
	 */
	private final Collection<ExcelAttribute> selectedAdditionalAttributes;

	/**
	 * All available attributes that are accessible
	 */
	private final Collection<ExcelAttribute> availableAdditionalAttributes;

	/**
	 * Creates new instance of ExcelExportResult with empty {@link #selectedItems}, {@link #selectedAttributes},
	 * {@link #selectedAdditionalAttributes}, {@link #availableAdditionalAttributes}
	 *
	 * @param workbook
	 *           workbook
	 */
	public ExcelExportResult(final Workbook workbook)
	{
		this(workbook, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
	}

	public ExcelExportResult(final Workbook workbook, final Collection<ItemModel> selectedItems,
			final Collection<SelectedAttribute> selectedAttributes, final Collection<ExcelAttribute> selectedAdditionalAttributes,
			final Collection<ExcelAttribute> availableAdditionalAttributes)
	{
		this.workbook = workbook;
		this.selectedItems = selectedItems;
		this.selectedAttributes = selectedAttributes;
		this.selectedAdditionalAttributes = selectedAdditionalAttributes;
		this.availableAdditionalAttributes = availableAdditionalAttributes;
	}

	public Workbook getWorkbook()
	{
		return workbook;
	}

	public Collection<ItemModel> getSelectedItems()
	{
		return selectedItems;
	}

	public @Nonnull Collection<SelectedAttribute> getSelectedAttributes()
	{
		return selectedAttributes != null ? selectedAttributes : new ArrayList<>();
	}

	public @Nonnull Collection<ExcelAttribute> getSelectedAdditionalAttributes()
	{
		return selectedAdditionalAttributes != null ? selectedAdditionalAttributes : new ArrayList<>();
	}

	public @Nonnull Collection<ExcelAttribute> getAvailableAdditionalAttributes()
	{
		return availableAdditionalAttributes != null ? availableAdditionalAttributes : new ArrayList<>();
	}
}

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

import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * Represents the input of exporting process. This object is used by
 * {@link com.hybris.backoffice.excel.exporting.ExcelExportParamsDecorator}
 */
public class ExcelExportParams
{
	/**
	 * All item models to be exported into excel workbook
	 */
	private final List<ItemModel> itemsToExport;
	/**
	 * All attributes to be exported connected with {@link de.hybris.platform.core.model.type.AttributeDescriptorModel}
	 */
	private final List<SelectedAttribute> selectedAttributes;
	/**
	 * All additional attributes, not connected within {@link de.hybris.platform.core.model.type.AttributeDescriptorModel}
	 */
	private final Collection<ExcelAttribute> additionalAttributes;

	/**
	 * Constructs an instance of {@link ExcelExportParams}. All parameters are required and cannot be null.
	 * 
	 * @param itemsToExport
	 *           cannot be null
	 * @param selectedAttributes
	 *           cannot be null
	 * @param additionalAttributes
	 *           cannot be null
	 */
	public ExcelExportParams(final List<ItemModel> itemsToExport, final List<SelectedAttribute> selectedAttributes,
			final Collection<ExcelAttribute> additionalAttributes)
	{
		this.itemsToExport = Objects.requireNonNull(itemsToExport, "ItemsToExport collection cannot be null");
		this.selectedAttributes = Objects.requireNonNull(selectedAttributes, "SelectedAttributes collection cannot be null");
		this.additionalAttributes = Objects.requireNonNull(additionalAttributes, "AdditionalAttributes collection cannot be null");
	}

	public List<ItemModel> getItemsToExport()
	{
		return itemsToExport;
	}

	public List<SelectedAttribute> getSelectedAttributes()
	{
		return selectedAttributes;
	}

	public Collection<ExcelAttribute> getAdditionalAttributes()
	{
		return additionalAttributes;
	}
}

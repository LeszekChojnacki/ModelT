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
package com.hybris.backoffice.excel.template.header;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.data.SelectedAttributeQualifier;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Service responsible for writing and reading attributes related to header rows, e.g.
 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.Header#DISPLAY_NAME},
 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.Header#REFERENCE_PATTERN},
 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.Header#DEFAULT_VALUE}
 */
public interface ExcelHeaderService
{

	/**
	 * Returns list of selected attributes for given sheet, based on metainformation from type system sheet.
	 *
	 * @param metaInformationSheet
	 *           {@link Sheet} contains metaInformation about attributes for each type sheets
	 * @param typeSheet
	 *           {@link Sheet} contains data for given sheet
	 * @return collection of selected attributes
	 */
	Collection<SelectedAttribute> getHeaders(final Sheet metaInformationSheet, final Sheet typeSheet);

	/**
	 * Returns list of attribute display names, that is: the contents of the display name row:
	 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.Header#DISPLAY_NAME}
	 * 
	 * @param sheet
	 *           The {@link Sheet} from which the display names should be taken
	 * @return List of display names for each attribute in the sheet
	 */
	Collection<String> getHeaderDisplayNames(Sheet sheet);

	/**
	 * Returns qualifiers of attributes which at selected in the typSheet
	 *
	 * @param metaInformationSheet
	 *           {@link Sheet} contains metaInformation about attributes for each type sheets
	 * @param typeSheet
	 *           {@link Sheet} contains data for given sheet
	 * @return collection of selected attributes.
	 */
	Collection<SelectedAttributeQualifier> getSelectedAttributesQualifiers(final Sheet metaInformationSheet,
			final Sheet typeSheet);

	/**
	 * Inserts value to the sheet's header
	 *
	 * @param sheet
	 *           where the value will be inserted
	 * @param excelAttribute
	 *           a pojo which allows to retrieve value to insert
	 * @param columnIndex
	 *           index of a column for inserted value
	 */
	void insertAttributeHeader(final Sheet sheet, final ExcelAttribute excelAttribute, final int columnIndex);

	/**
	 * A shortcut for {@link #insertAttributeHeader(Sheet, ExcelAttribute, int)} It is possible to inserts all values to
	 * the header at once instead of invoking {@link #insertAttributeHeader(Sheet, ExcelAttribute, int)} for every
	 * attribute separately
	 *
	 * @param sheet
	 *           where the value will be inserted
	 * @param excelAttributes
	 *           a collection of pojos which allows to retrieve value to insert
	 */
	void insertAttributesHeader(final Sheet sheet, final Collection<? extends ExcelAttribute> excelAttributes);

	/**
	 * Removes special characters, defined by {@link ExcelTemplateConstants.SpecialMark}, from given input
	 *
	 * @param headerValue
	 *           input
	 * @return headerValue without special marks
	 */
	default String getHeaderValueWithoutSpecialMarks(@Nonnull final String headerValue)
	{
		return headerValue.replaceAll("[" + ExcelTemplateConstants.SpecialMark.getMergedMarks() + "]", StringUtils.EMPTY);
	}

}

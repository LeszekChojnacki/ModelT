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
package com.hybris.backoffice.excel.template.workbook;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;


/**
 * Service responsible for operation on excel's workbook object.
 */
public interface ExcelWorkbookService
{

	/**
	 * Creates workbook object based on inputStream of excel file. If inputStream doesn't contains excel file then new
	 * empty Workbook will be returned.
	 *
	 * @param inputStream
	 *           Input stream of excel file
	 * @return {@link Workbook} object which represents excel file
	 */
	Workbook createWorkbook(@WillNotClose final InputStream inputStream);

	/**
	 * Returns sheet which contains metadata about type system
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return {@link Sheet} which contains information about type system
	 */
	Sheet getMetaInformationSheet(@WillNotClose final Workbook workbook);

	/**
	 * Adds property to given workbook
	 * 
	 * @param workbook
	 *           workbook which should be decorated by property
	 * @param key
	 *           key of property
	 * @param value
	 *           value of property
	 */
	void addProperty(@WillNotClose final Workbook workbook, @Nonnull final String key, @Nonnull final String value);

	/**
	 * Retrieves property from given workbook
	 *
	 * @param worbook
	 *           the source of the properties
	 * @param key
	 *           key of the property
	 * @return value of searching property
	 */
	Optional<String> getProperty(@WillNotClose final Workbook worbook, @Nonnull final String key);

	/**
	 * Retrieves underlying properties
	 *
	 * @param workbook
	 *           the source of the properties
	 * @return collection of underlying properties
	 */
	Collection<CTProperty> getUnderlyingProperties(@WillNotClose final Workbook workbook);
}

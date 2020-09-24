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
package com.hybris.backoffice.excel.translators;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;


public interface ExcelAttributeTranslator<T extends ExcelAttribute> extends Ordered
{
	boolean canHandle(@Nonnull T excelAttribute);

	Optional<String> exportData(@Nonnull T excelAttribute, @Nonnull Object objectToExport);

	default String referenceFormat(@Nonnull final T excelAttribute)
	{
		return StringUtils.EMPTY;
	}

	/**
	 * Imports data based on provided importParameters for given excel attribute. The method returns {@link Impex} thanks to
	 * that it is possible to creating additional entries ( or example creating part-of entries: product - price row, ect.)
	 *
	 * @param excelAttribute
	 *           describes attribute which should be imported
	 * @param importParameters
	 *           contains information about language for localized field, type code, parsed parameters inserted into excel's
	 *           cell.
	 * @param excelImportContext
	 *           excel's context
	 * @return {@link Impex} object which is representation of impex script.
	 */
	Impex importData(ExcelAttribute excelAttribute, ImportParameters importParameters, ExcelImportContext excelImportContext);
}

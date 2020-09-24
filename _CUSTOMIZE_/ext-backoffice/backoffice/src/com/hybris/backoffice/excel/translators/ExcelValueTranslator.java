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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Map;
import java.util.Optional;

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Base excel translator interface. In case of importing excel, when a translator will be returned just single value and
 * creation of additional entries (for example creating part-of entries: product - price row, ect.) is not required,
 * then consider {@link AbstractExcelValueTranslator}
 *
 * @param <T>
 *           class which is supported by given translator
 */
public interface ExcelValueTranslator<T> extends Ordered
{

	/**
	 * Indicates whether the translator can handle given attribute descriptor.
	 * 
	 * @param attributeDescriptorModel
	 *           {@link AttributeDescriptorModel}
	 * @return whether the translator can handle request
	 */
	boolean canHandle(final AttributeDescriptorModel attributeDescriptorModel);

	/**
	 * Converts given object to value which should be put into cell of exported excel file.
	 * 
	 * @param objectToExport
	 * @return value which will be put into cell of exported excel file.
	 */
	Optional<Object> exportData(final T objectToExport);

	/**
	 * Converts given object to value which should be put into cell of exported excel file.
	 *
	 * @param attributeDescriptorModel
	 *           {@link AttributeDescriptorModel}
	 * @param objectToExport
	 * @return value which will be put into cell of exported excel file.
	 */
	default Optional<Object> exportData(final AttributeDescriptorModel attributeDescriptorModel, final T objectToExport)
	{
		return exportData(objectToExport);
	}

	/**
	 * Imports data based on provided importParameters for given attributeDescriptor. The method returns {@link Impex}
	 * thanks to that it is possible to creating additional entries ( or example creating part-of entries: product -
	 * price row, ect.)
	 *
	 * @param attributeDescriptor
	 *           describes attribute which should be imported
	 * @param importParameters
	 *           contains information about language for localized field, type code, parsed parameters inserted into
	 *           excel's cell.
	 * @return {@link Impex} object which is representation of impex script.
	 */
	Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters);

	/**
	 * Format how a reference should be presented. Usually references consist of a few fields indicated as unique.
	 * Therefore all unique attributes should be included in the format. Example reference format: "catalog:version"
	 *
	 * @param attributeDescriptor
	 *           {@link AttributeDescriptorModel}
	 * @return Format how a reference should be presented
	 */
	default String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return "";
	}

	/**
	 * Validates single cell based on parsed importParameters. If cell has reference format then parsed values are
	 * available in {@link ImportParameters#parameters}. Otherwise the value of cell can be obtained by
	 * {@link ImportParameters#cellValue}.
	 * 
	 * @param importParameters
	 *           - contains information about parsed cell value (for reference format)
	 *           {@link ImportParameters#parameters} and original cell value {@link ImportParameters#cellValue}
	 * @param attributeDescriptor
	 *           - contains information about selected attribute for current cell
	 * @param context
	 *           - shared context which can be used as a cache
	 * @return - validation result
	 */
	ExcelValidationResult validate(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor,
			final Map<String, Object> context);

}

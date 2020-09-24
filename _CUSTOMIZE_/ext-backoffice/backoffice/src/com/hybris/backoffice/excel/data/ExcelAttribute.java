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

/**
 * Represents selected attribute used by excel decorators.
 */
public interface ExcelAttribute
{

	/**
	 * @return name of attribute
	 */
	String getName();

	/**
	 * @return information whether attribute is localized
	 */
	boolean isLocalized();

	/**
	 * @return isoCode of attribute
	 */
	String getIsoCode();

	/**
	 * @return qualifier of attribute
	 */
	String getQualifier();

	/**
	 * @return information whether attribute is mandatory
	 */
	boolean isMandatory();

	/**
	 * @return type of attribute. For example, 'java.lang.String', 'java.util.Date', 'Product'
	 */
	String getType();

	/**
	 * @return indicates whether attribute is multiValue type
	 */
	boolean isMultiValue();
}

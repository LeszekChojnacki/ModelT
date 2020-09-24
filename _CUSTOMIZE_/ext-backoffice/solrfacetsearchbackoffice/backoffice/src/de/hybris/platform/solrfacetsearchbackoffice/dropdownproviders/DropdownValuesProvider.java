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
package de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders;

import java.util.List;
import java.util.Map;


/**
 * Provides data for configurable drop down.
 */
public interface DropdownValuesProvider
{
	/**
	 * Returns values to be displayed in configurable dropdown.
	 *
	 * @param dropDownValueClassTypes
	 *           - class type of values.
	 *
	 * @return List of values.
	 */
	List<Object> getValues(String dropDownValueClassTypes);

	/**
	 * Returns values to be displayed in configurable dropdown.
	 *
	 * @param dropDownValueClassTypes
	 *           - class type of values. * @param options - dropdown options
	 * @param options
	 *           - dropdown options
	 *
	 * @return List of values.
	 */
	List<Object> getValues(String dropDownValueClassTypes, Map<String, String> options);
}

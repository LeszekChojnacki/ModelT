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

import java.util.Map;


/**
 * Provides names for values displayed in configurable dropdown.
 */
public interface DropdownNamesProvider
{
	/**
	 * Returns a name to be displayed in configurable dropdown for a value
	 *
	 * @param data
	 *           - a value from configurable dropdown
	 * @return name to be displayed in configurable dropdown for a value.
	 */
	String getName(Object data);

	/**
	 * Returns a name to be displayed in configurable dropdown for a value.
	 *
	 * @param data
	 *           - a value from configurable dropdown
	 * @param options
	 *           - dropdown options
	 * @return name to be displayed in configurable dropdown for a value.
	 */
	String getName(Object data, Map<String, String> options);
}
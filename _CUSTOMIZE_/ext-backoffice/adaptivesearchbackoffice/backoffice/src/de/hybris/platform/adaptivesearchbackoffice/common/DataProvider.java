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
package de.hybris.platform.adaptivesearchbackoffice.common;

import java.util.List;
import java.util.Map;


/**
 * Provides data to be used in widgets, editors or actions.
 *
 * @param <D>
 *           - The type of the data
 * @param <V>
 *           - The type of the value
 */
public interface DataProvider<D, V>
{
	/**
	 * Returns the data.
	 *
	 * @param parameters
	 *           - the parameters
	 *
	 * @return the data
	 */
	List<D> getData(Map<String, Object> parameters);

	/**
	 * Returns the value for a data object.
	 *
	 * @param data
	 *           - the data object
	 * @param parameters
	 *           - the parameters
	 *
	 * @return the value object
	 */
	V getValue(D data, Map<String, Object> parameters);

	/**
	 * Returns the label for a data object.
	 *
	 * @param data
	 *           - the data object
	 * @param parameters
	 *           - the parameters
	 *
	 * @return the label
	 */
	String getLabel(D data, Map<String, Object> parameters);
}

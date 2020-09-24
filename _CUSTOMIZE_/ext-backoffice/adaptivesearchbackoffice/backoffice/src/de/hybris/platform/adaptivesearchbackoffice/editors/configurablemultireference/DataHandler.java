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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;

import java.util.Collection;
import java.util.Map;

import org.zkoss.zul.ListModel;


/**
 * Data handler for the generic configurable reference editor.
 *
 * @param <D>
 *           - the type of the item data
 * @param <V>
 *           - the type of the item value
 */
public interface DataHandler<D extends AbstractEditorData, V>
{
	/**
	 * Returns the type code for the model.
	 *
	 * @return the type code
	 */
	String getTypeCode();

	/**
	 * Initializes the data handler with new data.
	 *
	 * @param initialValue
	 *           - the initial value
	 * @param searchResult
	 *           - the search result
	 * @param parameters
	 *           - the data handler parameters
	 *
	 * @return the data
	 */
	ListModel<D> loadData(Collection<V> initialValue, SearchResultData searchResult, Map<String, Object> parameters);

	/**
	 * Returns the value.
	 *
	 * @param data
	 *           - the data object
	 *
	 * @return the current value
	 *
	 * @since 6.7
	 */
	Collection<V> getValue(ListModel<D> data);

	/**
	 * Returns the value for a single item.
	 *
	 * @param data
	 *           - the data object
	 *
	 * @return the current value
	 *
	 * @since 6.7
	 */
	V getItemValue(D data);

	/**
	 * Extracts an attribute class from a specific editor data object.
	 *
	 * @param data
	 *           - the data object
	 * @param attributeName
	 *           - the attribute name
	 *
	 * @return the attribute class
	 */
	Class<?> getAttributeType(D data, String attributeName);

	/**
	 * Extracts an attribute value from a specific editor data object.
	 *
	 * @param data
	 *           - the data object
	 * @param attributeName
	 *           - the attribute name
	 *
	 * @return the attribute value
	 */
	Object getAttributeValue(D data, String attributeName);

	/**
	 * Sets an attribute value to a specific data object.
	 *
	 * @param data
	 *           - the data object.
	 * @param attributeName
	 *           - the attribute name
	 * @param attributeValue
	 *           - new attribute value
	 */
	void setAttributeValue(D data, String attributeName, Object attributeValue);
}

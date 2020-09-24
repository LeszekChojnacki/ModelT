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
package de.hybris.platform.solrfacetsearch.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * This interface represents a search result document.
 */
public interface Document extends Serializable
{
	/**
	 * Returns the field names defined in this document.
	 *
	 * @return the field names defined in this document
	 */
	Collection<String> getFieldNames();

	/**
	 * Returns the value or collection of values for a given field.
	 *
	 * @return the value or collection of values for a given field
	 */
	Object getFieldValue(String fieldName);

	/**
	 * Returns the fields defined in this document.
	 *
	 * @return the fields defined in this document
	 */
	Map<String, Object> getFields();

	/**
	 * Returns the tags defined in this document.
	 *
	 * @return the tags defined in this document
	 */
	Set<String> getTags();
}

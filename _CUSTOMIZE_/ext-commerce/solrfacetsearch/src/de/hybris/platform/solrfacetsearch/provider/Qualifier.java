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
package de.hybris.platform.solrfacetsearch.provider;



/**
 * Represents a qualifier. A qualifier can be: a language, a currency, a combination of them, etc.
 */
public interface Qualifier
{
	/**
	 * Extracts a value from this
	 * <code>Qualifier<code> object. The type should be one of the supported types of the corresponding
	 * provider (see {@link QualifierProvider#getSupportedTypes()}).
	 *
	 * @param type
	 *           - the type of the value to extract
	 *
	 * @return the extracted value
	 *
	 * @throws IllegalArgumentException
	 *            if the type is not supported
	 */
	<U> U getValueForType(Class<U> type);

	/**
	 * Returns a string representation of this <code>Qualifier</code> object.
	 *
	 * @return a <code>String<code> representation of this object
	 */
	String toFieldQualifier();
}

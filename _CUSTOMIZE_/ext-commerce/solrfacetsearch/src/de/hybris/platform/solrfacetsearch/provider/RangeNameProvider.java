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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;

import java.util.List;


/**
 * Service responsible for resolving the ranges for indexed property values. This is called during indexing.
 */
public interface RangeNameProvider
{
	/**
	 * Checks is an indexed property is ranged.
	 *
	 * @param property
	 *           - the indexed property
	 *
	 * @return <code>true</code> if the indexed property is ranged, <code>false</code> otherwise
	 */
	boolean isRanged(final IndexedProperty property);

	/**
	 * Returns the ranges associated with an indexed property that match a specific qualifier.
	 *
	 * @param property
	 *           - the indexed property
	 * @param qualifier
	 *           - the qualifier used for matching
	 *
	 * @return the list of ranges
	 */
	List<ValueRange> getValueRanges(final IndexedProperty property, final String qualifier);

	/**
	 * Resolves a list of range names for a specific value. For numerical types it allows open upper-limit range. If the
	 * property is not multiValue {@link IndexedProperty#isMultiValue()} only first matching range will be returned
	 *
	 * @param property
	 *           - the indexed property
	 * @param value
	 *           - the value for which the range names should be resolved
	 *
	 * @return the list of range names
	 *
	 * @throws FieldValueProviderException
	 *            if it is not possible to resolve a range name for a specific value
	 */
	List<String> getRangeNameList(final IndexedProperty property, final Object value) throws FieldValueProviderException;

	/**
	 * Resolves a list of range names for a specific value. For numerical types it allows open upper-limit range. If the
	 * property is not multiValue {@link IndexedProperty#isMultiValue()} only first matching range will be returned
	 *
	 * @param property
	 *           - the indexed property
	 * @param value
	 *           - the value for which the range names should be resolved
	 * @param qualifier
	 *           - the qualifier used for matching
	 *
	 * @return the list of range names
	 *
	 * @throws FieldValueProviderException
	 *            if it is not possible to resolve a range name for a specific value
	 */
	List<String> getRangeNameList(final IndexedProperty property, final Object value, final String qualifier)
			throws FieldValueProviderException;
}

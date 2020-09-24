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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.Map;


/**
 * Translates field names during search.
 */
public interface FieldNameTranslator
{
	/**
	 * Returns the translated field name for a given searchQuery, field and field type.
	 *
	 * @param searchQuery
	 *           - the search query
	 * @param field
	 *           - the field name to translate (might be an indexed property)
	 * @param fieldType
	 *           - the field type
	 *
	 * @return the translated field name
	 */
	String translate(final SearchQuery searchQuery, final String field, FieldType fieldType);

	/**
	 * Returns the translated field name for a given search context, field and field type.
	 *
	 * @param searchContext
	 *           - the facet search context
	 * @param field
	 *           - the field name to translate (might be an indexed property)
	 * @param fieldType
	 *           - the field type
	 *
	 * @return the translated field name
	 */
	String translate(FacetSearchContext searchContext, final String field, FieldType fieldType);

	/**
	 * Returns the translated field name for a given search context and field.
	 *
	 * @param searchContext
	 *           - the facet search context
	 * @param field
	 *           - the field name to translate (might be an indexed property)
	 *
	 * @return the translated field name
	 */
	String translate(FacetSearchContext searchContext, final String field);

	/**
	 * Returns information about the known fields, including the translated field names.
	 *
	 * @param searchContext
	 *           - the facet search context
	 *
	 * @return the information about the known fields
	 */
	FieldInfosMapping getFieldInfos(final FacetSearchContext searchContext);

	/**
	 * Placeholder for field details.
	 */
	interface FieldInfosMapping
	{
		/**
		 * Returns a {@link Map} containing the field infos with the field name as key.
		 *
		 * @return the field infos
		 */
		Map<String, FieldInfo> getFieldInfos();

		/**
		 * Returns a {@link Map} containing the field infos with the translated field name as key.
		 *
		 * @return the field infos
		 */
		Map<String, FieldInfo> getInvertedFieldInfos();
	}

	/**
	 * Placeholder for field details.
	 */
	interface FieldInfo
	{
		/**
		 * Returns the field name.
		 *
		 * @return the field name
		 */
		String getFieldName();

		/**
		 * Returns the translated field name.
		 *
		 * @return the translated field name
		 */
		String getTranslatedFieldName();

		/**
		 * Returns the indexed property that is associated with the field (if one exists).
		 *
		 * @return the indexed property
		 */
		IndexedProperty getIndexedProperty();
	}
}

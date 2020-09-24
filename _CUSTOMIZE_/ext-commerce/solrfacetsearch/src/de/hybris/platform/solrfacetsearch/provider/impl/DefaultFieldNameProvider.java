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
package de.hybris.platform.solrfacetsearch.provider.impl;

import static java.util.Locale.ROOT;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.util.Config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;


/**
 * Generates field names with the pattern propertyname[_specifier]_type for non-text properties and
 * propertyname_text[_specifier] for text properties.
 */
public class DefaultFieldNameProvider implements FieldNameProvider
{
	private static final String USED_SEPARATOR = Config.getString("solr.indexedproperty.forbidden.char",
			SolrFieldNameConstants.DEFAULT_SEPARATOR);

	private Converter<SolrIndexedPropertyModel, IndexedProperty> indexedPropertyConverter;

	public Converter<SolrIndexedPropertyModel, IndexedProperty> getIndexedPropertyConverter()
	{
		return indexedPropertyConverter;
	}

	public void setIndexedPropertyConverter(final Converter<SolrIndexedPropertyModel, IndexedProperty> indexedPropertyConverter)
	{
		this.indexedPropertyConverter = indexedPropertyConverter;
	}

	@Override
	public Collection<String> getFieldNames(final IndexedProperty indexedProperty, final String qualifier)
	{
		final Set<String> fields = new HashSet<>(FieldType.values().length);
		fields.add(getFieldNameForIndexing(indexedProperty, qualifier));
		fields.add(getFieldNameForSorting(indexedProperty, qualifier));

		if (indexedProperty.isAutoSuggest())
		{
			if (qualifier != null)
			{
				fields.add(SolrFieldNameConstants.AUTOSUGGEST_FIELD + SolrFieldNameConstants.DEFAULT_SEPARATOR
						+ qualifier.toLowerCase(ROOT));
			}
			else
			{
				fields.add(SolrFieldNameConstants.AUTOSUGGEST_FIELD);
			}
		}
		if (indexedProperty.isSpellCheck())
		{
			if (qualifier != null)
			{
				fields.add(SolrFieldNameConstants.SPELLCHECK_FIELD + SolrFieldNameConstants.DEFAULT_SEPARATOR
						+ qualifier.toLowerCase(ROOT));
			}
			else
			{
				fields.add(SolrFieldNameConstants.SPELLCHECK_FIELD);
			}
		}

		return fields;
	}

	@Override
	public String getFieldName(final IndexedProperty indexedProperty, final String qualifier, final FieldType fieldType)
	{
		if (fieldType == FieldType.INDEX)
		{
			return getFieldNameForIndexing(indexedProperty, qualifier);
		}
		else if (fieldType == FieldType.SORT)
		{
			return getFieldNameForSorting(indexedProperty, qualifier);
		}
		else
		{
			throw new IllegalArgumentException("Invalid field type: " + fieldType);
		}
	}


	protected String getFieldNameForIndexing(final IndexedProperty indexedProperty, final String specifier)
	{
		final String exportID = indexedProperty.getExportId();
		final String type = indexedProperty.getType();

		ServicesUtil.validateParameterNotNull(exportID, "ExportID or Name of IndexedProperty must not be null");
		ServicesUtil.validateParameterNotNull(type, "type of IndexedProperty must not be null");

		return getFieldName(indexedProperty, exportID, type, specifier);
	}

	protected String getFieldNameForSorting(final IndexedProperty indexedProperty, final String specifier)
	{
		if (indexedProperty.getSortableType() == null)
		{
			return getFieldNameForIndexing(indexedProperty, specifier);
		}

		String exportID = indexedProperty.getExportId();

		ServicesUtil.validateParameterNotNull(exportID, "ExportID or Name of IndexedProperty must not be null");

		exportID = exportID + USED_SEPARATOR + "sortable";

		String type = indexedProperty.getSortableType();
		if (type == null)
		{
			type = indexedProperty.getType();
		}

		ServicesUtil.validateParameterNotNull(type, "type of IndexedProperty must not be null");

		return getFieldName(indexedProperty, exportID, type, specifier);
	}

	protected String getFieldName(final IndexedProperty indexedProperty, final String name, final String type,
			final String specifier)
	{
		String rangeType = type;
		final String separator = USED_SEPARATOR;
		if (isRanged(indexedProperty))
		{
			rangeType = SolrFieldNameConstants.RANGE_TYPE;
		}
		rangeType = rangeType.toLowerCase(ROOT);

		final StringBuilder fieldName = new StringBuilder();

		if (specifier == null)
		{
			fieldName.append(name).append(separator).append(rangeType);
		}
		else
		{
			if (rangeType.equals(SolrFieldNameConstants.TEXT_TYPE))
			{
				fieldName.append(name).append(separator).append(SolrFieldNameConstants.TEXT_TYPE).append(separator)
						.append(specifier.toLowerCase(ROOT));
			}
			else
			{
				fieldName.append(name).append(separator).append(specifier.toLowerCase(ROOT)).append(separator).append(rangeType);
			}
		}

		if (indexedProperty.isMultiValue())
		{
			fieldName.append(separator).append(SolrFieldNameConstants.MV_TYPE);
		}

		return fieldName.toString();
	}

	protected boolean isRanged(final IndexedProperty property)
	{
		return !CollectionUtils.isEmpty(property.getValueRangeSets());
	}

	@Override
	public String getPropertyName(final String fieldName)
	{
		final int i = fieldName.indexOf(USED_SEPARATOR.charAt(0));
		if (i > 0)
		{
			return fieldName.substring(0, i);
		}
		else
		{
			return fieldName;
		}
	}

	@Override
	public String getFieldName(final SolrIndexedPropertyModel prop, final String qualifier, final FieldType fieldType)
			throws FacetConfigServiceException
	{
		return getFieldName(indexedPropertyConverter.convert(prop), qualifier, FieldType.INDEX);
	}
}

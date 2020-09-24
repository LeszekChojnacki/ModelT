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
package com.hybris.backoffice.solrsearch.decorators.impl;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Adds search type condition to filter query.
 */
public class DefaultTypeConditionDecorator extends AbstractOrderedSearchConditionDecorator
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTypeConditionDecorator.class);
	private String typeCodeFieldName;
	private TypeService typeService;

	@Override
	public void decorate(final SearchConditionData conditionData, final SearchQueryData queryData, final IndexedType indexedType)
	{
		if (hasIndexedTypeCodeField(indexedType))
		{
			final SolrSearchCondition typeCondition = prepareTypeCondition(queryData, indexedType);
			conditionData.addFilterQueryCondition(typeCondition);
		}
		else
		{
			LOG.warn("Field name {} is not configured in the indexed type {}. Search results might be inaccurate",
					typeCodeFieldName, indexedType.getIndexName());
		}
	}

	protected SolrSearchCondition prepareTypeCondition(final SearchQueryData queryData, final IndexedType indexedType)
	{
		final ComposedTypeModel composedTypeForCode = typeService.getComposedTypeForCode(queryData.getSearchType());
		final SolrSearchCondition typeCondition = new SolrSearchCondition(typeCodeFieldName,
				getIndexedPropertyType(indexedType), SearchQuery.Operator.OR);
		typeCondition.addConditionValue(queryData.getSearchType(), ValueComparisonOperator.EQUALS);
		if (queryData.isIncludeSubtypes())
		{
			composedTypeForCode.getAllSubTypes().forEach(
					ct -> typeCondition.addConditionValue(ct.getCode(), ValueComparisonOperator.EQUALS));
		}
		return typeCondition;
	}

	protected boolean hasIndexedTypeCodeField(final IndexedType indexedType)
	{
		return indexedType.getIndexedProperties().containsKey(getTypeCodeFieldName());
	}

	protected String getIndexedPropertyType(final IndexedType indexedType)
	{
		return indexedType.getIndexedProperties().get(getTypeCodeFieldName()).getType();
	}

	@Required
	public void setTypeCodeFieldName(final String typeCodeFieldName)
	{
		this.typeCodeFieldName = typeCodeFieldName;
	}

	public String getTypeCodeFieldName()
	{
		return typeCodeFieldName;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}
}

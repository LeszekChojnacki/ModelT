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
package com.hybris.backoffice.solrsearch.converters.impl;

import de.hybris.platform.solrfacetsearch.enums.SolrPropertiesTypes;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.converters.SearchConditionDataConverter;
import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class DefaultSearchConditionDataConverter implements SearchConditionDataConverter
{

	private Set<ValueComparisonOperator> fqApplicableOperators;
	private Set<SolrPropertiesTypes> fqApplicablePropertiesTypes;


	@Override
	public SearchConditionData convertConditions(final List<SolrSearchCondition> conditions,
			final SearchQuery.Operator globalOperator)
	{
		final SearchConditionData searchConditionData = new SearchConditionData();
		final List<SolrSearchCondition> flattenedConditions = flattenConditions(conditions);
		flattenedConditions.forEach(cnd -> {
			if (isFQApplicableCondition(cnd))
			{
				searchConditionData.addFilterQueryCondition(cnd);
			}
			else
			{
				searchConditionData.addQueryCondition(cnd);
			}
		});

		return searchConditionData;
	}

	protected boolean isFQApplicableCondition(final SolrSearchCondition condition)
	{
		if (condition.isFilterQueryCondition())
		{
			return true;
		}
		if (!condition.isNestedCondition())
		{
			final SolrPropertiesTypes type = SolrPropertiesTypes.valueOf(condition.getAttributeType());
			final Optional<SolrSearchCondition.ConditionValue> anyNotEqualsOperator = condition.getConditionValues().stream()
					.filter(cv -> !fqApplicableOperators.contains(cv.getComparisonOperator())).findAny();

			return !anyNotEqualsOperator.isPresent() && fqApplicablePropertiesTypes.contains(type);
		}
		return false;
	}

	protected List<SolrSearchCondition> flattenConditions(final List<SolrSearchCondition> conditions)
	{
		final List<SolrSearchCondition> flattened = new ArrayList<>();
		for (final SolrSearchCondition condition : conditions)
		{
			if (condition.isNestedCondition())
			{
				flattened.addAll(flattenConditions(condition.getNestedConditions()));
			}
			else
			{
				flattened.add(condition);
			}
		}
		return flattened;
	}

	@Required
	public void setFqApplicableOperators(final Set<ValueComparisonOperator> fqApplicableOperators)
	{
		this.fqApplicableOperators = fqApplicableOperators;
	}

	@Required
	public void setFqApplicablePropertiesTypes(final Set<SolrPropertiesTypes> fqApplicablePropertiesTypes)
	{
		this.fqApplicablePropertiesTypes = fqApplicablePropertiesTypes;
	}

}

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
package de.hybris.platform.ruleenginebackoffice.search.builder;

import com.hybris.backoffice.cockpitng.search.builder.impl.GenericConditionQueryBuilder;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericSearchField;
import de.hybris.platform.ruleenginebackoffice.util.Objects;
import org.assertj.core.util.Lists;

import java.util.Collection;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Customization of {@link GenericConditionQueryBuilder} to handle {@link ValueComparisonOperator#IN} for single token condition.
 * This is opposed to {@link GenericConditionQueryBuilder#createSingleTokenCondition(SearchQueryData, SearchAttributeDescriptor, Object, ValueComparisonOperator)}
 * that throws {@link IllegalArgumentException} in this case.
 */
public class RuleEngineGenericConditionQueryBuilder extends GenericConditionQueryBuilder
{
	@Override
	protected GenericCondition createSingleTokenCondition(final SearchQueryData searchQueryData,
			final SearchAttributeDescriptor searchAttributeDescriptor, final Object value,
			final ValueComparisonOperator givenOperator)
	{
		validateParameterNotNull(searchQueryData, "Parameter 'searchQueryData' must not be null!");
		validateParameterNotNull(searchQueryData.getSearchType(), "Parameter 'searchQueryData.searchType' must not be empty!");
		validateParameterNotNull(searchAttributeDescriptor, "Parameter 'searchAttributeDescriptor' must not be null!");

		final ValueComparisonOperator operator = givenOperator != null ? givenOperator :
				searchQueryData.getValueComparisonOperator(searchAttributeDescriptor);

		if (ValueComparisonOperator.IN.equals(operator))
		{
			final Collection collectionValue = value instanceof Collection ? (Collection) value : Lists.newArrayList(value);
			return Objects.getOrDefault(p -> getInCondition(searchQueryData, searchAttributeDescriptor, p), collectionValue);
		}

		return super.createSingleTokenCondition(searchQueryData, searchAttributeDescriptor, value, givenOperator);
	}

	private GenericCondition getInCondition(final SearchQueryData searchQueryData,
			final SearchAttributeDescriptor searchAttributeDescriptor, final Collection collectionValue)
	{
		return GenericCondition.in(
				new GenericSearchField(searchQueryData.getSearchType(), searchAttributeDescriptor.getAttributeName()),
				collectionValue);
	}
}

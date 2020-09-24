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
package com.hybris.backoffice.cockpitng.search.builder.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericConditionList;
import de.hybris.platform.core.GenericQuery;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.backoffice.cockpitng.search.builder.ConditionQueryBuilder;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.SearchQueryConditionList;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Responsible for unwrap all {@link com.hybris.cockpitng.search.data.SearchQueryConditionList} and delagate particular
 * {@link com.hybris.cockpitng.search.data.SearchQueryCondition} to corresponding {@link ConditionQueryBuilder} i.e. to:
 *
 * @spring genericQueryBuilder
 * @spring localizedQueryBuilder
 */
public class GenericMultiConditionQueryBuilder implements ConditionQueryBuilder
{
	private TypeService typeService;
	private ConditionQueryBuilder genericQueryBuilder;
	private ConditionQueryBuilder localizedQueryBuilder;


	/*
	 * Process given condition {@link AdvancedSearchQueryCondition} and translates that to the final {@link
	 * GenericCondition} list. Note. Its responsible unwrap {@link AdvancedSearchQueryConditionList} and delegate to
	 * corresponding queryBuilder.
	 */
	protected List<GenericCondition> processCondition(final GenericQuery query, final SearchQueryData searchQueryData,
			final SearchQueryCondition condition)
	{

		final List<GenericCondition> finalConditions = Lists.newLinkedList();
		if (condition instanceof SearchQueryConditionList)
		{
			final List<GenericCondition> conditions = Lists.newLinkedList();
			for (final SearchQueryCondition atomicCondition : ((SearchQueryConditionList) condition).getConditions())
			{
				conditions.addAll(processCondition(query, searchQueryData, atomicCondition));

			}
			if (!conditions.isEmpty())
			{
				if (ValueComparisonOperator.OR.equals(condition.getOperator()))
				{
					finalConditions.add(GenericConditionList.or(conditions));
				}
				else if (ValueComparisonOperator.AND.equals(condition.getOperator()))
				{

					finalConditions.add(GenericConditionList.and(conditions));
				}
			}

		}
		else
		{
			final ConditionQueryBuilder resolveQueryBuilder = lookupConditionQueryBuilder(searchQueryData.getSearchType(),
					condition.getDescriptor().getAttributeName());
			finalConditions
					.addAll(resolveQueryBuilder.buildQuery(query, searchQueryData.getSearchType(), condition, searchQueryData));
		}

		return finalConditions;

	}

	@Override
	public List<GenericCondition> buildQuery(final GenericQuery query, final String typeCode,
			final SearchAttributeDescriptor searchAttributeDescriptor, final SearchQueryData searchQueryData)
	{
		final List<GenericCondition> finalConditions = Lists.newLinkedList();
		final List<SearchQueryCondition> searchQueryConditionList = searchQueryData.getConditions().stream()
				.filter(condition -> ObjectUtils.equals(searchAttributeDescriptor, condition.getDescriptor()))
				.collect(Collectors.toList());

		for (final SearchQueryCondition condition : searchQueryConditionList)
		{
			finalConditions.addAll(processCondition(query, searchQueryData, condition));
		}
		return finalConditions;
	}

	@Override
	public List<GenericCondition> buildQuery(final GenericQuery query, final String typeCode, final SearchQueryCondition condition,
			final SearchQueryData searchQueryData)
	{
		final List<GenericCondition> finalConditions = Lists.newArrayList();
		finalConditions.addAll(processCondition(query, searchQueryData, condition));
		return finalConditions;
	}


	public ConditionQueryBuilder lookupConditionQueryBuilder(final String typeCode, final String qualifier)
	{
		validateParameterNotNull(typeCode, "Parameter 'typeCode' must not be null!");
		validateParameterNotNull(qualifier, "Parameter 'qualifier' must not be null!");

		final AttributeDescriptorModel attributeDescriptorModel = typeService.getAttributeDescriptor(typeCode, qualifier);
		if (BooleanUtils.isTrue(attributeDescriptorModel.getLocalized()))
		{
			return localizedQueryBuilder;
		}
		else
		{
			return genericQueryBuilder;
		}
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setGenericQueryBuilder(final ConditionQueryBuilder genericQueryBuilder)
	{
		this.genericQueryBuilder = genericQueryBuilder;
	}

	@Required
	public void setLocalizedQueryBuilder(final ConditionQueryBuilder localizedQueryBuilder)
	{
		this.localizedQueryBuilder = localizedQueryBuilder;
	}


}

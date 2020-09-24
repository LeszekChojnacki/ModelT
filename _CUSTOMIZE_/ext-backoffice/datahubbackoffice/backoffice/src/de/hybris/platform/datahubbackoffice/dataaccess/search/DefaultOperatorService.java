/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.dataaccess.search;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchOperatorService;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;

/**
 * Default operator service
 */
public class DefaultOperatorService implements AdvancedSearchOperatorService
{
	private Map<AdvancedSearchOperatorType, List<ValueComparisonOperator>> advancedSearchOperators;

	@Override
	public Collection<ValueComparisonOperator> getAvailableOperators(final DataAttribute dataAttribute)
	{
		final List<ValueComparisonOperator> ret = Lists.newArrayList();
		final DataType attributeValueType = dataAttribute.getValueType();
		if (attributeValueType != null)
		{
			if (attributeValueType.isAtomic())
			{
				return extractOperatorsForAtomicValue(attributeValueType);
			}

			switch (dataAttribute.getAttributeType())
			{
				case SINGLE:
					if (advancedSearchOperators.containsKey(AdvancedSearchOperatorType.REFERENCE))
					{
						return extractOperators(AdvancedSearchOperatorType.REFERENCE);
					}
					break;
				case COLLECTION:
				case LIST:
				case SET:
					if (advancedSearchOperators.containsKey((AdvancedSearchOperatorType.MULTIREFERENCE)))
					{
						return extractOperators(AdvancedSearchOperatorType.MULTIREFERENCE);
					}
					break;
				default:
					break;
			}
			ret.add(ValueComparisonOperator.EQUALS);
		}
		return ret;
	}

	private List<ValueComparisonOperator> extractOperatorsForAtomicValue(final DataType attributeValueType)
	{
		if (attributeValueType.getClazz().isAssignableFrom(String.class))
		{
			if (advancedSearchOperators.containsKey(AdvancedSearchOperatorType.LITERAL))
			{
				return extractOperators(AdvancedSearchOperatorType.LITERAL);
			}
		}
		else if (attributeValueType.getClazz().isAssignableFrom(Boolean.class))
		{
			return extractOperators(AdvancedSearchOperatorType.LOGICAL);
		}
		else if (attributeValueType.getClazz().isAssignableFrom(Date.class))
		{
			return extractOperators(AdvancedSearchOperatorType.DATE);
		}
		else
		{
			if (advancedSearchOperators.containsKey(AdvancedSearchOperatorType.NUMERIC))
			{
				return extractOperators(AdvancedSearchOperatorType.NUMERIC);
			}
		}
		return Collections.emptyList();
	}

	private List<ValueComparisonOperator> extractOperators(final AdvancedSearchOperatorType literal)
	{
		final List<ValueComparisonOperator> valueComparisonOperators = advancedSearchOperators.get(literal);
		return valueComparisonOperators == null ? Collections.emptyList() : valueComparisonOperators;
	}

	@Override
	public ValueComparisonOperator findMatchingOperator(final DataAttribute attribute, final String operatorCode)
	{
		final Collection<ValueComparisonOperator> availableOperators = getAvailableOperators(attribute);
		if (StringUtils.isNotBlank(operatorCode))
		{
			for (final ValueComparisonOperator operator : availableOperators)
			{
				if (operator.getOperatorCode().equals(operatorCode))
				{
					return ValueComparisonOperator.getOperatorByCode(operatorCode);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(availableOperators))
		{
			return availableOperators.iterator().next();
		}
		throw new IllegalStateException(String.format("Could not find any matching operator for value type [%s]",
				attribute.getValueType() != null ? attribute.getValueType().getCode() : ""));
	}

	public void setAdvancedSearchOperators(final Map<AdvancedSearchOperatorType,
			List<ValueComparisonOperator>> advancedSearchOperators)
	{
		this.advancedSearchOperators = advancedSearchOperators;
	}
}

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
package com.hybris.backoffice.solrsearch.dataaccess;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Representation of solr search condition.
 */
public class SolrSearchCondition implements Serializable
{

	private final List<ConditionValue> conditionValues = Lists.newArrayList();
	private List<SolrSearchCondition> nestedConditions;
	private String attributeName;
	private String attributeType;
	private boolean multiValue;
	private Locale language;
	private final SearchQuery.Operator operator;
	private final boolean nestedQuery;
	private final boolean filterQueryCondition;


	/**
	 * Creates condition for specified attribute and locale. All values of the condition are combined with given
	 * operator.
	 *
	 * @param attributeName
	 *           attribute name of field which is specified in this condition.
	 * @param attributeType
	 *           attribute type of field which is specified in this condition.
	 * @param language
	 *           locale of the condition. If condition locale equals current user's locale.
	 * @param operator
	 *           operator used to combine {@link #getConditionValues()}.
	 */
	public SolrSearchCondition(final String attributeName, final String attributeType, final Locale language,
			final SearchQuery.Operator operator)
	{
		this(attributeName, attributeType, false, language, operator, false);
	}

	/**
	 * Creates condition for specified attribute and locale. All values of the condition are combined with given
	 * operator.
	 *
	 * @param attributeName
	 *           attribute name of field which is specified in this condition.
	 * @param attributeType
	 *           attribute type of field which is specified in this condition.
	 * @param operator
	 *           operator used to combine {@link #getConditionValues()}.
	 */
	public SolrSearchCondition(final String attributeName, final String attributeType, final SearchQuery.Operator operator)
	{
		this(attributeName, attributeType, false, null, operator, false);
	}

	/**
	 * Creates condition for specified attribute and locale. All values of the condition are combined with given
	 * operator. Allows to specify if indexed property is multi value. Allows to notify Solr that it should use this
	 * condition as filter query.
	 *
	 * @param attributeName
	 *           attribute name of field which is specified in this condition.
	 * @param attributeType
	 *           attribute type of field which is specified in this condition.
	 * @param multiValue
	 *           tells if indexed property is multi value field {@link IndexedProperty#isMultiValue()}.
	 * @param locale
	 *           locale of the condition chosen in the condition it can be different than current user's locale.
	 * @param operator
	 *           operator used to combine {@link #getConditionValues()}.
	 * @param filterQueryCondition
	 *           tells if condition should be used as fq (filter query). Setting to false doesn't necessarily mean it
	 *           won't be used as fq, because every condition can be used as fq if its type and operator is a good match
	 *           for it.
	 */
	public SolrSearchCondition(final String attributeName, final String attributeType, final boolean multiValue,
			final Locale locale, final SearchQuery.Operator operator, final boolean filterQueryCondition)
	{
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.operator = operator;
		this.multiValue = multiValue;
		this.nestedQuery = false;
		this.filterQueryCondition = filterQueryCondition;
		if (locale != null)
		{
			this.language = locale;
		}
	}

	/**
	 * Creates condition which aggregates nested conditions. It's not bound to any specific attribute just has conditions
	 * and operator which combines them them.
	 *
	 * @param nestedConditions
	 *           list of nested conditions.
	 * @param operator
	 *           operator used to combine nested conditions.
	 */
	public SolrSearchCondition(final List<SolrSearchCondition> nestedConditions, final SearchQuery.Operator operator)
	{
		this(nestedConditions, operator, false);
	}

	/**
	 * Creates condition which aggregates nested conditions. It's not bound to any specific attribute just has conditions
	 * and operator which combines them them.
	 *
	 * @param nestedConditions
	 *           list of nested conditions.
	 * @param operator
	 *           operator used to combine nested conditions.
	 * @param filterQueryCondition
	 *           tells if condition should be used as fq (filter query)
	 */
	public SolrSearchCondition(final List<SolrSearchCondition> nestedConditions, final SearchQuery.Operator operator,
			final boolean filterQueryCondition)
	{
		this.nestedConditions = Lists.newArrayList(nestedConditions);
		this.operator = operator;
		this.nestedQuery = true;
		this.filterQueryCondition = filterQueryCondition;
	}

	public List<SolrSearchCondition> getNestedConditions()
	{
		return nestedConditions;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public String getAttributeType()
	{
		return attributeType;
	}

	public Locale getLanguage()
	{
		return language;
	}

	public SearchQuery.Operator getOperator()
	{
		return operator;
	}

	/**
	 * Tells if indexed property for this type is multi value {@link IndexedProperty#isMultiValue()}
	 *
	 * @return true if is multi value.
	 */
	public boolean isMultiValue()
	{
		return multiValue;
	}

	public List<ConditionValue> getConditionValues()
	{
		return conditionValues;
	}

	/**
	 * Adds a value to the condition.
	 *
	 * @param value
	 *           value which will be used in condition.
	 * @param comparisonOperator
	 *           operator used in a query {@link ValueComparisonOperator}
	 */
	public void addConditionValue(final Object value, final ValueComparisonOperator comparisonOperator)
	{
		if (!(value instanceof Serializable))
		{
			throw new IllegalArgumentException("Value has to be serializable");
		}
		conditionValues.add(new ConditionValue(((Serializable) value), comparisonOperator));
	}

	/**
	 * Informs if it is aggregating condition which consists of nested conditions.
	 *
	 * @return true if it is aggregating condition.
	 */
	public boolean isNestedCondition()
	{
		return nestedQuery;
	}

	/**
	 * Informs if it is a filter query (fq) condition.
	 */
	public boolean isFilterQueryCondition()
	{
		return filterQueryCondition;
	}


	public static class ConditionValue implements Serializable
	{

		private final ValueComparisonOperator comparisonOperator;
		private final Serializable value;

		public ConditionValue(final Serializable value, final ValueComparisonOperator comparisonOperator)
		{
			this.value = value;
			this.comparisonOperator = comparisonOperator;
		}

		/**
		 * If locale is present and value is instance of a map it will return value which is under given locale. Otherwise
		 * {@link #getValue()} is returned.
		 *
		 * @param locale
		 * @return localized value.
		 */
		public Serializable getValue(final Locale locale)
		{
			if (locale != null && value instanceof Map)
			{
				return (Serializable) ((Map) value).get(locale);
			}
			return getValue();
		}

		/**
		 * Returns value object.
		 *
		 * @return condition value.
		 */
		public Serializable getValue()
		{
			return value;
		}

		public ValueComparisonOperator getComparisonOperator()
		{
			return comparisonOperator;
		}

	}

}

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
package com.hybris.backoffice.widgets.searchadapters;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchInitializer;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class ReferenceFilterAdvancedSearchInitializer implements AdvancedSearchInitializer
{

	private static final Logger LOG = LoggerFactory.getLogger(ReferenceFilterAdvancedSearchInitializer.class);

	private Map<String, String> conditionFieldForTypeMap;

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		navigationNode.map(NavigationNode::getData)
				.ifPresent(nodeData -> addSearchDataConditions(searchData, (ItemModel) nodeData));
	}

	protected void addSearchDataConditions(final AdvancedSearchData searchData, final ItemModel data)
	{
		final String fieldName = conditionFieldForTypeMap.get(data.getItemtype());
		if (StringUtils.isNotBlank(fieldName))
		{
			final PK pk = data.getPk();
			final FieldType fieldType = new FieldType();
			fieldType.setDisabled(Boolean.FALSE);
			fieldType.setSelected(Boolean.TRUE);
			fieldType.setName(fieldName);
			searchData.addFilterQueryRawCondition(fieldType, ValueComparisonOperator.EQUALS, pk.getLong());

			logCondition(fieldName, pk.toString());
		}
		else
		{
			LOG.warn("Cannot find mapped fiter query condition field for {} itemtype", data.getItemtype());
		}
	}

	private void logCondition(final String attributeName, final String conditionValue)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Added condition: {} = {}", attributeName, conditionValue);
		}
	}

	protected Map<String, String> getConditionFieldForTypeMap()
	{
		return conditionFieldForTypeMap;
	}

	@Required
	public void setConditionFieldForTypeMap(final Map<String, String> conditionFieldForTypeMap)
	{
		this.conditionFieldForTypeMap = conditionFieldForTypeMap;
	}

}

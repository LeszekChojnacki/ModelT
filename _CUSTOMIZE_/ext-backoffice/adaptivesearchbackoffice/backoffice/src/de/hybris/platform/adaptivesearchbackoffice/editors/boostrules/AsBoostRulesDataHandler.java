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
package de.hybris.platform.adaptivesearchbackoffice.editors.boostrules;

import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.enums.AsBoostOperator;
import de.hybris.platform.adaptivesearch.enums.AsBoostType;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearchbackoffice.data.BoostRuleEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandler;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.DataHandler;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Implementation of {@link DataHandler} for the boost rules.
 */
public class AsBoostRulesDataHandler extends AbstractDataHandler<BoostRuleEditorData, AsBoostRuleModel>
{
	protected static final String AS_BOOST_TYPE_ADDITIVE_SYMBOL = "+";
	protected static final String AS_BOOST_TYPE_MULTIPLICATIVE_SYMBOL = "×";

	protected static final String AS_BOOST_OPERATOR_EQUAL_SYMBOL = "=";
	protected static final String AS_BOOST_OPERATOR_MATCH_SYMBOL = "≈";
	protected static final String AS_BOOST_OPERATOR_GREATER_THAN_SYMBOL = ">";
	protected static final String AS_BOOST_OPERATOR_GREATER_THAN_OR_EQUAL_SYMBOL = ">=";
	protected static final String AS_BOOST_OPERATOR_LESS_THAN_SYMBOL = "<";
	protected static final String AS_BOOST_OPERATOR_LESS_THAN_OR_EQUAL_SYMBOL = "<=";
	protected static final String AS_BOOST_OPERATOR_NOT_EQUAL_SYMBOL = "<>";

	@Override
	public String getTypeCode()
	{
		return AsBoostRuleModel._TYPECODE;
	}

	@Override
	protected BoostRuleEditorData createEditorData()
	{
		final BoostRuleEditorData editorData = new BoostRuleEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, BoostRuleEditorData> mapping, final SearchResultData searchResult,
			final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && CollectionUtils.isNotEmpty(searchProfileResult.getBoostRules()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration> boostRuleHolder : searchProfileResult
					.getBoostRules())
			{
				final AsBoostRule boostRule = boostRuleHolder.getConfiguration();
				final BoostRuleEditorData editorData = getOrCreateEditorData(mapping, boostRule.getUid());
				convertFromSearchProfileResult(boostRuleHolder, editorData, searchProfile);
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, BoostRuleEditorData> mapping,
			final Collection<AsBoostRuleModel> initialValue, final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsBoostRuleModel boostRule : initialValue)
			{
				final BoostRuleEditorData editorData = getOrCreateEditorData(mapping, boostRule.getUid());
				convertFromModel(boostRule, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(
			final AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration> source, final BoostRuleEditorData target,
			final AbstractAsSearchProfileModel searchProfile)
	{
		final AsBoostRule boostRule = source.getConfiguration();
		final String indexProperty = boostRule.getIndexProperty();
		final String boostTypeSymbol = boostRule.getBoostType().equals(AsBoostType.ADDITIVE) ? AS_BOOST_TYPE_ADDITIVE_SYMBOL
				: AS_BOOST_TYPE_MULTIPLICATIVE_SYMBOL;
		final String label = indexProperty + " " + mapBoostOperatorToSymbol(boostRule.getOperator()) + " " + boostRule.getValue();

		target.setUid(boostRule.getUid());
		target.setLabel(label);
		target.setIndexProperty(indexProperty);
		target.setBoostType(boostRule.getBoostType());
		target.setBoostTypeSymbol(boostTypeSymbol);
		target.setBoost(boostRule.getBoost());
		target.setBoostRuleConfiguration(boostRule);
		target.setFromSearchProfile(isConfigurationFromSearchProfile(source.getConfiguration(), searchProfile));

		final AbstractAsBoostRuleConfiguration replacedConfiguration = CollectionUtils
				.isNotEmpty(source.getReplacedConfigurations()) ? source.getReplacedConfigurations().get(0) : null;
		target.setOverride(replacedConfiguration != null);
		target.setOverrideFromSearchProfile(isConfigurationFromSearchProfile(replacedConfiguration, searchProfile));
	}

	protected boolean isConfigurationFromSearchProfile(final AbstractAsBoostRuleConfiguration configuration,
			final AbstractAsSearchProfileModel searchProfile)
	{
		if (configuration == null || searchProfile == null)
		{
			return false;
		}

		return StringUtils.equals(searchProfile.getCode(), configuration.getSearchProfileCode());
	}

	protected void convertFromModel(final AsBoostRuleModel source, final BoostRuleEditorData target)
	{
		// generates uid for new items
		if (StringUtils.isBlank(source.getUid()))
		{
			source.setUid(getAsUidGenerator().generateUid());
		}

		final String indexProperty = source.getIndexProperty();
		final String boostTypeSymbol = source.getBoostType().equals(AsBoostType.ADDITIVE) ? AS_BOOST_TYPE_ADDITIVE_SYMBOL
				: AS_BOOST_TYPE_MULTIPLICATIVE_SYMBOL;
		final String label = indexProperty + " " + mapBoostOperatorToSymbol(source.getOperator()) + " " + source.getValue();

		target.setUid(source.getUid());
		target.setLabel(label);
		target.setValid(getAsConfigurationService().isValid(source));
		target.setIndexProperty(indexProperty);
		target.setBoostType(source.getBoostType());
		target.setBoostTypeSymbol(boostTypeSymbol);
		target.setBoost(source.getBoost());
		target.setModel(source);
		target.setFromSearchProfile(true);
		target.setFromSearchConfiguration(true);
	}

	protected String mapBoostOperatorToSymbol(final AsBoostOperator operator)
	{
		switch (operator)
		{
			case EQUAL:
				return AS_BOOST_OPERATOR_EQUAL_SYMBOL;
			case MATCH:
				return AS_BOOST_OPERATOR_MATCH_SYMBOL;
			case GREATER_THAN:
				return AS_BOOST_OPERATOR_GREATER_THAN_SYMBOL;
			case GREATER_THAN_OR_EQUAL:
				return AS_BOOST_OPERATOR_GREATER_THAN_OR_EQUAL_SYMBOL;
			case LESS_THAN:
				return AS_BOOST_OPERATOR_LESS_THAN_SYMBOL;
			case LESS_THAN_OR_EQUAL:
				return AS_BOOST_OPERATOR_LESS_THAN_OR_EQUAL_SYMBOL;
			case NOT_EQUAL:
				return AS_BOOST_OPERATOR_NOT_EQUAL_SYMBOL;
			default:
				return operator.getCode();
		}
	}
}

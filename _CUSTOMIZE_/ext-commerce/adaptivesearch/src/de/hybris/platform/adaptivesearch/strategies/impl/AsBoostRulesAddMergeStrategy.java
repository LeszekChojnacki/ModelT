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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsBoostRulesMergeStrategy;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;


/**
 * Default implementation of {@link AsBoostRulesMergeStrategy} for ADD merge mode.
 */
public class AsBoostRulesAddMergeStrategy extends AbstractAsBoostRulesMergeStrategy
{
	@Override
	public void mergeBoostRules(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		mergeAfter(source.getBoostRules(), target.getBoostRules());
	}

	protected <V extends AsBoostRule> void mergeAfter(
			final List<AsConfigurationHolder<V, AbstractAsBoostRuleConfiguration>> source,
			final List<AsConfigurationHolder<V, AbstractAsBoostRuleConfiguration>> target)
	{
		if (CollectionUtils.isEmpty(source))
		{
			return;
		}

		for (final AsConfigurationHolder<V, AbstractAsBoostRuleConfiguration> configuration : source)
		{
			final AsConfigurationHolder<V, AbstractAsBoostRuleConfiguration> newConfiguration = getAsSearchProfileResultFactory()
					.cloneConfigurationHolder(configuration);
			target.add(newConfiguration);
		}
	}
}
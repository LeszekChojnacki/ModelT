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
package com.hybris.backoffice.excel.translators.generic.factory;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Default implementation of {@link ImportImpexFactory}. The service creates impex object based on hierarchical
 * structure of unique attributes.
 */
public class DefaultImportImpexFactory implements ImportImpexFactory
{

	private List<ImportImpexFactoryStrategy> strategies;

	@Override
	public Impex create(final RequiredAttribute rootUniqueAttribute, final ImportParameters importParameters)
	{
		final Optional<ImportImpexFactoryStrategy> foundStrategy = strategies.stream()
				.filter(strategy -> strategy.canHandle(rootUniqueAttribute, importParameters)).findFirst();
		return foundStrategy.map(strategy -> strategy.create(rootUniqueAttribute, importParameters)).orElse(new Impex());
	}

	public List<ImportImpexFactoryStrategy> getStrategies()
	{
		return strategies;
	}

	@Required
	public void setStrategies(final List<ImportImpexFactoryStrategy> strategies)
	{
		this.strategies = strategies;
	}
}

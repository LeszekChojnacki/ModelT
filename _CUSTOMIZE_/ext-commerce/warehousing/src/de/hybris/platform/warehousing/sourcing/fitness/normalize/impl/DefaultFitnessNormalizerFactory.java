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
 *
 */
package de.hybris.platform.warehousing.sourcing.fitness.normalize.impl;

import de.hybris.platform.warehousing.data.sourcing.SourcingFactorIdentifiersEnum;
import de.hybris.platform.warehousing.sourcing.fitness.normalize.FitnessNormalizer;
import de.hybris.platform.warehousing.sourcing.fitness.normalize.FitnessNormalizerFactory;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Holds a {@link Map} of fitness normalizers by {@link SourcingFactorIdentifiersEnum} as keys.
 */
public class DefaultFitnessNormalizerFactory implements FitnessNormalizerFactory, InitializingBean
{
	private Map<SourcingFactorIdentifiersEnum, FitnessNormalizer> fitnessNormalizerMap;

	@Override
	public FitnessNormalizer getNormalizer(final SourcingFactorIdentifiersEnum factorId)
	{
		return this.fitnessNormalizerMap.get(factorId);
	}

	protected Map<SourcingFactorIdentifiersEnum, FitnessNormalizer> getFitnessNormalizerMap()
	{
		return this.fitnessNormalizerMap;
	}

	@Required
	public void setFitnessNormalizerMap(final Map<SourcingFactorIdentifiersEnum, FitnessNormalizer> fitnessNormalizerMap)
	{
		this.fitnessNormalizerMap = fitnessNormalizerMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (fitnessNormalizerMap.isEmpty())
		{
			throw new IllegalArgumentException("Fitness normalizer map cannot be empty.");
		}
	}
}

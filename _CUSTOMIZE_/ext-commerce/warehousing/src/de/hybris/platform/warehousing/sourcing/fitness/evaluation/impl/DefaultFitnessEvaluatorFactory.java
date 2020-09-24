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
package de.hybris.platform.warehousing.sourcing.fitness.evaluation.impl;

import de.hybris.platform.warehousing.data.sourcing.SourcingFactorIdentifiersEnum;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluator;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluatorFactory;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Holds a {@link Map} of fitness evaluators by {@link SourcingFactorIdentifiersEnum} as keys
 */
public class DefaultFitnessEvaluatorFactory implements FitnessEvaluatorFactory, InitializingBean
{
	private Map<SourcingFactorIdentifiersEnum, FitnessEvaluator> fitnessEvaluatorMap;
	
	@Override
	public FitnessEvaluator getEvaluator(final SourcingFactorIdentifiersEnum factorId)
	{
		return getFitnessEvaluatorMap().get(factorId);
	}
	
	protected Map<SourcingFactorIdentifiersEnum, FitnessEvaluator> getFitnessEvaluatorMap()
	{
		return this.fitnessEvaluatorMap;
	}

	@Required
	public void setFitnessEvaluatorMap(final Map<SourcingFactorIdentifiersEnum, FitnessEvaluator> fitnessEvaluatorMap)
	{
		this.fitnessEvaluatorMap = fitnessEvaluatorMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (getFitnessEvaluatorMap().isEmpty())
		{
			throw new IllegalArgumentException("Fitness evaluators map cannot be empty.");
		}
	}
}

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
package de.hybris.platform.warehousing.sourcing.fitness.impl;

import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.data.sourcing.FitSourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingFactor;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.factor.SourcingFactorService;
import de.hybris.platform.warehousing.sourcing.fitness.FitnessService;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluator;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluatorFactory;
import de.hybris.platform.warehousing.sourcing.fitness.normalize.FitnessNormalizer;
import de.hybris.platform.warehousing.sourcing.fitness.normalize.FitnessNormalizerFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * The default implementation of calculation service
 */
public class DefaultFitnessService implements FitnessService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFitnessService.class);
	private static final double ONE_HUNDRED = 100d;

	private FitnessEvaluatorFactory fitnessEvaluatorFactory;
	private SourcingFactorService sourcingFactorService;
	private FitnessNormalizerFactory fitnessNormalizerFactory;
	private Comparator<FitSourcingLocation> fitnessComparator;

	@Override
	public List<SourcingLocation> sortByFitness(final SourcingContext sourcingContext)
	{
		ServicesUtil.validateParameterNotNull(sourcingContext, "SourcingContext cannot be null");
		ServicesUtil.validateIfAnyResult(sourcingContext.getSourcingLocations(), "No location found to check for its fitness");

		final Collection<SourcingLocation> sourcingLocations = sourcingContext.getSourcingLocations();
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Original sourcing location order: {}", sourcingLocations.stream().map(sl -> sl.getWarehouse().getCode())
					.collect(Collectors.joining(",", "{", "}")));
		}
		final FitSourcingLocation[] fitSourcingLocations = calculateFitness(sourcingContext);
		Arrays.sort(fitSourcingLocations, fitnessComparator);

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Post-Fitness sourcing location order: {}", Arrays.stream(fitSourcingLocations)
					.map(sl -> sl.getWarehouse().getCode()).collect(Collectors.joining(",", "{", "}")));
		}
		return Arrays.asList(fitSourcingLocations);
	}

	/**
	 * Calculate the fitness of each sourcing location in the givn sourcing context and provide the fitness in a "fit" sourcing location.
	 *
	 * @param sourcingContext
	 * 		- the sourcing context
	 * @return an array of "fit" sourcing locations; never <tt>null</tt>
	 */
	protected FitSourcingLocation[] calculateFitness(final SourcingContext sourcingContext)
	{
		ServicesUtil.validateParameterNotNull(sourcingContext, "SourcingContext cannot be null");
		ServicesUtil.validateIfAnyResult(sourcingContext.getSourcingLocations(), "No location found to check for its fitness");
		ServicesUtil.validateIfAnyResult(sourcingContext.getOrderEntries(), "No OrderEntries found in the SourcingContext");
		ServicesUtil.validateParameterNotNull(sourcingContext.getOrderEntries().iterator().next().getOrder(),
				"No Order found for the OrderEntries in the sourcingContext");
		ServicesUtil.validateParameterNotNull(sourcingContext.getOrderEntries().iterator().next().getOrder().getStore(),
				"No BaseStore found for the Order in the sourcingContext");

		final Collection<SourcingLocation> sourcingLocations = sourcingContext.getSourcingLocations();
		final BaseStoreModel baseStore = sourcingContext.getOrderEntries().iterator().next().getOrder().getStore();
		final Set<SourcingFactor> sourcingFactors = getSourcingFactorService().getAllSourcingFactorsForBaseStore(baseStore);

		return getFitSourcingLocations(sourcingLocations, sourcingFactors);
	}

	/**
	 * Evaluates fitness of a sourcing location based on given set of sourcingFactors
	 *
	 * @param sourcingLocations
	 * 		the collection of sourcingLocations to be evaulated for their fitness
	 * @param sourcingFactors
	 * 		the set of sourcingFactors against which sourcingLocations needs to be evaluated
	 * @return the array of {@link FitSourcingLocation}; never <tt>null</tt>
	 */
	protected FitSourcingLocation[] getFitSourcingLocations(final Collection<SourcingLocation> sourcingLocations,
			final Set<SourcingFactor> sourcingFactors)
	{
		ServicesUtil.validateIfAnyResult(sourcingLocations, "No location found to check for its fitness");
		ServicesUtil.validateIfAnyResult(sourcingFactors, "No sourcing factors found");

		final FitSourcingLocation[] fitSourcingLocations = new FitSourcingLocation[sourcingLocations.size()];
		final int rows = sourcingLocations.size();
		final int cols = sourcingFactors.size();
		final double[][] fitnessMatrix = new double[rows][cols];
		final double[] totalsMatrix = new double[cols];

		FitnessEvaluator evaluator;
		int locationCursor = 0;
		int factorCursor = 0;

		// Evaluate the fitness values and totals for each factor
		for (final SourcingLocation sourcingLocation : sourcingLocations)
		{
			for (final SourcingFactor factor : sourcingFactors)
			{
				evaluator = fitnessEvaluatorFactory.getEvaluator(factor.getFactorId());
				fitnessMatrix[locationCursor][factorCursor] = evaluator.evaluate(sourcingLocation);
				totalsMatrix[factorCursor] += fitnessMatrix[locationCursor][factorCursor];
				factorCursor++;
			}

			factorCursor = 0;
			locationCursor++;
		}

		// Normalize the fitness values
		FitnessNormalizer normalizer;
		locationCursor = 0;
		factorCursor = 0;
		double locationFitness = 0;
		for (final SourcingLocation sourcingLocation : sourcingLocations)
		{
			for (final SourcingFactor factor : sourcingFactors)
			{
				normalizer = fitnessNormalizerFactory.getNormalizer(factor.getFactorId());
				fitnessMatrix[locationCursor][factorCursor] = normalizer.normalize(fitnessMatrix[locationCursor][factorCursor],
						Double.doubleToLongBits(totalsMatrix[factorCursor]) == 0L ? 1 : totalsMatrix[factorCursor]);
				locationFitness += fitnessMatrix[locationCursor][factorCursor] * (factor.getWeight() / ONE_HUNDRED);
				factorCursor++;
			}

			final FitSourcingLocation fitSourcingLocation = buildFitSourcingLocation(sourcingLocation);
			fitSourcingLocation.setFitness(locationFitness);
			fitSourcingLocations[locationCursor] = fitSourcingLocation;

			factorCursor = 0;
			locationCursor++;
			locationFitness = 0;
		}
		return fitSourcingLocations;
	}

	/**
	 * Create a {@link FitSourcingLocation} from a {@link SourcingLocation}.
	 *
	 * @param sourcingLocation
	 * 		the sourcing location
	 * @return a fit sourcing location
	 */
	protected FitSourcingLocation buildFitSourcingLocation(final SourcingLocation sourcingLocation)
	{
		ServicesUtil.validateParameterNotNull(sourcingLocation, "SourcingLocation cannot be null");

		final FitSourcingLocation fitSourcingLocation = new FitSourcingLocation();
		try
		{
			BeanUtils.copyProperties(fitSourcingLocation, sourcingLocation);
		}
		catch (IllegalAccessException | InvocationTargetException e) //NOSONAR
		{
			throw new IllegalArgumentException("Sourcing location was not properly formatted."); //NOSONAR
		}
		return fitSourcingLocation;
	}

	protected FitnessEvaluatorFactory getFitnessEvaluatorFactory()
	{
		return this.fitnessEvaluatorFactory;
	}

	@Required
	public void setFitnessEvaluatorFactory(final FitnessEvaluatorFactory fitnessEvaluatorFactory)
	{
		this.fitnessEvaluatorFactory = fitnessEvaluatorFactory;
	}

	protected Comparator<FitSourcingLocation> getFitnessComparator()
	{
		return fitnessComparator;
	}

	@Required
	public void setFitnessComparator(final Comparator<FitSourcingLocation> fitnessComparator)
	{
		this.fitnessComparator = fitnessComparator;
	}

	protected SourcingFactorService getSourcingFactorService()
	{
		return sourcingFactorService;
	}

	@Required
	public void setSourcingFactorService(final SourcingFactorService sourcingFactorService)
	{
		this.sourcingFactorService = sourcingFactorService;
	}

	public FitnessNormalizerFactory getFitnessNormalizerFactory()
	{
		return fitnessNormalizerFactory;
	}

	@Required
	public void setFitnessNormalizerFactory(final FitnessNormalizerFactory fitnessNormalizerFactory)
	{
		this.fitnessNormalizerFactory = fitnessNormalizerFactory;
	}

}

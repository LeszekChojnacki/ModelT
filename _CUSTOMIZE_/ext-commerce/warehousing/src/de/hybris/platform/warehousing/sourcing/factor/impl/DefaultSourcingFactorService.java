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
package de.hybris.platform.warehousing.sourcing.factor.impl;

import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingFactor;
import de.hybris.platform.warehousing.data.sourcing.SourcingFactorIdentifiersEnum;
import de.hybris.platform.warehousing.model.SourcingConfigModel;
import de.hybris.platform.warehousing.sourcing.factor.SourcingFactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Default implementation of the {@link SourcingFactorService}
 */
public class DefaultSourcingFactorService implements SourcingFactorService
{

	private static final int ONE_HUNDRED = 100;
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSourcingFactorService.class);


	@Override
	public SourcingFactor getSourcingFactor(final SourcingFactorIdentifiersEnum sourcingFactorId, final BaseStoreModel baseStore)
	{
		ServicesUtil.validateParameterNotNull(baseStore, "BaseStore cannot be null");
		ServicesUtil.validateParameterNotNull(sourcingFactorId, "SourcingFactorId cannot be null");

		final Map<SourcingFactorIdentifiersEnum, SourcingFactor> sourcingFactorsMapForBaseStore = getSourcingFactorsMapForBaseStore(
				baseStore);
		return sourcingFactorsMapForBaseStore.get(sourcingFactorId);
	}

	@Override
	public Set<SourcingFactor> getAllSourcingFactorsForBaseStore(final BaseStoreModel baseStore)
	{
		ServicesUtil.validateParameterNotNull(baseStore, "BaseStore cannot be null");

		final Set<SourcingFactor> result = new HashSet<>();
		getSourcingFactorsMapForBaseStore(baseStore).forEach((sourcingFactorId, sourcingFactor) -> result.add(sourcingFactor));
		return result;
	}

	/**
	 * Creates Map of SourcingFactorIdentifiersEnum and SourcingFactor for the given baseStore
	 *
	 * @param baseStore
	 * 		the baseStore
	 * @return the map of SourcingFactorIdentifiersEnum and SourcingFactor for the given baseStore; never <tt>null</tt>
	 */
	protected Map<SourcingFactorIdentifiersEnum, SourcingFactor> getSourcingFactorsMapForBaseStore(final BaseStoreModel baseStore)
	{
		ServicesUtil.validateParameterNotNull(baseStore, "BaseStore cannot be null");

		final Map<SourcingFactorIdentifiersEnum, SourcingFactor> sourcingFactorsMapForBaseStore = new HashMap<>(); //NOSONAR
		Arrays.stream(SourcingFactorIdentifiersEnum.values()).forEach(sourcingFactorId -> sourcingFactorsMapForBaseStore
				.put(sourcingFactorId, createSourcingFactorFromSourcingConfig(sourcingFactorId, baseStore.getSourcingConfig())));
		final int totalWeightage = sourcingFactorsMapForBaseStore.keySet().stream()
				.mapToInt(sourcingFactorId -> sourcingFactorsMapForBaseStore.get(sourcingFactorId).getWeight()).sum();
		if (ONE_HUNDRED != totalWeightage)
		{
			throw new IllegalArgumentException(
					"Factor weights are percentages, therefore the sum of the factor weights should equal 100.");
		}
		return sourcingFactorsMapForBaseStore;
	}

	/**
	 * Create a single sourcing factor from the SourcingConfig.
	 *
	 * @param sourcingFactorId
	 * 		the sourcingFactorId
	 * @param sourcingConfig
	 * 		the sourcingConfig for the given baseStore
	 * @return new sourcing factor; never <tt>null</tt>
	 */
	protected SourcingFactor createSourcingFactorFromSourcingConfig(final SourcingFactorIdentifiersEnum sourcingFactorId,
			final SourcingConfigModel sourcingConfig)
	{
		ServicesUtil.validateParameterNotNull(sourcingFactorId, "SourcingFactorId cannot be null");
		ServicesUtil.validateParameterNotNull(sourcingConfig, "SourcingConfig cannot be null");

		final SourcingFactor sourcingFactor = new SourcingFactor();
		sourcingFactor.setFactorId(sourcingFactorId);
		sourcingFactor.setWeight(loadFactorValue(sourcingFactorId, sourcingConfig));
		return sourcingFactor;

	}

	/**
	 * Load the integer value that corresponds to the sourcing factor identifier provided.
	 *
	 * @param sourcingFactorId
	 * 		the sourcingFactorId
	 * @param sourcingConfig
	 * 		the sourcingConfig for the given baseStore
	 * @return the integer value or 0 if not found
	 */
	protected int loadFactorValue(final SourcingFactorIdentifiersEnum sourcingFactorId, final SourcingConfigModel sourcingConfig)
	{
		ServicesUtil.validateParameterNotNull(sourcingFactorId, "SourcingFactorId cannot be null");
		ServicesUtil.validateParameterNotNull(sourcingConfig, "SourcingConfig cannot be null");

		try
		{
			final Optional<PropertyDescriptor> propertyDescriptorOptional = Arrays
					.stream(Introspector.getBeanInfo(sourcingConfig.getClass()).getPropertyDescriptors())
					.filter(propDescriptor -> propDescriptor.getReadMethod() != null && propDescriptor.getName().toLowerCase()
							.contains(sourcingFactorId.toString().toLowerCase()))
					.findFirst();
			if (propertyDescriptorOptional.isPresent())
			{
				final Object result = propertyDescriptorOptional.get().getReadMethod().invoke(sourcingConfig);
				if (result instanceof Integer)
				{
					final int factorValue = ((Integer) result).intValue();
					Assert.isTrue(factorValue >= 0, "Negative weight has been found in sourcing factor, please reset to positive.");
					return factorValue;
				}
			}
		}
		catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) //NOSONAR
		{
			LOGGER.error(String.format(
					"Sourcing Config failed to interpret sourcing factor. Please make sure the SourcingConfig has appropriate weight defined for this sourcingFactor: [%s]. Returning 0 as weight for this sourcingFactor",
					sourcingFactorId)); //NOSONAR
		}

		return 0;

	}


}

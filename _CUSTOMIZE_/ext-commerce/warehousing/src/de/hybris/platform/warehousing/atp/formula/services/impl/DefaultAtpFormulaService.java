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
package de.hybris.platform.warehousing.atp.formula.services.impl;

import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.warehousing.atp.dao.AvailableToPromiseDao;
import de.hybris.platform.warehousing.atp.formula.dao.AtpFormulaDao;
import de.hybris.platform.warehousing.atp.formula.services.AtpFormulaService;
import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;



/**
 * Default implementation of {@link AtpFormulaService}
 */
public class DefaultAtpFormulaService implements AtpFormulaService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAtpFormulaService.class);

	protected static final String EXTERNAL = "external";
	protected static final String RETURNED = "returned";
	protected static final List<String> operatorList = Arrays.asList("+", "-");

	private AvailableToPromiseDao availableToPromiseDao;
	private BaseStoreService baseStoreService;
	private ModelService modelService;
	private AtpFormulaDao atpFormulaDao;
	private Map<String, String> atpFormulaVar2ArithmeticOperatorMap;


	@Override
	public AtpFormulaModel getAtpFormulaByCode(final String formulaCode)
	{
		return null;
	}

	@Override
	public Collection<AtpFormulaModel> getAllAtpFormula()
	{
		return getAtpFormulaDao().getAllAtpFormula();
	}

	@Override
	public AtpFormulaModel createAtpFormula(final AtpFormulaModel atpFormula)
	{
		return null;
	}

	@Override
	public AtpFormulaModel updateAtpFormula(final AtpFormulaModel atpFormula)
	{
		return null;
	}

	@Override
	public void deleteAtpFormula(final String formulaCode)
	{
		if (getBaseStoreService().getAllBaseStores().stream()
				.anyMatch(baseStore -> baseStore.getDefaultAtpFormula().getCode().equals(formulaCode)))
		{
			LOGGER.info(
					"The formula with code {} has not been deleted because it is use by at least one base store as a default formula.",
					formulaCode);
		}
		else
		{
			getModelService().remove(getAtpFormulaByCode(formulaCode));
			LOGGER.info("The formula with code {} has been deleted.", formulaCode);
		}
	}

	@Override
	public Long getAtpValueFromFormula(final AtpFormulaModel atpFormula, final Map<String, Object> params)
	{
		List<Long> results = new ArrayList();
		results.add(0L);

		final Set<PropertyDescriptor> propertyDescriptors = new HashSet<>();
		if (atpFormula != null)
		{
			try
			{
				Arrays.stream(Introspector.getBeanInfo(atpFormula.getClass()).getPropertyDescriptors())
						.filter(descriptor -> descriptor.getPropertyType().equals(Boolean.class))
						.forEach(propertyDescriptor -> propertyDescriptors.add(propertyDescriptor));
			}
			catch (final IntrospectionException e)//NOSONAR
			{
				LOGGER.error("Sourcing failed to interpret the ATP formula.");//NOSONAR
			}

			//Excluding the DAO call for Returned and External AtpFormula's variable, since these kind of stocklevels have already
			// been filtered in WarehousingAvailabilityCalculationStrategy#filterStocks()
			final Collection<String> atpAvailabilityVariables = Arrays.asList(RETURNED, EXTERNAL);
			propertyDescriptors.stream()
					.filter(propertyDescriptor -> !atpAvailabilityVariables.contains(propertyDescriptor.getName().toLowerCase()))
					.forEach(formulaVarPropDescriptor -> getAtpFormulaVariableValue(params, results, formulaVarPropDescriptor,
							atpFormula));
		}

		return results.stream().mapToLong(Long::longValue).sum();
	}

	/**
	 * Gets the value for a specific atpformula variable name
	 *
	 * @param params
	 * 		The parameters to apply to the method matching the name in {@link AvailableToPromiseDao}
	 * @param results
	 * 		The list of results already found and to which the result must be added
	 * @param formulaVarPropDescriptor
	 * 		The {@link PropertyDescriptor} for AtpFormulaVariable's property for which the value is requested
	 * @param atpFormula
	 * @return the complete list of values including the found value
	 */
	protected List<Long> getAtpFormulaVariableValue(final Map<String, Object> params, final List<Long> results,
			final PropertyDescriptor formulaVarPropDescriptor, final AtpFormulaModel atpFormula)
	{
		boolean isExceptionCaught = false;
		final List<Long> errorList = new ArrayList<>();
		try
		{
			final Object formulaVarValue = formulaVarPropDescriptor.getReadMethod().invoke(atpFormula);
			if (formulaVarValue instanceof Boolean)
			{
				final Boolean isIncluded = (Boolean) formulaVarValue;
				if (isIncluded)
				{
					final String atpFormulaVar = formulaVarPropDescriptor.getName().toLowerCase();

					if (getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar) != null && operatorList
							.contains(getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar)))
					{
						for (final Method method : getAvailableToPromiseDao().getClass().getMethods())
						{
							if (method.getName().toLowerCase().contains(atpFormulaVar))
							{
								Object result = method.invoke(getAvailableToPromiseDao(), params);
								if (result instanceof Long)
								{
									if ("-".equals(getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar)))
									{
										result = ((Long) result) * -1;
									}
									results.add((Long) result);
									LOGGER.debug("ATP value calculated for {}: {}", atpFormulaVar, result);
								}
							}
						}
					}
					else
					{
						LOGGER.error(
								"Failed to interpret the Arithmetic sign for ATP formula variable: [{}] -> [{}]. Please update your formula variable with appropriate sign (+ or -) in atpFormulaVar2ArithmeticOperatorMap",
								atpFormulaVar, getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar));
					}
				}
			}
		}
		catch (final SecurityException | InvocationTargetException | IllegalAccessException e)
		{
			isExceptionCaught = true;
			errorList.add(0L);
			LOGGER.error(
					"Sourcing failed to interpret the ATP formula. Please review your formula variable: {}", formulaVarPropDescriptor
							.getName());
			LOGGER.info(String.valueOf(e));
		}
		return isExceptionCaught ? errorList : results;
	}

	protected AvailableToPromiseDao getAvailableToPromiseDao()
	{
		return availableToPromiseDao;
	}

	@Required
	public void setAvailableToPromiseDao(AvailableToPromiseDao availableToPromiseDao)
	{
		this.availableToPromiseDao = availableToPromiseDao;
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService)
	{
		this.modelService = modelService;
	}


	protected AtpFormulaDao getAtpFormulaDao()
	{
		return atpFormulaDao;
	}

	@Required
	public void setAtpFormulaDao(final AtpFormulaDao atpFormulaDao)
	{
		this.atpFormulaDao = atpFormulaDao;
	}

	protected Map<String, String> getAtpFormulaVar2ArithmeticOperatorMap()
	{
		return atpFormulaVar2ArithmeticOperatorMap;
	}

	@Required
	public void setAtpFormulaVar2ArithmeticOperatorMap(final Map<String, String> atpFormulaVar2ArithmeticOperatorMap)
	{
		this.atpFormulaVar2ArithmeticOperatorMap = atpFormulaVar2ArithmeticOperatorMap;
	}
}

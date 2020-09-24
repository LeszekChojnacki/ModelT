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
 */
package de.hybris.platform.warehousing.atp.handlers;

import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Handler for {@link AtpFormulaModel#FORMULASTRING}
 */
public class AtpFormulaStringHandler implements DynamicAttributeHandler<String, AtpFormulaModel>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AtpFormulaStringHandler.class);

	protected static final String AVAILABILITY = "availability";
	protected static final List<String> operatorList = Arrays.asList("+", "-");

	private Map<String, String> atpFormulaVar2ArithmeticOperatorMap;

	/**
	 * Checks if the given {@link PropertyDescriptor} is for {@link AtpFormulaModel#AVAILABILITY}
	 *
	 * @return true if {@link AtpFormulaModel#AVAILABILITY}
	 */
	protected static Predicate<PropertyDescriptor> isAvailable()
	{
		return propertyDescriptor -> AVAILABILITY.equalsIgnoreCase(propertyDescriptor.getName());
	}

	@Override
	public String get(final AtpFormulaModel atpFormulaModel)
	{
		final StringBuilder formulaString = new StringBuilder();
		final Set<PropertyDescriptor> propertyDescriptors = new HashSet<>();
		if (atpFormulaModel != null)
		{
			try
			{
				Arrays.stream(Introspector.getBeanInfo(atpFormulaModel.getClass()).getPropertyDescriptors())
						.filter(descriptor -> descriptor.getPropertyType().equals(Boolean.class)).forEach(propertyDescriptors::add);
			}
			catch (final IntrospectionException e)//NOSONAR
			{
				LOGGER.error("Failed to interpret the ATP formula");//NOSONAR
			}
		}

		prepareFormulaString(atpFormulaModel, formulaString, propertyDescriptors);

		return formulaString.toString();
	}

	/**
	 * Prepares formula string from atpformula variables
	 *
	 * @param atpFormulaModel
	 * @param formulaString
	 * @param propertyDescriptors
	 */
	protected void prepareFormulaString(final AtpFormulaModel atpFormulaModel, final StringBuilder formulaString,
			final Set<PropertyDescriptor> propertyDescriptors)
	{
		if (CollectionUtils.isNotEmpty(propertyDescriptors))
		{
			final Optional<PropertyDescriptor> availablePropertyDesc = propertyDescriptors.stream().filter(isAvailable())
					.findFirst();
			if (availablePropertyDesc.isPresent() && atpFormulaModel.getAvailability())
			{
				formulaString
						.append(getAtpFormulaVar2ArithmeticOperatorMap().get(availablePropertyDesc.get().getName().toLowerCase()))
						.append(StringUtils.capitalize(availablePropertyDesc.get().getName().toLowerCase()));
			}

			propertyDescriptors.stream().filter(isAvailable().negate()).forEach(
					formulaVarPropDescriptor -> interpretFormulaVariable(atpFormulaModel, formulaString, formulaVarPropDescriptor));
		}
	}

	/**
	 * Interpret the given formula variable and append it to the given {@link StringBuilder}
	 *
	 * @param atpFormulaModel
	 * 		the {@link AtpFormulaModel}
	 * @param formulaString
	 * 		the {@link StringBuilder}
	 * @param formulaVarPropDescriptor
	 * 		the {@link PropertyDescriptor}
	 */
	protected void interpretFormulaVariable(final AtpFormulaModel atpFormulaModel, final StringBuilder formulaString,
			final PropertyDescriptor formulaVarPropDescriptor)
	{
		try
		{
			final Boolean formulaVarValue = (Boolean) formulaVarPropDescriptor.getReadMethod().invoke(atpFormulaModel);
			if (formulaVarValue != null && formulaVarValue)
			{
				final String atpFormulaVar = formulaVarPropDescriptor.getName().toLowerCase();
				if (getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar) != null && operatorList
						.contains(getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar)))
				{
					formulaString.append(getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar))
							.append(StringUtils.capitalize(atpFormulaVar));
				}
				else
				{
					LOGGER.error("Failed to interpret the Arithmetic sign for ATP formula variable: [{}] -> [{}]. "
									+ "Please update your formula variable with appropriate sign (+ or -) in atpFormulaVar2ArithmeticOperatorMap",
							atpFormulaVar, getAtpFormulaVar2ArithmeticOperatorMap().get(atpFormulaVar));
				}
			}
		}
		catch (final IllegalAccessException | InvocationTargetException e)//NOSONAR
		{
			LOGGER.error("Failed to interpret the ATP formula. Please review your formula variable: {}", formulaVarPropDescriptor
					.getName());
		}
	}

	@Override
	public void set(final AtpFormulaModel atpFormulaModel, final String formulaString)
	{
		throw new UnsupportedOperationException();
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

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
package de.hybris.platform.chinesetaxinvoicebackoffice.renderers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;


/**
 * Contains common methods to the various nested attribute renderers.
 */
public class NestedAttributeUtils
{
	public static final String FIRST_TOKEN = "nestedObject";
	public static final String SECOND_TOKEN = "nestedProperty";
	public static final String GETTER = "get";

	/**
	 * Convert a string representing an attribute name to the name of a getter method.
	 */
	public String propertyNameToGetter(final String propertyName)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(GETTER);
		builder.append(WordUtils.capitalize(propertyName));
		return builder.toString();
	}

	/**
	 * Splits a qualifier for a nested attribute (ex. product.name) into a map to retrieve the two tokens.
	 */
	public Map<String, String> splitQualifier(final String qualifier) throws InvalidNestedAttributeException
	{
		final String[] tokenMap = qualifier.split("\\.");

		if (tokenMap.length != 2)
		{
			throw new InvalidNestedAttributeException("The qualifier " + qualifier + " is invalid for a nested attribute");
		}

		final Map<String, String> splitMap = new HashMap<>();
		splitMap.put(FIRST_TOKEN, tokenMap[0]);
		splitMap.put(SECOND_TOKEN, tokenMap[1]);

		return splitMap;
	}

	/**
	 * Retrieve the instance of an attribute of an object.
	 */
	public Object getNestedObject(final Object object, final String propertyName) throws IllegalAccessException,//NOSONAR
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,//NOSONAR
			InvalidNestedAttributeException //NOSONAR
	{
		final Class objectClass = object.getClass();

		final Method getter = objectClass.getMethod(propertyNameToGetter(propertyName));
		final Object result = getter.invoke(object, null);

		return result;
	}

	/**
	 * Obtain the name of the class that the object belongs to, but without the "Model" at the end.
	 */
	public String getNameOfClassWithoutModel(final Object object)
	{
		final String objectClass = object.getClass().getSimpleName();
		final String classWithoutModel = objectClass.substring(0, objectClass.length() - 5);
		return classWithoutModel;
	}
}

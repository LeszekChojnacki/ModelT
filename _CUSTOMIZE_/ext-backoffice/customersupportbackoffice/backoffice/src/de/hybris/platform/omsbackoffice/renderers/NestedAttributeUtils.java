/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.renderers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;


/**
 * Contains common methods to the various nested attribute renderers.
 */
public class NestedAttributeUtils
{
	public static final String GETTER = "get";
	public static final String FAIL_TO_FIND_COLLECTION_ITEM = "Failed to find collection item for property ";
	private static final Pattern COLLECTION_PATTERN = Pattern.compile("^([^\\[]+)\\[(\\d+)\\]$");
	private static final String END_BRACKET = "]";

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
	public List<String> splitQualifier(final String qualifier)
	{
		final String[] tokenMap = qualifier.split("\\.");
		return Arrays.asList(tokenMap);
	}

	/**
	 * Retrieve the instance of an attribute of an object.
	 */
	public Object getNestedObject(final Object object, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			InvalidNestedAttributeException
	{
		if (propertyName.endsWith(END_BRACKET))
		{
			return getArrayItem(object, propertyName);
		}
		else
		{
			return invokePropertyAsMethod(object, propertyName);
		}
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

	/**
	 * Retreives a collection item at a specified index.
	 *
	 * @param object
	 * 		Object containing the collection.
	 * @param propertyName
	 * 		Collection item with this format: collection[i]
	 * @return object at specified index.
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws InvalidNestedAttributeException
	 */
	public Object getArrayItem(final Object object, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InvalidNestedAttributeException //NOSONAR
	{
		final Matcher matcher = COLLECTION_PATTERN.matcher(propertyName);

		if (!matcher.matches() || matcher.groupCount() != 2)
		{
			throw new InvalidNestedAttributeException(FAIL_TO_FIND_COLLECTION_ITEM + propertyName);
		}
		final String collectionProperty = matcher.group(1);
		final int index = Integer.parseInt(matcher.group(2));

		final Object collection = invokePropertyAsMethod(object, collectionProperty);

		if (!(collection instanceof Collection))
		{
			throw new InvalidNestedAttributeException(FAIL_TO_FIND_COLLECTION_ITEM + propertyName);
		}

		int counter = 0;
		final Iterator iterator = ((Collection) collection).iterator();

		while (iterator.hasNext())
		{
			if (counter == index)
			{
				return iterator.next();
			}
			iterator.next();
			counter++;
		}

		throw new InvalidNestedAttributeException(FAIL_TO_FIND_COLLECTION_ITEM + propertyName);
	}

	/**
	 * Converts a property to a getter. Invokes it as a method on the passed object.
	 *
	 * @param object
	 * 		Object to invoke the method on.
	 * @param propertyName
	 * 		Property to invoke as method.
	 * @return Result of the method invocation
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public Object invokePropertyAsMethod(final Object object, final String propertyName)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		final Class objectClass = object.getClass();
		final Method getter = objectClass.getMethod(propertyNameToGetter(propertyName));
		return getter.invoke(object, null);
	}

}

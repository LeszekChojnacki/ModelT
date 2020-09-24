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
package com.hybris.backoffice.cockpitng.json;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.lang3.tuple.Pair;


/**
 * Register of {@link Converter}
 */
public interface ConverterRegistry
{

	/**
	 * Looks for target type that is assigned for provided.
	 *
	 * @param sourceClass
	 *           source object type
	 * @param <S>
	 *           type of source
	 * @param <T>
	 *           type of target
	 * @return target class for provided source or <code>null</code> if no converter was found for specified source
	 */
	<S, T> Class<T> getTargetClass(Class<? extends S> sourceClass);

	/**
	 * Looks for converter able to convert objects to provided type
	 *
	 * @param targetClass
	 *           target object type
	 * @param <S>
	 *           type of source
	 * @param <T>
	 *           type of target
	 * @return converter able to convert objects to provided type or <code>null</code> if no converter was found for
	 *         specified target
	 */
	<S, T> Converter<S, T> getConverterForTarget(Class<? extends T> targetClass);

	/**
	 * Looks for source type that is assigned for provided.
	 *
	 * @param targetClass
	 *           source object type
	 * @param <S>
	 *           type of source
	 * @param <T>
	 *           type of target
	 * @return source class for provided target or <code>null</code> if no converter was found for specified target
	 */
	<S, T> Class<S> getSourceClass(Class<? extends T> targetClass);

	/**
	 * Looks for converter able to convert objects from provided type
	 *
	 * @param sourceClass
	 *           source object type
	 * @param <S>
	 *           type of source
	 * @param <T>
	 *           type of target
	 * @return converter able to convert objects from provided type or <code>null</code> if no converter was found for
	 *         specified source
	 */
	<S, T> Converter<S, T> getConverterForSource(Class<? extends S> sourceClass);

	/**
	 * Gets source and target types of provided converter
	 *
	 * @param converter
	 *           converter to be checked
	 * @param <S>
	 *           type of source
	 * @param <T>
	 *           type of target
	 * @return &lt;source, target&gt; of provided converter; one, both or none side of result may be set
	 */
	<S, T> Pair<Class<S>, Class<T>> getConverterParameters(final Converter<S, T> converter);

}

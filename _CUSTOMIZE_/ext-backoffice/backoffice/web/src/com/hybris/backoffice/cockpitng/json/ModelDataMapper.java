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

import java.util.Map;

import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Maps platform model to/from DTO
 */
public interface ModelDataMapper
{

	/**
	 * Maps provided values with proper fields in provided target object
	 *
	 * @param widgetInstanceManager
	 *           widget instance manager for widget that requested mapping
	 * @param target
	 *           target object, which fields should be mapped
	 * @param values
	 *           values to map
	 * @param <T>
	 *           type of target model
	 */
	<T> void map(final WidgetInstanceManager widgetInstanceManager, final T target, final Map<String, Object> values);

	/**
	 * Maps provided object into its proper representation (DTO into platform model or another way round)
	 *
	 * @param widgetInstanceManager
	 *           widget instance manager for widget that requested mapping
	 * @param model
	 *           object to be mapped
	 * @param <S>
	 *           type of source object
	 * @param <T>
	 *           type of target object
	 * @return object with mapped values
	 */
	<S, T> T map(final WidgetInstanceManager widgetInstanceManager, final S model);

	/**
	 * Looks for source class for provided target type.
	 *
	 * @param widgetInstanceManager
	 *           widget instance manager for widget that requested mapping
	 * @param targetClass
	 *           mapping target class
	 * @param <S>
	 *           type of source object
	 * @param <T>
	 *           type of target source
	 * @return class that would be mapped into provided target class or <code>null</code>, if mapper is unable to map
	 *         anything into provided tagert class
	 */
	<S, T> Class<S> getSourceType(final WidgetInstanceManager widgetInstanceManager, final Class<? extends T> targetClass);

}

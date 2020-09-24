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
package com.hybris.backoffice.cockpitng.json.impl;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.AbstractItemModel;

import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.json.ConverterRegistry;
import com.hybris.backoffice.cockpitng.json.ModelDataMapper;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


/**
 * Mapper using converters to map platform model into DTO. If no converter is found, then simple reflection is used - a
 * setter method is searched for each property sent through socket.
 */
public class ConverterModelDataMapper implements ModelDataMapper
{
	// This is a workaround of https://issues.apache.org/jira/browse/BEANUTILS-454
	// Bug is fixed in 1.9.0
	static class NullableDateTimeConverter extends DateTimeConverter
	{

		private final Class defaultType;

		NullableDateTimeConverter(final Class defaultType)
		{
			this.defaultType = defaultType;
		}

		@Override
		public Object convert(final Class aClass, final Object o)
		{
			return o == null ? o : super.convert(aClass, o);
		}

		@Override
		protected Class getDefaultType()
		{
			return defaultType;
		}

	}

	static
	{
		BeanUtilsBean.getInstance().getConvertUtils().register(new NullableDateTimeConverter(Date.class), Date.class);
		BeanUtilsBean.getInstance().getConvertUtils().register(new NullableDateTimeConverter(java.sql.Date.class),
				java.sql.Date.class);
		BeanUtilsBean.getInstance().getConvertUtils().register(new NullableDateTimeConverter(Timestamp.class), Timestamp.class);
		BeanUtilsBean.getInstance().getConvertUtils().register(new NullableDateTimeConverter(Time.class), Time.class);
		BeanUtilsBean.getInstance().getConvertUtils().register(new NullableDateTimeConverter(Calendar.class), Calendar.class);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterModelDataMapper.class);

	private static final String CONVERTER_BEAN_PATTERN = "converter.%s";
	private static final String DTO_CLASS_PATTERN = "dto.%s";

	private ConverterRegistry converterRegistry;



	private TypeFacade typeFacade;

	@Override
	public <T> void map(final WidgetInstanceManager widgetInstanceManager, final T target, final Map<String, Object> values)
	{
		final DataType dataType = loadDataTypeIfPossible(target);

		values.forEach((name, value) -> {
			try
			{
				String normalizedPropertyName = name;
				boolean assignable = true;
				if (dataType != null)
				{
					final DataAttribute dataAttribute = dataType.getAttribute(name);
					if (dataAttribute != null)
					{
						normalizedPropertyName = dataAttribute.getQualifier();
						assignable = dataAttribute.isWritable() && dataAttribute.getValueType().isAtomic();
					}
				}
				if (assignable)
				{
					BeanUtils.setProperty(target, normalizedPropertyName, value);
				}

			}
			catch (final IllegalAccessException | InvocationTargetException | ConversionException e)
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(e.getLocalizedMessage(), e);
				}
			}
		});
	}

	private DataType loadDataTypeIfPossible(final Object target)
	{
		DataType dataType = null;
		if (target instanceof AbstractItemModel)
		{
			final String typeCode = typeFacade.getType(target);
			try
			{
				dataType = typeFacade.load(typeCode);
			}
			catch (final TypeNotFoundException e)
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(String.format("Cannot find data type for code %s", typeCode), e);
				}
			}
		}
		return dataType;
	}

	private <T> String getConverterBean(final WidgetInstanceManager widgetInstanceManager, final Class<? extends T> type)
	{
		final String settingKey = String.format(CONVERTER_BEAN_PATTERN, type.getName());
		if (widgetInstanceManager.getWidgetSettings().containsKey(settingKey))
		{
			return widgetInstanceManager.getWidgetSettings().getString(settingKey);
		}
		else if (type.getSuperclass() != null)
		{
			return getConverterBean(widgetInstanceManager, type.getSuperclass());
		}
		else if (!type.isInterface())
		{
			String result = null;
			final Class[] interfaces = type.getInterfaces();
			for (int i = 0; i < interfaces.length && result == null; i++)
			{
				result = getConverterBean(widgetInstanceManager, interfaces[i]);
			}
			return result;
		}
		else
		{
			return null;
		}
	}

	@Override
	public <S, T> T map(final WidgetInstanceManager widgetInstanceManager, final S model)
	{
		if (model == null)
		{
			return null;
		}
		else
		{
			final Converter<S, T> converter;
			final String converterBean = getConverterBean(widgetInstanceManager, model.getClass());
			if (!StringUtils.isEmpty(converterBean))
			{
				converter = BackofficeSpringUtil.getBean(converterBean, Converter.class);
			}
			else if (converterBean == null)
			{
				converter = getConverterRegistry().getConverterForSource((Class<? extends S>) model.getClass());
			}
			else
			{
				converter = null;
			}
			if (converter != null)
			{
				return converter.convert(model);
			}
			else
			{
				return (T) model;
			}
		}
	}


	@Override
	public <S, T> Class<S> getSourceType(final WidgetInstanceManager widgetInstanceManager, final Class<? extends T> targetClass)
	{
		Class<S> sourceClass = null;
		final String dtoClassName = widgetInstanceManager.getWidgetSettings()
				.getString(String.format(DTO_CLASS_PATTERN, targetClass.getName()));
		if (dtoClassName != null)
		{
			try
			{
				sourceClass = (Class<S>) Class.forName(dtoClassName);
			}
			catch (final ClassNotFoundException e)
			{
				LOGGER.error(e.getLocalizedMessage(), e);
			}
		}
		else
		{
			sourceClass = getConverterRegistry().getSourceClass(targetClass);
		}
		if (sourceClass != null)
		{
			return sourceClass;
		}
		else
		{
			return null;
		}
	}

	protected ConverterRegistry getConverterRegistry()
	{
		return converterRegistry;
	}

	@Required
	public void setConverterRegistry(final ConverterRegistry converterRegistry)
	{
		this.converterRegistry = converterRegistry;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}
}

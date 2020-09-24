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

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Listcell;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;


/**
 * Proxy for DefaultListCellRenderer. It checks if a nested attribute has a null parent before rendering. If null, it
 * renders nothing. If not null, it delegates to the CockpitNG default renderer.
 */
public class NullSafeListCellRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(NullSafeListCellRenderer.class);

	private WidgetComponentRenderer<Listcell, ListColumn, Object> defaultListCellRenderer;
	private NestedAttributeUtils nestedAttributeUtils;

	@Override
	public void render(final Listcell parent, final ListColumn columnConfiguration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		List<String> tokenMap;
		final String qualifier = columnConfiguration.getQualifier();

		Object nestedObject = object;
		Object targetField = object;

		try
		{
			tokenMap = getNestedAttributeUtils().splitQualifier(qualifier);
			// get the last parent nested attribute to check if it is null.
			for (int i = 0; i < tokenMap.size() - 1; i++)
			{
				nestedObject = getNestedAttributeUtils().getNestedObject(nestedObject, tokenMap.get(i));
			}

			// get the field to check if it is null.
			for (final String aTokenMap : tokenMap)
			{
				targetField = getNestedAttributeUtils().getNestedObject(targetField, aTokenMap);
			}

			if (nestedObject == null || targetField == null || checkIfObjectIsEmptyCollection(targetField))
			{
				LOG.info(String.format("Either Property %s is null or the field %s is null, skipping render of %s", nestedObject,
						qualifier, qualifier));
			}
			else
			{
				getDefaultListCellRenderer().render(parent, columnConfiguration, object, dataType, widgetInstanceManager);
			}
		}
		catch (final InvalidNestedAttributeException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	protected WidgetComponentRenderer<Listcell, ListColumn, Object> getDefaultListCellRenderer()
	{
		return defaultListCellRenderer;
	}

	@Required
	public void setDefaultListCellRenderer(final WidgetComponentRenderer<Listcell, ListColumn, Object> defaultListCellRenderer)
	{
		this.defaultListCellRenderer = defaultListCellRenderer;
	}

	/**
	 * Identify if the object is a collection type and it's empty or null
	 *
	 * @param object
	 * 		the object to be verified
	 * @return true if the object is a collection and it's empty
	 */
	protected boolean checkIfObjectIsEmptyCollection(final Object object)
	{
		return object instanceof Collection<?> && CollectionUtils.isEmpty((Collection) object);
	}

	protected NestedAttributeUtils getNestedAttributeUtils()
	{
		return nestedAttributeUtils;
	}

	@Required
	public void setNestedAttributeUtils(final NestedAttributeUtils nestedAttributeUtils)
	{
		this.nestedAttributeUtils = nestedAttributeUtils;
	}
}

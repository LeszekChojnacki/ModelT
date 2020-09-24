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
package de.hybris.platform.solrfacetsearchbackoffice.extendedreferenceeditor.renderers;

import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public class MultilineCellRenderer extends AbstractWidgetComponentRenderer<Listcell, ListColumn, Object>
{

	private PropertyValueService propertyValueService;
	private String qualifier;

	@Override
	public void render(final Listcell listcell, final ListColumn configuration, final Object o, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Label label = new Label();
		final Object value = getPropertyValueService().readValue(o, getQualifier());
		label.setValue(value.toString());
		label.setMultiline(true);
		listcell.appendChild(label);
	}

	public PropertyValueService getPropertyValueService()
	{
		return propertyValueService;
	}

	public void setPropertyValueService(final PropertyValueService propertyValueService)
	{
		this.propertyValueService = propertyValueService;
	}

	public String getQualifier()
	{
		return qualifier;
	}

	public void setQualifier(final String qualifier)
	{
		this.qualifier = qualifier;
	}

}

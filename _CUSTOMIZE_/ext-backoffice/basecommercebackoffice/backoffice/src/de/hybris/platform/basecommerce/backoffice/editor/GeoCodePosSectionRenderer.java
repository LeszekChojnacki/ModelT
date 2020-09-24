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
package de.hybris.platform.basecommerce.backoffice.editor;


import de.hybris.platform.basecommerce.backoffice.constants.BasecommercebackofficeConstants.NotificationSource;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent.Level;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEventTypes;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaSectionRenderer;


public class GeoCodePosSectionRenderer extends DefaultEditorAreaSectionRenderer
{
	private static final String GEOCODE_BUTTON_LABEL = "hmc.geocode";
	private GeoWebServiceWrapper geoServiceWrapper;
	private NotificationService notificationService;

	@Override
	public void render(final Component parent, final AbstractSection abstractSectionConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final Div cnt = new Div();
		final Button button = new Button(Labels.getLabel(GEOCODE_BUTTON_LABEL));
		final PointOfServiceModel pos = widgetInstanceManager.getModel().getValue("currentObject", PointOfServiceModel.class);

		if (hasUserPermissionToWrite(pos))
		{
			button.setDisabled(true);
		}

		final AddressData address = new AddressData(pos.getAddress());

		button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event)
			{
				final boolean geocodingResult = geoCodeAddress(address, pos, widgetInstanceManager);
				getNotificationService().notifyUser(NotificationSource.GEOCODE_MESSAGE_SOURCE,
						NotificationEventTypes.EVENT_TYPE_GENERAL, geocodingResult ? Level.SUCCESS : Level.FAILURE);
			}
		});

		final int columns = 2;
		final String width = calculateWidthPercentage(columns);
		final List<Attribute> attributes = getAttributes();
		final ProxyRenderer proxyRenderer = new ProxyRenderer(this, parent, abstractSectionConfiguration, object);
		renderAttributes(attributes, proxyRenderer, columns, width, dataType, widgetInstanceManager, object);
		parent.appendChild(cnt);
		cnt.appendChild(button);
	}

	protected boolean geoCodeAddress(final AddressData address, final PointOfServiceModel pos,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (address == null || pos == null || widgetInstanceManager == null)
		{
			return false;
		}

		final GPS gps = geoServiceWrapper.geocodeAddress(address);

		if (gps != null)
		{
			final Date geocodeTimestamp = new Date();
			updatePointOfService(gps, geocodeTimestamp, pos);
			desynchronizeView(gps, geocodeTimestamp, widgetInstanceManager);

			return true;
		}
		else
		{
			return false;
		}
	}

	protected void desynchronizeView(final GPS gps, final Date geocodeTimestamp, final WidgetInstanceManager widgetInstanceManager)
	{
		if (widgetInstanceManager != null && widgetInstanceManager.getModel() != null && gps != null)
		{
			widgetInstanceManager.getModel().setValue("currentObject.latitude", Double.valueOf(gps.getDecimalLatitude()));
			widgetInstanceManager.getModel().setValue("currentObject.longitude", Double.valueOf(gps.getDecimalLongitude()));
			widgetInstanceManager.getModel().setValue("currentObject.geocodeTimestamp", geocodeTimestamp);
		}
	}

	protected void updatePointOfService(final GPS gps, final Date geocodeTimestamp, final PointOfServiceModel pos)
	{
		if (gps != null && pos != null)
		{
			pos.setLatitude(Double.valueOf(gps.getDecimalLatitude()));
			pos.setLongitude(Double.valueOf(gps.getDecimalLongitude()));
			pos.setGeocodeTimestamp(geocodeTimestamp);
		}
	}

	protected boolean hasUserPermissionToWrite(final PointOfServiceModel pos)
	{
		return !getPermissionFacade().canChangeInstanceProperty(pos, "latitude")
				|| !getPermissionFacade().canChangeInstanceProperty(pos, "longitude")
				|| !getPermissionFacade().canChangeInstanceProperty(pos, "geocodeTimestamp");
	}

	protected List<Attribute> getAttributes()
	{
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(createAttribute("latitude"));
		attributes.add(createAttribute("longitude"));
		attributes.add(createAttribute("geocodeTimestamp"));
		return attributes;
	}

	private Attribute createAttribute(final String qualifier)
	{
		final Attribute attribute = new Attribute();
		attribute.setQualifier(qualifier);
		return attribute;
	}

	@Required
	public void setGeoServiceWrapper(final GeoWebServiceWrapper geoServiceWrapper)
	{
		this.geoServiceWrapper = geoServiceWrapper;
	}

	protected GeoWebServiceWrapper getGeoServiceWrapper()
	{
		return this.geoServiceWrapper;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}

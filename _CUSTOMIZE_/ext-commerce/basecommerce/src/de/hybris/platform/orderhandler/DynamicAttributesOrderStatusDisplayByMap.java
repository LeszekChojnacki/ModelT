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
package de.hybris.platform.orderhandler;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.util.localization.Localization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * <p>
 * This dynamic attribute handler for Order type maps the order status with the localized value such as defined by the
 * map put in 'statusDisplayMap' parameter of the handler Spring bean.
 *
 * <pre>
 * i.e.:
 * &lt;property name="statusDisplayMap">
 * 	&lt;map>
 * 		&lt;entry key="CANCELLED" value="orderStatusDisplay.CANCELLED"/>
 * 		&lt;entry key="CANCELLING" value="orderStatusDisplay.CANCELLING"/>
 * 		&lt;entry key="COMPLETED" value="orderStatusDisplay.COMPLETED"/>
 * 		&lt;entry key="CREATED" value="orderStatusDisplay.CREATED"/>
 * 		&lt;entry key="ON_VALIDATION" value="orderStatusDisplay.ON_VALIDATION"/>
 * 	&lt;/map>
 * &lt;/property>
 * </pre>
 *
 *
 */
public class DynamicAttributesOrderStatusDisplayByMap implements DynamicAttributeHandler<String, OrderModel>
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DynamicAttributesOrderStatusDisplayByMap.class);

	private Map<String, String> statusDisplayMap = new HashMap<String, String>();
	private String defaultStatus;

	public Map<String, String> getStatusDisplayMap()
	{
		return (statusDisplayMap == null) ? Collections.<String, String> emptyMap() : statusDisplayMap;
	}

	public void setStatusDisplayMap(final Map<String, String> statusDisplayMap)
	{
		this.statusDisplayMap = statusDisplayMap;
	}

	public String getDefaultStatus()
	{
		return defaultStatus;
	}

	public void setDefaultStatus(final String defaultStatus)
	{
		this.defaultStatus = defaultStatus;
	}

	@Override
	public String get(final OrderModel order)
	{
		String statusLocalisationKey = getDefaultStatus();

		if (order != null && order.getStatus() != null && order.getStatus().getCode() != null)
		{
			final String statusCode = order.getStatus().getCode();
			final String statusDisplayEntry = getStatusDisplayMap().get(statusCode);
			if (statusDisplayEntry != null)
			{
				statusLocalisationKey = statusDisplayEntry;
			}
		}

		if (statusLocalisationKey == null || statusLocalisationKey.isEmpty())
		{
			return "";
		}
		return Localization.getLocalizedString(statusLocalisationKey);
	}

	@Override
	public void set(final OrderModel model, final String value)
	{
		throw new UnsupportedOperationException();
	}
}

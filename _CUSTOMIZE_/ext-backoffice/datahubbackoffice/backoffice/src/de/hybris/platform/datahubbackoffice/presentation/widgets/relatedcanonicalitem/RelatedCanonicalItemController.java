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
package de.hybris.platform.datahubbackoffice.presentation.widgets.relatedcanonicalitem;

import de.hybris.platform.datahubbackoffice.dataaccess.search.strategy.DatahubCanonicalObjectFacadeStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.item.ItemData;

public class RelatedCanonicalItemController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(RelatedCanonicalItemController.class);

	public static final String SOCKET_IN_ERROR = "error";
	public static final String SOCKET_OUT_CANONICAL_ITEM = "canonicalItem";

	@WireVariable("canonicalItemObjectFacadeStrategy")
	protected transient DatahubCanonicalObjectFacadeStrategy objectFacadeStrategy;

	@SocketEvent(socketId = SOCKET_IN_ERROR)
	public void getRelatedCanonicalItem(final ErrorData errorData)
	{
		try
		{
			final Long itemId = errorData != null ? errorData.getCanonicalItemId() : null;
			if (itemId != null)
			{
				final ItemData canonicalItem = objectFacadeStrategy.load(itemId);
				sendOutput(SOCKET_OUT_CANONICAL_ITEM, canonicalItem);
			}
		}
		catch (final ObjectNotFoundException e)
		{
			LOG.warn("Cannot load canonical item for {}", errorData, e);
		}
	}
}

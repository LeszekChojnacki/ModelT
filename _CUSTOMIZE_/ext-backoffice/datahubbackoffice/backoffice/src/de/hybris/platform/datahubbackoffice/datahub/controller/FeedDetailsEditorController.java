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
package de.hybris.platform.datahubbackoffice.datahub.controller;

import de.hybris.platform.datahubbackoffice.dataaccess.feed.FeedObjectFacadeStrategy;

import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.feed.FeedData;

public class FeedDetailsEditorController extends DefaultWidgetController
{
	@WireVariable("feedObjectFacadeStrategy")
	protected transient FeedObjectFacadeStrategy objectFacadeStrategy;

	private Label nameLabel;
	private Label descriptionLabel;

	@SocketEvent(socketId = "feed")
	public void getDetailsForGivenFeed(final FeedData feedData)
	{
		final FeedData feedDetails = objectFacadeStrategy.load(feedData.getName(), null);
		nameLabel.setValue(feedDetails.getName());
		descriptionLabel.setValue(feedDetails.getDescription());
	}

	public void setFieldSearchFacade(final FeedObjectFacadeStrategy objectFacadeStrategy)
	{
		this.objectFacadeStrategy = objectFacadeStrategy;
	}
}

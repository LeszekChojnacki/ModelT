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
package de.hybris.platform.datahubbackoffice.dataaccess.feed;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.datahub.client.DataFeedClient;
import com.hybris.datahub.dto.feed.FeedData;

public class FeedObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final String DATAHUB_FEED_TYPECODE = "Datahub_Feed";
	private static final String FEED_CREATION_ERROR = "datahub.feed.creation.error";

	private DataFeedClient feedClient;

	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return StringUtils.equals(ObjectUtils.toString(objectOrCode), DATAHUB_FEED_TYPECODE) || objectOrCode instanceof FeedData;
	}

	@Override
	public FeedData load(final String feedName, final Context context)
	{
		return feedClient.getDataFeed(feedName);
	}

	@Override
	public <T> void delete(final T objectOrCode, final Context context)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public FeedData create(final String code, final Context context)
	{
		return new FeedData();
	}

	@Override
	public <T> T save(final T t, final Context context) throws ObjectSavingException
	{
		try
		{
			final FeedData spec = (FeedData) t;
			feedClient.createDataFeed(spec.getName(), spec.getPoolingCondition(), spec.getPoolingStrategy(), spec.getDescription());
		}
		catch (final Exception e)
		{
			throw new ObjectSavingException(FEED_CREATION_ERROR, e.getMessage(), e);
		}
		return t;
	}

	@Override
	public <T> T reload(final T t, final Context context)
	{
		final FeedData feedData = ((FeedData) t);
		return (T) load(ObjectUtils.toString(feedData.getName()), context);
	}

	@Required
	public void setFeedClient(final DataFeedClient feedClient)
	{
		this.feedClient = feedClient;
	}
}

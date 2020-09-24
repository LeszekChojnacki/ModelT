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
package de.hybris.platform.datahubbackoffice.dataaccess.dataloading;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.dataloading.DataLoadingActionData;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

public class DataLoadingActionTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String FEED_NAME_ATTR = "feed.name";
	private static final String START_TIME_ATTR = "startTime";
	private static final String END_TIME_ATTR = "endTime";
	private static final String ID_ATTR = "actionId";
	private static final String ITEMS_COUNT_ATTR = "count";
	private static final String STATUS_ATTR = "status";

	private static final DataType LONG_DATA_TYPE = new DataType.Builder("java.lang.Long").clazz(Long.class).build();
	private static final DataType STRING_DATA_TYPE = new DataType.Builder("java.lang.String").clazz(String.class).build();
	private static final DataType DATE_DATA_TYPE = new DataType.Builder("java.util.Date").clazz(Date.class).build();

	@Override
	public boolean canHandle(final String typeCode)
	{
		return StringUtils.equals(typeCode, DataLoadingActionConstants.DATA_LOADING_ACTIONS_IN_ERROR_TYPE_CODE)
				|| StringUtils.equals(typeCode, DataLoadingActionData.class.getName());
	}

	@Override
	public DataType load(final String qualifier) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder(
				DataLoadingActionConstants.DATA_LOADING_ACTIONS_IN_ERROR_TYPE_CODE);

		dataTypeBuilder.clazz(DataLoadingActionData.class).searchable(true).label(Locale.ENGLISH, "Data Loading Action Failures");

		final DataAttribute.Builder startTimeAttrBuilder = new DataAttribute.Builder(START_TIME_ATTR);
		startTimeAttrBuilder.label(Locale.ENGLISH, START_TIME_ATTR)
							.searchable(true)
							.writable(false)
							.valueType(DATE_DATA_TYPE);

		final DataAttribute.Builder endTimeAttrBuilder = new DataAttribute.Builder(END_TIME_ATTR);
		endTimeAttrBuilder.label(Locale.ENGLISH, END_TIME_ATTR)
						  .searchable(true)
						  .writable(false)
						  .valueType(DATE_DATA_TYPE);

		final DataAttribute.Builder idAttrBuilder = new DataAttribute.Builder(ID_ATTR);
		idAttrBuilder.label(Locale.ENGLISH, ID_ATTR)
					 .searchable(false)
					 .writable(false)
					 .valueType(LONG_DATA_TYPE);

		final DataAttribute.Builder itemCountAttrBuilder = new DataAttribute.Builder(ITEMS_COUNT_ATTR);
		itemCountAttrBuilder.label(Locale.ENGLISH, ITEMS_COUNT_ATTR)
							.searchable(false)
							.writable(false)
							.valueType(LONG_DATA_TYPE);

		final DataAttribute.Builder feedNameAttrBuilder = new DataAttribute.Builder(FEED_NAME_ATTR);
		feedNameAttrBuilder.label(Locale.ENGLISH, FEED_NAME_ATTR)
						   .writable(false)
						   .searchable(true)
						   .valueType(STRING_DATA_TYPE);

		final DataAttribute.Builder statusAttrBuilder = new DataAttribute.Builder(STATUS_ATTR);
		statusAttrBuilder.label(Locale.ENGLISH, STATUS_ATTR)
						 .writable(false)
						 .searchable(true)
						 .valueType(STRING_DATA_TYPE);

		dataTypeBuilder.attribute(startTimeAttrBuilder.build())
					   .attribute(endTimeAttrBuilder.build())
					   .attribute(idAttrBuilder.build())
					   .attribute(itemCountAttrBuilder.build())
					   .attribute(feedNameAttrBuilder.build())
					   .attribute(statusAttrBuilder.build());

		return dataTypeBuilder.build();
	}

	@Override
	public DataType load(final String qualifier, final Context ctx) throws TypeNotFoundException
	{
		return load(qualifier);
	}

	@Override
	public String getType(final Object object)
	{
		String ret = StringUtils.EMPTY;
		if (object instanceof DataLoadingActionData)
		{
			ret = DataLoadingActionConstants.DATA_LOADING_ACTIONS_IN_ERROR_TYPE_CODE;
		}

		return ret;
	}

	@Override
	public String getAttributeDescription(final String type, final String attribute)
	{
		return null;
	}

	@Override
	public String getAttributeDescription(final String type, final String attribute, final Locale locale)
	{
		return null;
	}
}

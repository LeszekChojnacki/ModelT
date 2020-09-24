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
package de.hybris.platform.datahubbackoffice.dataaccess.publication;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.metadata.TargetSystemData;
import com.hybris.datahub.dto.pool.PoolData;
import com.hybris.datahub.dto.publication.TargetSystemPublicationData;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;

public class PublicationsInErrorTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_PUBLICATION_TYPECODE = PublicationConstants.PUBLICATIONS_IN_ERROR_TYPE_CODE;
	private static final String TARGET_SYSTEM_ATTR = "targetSystem";
	private static final String POOL_NAME_ATTR = "pool";
	private static final String START_TIME_ATTR = "startTime";
	private static final String END_TIME_ATTR = "endTime";
	private static final String ID_ATTR = "publicationId";
	private static final String ITEMS_IN_ERROR_ATTR = "itemsInError";
	private static final String STATUS_ATTR = "status";

	private static final DataType LONG_DATA_TYPE = new DataType.Builder("java.lang.Long").clazz(Long.class).build();
	private static final DataType STRING_DATA_TYPE = new DataType.Builder("java.lang.String").clazz(String.class).build();
	private static final DataType DATE_DATA_TYPE = new DataType.Builder("java.util.Date").clazz(Date.class).build();

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.equals(code, DATAHUB_PUBLICATION_TYPECODE)
				|| StringUtils.equals(code, TargetSystemPublicationData.class.getName());
	}

	@Override
	public DataType load(final String code) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder((DATAHUB_PUBLICATION_TYPECODE));

		dataTypeBuilder.clazz(TargetSystemPublicationData.class)
					   .searchable(true)
					   .label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.aggregateTitle", "Publication Failures and Errors"));

		final DataAttribute.Builder startTimeAttrBuilder = new DataAttribute.Builder(START_TIME_ATTR);
		startTimeAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.startTime"))
							.searchable(true)
							.writable(false)
							.valueType(DATE_DATA_TYPE);

		final DataAttribute.Builder endTimeAttrBuilder = new DataAttribute.Builder(END_TIME_ATTR);
		endTimeAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.endTime"))
						  .searchable(true)
						  .writable(false)
						  .valueType(DATE_DATA_TYPE);

		final DataAttribute.Builder idAttrBuilder = new DataAttribute.Builder(ID_ATTR);
		idAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.publicationId"))
					 .searchable(false)
					 .writable(false)
					 .valueType(LONG_DATA_TYPE);

		final DataAttribute.Builder itemsInErrorAttrBuilder = new DataAttribute.Builder(ITEMS_IN_ERROR_ATTR);
		itemsInErrorAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.itemsInError"))
							   .searchable(false).writable(false)
							   .valueType(LONG_DATA_TYPE);

		final DataType.Builder targetSystemType = new DataType.Builder("Datahub_TargetSystem").clazz(TargetSystemData.class);
		final DataAttribute.Builder targetSystemAttr = new DataAttribute.Builder(TARGET_SYSTEM_ATTR);
		targetSystemAttr.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.targetSystem"))
						.writable(false)
						.searchable(true)
						.valueType(targetSystemType.build());

		final DataType.Builder poolType = new DataType.Builder("Datahub_Pool").clazz(PoolData.class);
		final DataAttribute.Builder attributePool = new DataAttribute.Builder(POOL_NAME_ATTR);
		attributePool.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.pool"))
					 .writable(false)
					 .searchable(true)
					 .valueType(poolType.build());

		final DataAttribute.Builder statusAttribute = new DataAttribute.Builder(STATUS_ATTR);
		statusAttribute.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.status"))
					   .writable(false)
					   .searchable(true)
					   .valueType(STRING_DATA_TYPE);

		dataTypeBuilder.attribute(startTimeAttrBuilder.build())
					   .attribute(endTimeAttrBuilder.build())
					   .attribute(idAttrBuilder.build())
					   .attribute(itemsInErrorAttrBuilder.build())
					   .attribute(attributePool.build())
					   .attribute(targetSystemAttr.build())
					   .attribute(statusAttribute.build());

		return dataTypeBuilder.build();
	}

	@Override
	public DataType load(final String code, final Context context) throws TypeNotFoundException
	{
		return load(code);
	}

	@Override
	public String getType(final Object entity)
	{
		String ret = StringUtils.EMPTY;
		if (entity instanceof TargetSystemPublicationData)
		{

			ret = DATAHUB_PUBLICATION_TYPECODE;
		}

		return ret;
	}

	@Override
	public String getAttributeDescription(final String s, final String s2)
	{
		return null;
	}

	@Override
	public String getAttributeDescription(final String s, final String s2, final Locale locale)
	{
		return null;
	}
}

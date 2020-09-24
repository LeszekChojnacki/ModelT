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

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.feed.FeedData;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;

public class FeedTypeFacadeStrategy implements TypeFacadeStrategy
{
	private static final String DATAHUB_FEED_TYPECODE = "Datahub_Feed";
	private static final String FEED_ID_ATTR = "feedId";
	public static final String NAME_ATTR = "name";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String POOLING_STRATEGY_ATTR = "poolingStrategy";
	private static final String POOLING_CONDITION_ATTR = "poolingCondition";
	private static final String DEFAULT_COMPOSITION_STRATEGY_ATTR = "defaultCompositionStrategy";
	private static final String DEFAULT_PUBLICATION_STRATEGY_ATTR = "defaultPublicationStrategy";

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.equals(code, DATAHUB_FEED_TYPECODE) || StringUtils.equals(code, FeedData.class.getName());
	}

	@Override
	public DataType load(final String typeCode) throws TypeNotFoundException
	{
		final DataType.Builder dataTypeBuilder = new DataType.Builder(DATAHUB_FEED_TYPECODE);

		dataTypeBuilder.clazz(FeedData.class).searchable(true).label(Locale.ENGLISH, "Feeds");

		final DataAttribute.Builder feedIdAttrBuilder = new DataAttribute.Builder(FEED_ID_ATTR);
		feedIdAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.feedId"))
						 .description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.feedId"))
						 .searchable(true)
						 .writable(true).valueType(DataType.LONG);

		final DataAttribute.Builder nameAttrBuilder = new DataAttribute.Builder(NAME_ATTR);
		nameAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.name"))
					   .description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.name"))
					   .searchable(true)
					   .writable(true)
					   .valueType(DataType.STRING);

		final DataAttribute.Builder descriptionAttrBuilder = new DataAttribute.Builder(DESCRIPTION_ATTR);
		descriptionAttrBuilder.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.description"))
							  .description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.description"))
							  .searchable(true)
							  .writable(true)
							  .valueType(DataType.STRING);

		final DataAttribute.Builder poolingStrategyAttr = new DataAttribute.Builder(POOLING_STRATEGY_ATTR);
		poolingStrategyAttr.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.poolingStrategy"))
						   .description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.poolingStrategy"))
						   .searchable(true)
						   .writable(true)
						   .valueType(DataType.STRING);

		final DataAttribute.Builder poolingConditionAttr = new DataAttribute.Builder(POOLING_CONDITION_ATTR);
		poolingConditionAttr.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.poolingCondition"))
							.description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.poolingCondition"))
							.searchable(true)
							.writable(true)
							.valueType(DataType.STRING);

		final DataAttribute.Builder defaultCompositionStrategyAttr = new DataAttribute.Builder(DEFAULT_COMPOSITION_STRATEGY_ATTR);
		defaultCompositionStrategyAttr.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.defaultCompositionStrategy"))
									  .searchable(true)
									  .writable(false)
									  .valueType(DataType.STRING);

		final DataAttribute.Builder defaultPublicationStrategyAttr = new DataAttribute.Builder(DEFAULT_PUBLICATION_STRATEGY_ATTR);
		defaultPublicationStrategyAttr.label(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.defaultPublicationStrategy"))
									  .description(Locale.ENGLISH, Labels.getLabel("datahub.attribute.feed.defaultPublicationStrategy"))
									  .searchable(true)
									  .writable(false)
									  .valueType(DataType.STRING);

		dataTypeBuilder.attribute(feedIdAttrBuilder.build())
				.attribute(nameAttrBuilder.build())
				.attribute(descriptionAttrBuilder.build())
				.attribute(poolingStrategyAttr.build())
				.attribute(poolingConditionAttr.build())
				.attribute(defaultCompositionStrategyAttr.build())
				.attribute(defaultPublicationStrategyAttr.build());
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
		if (entity instanceof FeedData)
		{
			ret = DATAHUB_FEED_TYPECODE;
		}
		return ret;
	}

	@Override
	public String getAttributeDescription(final String typeCode, final String qualifier)
	{
		return qualifier;
	}

	@Override
	public String getAttributeDescription(final String typeCode, final String qualifier, final Locale locale)
	{
		return qualifier;
	}
}

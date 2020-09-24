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
package com.hybris.backoffice.solrsearch.resolvers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.QualifierProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.NoOpQualifierProvider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.hybris.backoffice.solrsearch.populators.conditionvalueconverters.DateConditionValueConverter;


@IntegrationTest
public class DateValueResolverTest extends ServicelayerTransactionalTest
{

	private static final String SOLR_PROPERTY_NAME_CREATION_TIME_DATE = "creationTime_date";
	private static final String SOLR_PROPERTY_NAME_ONLINE_DATE_DATE = "onlineDate_date";
	private static final String INDEXED_PROPERTY_NAME_CREATION_TIME = "creationTime";
	private static final String INDEXED_PROPERTY_NAME_ONLINE_DATE = "onlineDate";
	private static final String INDEXED_PROPERTY_TYPE_NAME_DATE = "date";
	private final DateValueResolver dateValueResolver = new DateValueResolver();

	private final DateConditionValueConverter dateConditionValueConverter = new DateConditionValueConverter();
	private QualifierProvider qualifierProvider = new NoOpQualifierProvider();

	@Resource
	private SessionService sessionService;

	@Resource
	private ModelService modelService;


	@Before
	public void setUp()
	{
		dateValueResolver.setSessionService(sessionService);
		dateValueResolver.setModelService(modelService);
		dateValueResolver.setQualifierProvider(qualifierProvider);
	}

	@Test
	public void shouldDateConditionValueConverterBeConsistentWithDateValueResolver() throws FieldValueProviderException
	{
		//given
		final Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.MARCH, 2, 10, 11, 12);

		for (final String timeZoneID : TimeZone.getAvailableIDs())
		{
			calendar.setTimeZone(TimeZone.getTimeZone(timeZoneID));
			final Date dateToTest = calendar.getTime();

			//when
			final InputDocument document = mock(InputDocument.class);
			final IndexerBatchContext batchContext = mock(IndexerBatchContext.class);

			final IndexedProperty creationTimeIndexedProperty = new IndexedProperty();
			creationTimeIndexedProperty.setName(INDEXED_PROPERTY_NAME_CREATION_TIME);
			creationTimeIndexedProperty.setType(INDEXED_PROPERTY_TYPE_NAME_DATE);
			creationTimeIndexedProperty.setCurrency(false);

			final IndexedProperty onlineDateIndexedProperty = new IndexedProperty();
			onlineDateIndexedProperty.setName(INDEXED_PROPERTY_NAME_ONLINE_DATE);
			onlineDateIndexedProperty.setType(INDEXED_PROPERTY_TYPE_NAME_DATE);
			onlineDateIndexedProperty.setLocalized(false);
			onlineDateIndexedProperty.setCurrency(false);

			final ProductModel productModel = new ProductModel();
			productModel.setCreationtime(dateToTest);
			productModel.setOnlineDate(dateToTest);

			dateValueResolver.resolve(document, batchContext, Arrays.asList(creationTimeIndexedProperty, onlineDateIndexedProperty),
					productModel);

			//then
			final ArgumentCaptor<String> captorCreationTime = ArgumentCaptor.forClass(String.class);
			verify(document, times(1)).addField(eq(SOLR_PROPERTY_NAME_CREATION_TIME_DATE), captorCreationTime.capture());
			assertThat(captorCreationTime.getValue()).isEqualTo(dateConditionValueConverter.apply(dateToTest));

			final ArgumentCaptor<String> captorOnlineDate = ArgumentCaptor.forClass(String.class);
			verify(document, times(1)).addField(eq(SOLR_PROPERTY_NAME_ONLINE_DATE_DATE), captorOnlineDate.capture());
			assertThat(captorOnlineDate.getValue()).isEqualTo(dateConditionValueConverter.apply(dateToTest));
		}
	}

}

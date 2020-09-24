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
package com.hybris.backoffice.solrsearch.setup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.hybris.backoffice.solrsearch.events.AfterInitializationEndBackofficeSearchListener;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


@RunWith(MockitoJUnitRunner.class)
public class BackofficeSolrSearchIndexInitializerTest
{

	@InjectMocks
	private final BackofficeSolrSearchIndexInitializer initializer = new BackofficeSolrSearchIndexInitializer()
	{

		@Override
		protected boolean shouldInitializeIndexes()
		{
			return isIndexAutoinitEnabled();
		}

	};

	@Mock
	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;
	@Mock
	private SolrIndexService solrIndexService;
	@Mock
	private IndexerService indexerService;
	@Mock
	private AfterInitializationEndBackofficeSearchListener afterInitializationEndBackofficeListener;

	@Mock
	private FacetSearchConfig searchConfig;
	@Mock
	private IndexConfig indexConfig;
	@Mock
	private IndexedType indexedType;

	private boolean indexAutoinitEnabled;

	private static final String INDEXED_TYPE_IDENTIFIER = "INDEXED_TYPE";
	private static final String SEARCH_CONFIG_NAME = "SEARCH_CONFIG";


	private boolean isIndexAutoinitEnabled()
	{
		return indexAutoinitEnabled;
	}

	@Before
	public void setUp()
	{
		when(indexedType.getIdentifier()).thenReturn(INDEXED_TYPE_IDENTIFIER);
		when(searchConfig.getName()).thenReturn(SEARCH_CONFIG_NAME);
	}

	@Test
	public void shouldInitializeIndexIfNotYetInitialized() throws SolrServiceException, IndexerException
	{
		// given
		indexAutoinitEnabled = true;
		when(backofficeFacetSearchConfigService.getAllMappedFacetSearchConfigs()).thenReturn(
				Collections.singletonList(searchConfig));
		when(searchConfig.getIndexConfig()).thenReturn(indexConfig);
		when(indexConfig.getIndexedTypes()).thenReturn(Collections.singletonMap(INDEXED_TYPE_IDENTIFIER, indexedType));
		when(solrIndexService.getActiveIndex(SEARCH_CONFIG_NAME, indexedType.getIdentifier()))
				.thenThrow(new SolrServiceException());

		// when
		initializer.initialize();

		// then
		verify(indexerService).performFullIndex(searchConfig);
	}

	@Test
	public void shouldNotInitializeIndexIfAlreadyInitialized() throws SolrServiceException
	{
		// given
		indexAutoinitEnabled = true;
		when(backofficeFacetSearchConfigService.getAllMappedFacetSearchConfigs()).thenReturn(
				Collections.singletonList(searchConfig));
		when(searchConfig.getIndexConfig()).thenReturn(indexConfig);
		when(indexConfig.getIndexedTypes()).thenReturn(Collections.singletonMap(INDEXED_TYPE_IDENTIFIER, indexedType));
		when(solrIndexService.getActiveIndex(SEARCH_CONFIG_NAME, indexedType.getIdentifier())).thenReturn(
				mock(SolrIndexModel.class));

		// when
		initializer.initialize();

		// then
		verifyZeroInteractions(indexerService);
	}

	@Test
	public void shouldNotInitializeWhenDisabled()
	{
		// given
		indexAutoinitEnabled = false;

		// when
		initializer.initialize();

		// then
		verifyZeroInteractions(backofficeFacetSearchConfigService);
	}

}

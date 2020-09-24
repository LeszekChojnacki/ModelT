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
package de.hybris.platform.solrfacetsearch.solr.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.DELETE;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.GET;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SolrClientConfig;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrStopWordModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrSynonymConfigModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.IndexNameResolver;
import de.hybris.platform.solrfacetsearch.solr.SolrClientPool;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract implementation of {@link SolrSearchProvider}.
 */
public abstract class AbstractSolrSearchProvider implements SolrSearchProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrSearchProvider.class);

	protected static final String INDEX_PARAM = "index";

	protected static final int DEFAULT_ALIVE_CHECK_INTERVAL = 5000;
	protected static final int DEFAULT_MAX_CONNECTIONS = 100;
	protected static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 50;
	protected static final int DEFAULT_SOCKET_TIMEOUT = 8000;
	protected static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

	protected static final String DEFAULT_CONFIGSET_PROPERTY = "solrfacetsearch.configsets.default";
	protected static final String DEFAULT_CONFIGSET_VALUE = "default";
	protected static final String SYNONYM_SPLIT_CHAR = ",";

	protected static final String MANAGED_INIT_ARGS_FIELD = "initArgs";
	protected static final String MANAGED_IGNORE_CASE_FIELD = "ignoreCase";
	protected static final String MANAGED_LIST_FIELD = "managedList";
	protected static final String MANAGED_MAP_FIELD = "managedMap";

	protected static final String MANAGED_RESOURCES_PATH = "/schema/managed";
	protected static final String MANAGED_RESOURCES_ROOT_FIELD = "managedResources";

	protected static final String MANAGED_SYNONYMS_IGNORE_CASE_KEY = "solrfacetsearch.synonyms.filter.ignoreCase";
	protected static final String MANAGED_SYNONYMS_TYPE = "org.apache.solr.rest.schema.analysis.ManagedSynonymFilterFactory$SynonymManager";
	protected static final String MANAGED_SYNONYMS_PATH = "/schema/analysis/synonyms/{0}";
	protected static final String MANAGED_SYNONYMS_ROOT_FIELD = "synonymMappings";

	protected static final String MANAGED_STOP_WORDS_IGNORE_CASE_KEY = "solrfacetsearch.stopwords.filter.ignoreCase";
	protected static final String MANAGED_STOP_WORDS_TYPE = "org.apache.solr.rest.schema.analysis.ManagedWordSetResource";
	protected static final String MANAGED_STOP_WORDS_PATH = "/schema/analysis/stopwords/{0}";
	protected static final String MANAGED_STOP_WORDS_ROOT_FIELD = "wordSet";

	protected static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
	protected static final String UTF8_ENCODING = "UTF-8";
	protected static final String SOLR_QUERY_SELECT_ALL = "*:*";

	private SolrFacetSearchConfigDao solrFacetSearchConfigDao;
	private IndexNameResolver indexNameResolver;
	private ConfigurationService configurationService;
	private SolrClientPool solrClientPool;

	@Override
	public Index resolveIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final String qualifier)
	{
		validateParameterNotNullStandardMessage("facetSearchConfig", facetSearchConfig);
		validateParameterNotNullStandardMessage("indexType", indexedType);
		validateParameterNotNullStandardMessage("qualifier", indexedType);

		final String indexName = indexNameResolver.resolve(facetSearchConfig, indexedType, qualifier);

		final DefaultIndex index = new DefaultIndex();
		index.setName(indexName);
		index.setFacetSearchConfig(facetSearchConfig);
		index.setIndexedType(indexedType);
		index.setQualifier(qualifier);

		return index;
	}

	@Override
	public void deleteAllDocuments(final Index index) throws SolrServiceException
	{
		final SolrClient solrClient = getClientForIndexing(index);

		try
		{
			solrClient.deleteByQuery(index.getName(), SOLR_QUERY_SELECT_ALL);
		}
		catch (final SolrServerException | IOException exception)
		{
			throw new SolrServiceException(exception);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void deleteOldDocuments(final Index index, final long indexOperationId) throws SolrServiceException
	{
		final SolrClient solrClient = getClientForIndexing(index);

		try
		{
			final String query = "-" + SolrfacetsearchConstants.INDEX_OPERATION_ID_FIELD + ":[" + indexOperationId + " TO *]";
			solrClient.deleteByQuery(index.getName(), query);
		}
		catch (final SolrServerException | IOException exception)
		{
			throw new SolrServiceException(exception);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void deleteDocumentsByPk(final Index index, final Collection<PK> pks) throws SolrServiceException
	{
		final SolrClient solrClient = getClientForIndexing(index);

		try
		{
			final String query = SolrfacetsearchConstants.PK_FIELD + ":("
					+ pks.stream().map(PK::getLongValueAsString).collect(Collectors.joining(" OR ")) + ")";
			solrClient.deleteByQuery(index.getName(), query);
		}
		catch (final SolrServerException | IOException exception)
		{
			throw new SolrServiceException(exception);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void commit(final Index index, final CommitType commitType) throws SolrServiceException
	{
		final SolrClient solrClient = getClientForIndexing(index);

		try
		{
			switch (commitType)
			{
				case HARD:
					solrClient.commit(index.getName(), true, true, false);
					break;

				case SOFT:
					solrClient.commit(index.getName(), false, false, true);
					break;

				default:
					break;
			}
		}
		catch (final SolrServerException | IOException e)
		{
			throw new SolrServiceException(e);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void optimize(final Index index) throws SolrServiceException
	{
		final SolrClient solrClient = getClientForIndexing(index);

		try
		{
			solrClient.optimize(index.getName(), false, false);
		}
		catch (final SolrServerException | IOException e)
		{
			throw new SolrServiceException(e);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	protected String resolveConfigSet(final Index index)
	{
		String configSet = index.getIndexedType().getConfigSet();

		if (StringUtils.isBlank(configSet))
		{
			configSet = getConfigurationService().getConfiguration().getString(DEFAULT_CONFIGSET_PROPERTY, DEFAULT_CONFIGSET_VALUE);
		}

		return configSet;
	}

	protected int getIntegerValue(final Integer bigInt, final int defaultValue)
	{
		return bigInt == null ? defaultValue : bigInt;
	}

	protected HttpClient createHttpClient(final SolrClientConfig solrClientConfig)
	{
		final ModifiableSolrParams clientParams = new ModifiableSolrParams();
		clientParams.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST,
				getIntegerValue(solrClientConfig.getMaxConnectionsPerHost(), DEFAULT_MAX_CONNECTIONS_PER_HOST));
		clientParams.set(HttpClientUtil.PROP_MAX_CONNECTIONS,
				getIntegerValue(solrClientConfig.getMaxConnections(), DEFAULT_MAX_CONNECTIONS));

		return HttpClientUtil.createClient(clientParams);
	}

	protected void closeHttpClient(final HttpClient httpClient)
	{
		HttpClientUtil.close(httpClient);
	}


	protected void exportConfig(final Index index, final SolrClient solrClient)
			throws SolrServiceException, SolrServerException, IOException
	{
		final Map<String, ManagedResource> managedResources = loadManagedResourcesFromServer(index, solrClient);
		final List<String> languages = collectLanguages(index.getFacetSearchConfig());

		exportSynonyms(index, solrClient, managedResources, languages);
		exportStopWords(index, solrClient, managedResources, languages);
	}

	protected Map<String, ManagedResource> loadManagedResourcesFromServer(final Index index, final SolrClient solrClient)
			throws SolrServerException, IOException
	{
		final NamedList<Object> response = executeGet(solrClient, index.getName(), MANAGED_RESOURCES_PATH);

		final Map<String, ManagedResource> managedResources = new HashMap<>();
		final List<Map<String, String>> respManagedResources = (List<Map<String, String>>) response
				.get(MANAGED_RESOURCES_ROOT_FIELD);

		if (respManagedResources != null)
		{
			for (final Map<String, String> respManagedResource : respManagedResources)
			{
				final ManagedResource managedResource = new ManagedResource();
				managedResource.setResourceId(respManagedResource.get("resourceId"));
				managedResource.setType(respManagedResource.get("class"));

				managedResources.put(managedResource.getResourceId(), managedResource);
			}
		}

		return managedResources;
	}

	protected List<String> collectLanguages(final FacetSearchConfig configuration)
	{
		return configuration.getIndexConfig().getLanguages().stream().map(LanguageModel::getIsocode).collect(Collectors.toList());
	}

	protected void exportSynonyms(final Index index, final SolrClient solrClient,
			final Map<String, ManagedResource> managedResources, final List<String> languages)
			throws IOException, SolrServiceException
	{
		try
		{
			final Map<String, Map<String, Set<String>>> configurationSynonyms = loadSynonymsFromConfiguration(
					index.getFacetSearchConfig(), languages);

			for (final String language : languages)
			{
				final String managedResourcePath = MessageFormat.format(MANAGED_SYNONYMS_PATH, encode(language));
				if (!managedResources.containsKey(managedResourcePath))
				{
					// skip, there is no field using this managed resource
					LOG.info("Skipping sysnonyms export (no mapped field): index={}, language={}", index.getName(), language);
					continue;
				}

				final Map<String, Set<String>> synonyms = MapUtils.emptyIfNull(configurationSynonyms.get(language));
				final Map<String, Set<String>> serverSynonyms = MapUtils
						.emptyIfNull(loadSynonymsFromServer(solrClient, index.getName(), managedResourcePath));

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Synonyms: index={}, language={}", index.getName(), language);
					LOG.debug("- from configuration: {}", synonyms);
					LOG.debug("- from server: {}", serverSynonyms);
				}

				updateSynonymsOnServer(solrClient, index.getName(), managedResourcePath, synonyms, serverSynonyms);
			}
		}
		catch (final Exception e)
		{
			throw new SolrServiceException(e);
		}
	}

	protected Map<String, Map<String, Set<String>>> loadSynonymsFromConfiguration(final FacetSearchConfig facetSearchConfig,
			final Collection<String> languages)
	{
		final Map<String, Map<String, Set<String>>> synonyms = new HashMap<>();

		for (final String language : languages)
		{
			synonyms.put(language, new HashMap<>());
		}

		final SolrFacetSearchConfigModel solrFacetSearchConfigModel = solrFacetSearchConfigDao
				.findFacetSearchConfigByName(facetSearchConfig.getName());
		for (final SolrSynonymConfigModel solrSynonymConfigModel : solrFacetSearchConfigModel.getSynonyms())
		{
			final String key = solrSynonymConfigModel.getLanguage().getIsocode();
			final Map<String, Set<String>> values = synonyms.get(key);

			if (values != null)
			{
				buildSynonyms(values, solrSynonymConfigModel.getSynonymFrom(), solrSynonymConfigModel.getSynonymTo());
			}
		}

		return synonyms;
	}

	protected void buildSynonyms(final Map<String, Set<String>> synonyms, final String from, final String to)
	{
		List<String> synonymsFrom = null;
		List<String> synonymsTo = null;

		if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to))
		{
			synonymsFrom = expandSynonyms(from);
			synonymsTo = expandSynonyms(to);
		}
		else if (StringUtils.isNotBlank(from))
		{
			synonymsFrom = expandSynonyms(from);
			synonymsTo = synonymsFrom;
		}

		if (CollectionUtils.isNotEmpty(synonymsFrom) && CollectionUtils.isNotEmpty(synonymsTo))
		{
			for (final String synonymFrom : synonymsFrom)
			{
				Set<String> values = synonyms.get(synonymFrom);
				if (values == null)
				{
					values = new HashSet<>();
					synonyms.put(synonymFrom, values);
				}

				values.addAll(synonymsTo);
			}
		}
	}

	protected List<String> expandSynonyms(final String value)
	{
		if (value == null)
		{
			return Collections.emptyList();
		}

		return Arrays.stream(value.split(SYNONYM_SPLIT_CHAR)).map(String::trim).filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
	}

	protected Map<String, Set<String>> loadSynonymsFromServer(final SolrClient solrClient, final String indexName,
			final String managedResourcePath) throws SolrServerException, IOException
	{
		final NamedList<Object> response = executeGet(solrClient, indexName, managedResourcePath);
		final Map<String, Set<String>> respSynonyms = (Map<String, Set<String>>) Utils.getObjectByPath(response, false,
				Arrays.asList(MANAGED_SYNONYMS_ROOT_FIELD, MANAGED_MAP_FIELD));

		if (MapUtils.isEmpty(respSynonyms))
		{
			return Collections.emptyMap();
		}

		return new HashMap<>(respSynonyms);
	}

	protected void updateSynonymsOnServer(final SolrClient solrClient, final String indexName, final String managedResourcePath,
			final Map<String, Set<String>> synonyms, final Map<String, Set<String>> serverSynonyms)
			throws SolrServiceException, SolrServerException, IOException
	{
		// delete synonyms

		final Set<String> synonymsToRemove = new HashSet<>(serverSynonyms.keySet());

		for (final Map.Entry<String, Set<String>> entry : serverSynonyms.entrySet())
		{
			final String synonym = entry.getKey();

			if (!Objects.equals(entry.getValue(), serverSynonyms.get(synonym)))
			{
				synonymsToRemove.add(synonym);
			}
		}

		for (final String synonym : synonymsToRemove)
		{
			executeDelete(solrClient, indexName, managedResourcePath + "/" + encode(synonym));
		}

		// add/update synonyms

		final boolean ignoreCase = configurationService.getConfiguration().getBoolean(MANAGED_SYNONYMS_IGNORE_CASE_KEY, true);

		final Map<String, Object> requestData = new LinkedHashMap<>();
		requestData.put(MANAGED_INIT_ARGS_FIELD, Collections.singletonMap(MANAGED_IGNORE_CASE_FIELD, Boolean.toString(ignoreCase)));
		requestData.put(MANAGED_MAP_FIELD, synonyms);

		executePost(solrClient, indexName, managedResourcePath, requestData);
	}

	protected void exportStopWords(final Index index, final SolrClient solrClient,
			final Map<String, ManagedResource> managedResources, final List<String> languages)
			throws IOException, SolrServiceException
	{
		try
		{
			final Map<String, Set<String>> configurationStopWords = loadStopWordsFromConfiguration(index.getFacetSearchConfig(),
					languages);

			for (final String language : languages)
			{
				final String managedResourcePath = MessageFormat.format(MANAGED_STOP_WORDS_PATH, encode(language));
				if (!managedResources.containsKey(managedResourcePath))
				{
					// skip, there is no field using this managed resource
					LOG.info("Skipping stopwords export (no mapped field): index={}, language={}", index.getName(), language);
					continue;
				}

				final Set<String> stopWords = SetUtils.emptyIfNull(configurationStopWords.get(language));
				final Set<String> serverStopWords = SetUtils
						.emptyIfNull(loadStopWordsFromServer(solrClient, index.getName(), managedResourcePath));

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Stop words: index={}, language={}", index.getName(), language);
					LOG.debug("- from configuration: {}", stopWords);
					LOG.debug("- from server: {}", serverStopWords);
				}

				updateStopWordsOnServer(solrClient, index.getName(), managedResourcePath, stopWords, serverStopWords);
			}
		}
		catch (final Exception e)
		{
			throw new SolrServiceException(e);
		}
	}

	protected Map<String, Set<String>> loadStopWordsFromConfiguration(final FacetSearchConfig facetSearchConfig,
			final Collection<String> languages)
	{
		final Map<String, Set<String>> stopWords = new HashMap<>();

		for (final String language : languages)
		{
			stopWords.put(language, new HashSet<>());
		}

		final SolrFacetSearchConfigModel solrFacetSearchConfigModel = getSolrFacetSearchConfigDao()
				.findFacetSearchConfigByName(facetSearchConfig.getName());
		for (final SolrStopWordModel solrStopWordModel : solrFacetSearchConfigModel.getStopWords())
		{
			final String key = solrStopWordModel.getLanguage().getIsocode();
			final Set<String> values = stopWords.get(key);

			if (values != null)
			{
				values.add(solrStopWordModel.getStopWord());
			}
		}

		return stopWords;
	}

	protected Set<String> loadStopWordsFromServer(final SolrClient solrClient, final String indexName,
			final String managedResourcePath) throws SolrServerException, IOException
	{
		final NamedList<Object> response = executeGet(solrClient, indexName, managedResourcePath);
		final List<String> respStopWords = (List<String>) Utils.getObjectByPath(response, false,
				Arrays.asList(MANAGED_STOP_WORDS_ROOT_FIELD, MANAGED_LIST_FIELD));

		if (CollectionUtils.isEmpty(respStopWords))
		{
			return Collections.emptySet();
		}

		return new HashSet<>(respStopWords);
	}

	protected void updateStopWordsOnServer(final SolrClient solrClient, final String indexName, final String managedResourcePath,
			final Set<String> stopWords, final Set<String> serverStopWords)
			throws SolrServiceException, SolrServerException, IOException
	{
		// delete stop words

		final Set<String> stopWordsToRemove = new HashSet<>(serverStopWords);
		stopWordsToRemove.removeAll(stopWords);

		for (final String stopWord : stopWordsToRemove)
		{
			executeDelete(solrClient, indexName, managedResourcePath + "/" + encode(stopWord));
		}

		// add/update stop words

		final boolean ignoreCase = configurationService.getConfiguration().getBoolean(MANAGED_STOP_WORDS_IGNORE_CASE_KEY, true);

		final Map<String, Object> requestData = new LinkedHashMap<>();
		requestData.put(MANAGED_INIT_ARGS_FIELD, Collections.singletonMap(MANAGED_IGNORE_CASE_FIELD, Boolean.toString(ignoreCase)));
		requestData.put(MANAGED_LIST_FIELD, stopWords);

		executePost(solrClient, indexName, managedResourcePath, requestData);
	}

	protected String encode(final String string) throws SolrServiceException
	{
		try
		{
			return URLEncoder.encode(string, UTF8_ENCODING);
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new SolrServiceException("error in encoding", e);
		}
	}

	protected NamedList<Object> executeGet(final SolrClient solrClient, final String indexName, final String path)
			throws SolrServerException, IOException
	{
		final GenericSolrRequest request = new GenericSolrRequest(GET, path, null);
		return solrClient.request(request, indexName);
	}

	protected void executePost(final SolrClient solrClient, final String indexName, final String path, final Object payload)
			throws SolrServerException, IOException
	{
		final GenericSolrRequest request = new GenericSolrRequest(POST, path, null);

		if (payload != null)
		{
			final RequestWriter.StringPayloadContentWriter contentWriter = new RequestWriter.StringPayloadContentWriter(Utils.toJSONString(payload), JSON_CONTENT_TYPE);
			request.setContentWriter(contentWriter);
		}

		solrClient.request(request, indexName);
	}

	protected void executeDelete(final SolrClient solrClient, final String indexName, final String path)
			throws SolrServerException, IOException
	{
		final GenericSolrRequest request = new GenericSolrRequest(DELETE, path, null);
		solrClient.request(request, indexName);
	}

	public SolrFacetSearchConfigDao getSolrFacetSearchConfigDao()
	{
		return solrFacetSearchConfigDao;
	}

	@Required
	public void setSolrFacetSearchConfigDao(final SolrFacetSearchConfigDao solrFacetSearchConfigDao)
	{
		this.solrFacetSearchConfigDao = solrFacetSearchConfigDao;
	}

	public IndexNameResolver getIndexNameResolver()
	{
		return indexNameResolver;
	}

	@Required
	public void setIndexNameResolver(final IndexNameResolver indexNameResolver)
	{
		this.indexNameResolver = indexNameResolver;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public SolrClientPool getSolrClientPool()
	{
		return solrClientPool;
	}

	@Required
	public void setSolrClientPool(final SolrClientPool solrClientPool)
	{
		this.solrClientPool = solrClientPool;
	}
}

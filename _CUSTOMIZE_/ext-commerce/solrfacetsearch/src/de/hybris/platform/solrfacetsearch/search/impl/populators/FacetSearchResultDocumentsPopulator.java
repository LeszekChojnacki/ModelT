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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator.FieldInfo;
import de.hybris.platform.solrfacetsearch.search.SearchResultGroup;
import de.hybris.platform.solrfacetsearch.search.SearchResultGroupCommand;
import de.hybris.platform.solrfacetsearch.search.impl.DefaultDocument;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResultGroup;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResultGroupCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates documents of {@link SolrSearchResult} from {@link SearchResultConverterData}
 */
public class FacetSearchResultDocumentsPopulator implements Populator<SearchResultConverterData, SolrSearchResult>
{
	private FieldNameTranslator fieldNameTranslator;

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	@Override
	public void populate(final SearchResultConverterData source, final SolrSearchResult target)
	{
		final QueryResponse queryResponse = source.getQueryResponse();

		if (queryResponse == null)
		{
			return;
		}

		final GroupResponse groupResponse = queryResponse.getGroupResponse();

		final FieldNameTranslator.FieldInfosMapping fieldInfosMapping = fieldNameTranslator
				.getFieldInfos(source.getFacetSearchContext());
		final Map<String, FieldInfo> fieldInfos = fieldInfosMapping.getInvertedFieldInfos();


		long numberOfResults = 0;
		final List<Document> documents = new ArrayList<>();
		final List<SolrDocument> solrDocuments = new ArrayList<>();

		if (groupResponse != null)
		{
			final List<SearchResultGroupCommand> searchResultGroupCommands = new ArrayList<>();
			int groupCommandIndex = 0;

			for (final GroupCommand groupCommand : groupResponse.getValues())
			{
				if (groupCommandIndex == 0)
				{
					numberOfResults = groupCommand.getNGroups();
				}

				final List<SearchResultGroup> searchResultGroups = new ArrayList<>();

				populateGroupedResults(documents, solrDocuments, searchResultGroups, fieldInfos, groupCommand, groupCommandIndex,
						queryResponse.getHighlighting());

				final String groupCommandName = resolveFieldName(groupCommand.getName(), fieldInfos);

				final SolrSearchResultGroupCommand searchResultGroupCommand = new SolrSearchResultGroupCommand();
				searchResultGroupCommand.setName(groupCommandName);
				searchResultGroupCommand.setNumberOfMatches(groupCommand.getMatches());
				searchResultGroupCommand.setNumberOfGroups(groupCommand.getNGroups());
				searchResultGroupCommand.setGroups(searchResultGroups);

				searchResultGroupCommands.add(searchResultGroupCommand);
				groupCommandIndex++;
			}

			target.getGroupCommands().addAll(searchResultGroupCommands);
		}
		else
		{
			final SolrDocumentList results = queryResponse.getResults();
			if (CollectionUtils.isNotEmpty(results))
			{
				numberOfResults = results.getNumFound();

				for (final SolrDocument solrDocument : queryResponse.getResults())
				{
					final Document document = convertDocument(solrDocument, fieldInfos, queryResponse.getHighlighting());
					documents.add(document);
					solrDocuments.add(solrDocument);
				}
			}
		}

		target.setNumberOfResults(numberOfResults);
		target.setDocuments(documents);
		target.setSolrDocuments(solrDocuments);
	}

	protected void populateGroupedResults(final List<Document> documents, final List<SolrDocument> solrDocuments,
			final List<SearchResultGroup> searchResultGroups, final Map<String, FieldInfo> fieldInfos,
			final GroupCommand groupCommand, final int groupCommandIndex, final Map<String, Map<String, List<String>>> highlighting)
	{
		for (final Group group : groupCommand.getValues())
		{
			final List<Document> groupDocuments = new ArrayList<>();
			long groupDocumentIndex = 0;

			final SolrDocumentList groupResult = group.getResult();

			for (final SolrDocument solrGroupDocument : groupResult)
			{
				final Document groupDocument = convertDocument(solrGroupDocument, fieldInfos, highlighting);
				groupDocuments.add(groupDocument);

				// add only the first element of each group to the main results
				if ((groupCommandIndex == 0) && (groupDocumentIndex == 0))
				{
					documents.add(groupDocument);
					solrDocuments.add(solrGroupDocument);
				}

				groupDocumentIndex++;
			}

			final SolrSearchResultGroup searchResultGroup = new SolrSearchResultGroup();
			searchResultGroup.setGroupValue(group.getGroupValue());
			searchResultGroup.setNumberOfResults(groupResult.getNumFound());
			searchResultGroup.setDocuments(groupDocuments);

			searchResultGroups.add(searchResultGroup);
		}
	}

	protected String resolveFieldName(final String fieldName, final Map<String, FieldInfo> fieldInfos)
	{
		final FieldInfo fieldInfo = fieldInfos.get(fieldName);
		if (fieldInfo != null)
		{
			return fieldInfo.getFieldName();
		}

		return fieldName;
	}

	protected Document convertDocument(final SolrDocument solrDocument, final Map<String, FieldInfo> fieldInfos,
			final Map<String, Map<String, List<String>>> highlighting)
	{
		final DefaultDocument document = new DefaultDocument();
		final Map<String, Object> documentFields = document.getFields();

		documentFields.putAll(solrDocument);
		replaceWithHighlightedFields(highlighting, documentFields);

		for (final FieldNameTranslator.FieldInfo fieldInfo : fieldInfos.values())
		{
			final Object fieldValue = documentFields.get(fieldInfo.getTranslatedFieldName());
			if (fieldValue != null)
			{
				documentFields.put(fieldInfo.getFieldName(), fieldValue);
				documentFields.remove(fieldInfo.getTranslatedFieldName());
			}
		}

		return document;
	}

	protected void replaceWithHighlightedFields(final Map<String, Map<String, List<String>>> highlighting,
			final Map<String, Object> documentFields)
	{
		final Object id = documentFields.get(SolrfacetsearchConstants.ID_FIELD);
		if (MapUtils.isEmpty(highlighting) || id == null)
		{
			return;
		}

		final Map<String, List<String>> highlightingForDoc = highlighting.get(id);
		if (MapUtils.isEmpty(highlightingForDoc))
		{
			return;
		}

		highlightingForDoc.entrySet().stream().forEach(highlight -> {
			if (documentFields.get(highlight.getKey()) != null && CollectionUtils.isNotEmpty(highlight.getValue()))
			{
				documentFields.put(highlight.getKey(), highlight.getValue().get(0));
			}
		});
	}
}

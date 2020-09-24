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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.jalo.config.SolrFacetSearchConfig;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;

import java.util.Collection;


/**
 * Validator checks if there are mandatory attribute :Item types, Indexer configuration, Solr server confguration and
 * forbids using xml document because it is no longer used since 4.6 version
 */
public class SolrFacetSearchConfigValidator implements ValidateInterceptor
{
	protected static final String DOCUMENT_DEPRECATED_ERROR = "XML configuration document is deprecated and is no longer used (since 4.6 version)";
	protected static final String REQUIRED_MEMBER_ITEMS = "Those member items are required : ";

	private TypeService typeService;

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrFacetSearchConfigModel)
		{
			final SolrFacetSearchConfigModel config = (SolrFacetSearchConfigModel) model;
			final boolean hasDocument = config.getDocument() != null;

			if (hasDocument)
			{
				throw new InterceptorException(DOCUMENT_DEPRECATED_ERROR);
			}
			else
			{
				final StringBuilder types = checkMemberItems(config);
				if (types.length() != 0)
				{
					final StringBuilder message = new StringBuilder(REQUIRED_MEMBER_ITEMS);
					message.append(types);
					throw new InterceptorException(message.toString());
				}
			}
		}
	}

	protected StringBuilder checkMemberItems(final SolrFacetSearchConfigModel config)
	{
		final Collection<SolrIndexedTypeModel> indexTypes = config.getSolrIndexedTypes();
		final SolrServerConfigModel serverConfig = config.getSolrServerConfig();
		final SolrIndexConfigModel indexConfig = config.getSolrIndexConfig();

		final boolean hasIndexedTypes = indexTypes != null && !indexTypes.isEmpty();
		final boolean hasIndexConfig = indexConfig != null;
		final boolean hasServerConfig = serverConfig != null;
		final char separator = ',';
		final int separatorLength = 1;
		final StringBuilder types = new StringBuilder();

		if (!hasIndexedTypes)
		{
			types.append(getFieldDescriptor(SolrFacetSearchConfig.SOLRINDEXEDTYPES)).append(separator);
		}


		if (!hasIndexConfig)
		{
			types.append(getFieldDescriptor(SolrFacetSearchConfig.SOLRINDEXCONFIG)).append(separator);
		}


		if (!hasServerConfig)
		{
			types.append(getFieldDescriptor(SolrFacetSearchConfig.SOLRSERVERCONFIG)).append(separator);
		}

		if (types.length() > 0)
		{
			types.deleteCharAt(types.length() - separatorLength);
		}

		return types;
	}

	protected String getFieldDescriptor(final String qualifier)
	{
		final AttributeDescriptorModel descriptor = typeService.getAttributeDescriptor(
				SolrfacetsearchConstants.TC.SOLRFACETSEARCHCONFIG, qualifier);
		return descriptor.getName();
	}

	/**
	 * @param typeService
	 *           the typeService to set
	 */
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

}

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
package de.hybris.platform.solrfacetsearch.common;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for some Solr services.
 */
public abstract class AbstractYSolrService
{

	protected FacetSearchConfigService facetSearchConfigService;
	protected FieldNameProvider solrFieldNameProvider;

	private static final Logger LOG = Logger.getLogger(AbstractYSolrService.class);

	protected void filterQualifyingIndexProperties(final Collection<String> fields, final LanguageModel language,
			final SolrIndexedPropertyModel prop)
	{
		if (checkIfIndexPropertyQualifies(prop))
		{
			try
			{
				fields.add(resolveIndexedPropertyFieldName(prop, language));
			}
			catch (final FacetConfigServiceException e)
			{
				LOG.error("Could not resolve indexed field name for property [" + prop.getName() + "], language["
						+ language.getIsocode() + "]", e);
			}
		}
	}

	protected abstract boolean checkIfIndexPropertyQualifies(SolrIndexedPropertyModel indexedProperty);

	protected String resolveIndexedPropertyFieldName(final SolrIndexedPropertyModel indexedProperty, final LanguageModel language)
			throws FacetConfigServiceException
	{
		return solrFieldNameProvider.getFieldName(indexedProperty, indexedProperty.isLocalized() ? language.getIsocode() : null,
				FieldType.INDEX);
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Required
	public void setSolrFieldNameProvider(final FieldNameProvider solrFieldNameProvider)
	{
		this.solrFieldNameProvider = solrFieldNameProvider;
	}
}

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
package de.hybris.platform.solrfacetsearch.loader.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.loader.ModelLoader;
import de.hybris.platform.solrfacetsearch.loader.ModelLoadingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Required;


/**
 * ModelLoader implementation for {@link ProductModel}. <br>
 * Gets information on ProductModels from given collection of {@link SolrDocument}s
 *
 *
 *
 */
public class DefaultModelLoader implements ModelLoader<Object>
{

	private static final String CODE = "code";
	private ModelService modelService;

	@Override
	public List<Object> loadModels(final Collection<SolrDocument> documents) throws ModelLoadingException
	{
		if (documents == null)
		{
			throw new IllegalArgumentException("Collection of SolrDocuments must not be null");
		}
		if (documents.isEmpty())
		{
			return Collections.emptyList();
		}
		final List<Object> result = new ArrayList<Object>(documents.size());
		for (final SolrDocument doc : documents)
		{
			final PK pk = getPKFromDocument(doc);
			final Object model = modelService.get(pk);
			result.add(model);
		}
		return result;
	}

	@Override
	public List<String> loadCodes(final Collection<SolrDocument> documents) throws ModelLoadingException
	{
		if (documents == null)
		{
			throw new IllegalArgumentException("Collection of SolrDocuments must not be null");
		}
		if (documents.isEmpty())
		{
			return Collections.emptyList();
		}
		final List<String> result = new ArrayList<String>(documents.size());
		for (final SolrDocument doc : documents)
		{
			final PK pk = getPKFromDocument(doc);
			final Object model = modelService.get(pk);
			result.add(getModelAttribute(CODE, model, pk));
		}
		return result;
	}

	/**
	 *
	 * @param attribute
	 * @param model
	 * @param pk
	 * @return string
	 * @throws ModelLoadingException
	 */
	protected String getModelAttribute(final String attribute, final Object model, final PK pk) throws ModelLoadingException
	{
		try
		{
			return modelService.getAttributeValue(model, attribute);
		}
		catch (final IllegalArgumentException e)
		{
			throw new ModelLoadingException("Could not load attribute [" + CODE + "] from  Item [" + pk.toString() + "]", e);
		}
	}


	/**
	 *
	 * @param doc
	 * @return PK
	 * @throws ModelLoadingException
	 */
	protected PK getPKFromDocument(final SolrDocument doc) throws ModelLoadingException
	{
		final Long pk = (Long) doc.getFirstValue("pk");
		if (pk == null)
		{
			throw new ModelLoadingException("SolrDocument does not contain field 'pk'");
		}
		return PK.fromLong(pk.longValue());
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}



}

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
package com.hybris.backoffice.solrsearch.aspects;

import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.events.SolrIndexSynchronizationStrategy;

/**
 * Contains logic of aspect responsible for updating solr index.<br>
 * Shall be called when Backoffice {@link com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade} performs changes on {@link ItemModel}.
 * This class uses underlying {@link SolrIndexSynchronizationStrategy} to perform operations on solr index.
 */
public class ObjectFacadeSolrIndexingAspect
{
	private ModelService modelService;
	private SolrIndexSynchronizationStrategy solrIndexSynchronizationStrategy;

	/**
	 * Logic to be called when item is being changed via {@link com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade}
	 * @param joinPoint aspect joint point
	 * @param retVal aspect returned value
	 */
	public void updateChanged(final JoinPoint joinPoint, final Object retVal)
	{
		final Map<String, List<PK>> models = extractModelsFromArgs(joinPoint, retVal);

		for (Map.Entry<String, List<PK>> entry : models.entrySet())
		{
			getSolrIndexSynchronizationStrategy().updateItems(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Logic to be called when item is being removed via {@link com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade}
	 * @param joinPoint aspect joint point
	 * @param retVal aspect returned value
	 */
	public void updateRemoved(final JoinPoint joinPoint, final Object retVal)
	{
		final Map<String, List<PK>> models = extractModelsFromArgs(joinPoint, retVal);

		for (Map.Entry<String, List<PK>> entry : models.entrySet())
		{
			getSolrIndexSynchronizationStrategy().removeItems(entry.getKey(), entry.getValue());
		}
	}

	protected Map<String, List<PK>> extractModelsFromArgs(final JoinPoint joinPoint, final Object retVal)
	{
		final Object model = joinPoint.getArgs().length == 0 ? null : joinPoint.getArgs()[0];

		if (model instanceof Collection)
		{
			final Collection<Object> models = (Collection) model;

			final Set failedModels = findFailedObjects(retVal);

			return models.stream()
					.filter(m -> !failedModels.contains(m))
					.filter(ItemModel.class::isInstance)
					.map(ItemModel.class::cast)
					.collect(Collectors.groupingBy(getModelService()::getModelType, Collectors.mapping(ItemModel::getPk, Collectors.toList())));
		}
		else if (model instanceof ItemModel)
		{
			final Map<String, List<PK>> map = new HashMap<>();
			map.put(getModelService().getModelType(model), Collections.singletonList(((ItemModel) model).getPk()));
			return map;
		}
		else
		{
			return Collections.emptyMap();
		}
	}

	private Set findFailedObjects(final Object retVal)
	{
		final Set failedObjects;
		if (retVal instanceof ObjectFacadeOperationResult)
		{
			failedObjects = ((ObjectFacadeOperationResult) retVal).getFailedObjects();
		}
		else
		{
			failedObjects = Collections.emptySet();
		}
		return failedObjects;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected SolrIndexSynchronizationStrategy getSolrIndexSynchronizationStrategy()
	{
		return solrIndexSynchronizationStrategy;
	}

	@Required
	public void setSolrIndexSynchronizationStrategy(final SolrIndexSynchronizationStrategy solrIndexSynchronizationStrategy)
	{
		this.solrIndexSynchronizationStrategy = solrIndexSynchronizationStrategy;
	}
}

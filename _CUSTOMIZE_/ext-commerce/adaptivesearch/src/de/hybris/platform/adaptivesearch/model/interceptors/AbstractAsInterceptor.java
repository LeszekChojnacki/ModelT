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
package de.hybris.platform.adaptivesearch.model.interceptors;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_NULL_IDENTIFIER;

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearch.strategies.AsUidGenerator;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelInternalContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Date;

import org.springframework.beans.factory.annotation.Required;


/**
 * Base interceptor for adaptive search related types.
 */
public class AbstractAsInterceptor
{
	private ModelService modelService;
	private AsUidGenerator asUidGenerator;
	private AsSearchProviderFactory searchProviderFactory;

	protected String generateUid()
	{
		return asUidGenerator.generateUid();
	}

	protected String generateItemIdentifier(final ItemModel item, final InterceptorContext context)
	{
		if (item == null)
		{
			return UNIQUE_IDX_NULL_IDENTIFIER;
		}

		final PK itemPk = context.isNew(item) ? getNewPkForNotSavedItem(item) : item.getPk();
		if (itemPk == null)
		{
			throw new IllegalStateException("Could not generate identifier for item with unknown pk");
		}

		return itemPk.getLongValueAsString();
	}

	protected String decorateIdentifier(final String identifier)
	{
		if (identifier == null)
		{
			return UNIQUE_IDX_NULL_IDENTIFIER;
		}

		return identifier;
	}

	protected PK getNewPkForNotSavedItem(final AbstractItemModel item)
	{
		final ItemModelInternalContext ictx = (ItemModelInternalContext) ModelContextUtils.getItemModelContext(item);
		final PK newPK = ictx.getNewPK();
		return newPK == null ? ictx.generateNewPK() : newPK;
	}

	protected AsSearchProvider resolveSearchProvider()
	{
		return searchProviderFactory.getSearchProvider();
	}

	protected AbstractAsSearchProfileModel resolveSearchProfile(final ItemModel model)
	{
		return modelService.getAttributeValue(model, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected AbstractAsSearchProfileModel resolveAndValidateSearchProfile(final AbstractAsSearchConfigurationModel model)
			throws InterceptorException
	{
		final Object searchProfile = modelService.getAttributeValue(model, SEARCH_PROFILE_ATTRIBUTE);

		if (!(searchProfile instanceof AbstractAsSearchProfileModel))
		{
			throw new InterceptorException("Invalid search profile");
		}

		return (AbstractAsSearchProfileModel) searchProfile;
	}

	protected AbstractAsSearchConfigurationModel resolveSearchConfiguration(final ItemModel model)
	{
		return modelService.getAttributeValue(model, SEARCH_CONFIGURATION_ATTRIBUTE);
	}

	protected AbstractAsSearchConfigurationModel resolveAndValidateSearchConfiguration(final ItemModel model)
			throws InterceptorException
	{
		final Object searchConfiguration = modelService.getAttributeValue(model, SEARCH_CONFIGURATION_ATTRIBUTE);

		if (!(searchConfiguration instanceof AbstractAsSearchConfigurationModel))
		{
			throw new InterceptorException("Invalid search configuration");
		}

		return (AbstractAsSearchConfigurationModel) searchConfiguration;
	}

	protected void markItemAsModified(final InterceptorContext context, final ItemModel item, final String... path)
	{
		ItemModel currentItem = item;
		int index = 0;

		while (currentItem != null)
		{
			if (!context.isRemoved(currentItem) && !context.contains(currentItem, PersistenceOperation.SAVE))
			{
				if(!context.isModified(currentItem))
				{
					currentItem.setModifiedtime(new Date());
				}
				context.registerElementFor(currentItem, PersistenceOperation.SAVE);
			}

			if (index < path.length)
			{
				currentItem = modelService.getAttributeValue(currentItem, path[index]);
				index++;
			}
			else
			{
				currentItem = null;
			}
		}
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public AsUidGenerator getAsUidGenerator()
	{
		return asUidGenerator;
	}

	@Required
	public void setAsUidGenerator(final AsUidGenerator asUidGenerator)
	{
		this.asUidGenerator = asUidGenerator;
	}

	public AsSearchProviderFactory getSearchProviderFactory()
	{
		return searchProviderFactory;
	}

	@Required
	public void setSearchProviderFactory(final AsSearchProviderFactory searchProviderFactory)
	{
		this.searchProviderFactory = searchProviderFactory;
	}
}

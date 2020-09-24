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
package com.hybris.backoffice.solrsearch.events;


import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.enums.SolrItemModificationType;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;


public class CronJobSolrIndexSynchronizationStrategy implements SolrIndexSynchronizationStrategy
{

	private static final String BACKOFFICESOLRSEARCH_UPDATE_MODIFIED_ITEM_ENABLED = "backofficesolrsearch.item.updateModified";
	protected ModelService modelService;

	@Override
	public void updateItem(final String typecode, final long pk)
	{
		updateItems(typecode, Collections.singletonList(PK.fromLong(pk)));
	}

	@Override
	public void updateItems(final String typecode, final List<PK> pkList)
	{
		if (shouldUpdateModifiedItem())
		{
			addModifiedItems(typecode, pkList, SolrItemModificationType.UPDATE);
		}
	}

	@Override
	public void removeItem(final String typecode, final long pk)
	{
		addModifiedItem(typecode, pk, SolrItemModificationType.DELETE);
	}

	@Override
	public void removeItems(final String typecode, final List<PK> pkList)
	{
		addModifiedItems(typecode, pkList, SolrItemModificationType.DELETE);
	}

	protected void addModifiedItem(final String typecode, final long pk, final SolrItemModificationType modificationType)
	{
		final PK pkInstance = PK.fromLong(pk);
		if (pkInstance != null)
		{
			final SolrModifiedItemModel modifiedItem = createSolrModifiedItem(typecode, pkInstance, modificationType);
			modelService.save(modifiedItem);
		}
	}

	private SolrModifiedItemModel createSolrModifiedItem(final String typecode, final PK pk, final SolrItemModificationType modificationType)
	{
		final SolrModifiedItemModel modifiedItem = getModelService().create(SolrModifiedItemModel.class);
		modifiedItem.setModificationType(modificationType);
		modifiedItem.setModifiedPk(pk.getLong());
		modifiedItem.setModifiedTypeCode(typecode);
		return modifiedItem;
	}

	protected void addModifiedItems(final String typecode, final List<PK> pks, final SolrItemModificationType modificationType)
	{
		final List<SolrModifiedItemModel> modifiedItems = pks.stream().map(
				pk -> createSolrModifiedItem(typecode, pk, modificationType)).collect(Collectors.toList());

		getModelService().saveAll(modifiedItems);
	}

	protected boolean shouldUpdateModifiedItem()
	{
		return Config.getBoolean(BACKOFFICESOLRSEARCH_UPDATE_MODIFIED_ITEM_ENABLED, false);
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}
}

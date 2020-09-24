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
package com.hybris.pcmbackoffice.widgets.contextpopulator;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.contextpopulator.ContextPopulator;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.widgets.common.explorertree.data.PartitionNodeData;


public class CatalogContextPopulator implements ContextPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(CatalogContextPopulator.class);
	public static final String CHILD_TYPE_CODE = "childTypeCode";
	public static final String SELECTED_TYPE_CODE = "selectedTypeCode";

	private TypeFacade typeFacade;

	@Override
	public Map<String, Object> populate(final Object data)
	{
		final Map<String, Object> context = new HashMap<>();

		context.put(ContextPopulator.SELECTED_OBJECT, data);

		if (data instanceof PartitionNodeData)
		{
			return context;
		}

		try
		{
			context.put(SELECTED_TYPE_CODE, typeFacade.getType(data));
		}
		catch (final RuntimeException e)
		{
			final String errorMessage = String.format("Could not resolve type for : [%s]", data);
			if (LOG.isDebugEnabled())
			{
				LOG.warn(errorMessage, e);
			}
			else
			{
				LOG.warn(errorMessage);
			}
		}

		if (data instanceof CatalogModel)
		{
			context.put(CHILD_TYPE_CODE, CatalogVersionModel._TYPECODE);
			context.put(CatalogVersionModel.CATALOG, data);
		}
		else if (data instanceof CatalogVersionModel)
		{
			context.put(CHILD_TYPE_CODE, CategoryModel._TYPECODE);
			context.put(CategoryModel.CATALOG, ((CatalogVersionModel) data).getCatalog());
			context.put(CategoryModel.CATALOGVERSION, data);
		}
		else if (data instanceof CategoryModel)
		{
			final CategoryModel category = (CategoryModel) data;
			context.put(CHILD_TYPE_CODE, CategoryModel._TYPECODE);
			if (category.getCatalogVersion() != null)
			{
				context.put(CategoryModel.CATALOG, category.getCatalogVersion().getCatalog());
			}
			context.put(CategoryModel.CATALOGVERSION, category.getCatalogVersion());
			context.put(CategoryModel.SUPERCATEGORIES, Collections.singletonList(category));
		}
		else
		{
			context.put(CHILD_TYPE_CODE, CatalogModel._TYPECODE);
		}

		return context;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}
}

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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearch.strategies.AsUidGenerator;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRuntimeException;
import de.hybris.platform.core.model.ItemModel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;


/**
 * Base class for {@link DataHandler} implementations.
 *
 * @param <D>
 *           - the type of the item data
 * @param <V>
 *           - the type of the item value
 */
public abstract class AbstractDataHandler<D extends AbstractEditorData, V extends ItemModel> implements DataHandler<D, V>
{
	protected static final String SEARCH_PROFILE_PARAM = "searchProfile";

	private AsConfigurationService asConfigurationService;
	private AsUidGenerator asUidGenerator;

	@Override
	public ListModel<D> loadData(final Collection<V> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters)
	{
		final Map<Object, D> mapping = new LinkedHashMap<>();

		loadDataFromSearchResult(mapping, searchResult, parameters);
		loadDataFromInitialValue(mapping, initialValue, parameters);

		final ListModelList<D> listModel = new ListModelList(mapping.values());

		postLoadData(initialValue, searchResult, parameters, listModel);

		return listModel;
	}

	protected abstract void loadDataFromSearchResult(final Map<Object, D> mapping, final SearchResultData searchResult,
			final Map<String, Object> parameters);

	protected abstract void loadDataFromInitialValue(final Map<Object, D> mapping, final Collection<V> initialValue,
			final Map<String, Object> parameters);

	@Override
	public List<V> getValue(final ListModel<D> data)
	{
		final List<V> value = new ArrayList<>();

		for (int index = 0; index < data.getSize(); index++)
		{
			final D dataItem = data.getElementAt(index);
			if (dataItem.getModel() != null)
			{
				value.add((V) dataItem.getModel());
			}
		}

		return value;
	}

	@Override
	public V getItemValue(final D data)
	{
		return (V) data.getModel();
	}

	protected void postLoadData(final Collection<V> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters, final ListModelList<D> data)
	{
		// empty
	}

	@Override
	public Object getAttributeValue(final D editorData, final String attributeName)
	{
		try
		{
			return PropertyUtils.getProperty(editorData, attributeName);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			throw new EditorRuntimeException(e);
		}
	}

	@Override
	public Class<?> getAttributeType(final D editorData, final String attributeName)
	{
		try
		{
			return PropertyUtils.getPropertyType(editorData, attributeName);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			throw new EditorRuntimeException(e);
		}
	}

	@Override
	public void setAttributeValue(final D editorData, final String attributeName, final Object attributeValue)
	{
		try
		{
			PropertyUtils.setProperty(editorData, attributeName, attributeValue);

			if (editorData.getModel() != null)
			{
				editorData.getModel().setProperty(attributeName, attributeValue);
			}
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			throw new EditorRuntimeException(e);
		}
	}

	protected D getOrCreateEditorData(final Map<Object, D> mapping, final String key)
	{
		D editorData = mapping.get(key);
		if (editorData == null)
		{
			editorData = createEditorData();
			mapping.put(key, editorData);
		}

		return editorData;
	}

	protected abstract D createEditorData();

	public AsConfigurationService getAsConfigurationService()
	{
		return asConfigurationService;
	}

	@Required
	public void setAsConfigurationService(final AsConfigurationService asConfigurationService)
	{
		this.asConfigurationService = asConfigurationService;
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
}

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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.savedvalues;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.hmc.model.SavedValuesModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloConnection;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.internal.model.impl.ModelValueHistory;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ItemModelContextImpl;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


/**
 * This service simplifies work with item history ({@link SavedValuesModel}). It covers both standard and localized
 * attributes.
 */
public class DefaultItemModificationHistoryService implements ItemModificationHistoryService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultItemModificationHistoryService.class);

	private ModelService modelService;
	private I18NService i18NService;
	private CommonI18NService commonI18NService;
	private FlexibleSearchService flexibleSearchService;
	private TypeFacade typeFacade;

	private static ModelValueHistory getModelValueHistory(final ItemModel itemModel)
	{
		ModelValueHistory ret = null;
		final ItemModelContext itemModelContext = ModelContextUtils.getItemModelContext(itemModel);
		if (itemModelContext != null)
		{
			ret = ((ItemModelContextImpl) itemModelContext).getValueHistory();
		}
		return ret;
	}

	@Override
	public List<SavedValuesModel> getSavedValues(final ItemModel item)
	{
		final SearchResult<SavedValuesModel> searchResult = flexibleSearchService
				.search("select {pk} from {SavedValues} where {modifiedItem} = " + item.getPk().getLongValue());
		return searchResult.getResult();
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
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

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	@Override
	public void logItemModification(final ItemModificationInfo modificationInfo)
	{
		logModifications(modificationInfo.getModel(), modificationInfo);
	}

	@Override
	public ItemModificationInfo createModificationInfo(final ItemModel itemModel)
	{
		final ModelValueHistory modelHistory = getModelValueHistory(itemModel);
		if (modelHistory == null)
		{
			return new ItemModificationInfo(itemModel);
		}
		final boolean isNew = getModelService().isNew(itemModel);
		if (!modelHistory.isDirty() && !isNew)
		{
			return new ItemModificationInfo(itemModel);
		}

		return computeModificationsForModifiedItem(itemModel, modelHistory, isNew);
	}

	protected ItemModificationInfo computeModificationsForModifiedItem(final ItemModel itemModel,
			final ModelValueHistory modelHistory, final boolean isNew)
	{
		final ItemModificationInfo info = new ItemModificationInfo(itemModel);

		info.setNew(isNew);

		// add non-localized modifications
		final Set<String> dirtyAttributes = modelHistory.getDirtyAttributes();
		final String typeCode = typeFacade.getType(itemModel);
		try
		{
			final DataType dataType = typeFacade.load(typeCode);
			dirtyAttributes.stream() //
					.filter(attribute -> dataType.getAttribute(attribute) != null) //
					.forEach(attribute -> info.addEntry(attribute, false, getOriginalValue(itemModel, attribute, modelHistory),
							getModelService().getAttributeValue(itemModel, attribute)));
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn(String.format("Cannot load type %s", typeCode), e);
		}

		// add localized modifications
		final Map<Locale, Set<String>> localizedDirtyAttributes = modelHistory.getDirtyLocalizedAttributes();
		final Map<String, Set<Locale>> reversed = new HashMap<>();
		for (final Entry<Locale, Set<String>> entry : localizedDirtyAttributes.entrySet())
		{
			for (final String attribute : entry.getValue())
			{
				Set<Locale> locales = reversed.get(attribute);
				if (locales == null)
				{
					locales = new HashSet<>(1);
				}
				locales.add(entry.getKey());
				reversed.put(attribute, locales);
			}
		}
		for (final Entry<String, Set<Locale>> entry : reversed.entrySet())
		{
			final Locale localeBackup = getI18NService().getCurrentLocale();
			try
			{
				final Map<Locale, Object> originalValue = new HashMap<>(entry.getValue().size());
				final Map<Locale, Object> modifiedValue = new HashMap<>(entry.getValue().size());
				for (final Locale locale : entry.getValue())
				{
					getI18NService().setCurrentLocale(locale);
					originalValue.put(locale, getOriginalValue(itemModel, entry.getKey(), locale, modelHistory));
					modifiedValue.put(locale, getModelService().getAttributeValue(itemModel, entry.getKey()));
				}
				info.addEntry(entry.getKey(), true, originalValue, modifiedValue);
			}
			finally
			{
				getI18NService().setCurrentLocale(localeBackup);
			}
		}
		return info;
	}

	protected Object getOriginalValue(final ItemModel model, final String attribute, final ModelValueHistory history)
	{
		Object value = null;
		if (!getModelService().isNew(model) && history.isValueLoaded(attribute))
		{
			value = history.getOriginalValue(attribute);
		}
		return value;
	}

	protected Object getOriginalValue(final ItemModel model, final String attribute, final Locale locale,
			final ModelValueHistory history)
	{
		Object value = null;
		if (!getModelService().isNew(model) && history.isValueLoaded(attribute, locale))
		{
			value = history.getOriginalValue(attribute, locale);
		}
		return value;
	}

	protected void logModifications(final ItemModel model, final ItemModificationInfo modificationInfo)
	{
		final Map<String, Object> originalValues = new HashMap<>();
		final Map<String, Object> modifiedValues = new HashMap<>();
		for (final String attribute : modificationInfo.getModifiedAttributes())
		{
			originalValues.put(attribute,
					toPersistenceLayer(modificationInfo.getOriginalValue(attribute), modificationInfo.isLocalized(attribute)));
			modifiedValues.put(attribute,
					toPersistenceLayer(modificationInfo.getModifiedValue(attribute), modificationInfo.isLocalized(attribute)));
		}
		if (!modifiedValues.isEmpty())
		{
			if (modificationInfo.isNew())
			{
				getJaloConnection().logItemCreation(model.getPk(), modifiedValues);
			}
			else
			{
				getJaloConnection().logItemModification(model.getPk(), modifiedValues, originalValues, false);
			}
		}
		else if (getModelService().isRemoved(model))
		{
			try
			{
				final Object deletedObject = getModelService().getSource(model);
				getJaloConnection().logItemRemoval((Item) deletedObject, false);
			}
			catch (final Exception exception)
			{
				LOG.warn("Could not persist item removal modification history due to unexpected error", exception);
			}
		}
	}

	protected JaloConnection getJaloConnection()
	{
		return JaloConnection.getInstance();
	}

	protected Object toPersistenceLayer(final Object value, final boolean localized)
	{
		Object ret = null;
		if (localized)
		{
			if (value instanceof Map)
			{
				ret = convertLocalizedMapToPersistenceLayer((Map) value);
			}
		}
		else
		{
			ret = getModelService().toPersistenceLayer(value);
		}
		return ret;
	}

	protected Object convertLocalizedMapToPersistenceLayer(final Map<Locale, Object> localizedMap)
	{
		final ModelService modelServiceVar = getModelService();
		final CommonI18NService commonI18NServiceVar = getCommonI18NService();
		final I18NService i18NServiceVar = getI18NService();

		final Map<Language, Object> wrapped = new HashMap<>();
		for (final Entry<Locale, Object> entry : localizedMap.entrySet())
		{
			final Locale locale = entry.getKey();
			LanguageModel language;
			try
			{
				language = commonI18NServiceVar.getLanguage(i18NServiceVar.getBestMatchingLocale(locale).toString());
			}
			catch (final UnknownIdentifierException e)
			{
				language = commonI18NServiceVar.getLanguage(locale.toString());
			}
			wrapped.put(modelServiceVar.getSource(language), modelServiceVar.toPersistenceLayer(entry.getValue()));
		}
		return wrapped;
	}
}

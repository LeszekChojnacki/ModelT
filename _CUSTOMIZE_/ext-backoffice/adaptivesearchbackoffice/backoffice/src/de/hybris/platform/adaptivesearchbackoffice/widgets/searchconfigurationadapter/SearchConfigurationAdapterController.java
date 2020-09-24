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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchconfigurationadapter;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.CURRENT_OBJECT_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_RESULT_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.VALUE_CHANGED_KEY;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractSearchRequestData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Date;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.model.ModelObserver;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Adapter between search result and the editor area widget.
 */
public class SearchConfigurationAdapterController extends DefaultWidgetController
{
	protected static final String AUTO_SAVE_ENABLED_PARAM = "autoSaveEnabled";

	protected static final String REFRESH_SEARCH_CONFIGURATION_IN_SOCKET = "refreshSearchConfiguration";
	protected static final String SEARCH_CONFIGURATION_OUT_SOCKET = "searchConfiguration";
	protected static final String REFRESH_SEARCH_OUT_SOCKET = "refreshSearch";

	// properties of the editor area widget model
	protected static final String SEARCH_RESULT_KEY = "searchResult";

	protected static final String VALUE_CHANGED_OBSERVER_ID = "asSearchConfiguration.valueChanged";

	protected Widgetslot searchConfigurationEditor;

	@WireVariable
	protected transient ModelService modelService;

	@WireVariable
	protected transient AsSearchConfigurationFacade asSearchConfigurationFacade;

	/**
	 * Event handler for search result changes.
	 *
	 * @param searchResult
	 *           - the search result
	 */
	@SocketEvent(socketId = SEARCH_RESULT_SOCKET)
	public void onSearchResultChanged(final SearchResultData searchResult)
	{
		final NavigationContextData navigationContext = searchResult.getNavigationContext();
		final SearchContextData searchContext = searchResult.getSearchContext();

		final WidgetModel searchConfigurationModel = searchConfigurationEditor.getViewModel();
		searchConfigurationModel.removeObserver(VALUE_CHANGED_OBSERVER_ID);

		if (StringUtils.isBlank(navigationContext.getCurrentSearchProfile()))
		{
			searchConfigurationModel.setValue(SEARCH_RESULT_KEY, null);

			sendOutput(SEARCH_CONFIGURATION_OUT_SOCKET, null);
		}
		else
		{
			searchConfigurationModel.setValue(SEARCH_RESULT_KEY, searchResult);

			final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = asSearchConfigurationFacade
					.getOrCreateSearchConfiguration(navigationContext, searchContext);

			// workaround, forces the editor area to re-render
			sendOutput(SEARCH_CONFIGURATION_OUT_SOCKET, modelService.clone(searchConfiguration));

			sendOutput(SEARCH_CONFIGURATION_OUT_SOCKET, searchConfiguration);

			final boolean autoSaveEnabled = getWidgetSettings().getBoolean(AUTO_SAVE_ENABLED_PARAM);
			if (autoSaveEnabled)
			{
				final ModelObserver valueChangedObserver = new ModelObserver()
				{
					@Override
					public Object getId()
					{
						return VALUE_CHANGED_OBSERVER_ID;
					}

					@Override
					public void modelChanged()
					{
						final WidgetModel widgetModel = searchConfigurationEditor.getViewModel();
						final Boolean valueChanged = widgetModel.getValue(VALUE_CHANGED_KEY, Boolean.class);
						final ItemModel currentObject = widgetModel.getValue(CURRENT_OBJECT_KEY, ItemModel.class);

						if (BooleanUtils.isTrue(valueChanged) && (modelService.isModified(currentObject)))
						{
							// makes sure that the currentObject is modified and will be saved
							currentObject.setModifiedtime(new Date());

							final Component saveButton = searchConfigurationEditor.query("#saveButton");
							Events.postEvent(new Event(Events.ON_CLICK, saveButton));
						}
					}
				};

				searchConfigurationModel.addObserver(VALUE_CHANGED_KEY, valueChangedObserver);
			}
		}
	}

	/**
	 * Event handler for search configuration changes.
	 *
	 * @param searchConfiguration
	 *           - the updated search configuration
	 */
	@SocketEvent(socketId = REFRESH_SEARCH_CONFIGURATION_IN_SOCKET)
	public void onSearchConfigurationChanged(final Object searchConfiguration)
	{
		sendOutput(REFRESH_SEARCH_OUT_SOCKET, new Object());
	}

	@SocketEvent(socketId = SEARCH_REQUEST_SOCKET)
	public void onSearchRequest(final AbstractSearchRequestData request)
	{
		sendOutput(SEARCH_REQUEST_SOCKET, request);
	}
}

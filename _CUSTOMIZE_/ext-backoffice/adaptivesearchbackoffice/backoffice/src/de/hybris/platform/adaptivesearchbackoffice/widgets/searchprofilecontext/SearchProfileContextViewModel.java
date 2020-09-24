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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchprofilecontext;


import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_PROFILE_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_RESULT_SOCKET;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContextFactory;
import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchConfigurationService;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.widgets.AbstractWidgetViewModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.events.processes.ProcessFinishedEvent;
import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.labels.LabelService;


/**
 * Controller for the context widget.
 */
public class SearchProfileContextViewModel extends AbstractWidgetViewModel
{
	protected static final String SEARCH_PROFILE_INFO = "searchProfileInfo";
	protected static final String IN_SYNC = "inSync";

	protected static final String CATALOG_SYNC_JOB = "CatalogVersionSyncCronJob";

	@WireVariable
	protected ModelService modelService;

	@WireVariable
	protected SessionService sessionService;

	@WireVariable
	protected I18NService i18nService;

	@WireVariable
	protected CatalogVersionService catalogVersionService;

	@WireVariable
	protected AsSearchProfileService asSearchProfileService;

	@WireVariable
	protected AsSearchConfigurationService asSearchConfigurationService;

	@WireVariable
	protected AsSearchProfileContextFactory asSearchProfileContextFactory;

	@WireVariable
	protected LabelService labelService;

	@WireVariable
	protected SynchronizationFacade synchronizationFacade;

	private NavigationContextData navigationContext;

	private SearchProfileInfoModel searchProfileInfo;
	private boolean inSync;

	@SocketEvent(socketId = SEARCH_RESULT_SOCKET)
	public void update(final SearchResultData searchResult)
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				i18nService.setLocalizationFallbackEnabled(true);

				updateSearchProfileInfo(searchResult);
				updateSyncStatus();
			}
		});
	}

	protected void updateSearchProfileInfo(final SearchResultData searchResult)
	{
		navigationContext = null;
		searchProfileInfo = null;

		if (searchResult == null || searchResult.getNavigationContext() == null || searchResult.getAsSearchResult() == null)
		{
			BindUtils.postNotifyChange(null, null, this, SEARCH_PROFILE_INFO);
			return;
		}

		navigationContext = searchResult.getNavigationContext();

		final Optional<AbstractAsSearchProfileModel> searchProfileOptional = resolveSearchProfile();

		if (!searchProfileOptional.isPresent())
		{
			BindUtils.postNotifyChange(null, null, this, SEARCH_PROFILE_INFO);
			return;
		}

		final AbstractAsSearchProfileModel searchProfile = searchProfileOptional.get();

		final List<CatalogVersionModel> catalogVersions = searchResult.getAsSearchResult().getCatalogVersions();
		final List<CategoryModel> categoryPath = searchResult.getAsSearchResult().getCategoryPath();

		final AsSearchProfileContext searchProfileContext = asSearchProfileContextFactory.createContext(
				navigationContext.getIndexConfiguration(), navigationContext.getIndexType(), catalogVersions, categoryPath);

		final AsSearchConfigurationInfoData searchConfigurationInfo = asSearchConfigurationService
				.getSearchConfigurationInfoForContext(searchProfileContext, searchProfile);

		searchProfileInfo = new SearchProfileInfoModel();
		searchProfileInfo.setSearchConfigurationInfo(searchConfigurationInfo);
		searchProfileInfo.setSearchProfileLabel(buildSearchProfileLabel(searchProfile, searchConfigurationInfo));

		BindUtils.postNotifyChange(null, null, this, SEARCH_PROFILE_INFO);
	}

	protected String buildSearchProfileLabel(final AbstractAsSearchProfileModel searchProfile,
			final AsSearchConfigurationInfoData searchConfigurationInfo)
	{
		return searchProfile.getCode() + " - " + searchConfigurationInfo.getContextType();
	}

	public void updateSyncStatus()
	{
		setInSync(false);

		final Optional<AbstractAsSearchProfileModel> searchProfileOptional = resolveSearchProfile();

		if (searchProfileOptional.isPresent())
		{
			final Optional<Boolean> isInSync = synchronizationFacade.isInSync(searchProfileOptional.get(), Collections.emptyMap());
			if (isInSync.isPresent())
			{
				setInSync(isInSync.get().booleanValue());
			}
		}

		BindUtils.postNotifyChange(null, null, this, IN_SYNC);
	}

	@Command
	public void synchronize()
	{
		final Optional<AbstractAsSearchProfileModel> searchProfileOptional = resolveSearchProfile();

		if (searchProfileOptional.isPresent())
		{
			sendOutput(SEARCH_PROFILE_SOCKET, searchProfileOptional.get());
		}
	}

	@GlobalCockpitEvent(eventName = ProcessFinishedEvent.EVENT_NAME, scope = CockpitEvent.APPLICATION)
	public void processFinished(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getData() instanceof ProcessFinishedEvent)
		{
			final AfterCronJobFinishedEvent processEvent = ((ProcessFinishedEvent) cockpitEvent.getData()).getProcessEvent();

			if (StringUtils.equals(CATALOG_SYNC_JOB, processEvent.getCronJobType()))
			{
				updateSyncStatus();
			}
		}
	}

	protected Optional<AbstractAsSearchProfileModel> resolveSearchProfile()
	{
		if (navigationContext == null || StringUtils.isBlank(navigationContext.getCurrentSearchProfile()))
		{
			return Optional.empty();
		}

		final CatalogVersionModel catalogVersion = navigationContext.getCatalogVersion() == null ? null
				: catalogVersionService.getCatalogVersion(navigationContext.getCatalogVersion().getCatalogId(),
						navigationContext.getCatalogVersion().getVersion());

		final Optional<AbstractAsSearchProfileModel> searchProfileOptional = asSearchProfileService
				.getSearchProfileForCode(catalogVersion, navigationContext.getCurrentSearchProfile());

		if (searchProfileOptional.isPresent())
		{
			modelService.refresh(searchProfileOptional.get());
		}

		return searchProfileOptional;
	}

	public NavigationContextData getNavigationContext()
	{
		return navigationContext;
	}

	public void setNavigationContext(final NavigationContextData navigationContext)
	{
		this.navigationContext = navigationContext;
	}

	public SearchProfileInfoModel getSearchProfileInfo()
	{
		return searchProfileInfo;
	}

	protected void setSearchProfileInfo(final SearchProfileInfoModel searchProfileInfo)
	{
		this.searchProfileInfo = searchProfileInfo;
	}

	public boolean isInSync()
	{
		return inSync;
	}

	protected void setInSync(final boolean isInSync)
	{
		this.inSync = isInSync;
	}
}

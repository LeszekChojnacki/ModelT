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

package de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cockpit.components.contentbrowser.AbstractContentBrowser;
import de.hybris.platform.cockpit.events.CockpitEvent;
import de.hybris.platform.cockpit.events.impl.ItemChangedEvent;
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.BrowserModelListener;
import de.hybris.platform.cockpit.session.PageableBrowserModel;
import de.hybris.platform.cockpit.session.SearchBrowserModel;
import de.hybris.platform.cockpit.session.impl.AbstractBrowserArea;
import de.hybris.platform.cockpit.session.impl.DefaultSearchBrowserArea;
import de.hybris.platform.cockpit.session.impl.DefaultSearchContextBrowserModelListener;
import de.hybris.platform.productcockpit.session.ProductSearchBrowserModelListener;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;


/**
 * Browser area for 'navigation nodes' perspective
 */
public class BundleNavigationNodeBrowserArea extends DefaultSearchBrowserArea implements CatalogVersionAware
{

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(BundleNavigationNodeBrowserArea.class);
	public static final String INFO_AREA_CONTAINER = "infoAreaContainer3";

	private CatalogVersionModel activeCatalogVersion;
	private final BundleNavigationNodeBrowserListener browserListener = new BundleNavigationNodeBrowserListener(this);


	@Override
	public String getLabel()
	{
		return "testlabel";
		//return Labels.getLabel("naviagation.node.area");//is it ever shown to the user?
	}

	@Override
	public void setActiveCatalogVersion(final CatalogVersionModel activeCatalogVersion)
	{
		this.activeCatalogVersion = activeCatalogVersion;
	}

	public CatalogVersionModel getActiveCatalogVersion()
	{
		return activeCatalogVersion;
	}



	@Override
	public Component getInfoArea()
	{
		if (super.getContainerComponent() != null && super.getContainerComponent().getSpaceOwner() != null)
		{
			return super.getContainerComponent().getSpaceOwner().getFellowIfAny(INFO_AREA_CONTAINER);
		}
		return null;
	}

	@Override
	public void onCockpitEvent(final CockpitEvent event)
	{
		super.onCockpitEvent(event);
		if (event instanceof ItemChangedEvent)
		{
			final ItemChangedEvent itemChangedEvent = (ItemChangedEvent) event;
			final BrowserModel focusedBrowserModel = getPerspective().getBrowserArea().getFocusedBrowser();
			switch (itemChangedEvent.getChangeType())
			{
				case CHANGED:
					if (focusedBrowserModel != null)
					{
						focusedBrowserModel.updateItems();
					}
					break;
				case CREATED:
				case REMOVED:
					if (focusedBrowserModel != null)
					{
						focusedBrowserModel.updateItems();
					}
					break;
				default:
					break;
			}
		}
	}

	private class BundleNavigationNodeBrowserListener extends DefaultSearchContextBrowserModelListener implements
			ProductSearchBrowserModelListener
	{
		public BundleNavigationNodeBrowserListener(final AbstractBrowserArea area)
		{
			super(area);
		}

		@Override
		public void blacklistItems(final BrowserModel browserModel, final Collection<Integer> indexes)
		{
			// Empty method
		}

		@Override
		public void changed(final BrowserModel browserModel)
		{
			resetBrowserView(browserModel);
			fireBrowserChanged(browserModel);
		}


		@Override
		public void advancedSearchVisibiltyChanged(final SearchBrowserModel browserModel)
		{
			if (!isBrowserMinimized(browserModel))
			{
				final AbstractContentBrowser contentBrowser = getCorrespondingContentBrowser(browserModel);
				if (contentBrowser != null)
				{
					browserModel.getAdvancedSearchModel().getParameterContainer().clear();
					contentBrowser.updateCaption();
				}
			}
		}

		@Override
		public void pagingChanged(final PageableBrowserModel browserModel)
		{
			if (!isBrowserMinimized(browserModel))
			{
				final AbstractContentBrowser contentBrowser = getCorrespondingContentBrowser(browserModel);
				if (contentBrowser != null)
				{
					contentBrowser.updateToolbar();
				}
			}
		}
	}


	@Override
	public BrowserModelListener getBrowserListener()
	{
		return browserListener;
	}

}

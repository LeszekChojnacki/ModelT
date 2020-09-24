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
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.NavigationAreaListener;
import de.hybris.platform.cockpit.session.impl.BaseUICockpitPerspective;
import de.hybris.platform.cockpit.session.impl.DefaultNavigationAreaListener;
import de.hybris.platform.productcockpit.session.CatalogNavigationAreaListener;
import de.hybris.platform.productcockpit.session.impl.CatalogPerspective;

import java.util.Collections;

import org.apache.log4j.Logger;


public class BundleCatalogPerspective extends CatalogPerspective
{

	private static final Logger LOG = Logger.getLogger(BundleCatalogPerspective.class);

	private NavigationAreaListener navigationAreaListenernavListener;

	@Override
	protected NavigationAreaListener getNavigationAreaListener()
	{
		if (navigationAreaListenernavListener == null)
		{
			navigationAreaListenernavListener = new DefaultBundleNavigationAreaListener(this);
		}
		return navigationAreaListenernavListener;
	}

	public class DefaultBundleNavigationAreaListener extends DefaultNavigationAreaListener implements
			CatalogNavigationAreaListener
	{

		public DefaultBundleNavigationAreaListener(final BaseUICockpitPerspective perspective)
		{
			super(perspective);
		}

		@Override
		public void favoriteCategorySelected()
		{
			// ignore
		}

		@Override
		public void catalogItemSelectionChanged(final CatalogVersionModel version)
		{

			// create new browser model or change selection on existing one
			LOG.info("catalogItemSelectionChanged: " + version.getCatalog().getName() + version.getVersion());

			final BundleNavigationNodeBrowserArea browserArea = (BundleNavigationNodeBrowserArea) getBrowserArea();


			// initialize browserArea if that hasn't been done already (initialize-method will take care of this)
			browserArea.initialize(Collections.emptyMap());
			browserArea.setActiveCatalogVersion(version);

			for (final BrowserModel browserModel : browserArea.getBrowsers())
			{
				if (browserModel instanceof BundleNavigationNodeBrowserModel)
				{
					((BundleNavigationNodeBrowserModel) browserModel).clearPreservedState();
					break;
				}
			}

			browserArea.update();

			if (browserArea.getFocusedBrowser() instanceof BundleNavigationNodeBrowserModel)
			{
				final BundleNavigationNodeBrowserModel browserModel = (BundleNavigationNodeBrowserModel) browserArea
						.getFocusedBrowser();

				getNavigationArea().update();
				browserModel.updateItems(0);
				browserModel.updateLabels();
			}
		}
	}
}
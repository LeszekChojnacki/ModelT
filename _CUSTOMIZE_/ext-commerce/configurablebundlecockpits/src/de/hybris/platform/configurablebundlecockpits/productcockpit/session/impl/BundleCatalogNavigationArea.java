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
import de.hybris.platform.cockpit.session.UIBrowserArea;
import de.hybris.platform.productcockpit.session.impl.CatalogNavigationArea;


/**
 * Represents the category navigation area in bundle perspective
 */
public class BundleCatalogNavigationArea extends CatalogNavigationArea
{

	@Override
	public CatalogVersionModel getSelectedCatalogVersion()
	{
		final CatalogVersionModel ret;
		final UIBrowserArea browserArea = getPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			ret = ((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion();
		}
		else
		{
			ret = null;
		}

		return ret;
	}
}

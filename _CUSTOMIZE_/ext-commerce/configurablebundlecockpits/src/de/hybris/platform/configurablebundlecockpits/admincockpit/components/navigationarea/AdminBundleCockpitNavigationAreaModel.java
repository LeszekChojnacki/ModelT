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

package de.hybris.platform.configurablebundlecockpits.admincockpit.components.navigationarea;

import de.hybris.platform.cockpit.components.navigationarea.DefaultNavigationAreaModel;
import de.hybris.platform.cockpit.session.impl.AbstractUINavigationArea;
import de.hybris.platform.configurablebundlecockpits.admincockpit.session.impl.AdminBundleCockpitNavigationArea;


/**
 * AdminBundle navigation area model.
 */
public class AdminBundleCockpitNavigationAreaModel extends DefaultNavigationAreaModel
{
	public AdminBundleCockpitNavigationAreaModel()
	{
		super();
	}

	public AdminBundleCockpitNavigationAreaModel(final AbstractUINavigationArea area)
	{
		super(area);
	}

	@Override
	public AdminBundleCockpitNavigationArea getNavigationArea()
	{
		return (AdminBundleCockpitNavigationArea) super.getNavigationArea();
	}
}

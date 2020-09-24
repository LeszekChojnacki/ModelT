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

import de.hybris.platform.cockpit.components.contentbrowser.AbstractBrowserComponent;
import de.hybris.platform.cockpit.components.contentbrowser.DefaultAdvancedContentBrowser;

import org.apache.log4j.Logger;


/**
 * Content browser for navigation node see {@link BundleNavigationNodeBrowserModel}. </p>
 */
public class BundleNavigationNodeContentBrowser extends DefaultAdvancedContentBrowser
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(BundleNavigationNodeContentBrowser.class);

	protected final static String ADD_BTN = "/productcockpit/images/node_duplicate.png";
	protected final static String REMOVE_BTN = "/productcockpit/images/cnt_elem_remove_action.png";
	protected final static String NAVIGATION_TOOLBAR_SCLASS = "navigationToolbar";
	protected final static String TOOLBARBUTTON_SCLASS = "toolbarButton";

	public void fireAddRootNavigatioNode()
	{
		final AbstractBrowserComponent mainComponent = getMainAreaComponent();
		if (mainComponent instanceof BundleNavigationNodeContentMainComponent)
		{
			((BundleNavigationNodeContentMainComponent) mainComponent).fireAddRootNavigationNode();
		}
	}

	public void removeSelectedNavigationNode()
	{
		final AbstractBrowserComponent mainComponent = getMainAreaComponent();
		if (mainComponent instanceof BundleNavigationNodeContentMainComponent)
		{
			((BundleNavigationNodeContentMainComponent) mainComponent).removeSelectedNavigationNode();
		}
	}

	@Override
	protected AbstractBrowserComponent createMainAreaComponent()
	{
		return new BundleNavigationNodeContentMainComponent(getModel(), this);
	}

	@Override
	public BundleNavigationNodeBrowserModel getModel()
	{
		return (BundleNavigationNodeBrowserModel) super.getModel();
	}
}

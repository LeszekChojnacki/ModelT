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

package de.hybris.platform.configurablebundlecockpits.admincockpit.session.impl;

import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.PopupEditorAreaController;
import de.hybris.platform.cockpit.session.impl.DefaultEditorAreaController;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * AdminBundle popup editor area controller.
 */
public class AdminBundleCockpitPopupEditorAreaControllerImpl extends DefaultEditorAreaController implements
		PopupEditorAreaController
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(AdminBundleCockpitPopupEditorAreaControllerImpl.class);

	private BrowserModel contextEditorBrowser = null;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Map<String, Object> getAttributesMap()
	{
		return attributes;
	}

	@Override
	public void setContextEditorBrowser(final BrowserModel model)
	{
		this.contextEditorBrowser = model;
	}

	@Override
	public BrowserModel getContextEditorBrowserModel()
	{
		return contextEditorBrowser;
	}
}

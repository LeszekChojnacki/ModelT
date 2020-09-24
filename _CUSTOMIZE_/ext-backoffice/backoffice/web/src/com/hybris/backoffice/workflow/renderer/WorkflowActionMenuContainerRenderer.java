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
package com.hybris.backoffice.workflow.renderer;

import de.hybris.platform.workflow.model.WorkflowActionModel;

import org.zkoss.zul.impl.XulElement;

import com.hybris.cockpitng.common.renderer.AbstractCustomMenuActionRenderer;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;


public class WorkflowActionMenuContainerRenderer extends AbstractCustomMenuActionRenderer<XulElement, ListColumn, WorkflowActionModel>
{
	private static final String SCLASS_MENU_POPUP = "yw-workflows-menu-popup yw-pointer-menupopup yw-pointer-menupopup-top-right";

	@Override
	protected String getMenuPopupSclass()
	{
		return SCLASS_MENU_POPUP;
	}
}

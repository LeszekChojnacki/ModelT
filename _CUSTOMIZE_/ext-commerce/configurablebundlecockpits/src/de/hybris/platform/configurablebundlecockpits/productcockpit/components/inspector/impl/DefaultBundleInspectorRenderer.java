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

package de.hybris.platform.configurablebundlecockpits.productcockpit.components.inspector.impl;

import de.hybris.platform.cockpit.components.inspector.impl.DefaultCoverageInspectorRenderer;
import de.hybris.platform.cockpit.components.listview.ListViewAction.Context;
import de.hybris.platform.cockpit.components.listview.impl.CoverageInfoAction;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.util.ListActionHelper;
import de.hybris.platform.cockpit.util.UITools;
import de.hybris.platform.core.Registry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;


public class DefaultBundleInspectorRenderer extends DefaultCoverageInspectorRenderer
{

	@Override
	public void render(final Component parent, final TypedObject object)
	{
		final String objectTextLabel = UISessionUtils.getCurrentSession().getLabelService()
				.getObjectTextLabelForTypedObject(object);
		final Label label = new Label(objectTextLabel);
		label.setTooltiptext(objectTextLabel);

		final Div headerDiv = new Div();
		headerDiv.setSclass("infoAreaLabel");
		UITools.applyTestID(headerDiv, "inspectorMode");
		headerDiv.appendChild(label);
		parent.appendChild(headerDiv);

		final Div toolbarDiv = new Div();
		toolbarDiv.setSclass("infoHeaderToolbar");
		headerDiv.appendChild(toolbarDiv);
		renderToolbar(toolbarDiv, object);

		final Div propertiesCnt = new Div();
		propertiesCnt.setSclass("inspectorPropertiesCnt");
		parent.appendChild(propertiesCnt);

		propertiesCnt.appendChild(createCoverageComponent(object));
		propertiesCnt.appendChild(createFilledValuesComponent(object));
	}

	@Override
	protected void renderToolbar(final Component parent, final TypedObject item)
	{
		prepareEditActionButton(parent, item);

		final Div coverageActionCnt = new Div();
		coverageActionCnt.setSclass(INSPECTOR_TOOLBAR_BUTTON + " roundedCorners");
		UITools.applyTestID(coverageActionCnt, "inspectorToolbarCoverageButton");

		parent.appendChild(coverageActionCnt);
		final CoverageInfoAction coverageAction = Registry.getApplicationContext().getBean("CoverageInfoAction",
				CoverageInfoAction.class);
		if (coverageAction != null)
		{
			final Context context = coverageAction.createContext(ListActionHelper.createDefaultListModel(item), item);
			ListActionHelper.renderSingleAction(coverageAction, context, coverageActionCnt, INFO_TOOLBAR_ACTION);
		}
	}
}

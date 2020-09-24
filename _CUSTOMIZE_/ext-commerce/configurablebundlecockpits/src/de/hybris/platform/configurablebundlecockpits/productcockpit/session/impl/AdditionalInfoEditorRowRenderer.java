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

import de.hybris.platform.cockpit.components.sectionpanel.SectionPanel;
import de.hybris.platform.cockpit.components.sectionpanel.SectionRow;
import de.hybris.platform.cockpit.session.impl.EditorPropertyRow;
import de.hybris.platform.cockpit.session.impl.EditorRowRenderer;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Box;
import org.zkoss.zul.Label;


public class AdditionalInfoEditorRowRenderer extends EditorRowRenderer
{

	protected final static String ADDITIONAL_INFO_TEXT_SCLASS = "additionalInfoText";

	@Override
	public void render(final SectionPanel panel, final Component parent, final SectionRow row, final Map<String, Object> ctx)
	{
		final String additionalRowInfo = getAdditionalRowInfo(row);
		if (additionalRowInfo == null)
		{
			super.render(panel, parent, row, ctx);
		}
		else
		{
			super.render(panel, parent, row, ctx);

			final Label label = new Label(additionalRowInfo);
			// switch to external style class defintion
			label.setStyle("float: left; color: #999; margin: 1px; margin-bottom: 8px; text-align: left");

			final Component rowContainer = getRowContainer(parent);
			rowContainer.insertBefore(label, rowContainer.getFirstChild());
		}
	}

	protected String getAdditionalRowInfo(final SectionRow row)
	{
		if (row instanceof EditorPropertyRow)
		{
			return ((EditorPropertyRow) row).getRowConfiguration().getParameter("additionalPropertyInfo");
		}
		return null;
	}

	// Bit ugly but currently the only way without changes to the cockpit framework
	protected Component getRowContainer(final Component parent)
	{
		if (parent.getParent() instanceof Box && parent.getParent().getParent() != null) //NOPMD
		{
			return parent.getParent().getParent();
		}
		return parent;
	}

}

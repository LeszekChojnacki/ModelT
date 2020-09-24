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
package de.hybris.platform.commerceservices.backoffice.editor;

import de.hybris.platform.commerceservices.jalo.OrgUnit;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.strategies.OrgUnitAuthorizationStrategy;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.commonconfig.Positioned;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;


/**
 * Shows the Parent Unit assigned to an {@link OrgUnit}
 */
public class ParentUnitEditor extends DefaultEditorAreaPanelRenderer
{
	private OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy;
	private UserService userService;

	@Override
	public void render(final Component parent, final AbstractPanel panel, final Object o, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (o instanceof OrgUnitModel)
		{
			final Div parentWrapper = new Div();
			parentWrapper.setParent(parent);
			UITools.modifySClass(parentWrapper, "yw-editorarea-parent-unit-wrapper", true);

			for (final Positioned positioned : getAttributes(panel))
			{
				if (positioned instanceof Attribute)
				{
					createAttributeRenderer().render(parentWrapper, (Attribute) positioned, o, dataType, widgetInstanceManager);
				}
			}

			// create the editor
			final boolean isReadOnly = !getOrgUnitAuthorizationStrategy().canEditParentUnit(getUserService().getCurrentUser());
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("referenceSearchCondition_supplier", Boolean.TRUE);
			parameters.put("disableDisplayingDetails", String.valueOf(isReadOnly));

			final Editor editor = createEditor(widgetInstanceManager);
			editor.setParameters(parameters);
			editor.setProperty("parentUnit");
			editor.setReadOnly(isReadOnly);
			editor.afterCompose();
			editor.addEventListener(Editor.ON_VALUE_CHANGED, event -> {
				widgetInstanceManager.getModel().changed();
			});

			// create an additional div holding the editor and its label
			final Div addtional = new Div();
			addtional.appendChild(new Label(Labels.getLabel("organization.unit.parent")));
			UITools.modifySClass(addtional, SCLASS_EDITOR_CONTAINER, true);
			addtional.appendChild(editor);

			// finally add the additional div to the parent widget
			parentWrapper.appendChild(addtional);
		}
	}

	protected Editor createEditor(final WidgetInstanceManager widgetInstanceManager)
	{
		final Editor editor = new Editor();
		editor.setWidgetInstanceManager(widgetInstanceManager);
		editor.setType("Reference(OrgUnit)");
		return editor;

	}

	protected List<? extends Positioned> getAttributes(final AbstractPanel panel)
	{
		List<? extends Positioned> ret = new ArrayList<>();
		if (panel instanceof CustomPanel)
		{
			ret = ((CustomPanel) panel).getAttributeOrCustom();
		}
		return ret;

	}

	protected OrgUnitAuthorizationStrategy getOrgUnitAuthorizationStrategy()
	{
		return orgUnitAuthorizationStrategy;
	}

	@Required
	public void setOrgUnitAuthorizationStrategy(final OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy)
	{
		this.orgUnitAuthorizationStrategy = orgUnitAuthorizationStrategy;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}

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

import de.hybris.platform.core.model.user.EmployeeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Shows the organization roles assigned to a sales unit employee
 */
public class DefaultOrgUnitEmployeeRolesEditor extends DefaultEditorAreaPanelRenderer
{
	@Override
	public void render(final Component parent, final AbstractPanel panel, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (object instanceof EmployeeModel)
		{
			final Div parentWrapper = new Div();
			parentWrapper.setParent(parent);
			UITools.modifySClass(parentWrapper, "yw-editorarea-org-roles-wrapper", true);

			for (final Positioned positioned : getAttributes(panel))
			{
				if (positioned instanceof Attribute)
				{
					createAttributeRenderer().render(parentWrapper, (Attribute) positioned, object, dataType, widgetInstanceManager);
				}
			}

			// create the editor
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("availableValuesProvider", "orgRolesEditorSearchFacade");
			final Editor editor = createEditor(widgetInstanceManager);
			editor.setParameters(parameters);
			editor.setProperty("orgRoles");
			editor.afterCompose();
			editor.addEventListener(Editor.ON_VALUE_CHANGED, event -> {
				widgetInstanceManager.getModel().changed();
			});

			// create an additional div holding the editor and its label
			final Div addtional = new Div();
			addtional.appendChild(new Label(Labels.getLabel("organization.sales.employee.roles")));
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
		editor.setType("MultiReference-SET(UserGroup)");
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
}

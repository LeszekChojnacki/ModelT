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
package de.hybris.platform.b2bcommerce.backoffice.editor;

import de.hybris.platform.commerceservices.jalo.OrgUnit;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.strategies.OrgUnitAuthorizationStrategy;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;

import com.google.common.collect.Maps;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


/**
 * Shows a list of customers assigned to a {@link OrgUnit}
 */
public class OrgUnitCustomersEditor extends AbstractWidgetComponentRenderer<Component, CustomPanel, OrgUnitModel>
{
	private OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy;
	private UserService userService;

	@Override
	public void render(final Component parent, final CustomPanel customPanel, final OrgUnitModel currentObject, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final boolean canEditUnit = getOrgUnitAuthorizationStrategy().canEditUnit(getUserService().getCurrentUser());
		final Editor editor = createEditor(widgetInstanceManager);
		final Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("listConfigContext", "referenceListViewUnitCustomers");
		parameters.put("disableDisplayingDetails", String.valueOf(!canEditUnit));
		parameters.put("disableRemoveReference", String.valueOf(!canEditUnit));
		editor.setParameters(parameters);

		editor.setProperty("customersChanged");
		editor.setReadOnly(!canEditUnit);
		editor.afterCompose();
		editor.addEventListener(Editor.ON_VALUE_CHANGED, event -> {
			widgetInstanceManager.getModel().changed();
		});

		parent.appendChild(editor);
	}

	protected Editor createEditor(final WidgetInstanceManager widgetInstanceManager)
	{
		final Editor editor = new Editor();
		editor.setWidgetInstanceManager(widgetInstanceManager);
		editor.setType("ExtendedMultiReference-LIST(B2BUnit)");
		return editor;
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

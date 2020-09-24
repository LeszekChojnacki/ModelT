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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionDefinition;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.components.AbstractCockpitElementsContainer;
import com.hybris.cockpitng.components.DefaultCockpitActionsRenderer;
import com.hybris.cockpitng.core.CockpitComponentDefinitionService;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Action;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.ActionGroup;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Actions;
import com.hybris.cockpitng.core.util.impl.TypedSettingsMap;
import com.hybris.cockpitng.engine.CockpitWidgetEngine;
import com.hybris.cockpitng.engine.ComponentWidgetAdapterAware;
import com.hybris.cockpitng.engine.impl.ComponentWidgetAdapter;
import com.hybris.cockpitng.util.labels.CockpitComponentDefinitionLabelLocator;


/**
 * Default renderer for {@link ActionsMenu} component.
 *
 */
public class DefaultActionsMenuRenderer extends DefaultCockpitActionsRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultActionsMenuRenderer.class);

	protected static final String ACTIONS_SCLASS = "yas-actions";
	protected static final String ACTIONS_ICON_SCLASS = "z-icon-ellipsis-v";
	protected static final String ACTIONS_BUTTON_SCLASS = "yas-actions-button";
	protected static final String ACTIONS_POPUP_SCLASS = "yas-actions-popup";
	protected static final String ACTION_MENUITEM_SCLASS = "yas-action";

	private CockpitComponentDefinitionService componentDefinitionService;

	@Override
	public void render(final AbstractCockpitElementsContainer parent, final Object configuration)
	{
		if (configuration instanceof Actions && parent instanceof ActionsMenu)
		{
			final Actions actions = (Actions) configuration;
			final ActionsMenu parentDiv = (ActionsMenu) parent;
			parentDiv.setSclass(ACTIONS_SCLASS);

			final Menupopup actionsMenu = new Menupopup();
			actionsMenu.setParent(parentDiv);
			actionsMenu.setSclass(ACTIONS_POPUP_SCLASS);

			renderActions(parentDiv, actions, actionsMenu);

			final Button actionsButton = new Button();
			actionsButton.setParent(parentDiv);
			actionsButton.setSclass(ACTIONS_BUTTON_SCLASS);
			actionsButton.setIconSclass(ACTIONS_ICON_SCLASS);
			actionsButton.setPopup(actionsMenu);

			if (actionsMenu.getChildren().size() == 0)
			{
				actionsButton.setDisabled(true);
			}
		}
	}

	protected void renderActions(final ActionsMenu menuActions, final Actions actions, final Menupopup parent)
	{
		if (actions == null || actions.getGroup() == null)
		{
			return;
		}

		for (final ActionGroup group : actions.getGroup())
		{
			if (group.getActions() != null)
			{
				renderActionsGroup(group, menuActions, parent);
			}
		}

		parent.removeChild(parent.getLastChild());
	}

	public void renderActionsGroup(final ActionGroup actionGroup, final ActionsMenu menuActions, final Menupopup parent)
	{
		for (final Action action : actionGroup.getActions())
		{
			final Menuitem actionMenuitem = renderAction(menuActions, action);
			if (actionMenuitem != null)
			{
				actionMenuitem.setParent(parent);
			}

		}
		parent.getChildren().add(new Menuseparator());
	}

	protected Menuitem renderAction(final ActionsMenu menuActions, final Action action)
	{
		try
		{
			final ActionDefinition definition = (ActionDefinition) getComponentDefinitionService()
					.getComponentDefinitionForCode(action.getActionId());
			final String classname = definition.getActionClassName();
			final CockpitAction<?, ?> cockpitAction = getComponentDefinitionService().createAutowiredComponent(definition, classname,
					SpringUtil.getApplicationContext());
			final ActionContext actionContext = createActionContext(menuActions, action, definition);

			if (cockpitAction instanceof ComponentWidgetAdapterAware)
			{
				final ComponentWidgetAdapterAware componentWidgetAdapterAware = (ComponentWidgetAdapterAware) cockpitAction;
				componentWidgetAdapterAware.initialize(getComponentWidgetAdapter(), action.getActionId());
			}

			final boolean enabled = cockpitAction.canPerform(actionContext);
			if (!enabled)
			{
				return null;
			}

			final Menuitem actionMenuitem = new Menuitem();
			actionMenuitem.setDisabled(false);

			actionMenuitem.setSclass(ACTION_MENUITEM_SCLASS);
			final Object baseUri = definition.getLocationPath();
			actionMenuitem.setImage(baseUri + "/" + definition.getIconUri());
			actionMenuitem.setHoverImage(baseUri + "/" + definition.getIconHoverUri());
			final String name = actionContext.getLabel(definition.getName());
			actionMenuitem.setLabel(name);

			actionMenuitem.addEventListener(Events.ON_CLICK, event -> cockpitAction.perform(actionContext));

			return actionMenuitem;

		}
		catch (ReflectiveOperationException e)
		{
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	protected ActionContext createActionContext(final ActionsMenu menuActions, final Action action,
			final ActionDefinition actionDefinition)
	{
		final Map<String, Object> parameters = new HashMap<>();
		if (actionDefinition != null)
		{
			final TypedSettingsMap settings = actionDefinition.getDefaultSettings();
			if (MapUtils.isNotEmpty(settings))
			{
				parameters.putAll(settings.getAll());
			}

			parameters.put(CockpitWidgetEngine.COMPONENT_ROOT_PARAM, actionDefinition.getLocationPath());
			parameters.put(CockpitWidgetEngine.COMPONENT_RESOURCE_PATH_PARAM, actionDefinition.getResourcePath());
		}

		if (MapUtils.isNotEmpty(menuActions.getAttributes()))
		{
			parameters.putAll(menuActions.getAttributes());
		}

		final Map<String, Object> labels = actionDefinition != null
				? CockpitComponentDefinitionLabelLocator.getLabelMap(actionDefinition) : Collections.emptyMap();

		return new ActionContext(menuActions.getInputValue(), actionDefinition, parameters, labels);
	}

	public CockpitComponentDefinitionService getComponentDefinitionService()
	{
		if (componentDefinitionService == null)
		{
			componentDefinitionService = (CockpitComponentDefinitionService) SpringUtil.getBean("componentDefinitionService");
		}
		return componentDefinitionService;
	}

	public ComponentWidgetAdapter getComponentWidgetAdapter()
	{
		return (ComponentWidgetAdapter) SpringUtil.getBean("componentWidgetAdapter");
	}
}

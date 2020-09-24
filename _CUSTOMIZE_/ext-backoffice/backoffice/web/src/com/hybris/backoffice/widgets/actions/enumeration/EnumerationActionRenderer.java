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
package com.hybris.backoffice.widgets.actions.enumeration;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.enumeration.EnumerationService;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Window;

import com.google.common.collect.ImmutableList;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionListener;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;
import com.hybris.cockpitng.components.DefaultCockpitActionsRenderer;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.CockpitComponentsUtils;
import com.hybris.cockpitng.util.type.BackofficeTypeUtils;


/**
 * EnumerationActionRenderer is default renderer of EnumerationAction. It allows to display list of the enums on the
 * list. It supports additional view mode which allows to render action as menu item viewMode=menu. It should be used
 * when action is nested in the action group.
 */
public class EnumerationActionRenderer extends DefaultActionRenderer<Collection<Object>, Object>
{

	protected static final String LABEL_ENUMERATION_ACTION_UPDATE_STATUS = "enumeration.action.update.status";

	protected static final String SCLASS_YW_ENUMERATION_ACTION_POPUP = "yw-enumeration-action-popup yw-pointer-menupopup yw-pointer-menupopup-top";
	protected static final String SCLASS_YW_ENUMERATION_ACTION_CONTAINER = "yw-enumeration-action-container";
	protected static final String SCLASS_YW_ENUMERATION_ACTION_MENU_CONTAINER = "yw-enumeration-action-menu-container";
	protected static final String SCLASS_YW_ENUMERATION_ACTION_MENU_DISABLED = "yw-enumeration-action-menu-disabled";
	protected static final String SCLASS_YW_ENUMERATION_ACTION_MENU_POPUP = "yw-enumeration-action-menu-popup";
	protected static final String SCLASS_YW_ENUMERATION_ACTION_MENU_POPUP_MENUITEM = "yw-enumeration-action-menu-popup-menuitem";

	private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationActionRenderer.class);
	public static final String VIEW_MODE_MENU = "menu";
	public static final String ORIENT_VERTICAL = "vertical";

	@Resource
	private EnumerationService enumerationService;
	@Resource
	private TypeFacade typeFacade;
	@Resource
	private BackofficeTypeUtils typeUtils;
	@Resource
	private LabelService labelService;

	@Override
	public void render(final Component parent, final CockpitAction<Collection<Object>, Object> action,
			final ActionContext<Collection<Object>> context, final boolean updateMode, final ActionListener<Object> listener)
	{

		if (isViewMode(context, VIEW_MODE_MENU))
		{
			if (!checkDataCompatibleWithDefinedInput(action, context))
			{
				parent.getChildren().clear();
			}
			else
			{

				renderAsMenu(parent, action, context, updateMode, listener);
			}
		}
		else
		{
			super.render(parent, action, context, updateMode, listener);
		}
	}

	protected void renderAsMenu(final Component parent, final CockpitAction<Collection<Object>, Object> action,
			final ActionContext<Collection<Object>> context, final boolean updateMode, final ActionListener<Object> listener)
	{
		final List<Object> dataToUpdate = prepareData(context.getData());
		final String qualifier = (String) context.getParameter(EnumerationAction.PARAMETER_QUALIFIER);

		final boolean enabled = action.canPerform(context);
		final Menubar menubar = new Menubar();
		menubar.setSclass(SCLASS_YW_ENUMERATION_ACTION_MENU_CONTAINER);
		menubar.setAutodrop(true);
		menubar.setOrient(ORIENT_VERTICAL);
		parent.appendChild(menubar);

		final Menu menu = new Menu(getLocalizedName(context));
		menu.setParent(menubar);

		if (enabled)
		{
			final Menupopup menupopup = new Menupopup();
			menupopup.setSclass(SCLASS_YW_ENUMERATION_ACTION_MENU_POPUP);
			menupopup.setParent(menu);
			prepareEnums(qualifier, dataToUpdate).stream()
					.map(enumValue -> createMenuEntry(parent, action, context, listener, enumValue)).forEach(menupopup::appendChild);
		}
		else
		{
			menu.setDisabled(true);
			menu.setSclass(SCLASS_YW_ENUMERATION_ACTION_MENU_DISABLED);
		}
	}

	protected Menuitem createMenuEntry(final Component parent, final CockpitAction<Collection<Object>, Object> action,
			final ActionContext<Collection<Object>> context, final ActionListener<Object> listener, final HybrisEnumValue enumValue)
	{
		final Menuitem enumEntry = new Menuitem(labelService.getObjectLabel(enumValue));
		enumEntry.setIconSclass(" ");
		enumEntry.setSclass(SCLASS_YW_ENUMERATION_ACTION_MENU_POPUP_MENUITEM);
		enumEntry.addEventListener(Events.ON_CLICK, event -> {
			context.setParameter(EnumerationAction.ENUMERATION_KEY, enumValue);
			closeActionGroupPopup(parent);
			performWithConfirmationCheck(action, context, listener);
		});
		return enumEntry;
	}

	protected void closeActionGroupPopup(final Component parent)
	{
		CockpitComponentsUtils.findClosestComponent(parent, Popup.class, DefaultCockpitActionsRenderer.SCLASS_ACTION_GROUP_POPUP)
				.ifPresent(Popup::close);
	}

	protected boolean isViewMode(final ActionContext<Collection<Object>> context, final String viewMode)
	{
		return viewMode.equalsIgnoreCase((String) context.getParameter(ActionContext.VIEW_MODE_PARAM));
	}

	@Override
	protected EventListener<? extends Event> createEventListener(final CockpitAction<Collection<Object>, Object> action,
			final ActionContext<Collection<Object>> context, final ActionListener<Object> listener)
	{
		final EventListener<? extends Event> eventListener = super.createEventListener(action, context, listener);

		final String qualifier = (String) context.getParameter(EnumerationAction.PARAMETER_QUALIFIER);

		final Map<String, Object> cache = new HashMap<>();

		return event -> {
			if (isWindowAlreadyOpened(event.getTarget()))
			{
				return;
			}
			final List<Object> dataToUpdate = prepareData(context.getData());
			final List<HybrisEnumValue> hybrisEnumValues = (List<HybrisEnumValue>) cache.computeIfAbsent("enums",
					i -> prepareEnums(qualifier, dataToUpdate));

			final Window window = (Window) cache.computeIfAbsent("window", i -> {
				final Window w = createWindow();
				w.appendChild(createEnumList(hybrisEnumValues, context, afterEventCloseWindow(eventListener, w)));
				w.setParent(event.getTarget().getParent());
				return w;
			});
			if (window.getParent() == null)
			{
				window.setParent(event.getTarget().getParent());
			}
			openWindow(window);
		};
	}

	private EventListener<Event> afterEventCloseWindow(final EventListener<? extends Event> eventListener, final Window window)
	{
		return event -> {
			((EventListener<Event>) eventListener).onEvent(event);
			window.setVisible(false);
		};
	}

	protected List<Object> prepareData(final Collection<Object> data)
	{
		return ImmutableList
				.copyOf(CollectionUtils.emptyIfNull(data).stream().filter(Objects::nonNull).collect(Collectors.toList()));
	}

	protected List<HybrisEnumValue> prepareEnums(final String qualifier, final List<Object> dataToUpdate)
	{
		final String type = typeUtils.findClosestSuperType(dataToUpdate);
		final DataType dataType;
		try
		{
			final DataAttribute attribute = typeFacade.load(type).getAttribute(qualifier);
			if (attribute == null)
			{
				LOGGER.warn("Cannot find attribute {} on type {}", type, qualifier);
				return Collections.emptyList();
			}
			dataType = attribute.getDefinedType();
		}
		catch (final TypeNotFoundException e)
		{
			LOGGER.error("Supertype of given input data not found {}", e);
			return Collections.emptyList();
		}

		return dataType.isEnum() ? enumerationService.getEnumerationValues(dataType.getCode()) : Collections.emptyList();
	}

	protected Window createWindow()
	{
		final Window window = new Window();
		window.setSclass(SCLASS_YW_ENUMERATION_ACTION_POPUP);
		return window;
	}

	protected HtmlBasedComponent createEnumList(final List<HybrisEnumValue> hybrisEnumValues,
			final ActionContext<Collection<Object>> context, final EventListener<? extends Event> eventListener)
	{
		final Div div = new Div();
		div.setSclass(SCLASS_YW_ENUMERATION_ACTION_CONTAINER);
		final Radiogroup radiogroup = new Radiogroup();

		final Map<Radio, HybrisEnumValue> map = hybrisEnumValues.stream().collect(Collectors
				.toMap(hybrisEnumValue -> new Radio(labelService.getObjectLabel(hybrisEnumValue)), Function.identity(), (k, v) -> k));

		attachListenerToMenuitem(map, context);
		map.keySet().stream().sorted(Comparator.comparing(Radio::getLabel)).forEach(radio -> {
			radio.setRadiogroup(radiogroup);
			radio.setParent(radiogroup);
		});

		div.appendChild(radiogroup);
		final Button button = createButton(LABEL_ENUMERATION_ACTION_UPDATE_STATUS, eventListener, context);
		button.setDisabled(true);
		div.appendChild(button);
		radiogroup.addEventListener(Events.ON_CHECK, e -> button.setDisabled(false));

		return div;
	}

	protected Button createButton(final String labelKey, final EventListener<? extends Event> clickEventListener,
			final ActionContext<Collection<Object>> context)
	{
		final Button button = new Button(context.getLabel(labelKey));
		button.addEventListener(Events.ON_CLICK, clickEventListener);
		return button;
	}

	protected void attachListenerToMenuitem(final Map<Radio, HybrisEnumValue> map, final ActionContext<Collection<Object>> context)
	{
		map.forEach((radioKey, hybrisEnumValue) -> radioKey.addEventListener(Events.ON_CHECK,
				event -> context.setParameter(EnumerationAction.ENUMERATION_KEY, hybrisEnumValue)));
	}

	protected void openWindow(final Window window)
	{
		window.setPosition("parent");
		window.setTop("35px");
		window.setLeft("0px");
		window.doPopup();
	}

	protected boolean isWindowAlreadyOpened(final Component component)
	{
		return CollectionUtils.emptyIfNull(component.getChildren()).stream().filter(Window.class::isInstance)//
				.map(Window.class::cast)//
				.anyMatch(AbstractComponent::isVisible);
	}

}

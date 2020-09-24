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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ACTION_WIDGET_INSTANCE_MANAGER_KEY;

import de.hybris.platform.adaptivesearchbackoffice.components.ActionsMenu;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRenderer;

import java.util.StringJoiner;

import javax.annotation.Resource;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.editor.instant.InstantEditorRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;


/**
 * Default renderer for the item master section.
 *
 * @param <D>
 *           - the type of the item data
 * @param <V>
 *           - the type of the item value
 */
public class DefaultAsMultiReferenceItemMasterRenderer<D extends AbstractEditorData, V>
		implements EditorRenderer<MultiReferenceEditorLogic<D, V>, D>
{
	protected static final String TOGGLE_SCLASS = "yas-toggle";
	protected static final String TOGGLE_DISABLED_SCLASS = "yas-toggle-disabled";
	protected static final String TOGGLE_ICON_SCLASS = "yas-toggle-icon";
	protected static final String TOGGLE_ICON_OPEN_SCLASS = "z-icon-caret-down";
	protected static final String TOGGLE_ICON_CLOSED_SCLASS = "z-icon-caret-right";

	protected static final String INFO_SCLASS = "yas-info";
	protected static final String INFO_ICON_SCLASS = "yas-info-icon";

	protected static final String LABEL_SCLASS = "yas-label";

	protected static final String PROPERTY_SCLASS = "yas-property";
	protected static final String EDITABLE_SCLASS = "yas-editable";

	protected static final String ACTIONS_CONTEXT_SUFFIX = "-actions";

	protected static final String EDITOR_LOGIC_KEY = "editorLogic";

	@Resource
	private LabelService labelService;

	@Override
	public boolean isEnabled(final MultiReferenceEditorLogic<D, V> logic)
	{
		return true;
	}

	@Override
	public boolean canRender(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		return true;
	}

	@Override
	public void render(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		renderToggle(logic, parent, data);
		renderInfo(logic, parent, data);
		renderLabel(logic, parent, data);
		renderProperties(logic, parent, data);
		renderActions(logic, parent, data);
	}

	protected void renderToggle(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		final EditorRenderer itemDetailRenderer = logic.getItemDetailRenderer();

		if (itemDetailRenderer.isEnabled(logic))
		{
			final Div itemToggleDiv = new Div();
			itemToggleDiv.setParent(parent);

			final Div itemToggleIconDiv = new Div();
			itemToggleIconDiv.setParent(itemToggleDiv);

			if (itemDetailRenderer.canRender(logic, parent, data))
			{
				final Component item = logic.findEditorItem(parent);
				final boolean open = logic.isOpen(item);

				itemToggleDiv.setSclass(buildToggleSclass(true));
				itemToggleIconDiv.setSclass(buildToggleIconSclass(open));

				item.addEventListener(Events.ON_OPEN, event -> {
					final OpenEvent openEvent = (OpenEvent) event;
					itemToggleIconDiv.setSclass(buildToggleIconSclass(openEvent.isOpen()));
				});
			}
			else
			{
				itemToggleDiv.setSclass(buildToggleSclass(false));
				itemToggleIconDiv.setSclass(buildToggleIconSclass(false));
			}
		}
	}

	protected String buildToggleSclass(final boolean enabled)
	{
		final StringJoiner styleClass = new StringJoiner(" ");
		styleClass.add(TOGGLE_SCLASS);

		if (!enabled)
		{
			styleClass.add(TOGGLE_DISABLED_SCLASS);
		}

		return styleClass.toString();
	}

	protected String buildToggleIconSclass(final boolean open)
	{
		final StringJoiner styleClass = new StringJoiner(" ");
		styleClass.add(TOGGLE_ICON_SCLASS);

		if (open)
		{
			styleClass.add(TOGGLE_ICON_OPEN_SCLASS);
		}
		else
		{
			styleClass.add(TOGGLE_ICON_CLOSED_SCLASS);
		}

		return styleClass.toString();
	}

	protected void renderInfo(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		final Div infoDiv = new Div();
		infoDiv.setParent(parent);
		infoDiv.setSclass(INFO_SCLASS);

		final Div infoIconDiv = new Div();
		infoIconDiv.setParent(infoDiv);
		infoIconDiv.setSclass(INFO_ICON_SCLASS);
	}

	protected void renderLabel(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		final Div labelDiv = new Div();
		labelDiv.setParent(parent);
		labelDiv.setSclass(LABEL_SCLASS);

		final Label label = new Label();
		label.setParent(labelDiv);
		label.setValue(data.getLabel());
	}

	protected void renderProperties(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		logic.getColumns().stream().forEach(property -> renderProperty(logic, parent, data, property));
	}

	protected void renderProperty(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data,
			final String property)
	{
		final DataHandler dataHandler = logic.getDataHandler();
		final Object attributeValue = dataHandler.getAttributeValue(data, property);
		final Class<?> attributeType = dataHandler.getAttributeType(data, property);

		final Div propertyDiv = new Div();
		propertyDiv.setParent(parent);
		propertyDiv.setSclass(buildPropertySclass(property));

		if (logic.getEditableColumns().contains(property) && data.isFromSearchConfiguration())
		{
			UITools.modifySClass(propertyDiv, EDITABLE_SCLASS, true);

			final Editor editor = new Editor();
			editor.setParent(propertyDiv);
			editor.setInitialValue(attributeValue);
			editor.setDefaultEditor(InstantEditorRenderer.EDITOR_ID);
			editor.setType(attributeType.getName());
			editor.setReadOnly(false);
			editor.setOptional(false);
			editor.afterCompose();
			editor.addEventListener(Editor.ON_VALUE_CHANGED, event -> logic.updateAttributeValue(data, property, event.getData()));
		}
		else
		{
			if (attributeValue != null)
			{
				final Label label = new Label();
				label.setParent(propertyDiv);
				label.setValue(labelService.getObjectLabel(attributeValue));
			}
		}
	}

	protected String buildPropertySclass(final String property)
	{
		final StringBuilder styleClass = new StringBuilder();
		styleClass.append(PROPERTY_SCLASS);
		styleClass.append(' ');
		styleClass.append(PROPERTY_SCLASS).append('-').append(property);

		return styleClass.toString();
	}

	protected void renderActions(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		final String actionsContext = logic.getContext() + ACTIONS_CONTEXT_SUFFIX;
		final WidgetInstanceManager widgetInstanceManager = logic.getWidgetInstanceManager();

		final ActionsMenu actionsMenu = new ActionsMenu();
		actionsMenu.setInputValue(data);
		actionsMenu.setConfig(actionsContext);
		actionsMenu.setWidgetInstanceManager(widgetInstanceManager);
		actionsMenu.setAttribute(ACTION_WIDGET_INSTANCE_MANAGER_KEY, widgetInstanceManager);
		actionsMenu.setAttribute(EDITOR_LOGIC_KEY, logic);
		actionsMenu.initialize();
		actionsMenu.setParent(parent);
	}
}

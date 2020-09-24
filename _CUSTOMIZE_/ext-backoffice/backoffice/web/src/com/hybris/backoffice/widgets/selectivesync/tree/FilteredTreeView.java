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
package com.hybris.backoffice.widgets.selectivesync.tree;

import java.util.Objects;
import java.util.Optional;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeitemRenderer;

import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/** Tree view for {@link SelectiveSyncRenderer}. */
public class FilteredTreeView extends Div
{
	protected static final String TOGGLE_INCLUDED_ATTRIBUTES_EVENT = "onToggleIncludedAttributesEvent";
	protected static final String TOGGLE_NOT_INCLUDED_ATTRIBUTES_EVENT = "onToggleNotIncludedAttributesEvent";
	protected static final String FILTER_CHANGE_EVENT = "onSearchFilterChangeEvent";
	protected static final String MODEL_FILTER_CONTEXT = "filterContext";

	private static final String POPUP_ATTRIBUTE_IDENTIFIER = "Sync_Popup_Attribute_Identifier";
	private static final String LABEL_SEARCH = "syncAttribute.search";
	private static final String LABEL_INCLUDED = "syncAttribute.included";
	private static final String LABEL_NOT_INCLUDED = "syncAttribute.notIncluded";
	private static final String LABEL_FILTER = "syncAttribute.filter";

	private static final String SCLASS_CHECKBOX_SWITCH = "ye-switch-checkbox";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_TREE = "ye-selsync-attribute-config-tree";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_TREE_VIEW = "ye-selsync-attribute-config-tree-view";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER = "ye-selsync-attribute-config-filter";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_SEARCH = "ye-selsync-attribute-config-filter-search";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_BUTTON = "ye-selsync-attribute-config-filter-button";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_POPUP = "ye-selsync-attribute-config-filter-popup yw-pointer-menupopup yw-pointer-menupopup-top-right";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_POPUP_ITEM = "ye-selsync-attribute-config-filter-popup-item";

	private static final String YTESTID_ATTRIBUTE = "ytestid";
	private static final String FILTER_INCLUDED_YTESTID = "filter_included_checkbox";
	private static final String FILTER_NOT_INCLUDED_YTESTID = "filter_not_included_checkbox";

	private final Tree tree = new Tree();
	private final SyncAttributeTreeModel treeModel;

	private transient FilterContext filterContext;
	private transient WidgetInstanceManager widgetInstanceManager;

	public FilteredTreeView(final SyncAttributeTreeModel treeModel, final WidgetInstanceManager widgetInstanceManager)
	{
		this.treeModel = treeModel;
		this.widgetInstanceManager = widgetInstanceManager;

		this.filterContext = FilteredTreeView.this.getOrCreateFilterContext();

		tree.setModel(treeModel.filter(filterContext));
		tree.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_TREE);
		FilteredTreeView.this.addTreeListeners();

		final Div treePane = new Div();
		treePane.appendChild(FilteredTreeView.this.createFilterPanel());
		treePane.appendChild(tree);

		FilteredTreeView.this.appendChild(treePane);
		FilteredTreeView.this.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_TREE_VIEW);
	}

	protected FilterContext getOrCreateFilterContext()
	{
		return Optional //
				.ofNullable(widgetInstanceManager.getModel().getValue(MODEL_FILTER_CONTEXT, FilterContext.class)) //
				.orElseGet(FilterContext::new);

	}

	protected void addTreeListeners()
	{
		tree.addEventListener(TOGGLE_INCLUDED_ATTRIBUTES_EVENT, this::executeModelChange);
		tree.addEventListener(TOGGLE_NOT_INCLUDED_ATTRIBUTES_EVENT, this::executeModelChange);
		tree.addEventListener(FILTER_CHANGE_EVENT, this::executeModelChange);
	}

	private void executeModelChange(final Event event)
	{
		widgetInstanceManager.getModel().setValue(MODEL_FILTER_CONTEXT, this.filterContext);
		tree.setModel(this.treeModel.filter(filterContext));
	}

	/**
	 * Setter for {@link TreeitemRenderer}
	 *
	 * @param renderer
	 *           renderer to set on tree
	 */
	public void setTreeItemRenderer(final TreeitemRenderer<?> renderer)
	{
		tree.setItemRenderer(renderer);
	}

	protected Div createFilterPanel()
	{
		final Textbox search = createFilterTextbox();

		final Button filterButton = createFilterButton();

		return createFilterContainer(search, filterButton);
	}

	protected Textbox createFilterTextbox()
	{
		final Textbox search = new Textbox();
		search.setPlaceholder(getLabel(LABEL_SEARCH));
		search.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_SEARCH);
		search.addEventListener(Events.ON_CHANGING, (final InputEvent event) -> {
			filterContext.setFilterQuery(event.getValue());
			Events.echoEvent(FILTER_CHANGE_EVENT, tree, event.getValue());
		});
		search.setValue(filterContext.getFilterQuery());
		return search;
	}

	protected Button createFilterButton()
	{
		final Button filterButton = new Button(getLabel(LABEL_FILTER));
		filterButton.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_BUTTON);
		filterButton.addEventListener(Events.ON_CLICK, event -> {
			final Component target = event.getTarget().getParent();
			final Popup popup = findOrCreateActionPopup(target);
			if (!popup.isVisible())
			{
				popup.open(target, "after_end");
			}
		});
		return filterButton;
	}

	protected Div createFilterContainer(final Textbox search, final Button filterButton)
	{
		final Div filterPane = new Div();
		filterPane.appendChild(search);
		filterPane.appendChild(filterButton);
		filterPane.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER);
		return filterPane;
	}

	protected String getLabel(final String name)
	{
		return widgetInstanceManager.getLabel(name);
	}

	protected Popup findOrCreateActionPopup(final Component target)
	{
		return target.getChildren() //
				.stream() //
				.filter(child -> Boolean.TRUE.equals(child.getAttribute(POPUP_ATTRIBUTE_IDENTIFIER))) //
				.filter(child -> child instanceof Popup) //
				.map(child -> (Popup) child) //
				.findFirst() //
				.orElseGet(() -> createPopup(target));
	}

	protected Popup createPopup(final Component target)
	{
		final Popup popup = new Popup();
		popup.setAttribute(POPUP_ATTRIBUTE_IDENTIFIER, Boolean.TRUE);
		popup.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_POPUP);

		final Div list = new Div();

		list.appendChild(createPopupItem(getLabel(LABEL_INCLUDED), filterContext.getShowIncluded(), (final CheckEvent event) -> {
			filterContext.setShowIncluded(event.isChecked());
			Events.echoEvent(TOGGLE_INCLUDED_ATTRIBUTES_EVENT, tree, event);
		}, FILTER_INCLUDED_YTESTID));

		list.appendChild(
				createPopupItem(getLabel(LABEL_NOT_INCLUDED), filterContext.getShowNotIncluded(), (final CheckEvent event) -> {
					filterContext.setShowNotIncluded(event.isChecked());
					Events.echoEvent(TOGGLE_NOT_INCLUDED_ATTRIBUTES_EVENT, tree, event);
				}, FILTER_NOT_INCLUDED_YTESTID));

		popup.appendChild(list);
		target.appendChild(popup);
		return popup;
	}

	protected Div createPopupItem(final String label, final boolean checked,
			final EventListener<? extends Event> onCheckedEventListener, final String ytestid)
	{
		final Checkbox checkbox = new Checkbox();
		checkbox.setChecked(checked);
		checkbox.setLabel(label);
		checkbox.setSclass(SCLASS_CHECKBOX_SWITCH);
		checkbox.setAttribute(YTESTID_ATTRIBUTE, ytestid);
		checkbox.addEventListener(Events.ON_CHECK, onCheckedEventListener);

		final Div div = new Div();
		div.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_FILTER_POPUP_ITEM);
		div.appendChild(checkbox);
		return div;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		final FilteredTreeView that = (FilteredTreeView) o;
		return Objects.equals(tree, that.tree) && Objects.equals(treeModel, that.treeModel)
				&& Objects.equals(filterContext, that.filterContext);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(tree, treeModel, filterContext);
	}
}

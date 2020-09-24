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
package com.hybris.backoffice.excel.export.wizard.renderer.attributechooser;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.event.TreeDataEvent;

import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.tree.util.TreeUtils;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * Renders two lists: available attributes and selected attributes. For localized attributes renders sub-nodes with all
 * available langs. *
 *
 * @deprecated since 6.7 {@link com.hybris.backoffice.attributechooser.AttributeChooserRenderer}
 */
@Deprecated
public class DefaultAttributeChooserRenderer implements WidgetComponentRenderer<Component, Object, AttributeChooserForm>
{
	private static final String SCLASS_Y_ATTRIBUTE_PICKER = "y-attributepicker";
	private static final String LABEL_ALL_ATTRIBUTES_SELECTED = "attributechooser.all.attributes.selected";
	private static final String LABEL_INCLUDE_ALL_ATTRIBUTES = "attributechooser.include.all.attributes";
	private static final String LABEL_SECTION = "attributechooser.section.";
	private static final String LABEL_SEARCH = "attributechooser.search.";
	private static final String LABEL_BUTTON = "attributechooser.button.";
	protected static final String RENDERER_PREFIX = DefaultAttributeChooserRenderer.class.getSimpleName();
	protected static final String MODEL_INCLUDE_ALL_SELECTED = RENDERER_PREFIX + "IncludeAllSelected";
	protected static final String MODEL_SELECTED_QUALIFIERS = RENDERER_PREFIX + "SelectedQualifiers";
	protected static final String MODE_VISIBLE_ROWS_NUMBER = "visibleRowNumber";
	protected static final String MODEL_SELECTED_PATHS = RENDERER_PREFIX + "SelectedPaths_";
	protected static final String MODEL_SEARCH_TEXT = RENDERER_PREFIX + "SearchText_";
	protected static final String SECTION_ID_AVAILABLE = "available";
	protected static final String SECTION_ID_SELECTED = "selected";

	private Integer visibleRowsNumber;
	private TreeitemRenderer<DefaultTreeNode<SelectedAttribute>> treeItemRenderer;
	private NodeOperationsHandler nodeOperationsHandler;
	private CommonI18NService commonI18NService;

	@Override
	public void render(final Component parent, final Object config, final AttributeChooserForm form, final DataType dataType,
			final WidgetInstanceManager wim)
	{
		final Tree availableValuesTree = createTree(getVisibleRowsNumber(wim));
		final Tree selectedValuesTree = createTree(getVisibleRowsNumber(wim));

		populateTreeData(availableValuesTree, selectedValuesTree, form.getAllAttributes(), wim);
		form.setSelectedAttributes(getSelectedAttributes(availableValuesTree, selectedValuesTree, isIncludeAllSelected(wim)));

		restoreSelectedPaths(availableValuesTree, wim, SECTION_ID_AVAILABLE);
		restoreSelectedPaths(selectedValuesTree, wim, SECTION_ID_SELECTED);
		addSelectedPathsListener(availableValuesTree, wim, SECTION_ID_AVAILABLE);
		addSelectedPathsListener(selectedValuesTree, wim, SECTION_ID_SELECTED);

		addChosenAttributesListener(selectedValuesTree, form, wim);


		final Div mainContainer = new Div();
		mainContainer.setSclass(SCLASS_Y_ATTRIBUTE_PICKER);
		mainContainer.setParent(parent);

		final Div mainSelectionContainer = new Div();
		mainSelectionContainer.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-main");

		final Div attributesSelectionContainer = new Div();
		attributesSelectionContainer.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-main-container");
		mainSelectionContainer.appendChild(attributesSelectionContainer);

		final Component includeAllOverlay = createIncludeAllOverlay();
		includeAllOverlay.setParent(mainSelectionContainer);
		includeAllOverlay.setVisible(isIncludeAllSelected(wim));

		final Component includeAllSection = createIncludeAllSection(isIncludeAllSelected(wim), (final CheckEvent event) -> {

			wim.getModel().setValue(MODEL_INCLUDE_ALL_SELECTED, event.isChecked());
			form.setSelectedAttributes(getSelectedAttributes(availableValuesTree, selectedValuesTree, event.isChecked()));
			includeAllOverlay.setVisible(event.isChecked());
		});
		mainContainer.appendChild(includeAllSection);
		mainContainer.appendChild(mainSelectionContainer);

		attributesSelectionContainer.appendChild(createAttributesSection(availableValuesTree, wim, SECTION_ID_AVAILABLE));
		attributesSelectionContainer.appendChild(createButtonsSection(availableValuesTree, selectedValuesTree));
		attributesSelectionContainer.appendChild(createAttributesSection(selectedValuesTree, wim, SECTION_ID_SELECTED));

	}

	protected Component createIncludeAllSection(final boolean initialState, final EventListener<CheckEvent> onIncludeAllListener)
	{
		final Div section = new Div();
		section.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-include");
		final Checkbox includeAllCheckbox = new Checkbox();
		includeAllCheckbox.setSclass("ye-switch-checkbox");
		includeAllCheckbox.addEventListener(Events.ON_CHECK, onIncludeAllListener);
		includeAllCheckbox.setChecked(initialState);
		includeAllCheckbox.setLabel(Labels.getLabel(LABEL_INCLUDE_ALL_ATTRIBUTES));
		includeAllCheckbox.setParent(section);
		return section;
	}

	protected Component createIncludeAllOverlay()
	{
		final Div includeAllGreyOutArea = new Div();
		UITools.addSClass(includeAllGreyOutArea, SCLASS_Y_ATTRIBUTE_PICKER + "-greyout-area");
		includeAllGreyOutArea.appendChild(new Label(Labels.getLabel(LABEL_ALL_ATTRIBUTES_SELECTED)));
		return includeAllGreyOutArea;
	}

	protected Div createAttributesSection(final Tree attributesTree, final WidgetInstanceManager wim, final String sectionId)
	{

		final String initialSearchText = getFilterText(wim, sectionId);
		final Div availableAttributesContainer = new Div();
		availableAttributesContainer.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-" + sectionId + "-values-container");

		final Label availableAttributesLabel = new Label(Labels.getLabel(LABEL_SECTION + sectionId));
		availableAttributesLabel.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-values-label");
		availableAttributesContainer.appendChild(availableAttributesLabel);

		final Textbox availableValuesFilter = new Textbox(initialSearchText);
		availableValuesFilter.setPlaceholder(Labels.getLabel(LABEL_SEARCH));
		availableValuesFilter.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-values-filter");
		availableAttributesContainer.appendChild(availableValuesFilter);

		availableValuesFilter.addEventListener(Events.ON_CHANGING, (final InputEvent event) -> {
			wim.getModel().setValue(MODEL_SEARCH_TEXT + sectionId, event.getValue());
			refreshTreeSearch(attributesTree, event.getValue());
		});

		availableAttributesContainer.appendChild(attributesTree);
		return availableAttributesContainer;
	}

	protected String getFilterText(final WidgetInstanceManager wim, final String sectionId)
	{
		return ObjectUtils.defaultIfNull(wim.getModel().getValue(MODEL_SEARCH_TEXT + sectionId, String.class), StringUtils.EMPTY);
	}

	protected void addSelectedPathsListener(final Tree tree, final WidgetInstanceManager wim, final String sectionId)
	{
		tree.addEventListener(Events.ON_SELECT, (final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event) -> wim.getModel()
				.setValue(MODEL_SELECTED_PATHS + sectionId, getModel(tree).getSelectionPaths()));
	}

	protected void restoreSelectedPaths(final Tree tree, final WidgetInstanceManager wim, final String sectionId)
	{
		getModel(tree).addSelectionPaths(wim.getModel().getValue(MODEL_SELECTED_PATHS + sectionId, int[][].class));
	}

	protected Component createButtonsSection(final Tree availableTree, final Tree selectedTree)
	{
		final Div chooseButtons = new Div();
		UITools.addSClass(chooseButtons, SCLASS_Y_ATTRIBUTE_PICKER + "-choose-buttons");
		chooseButtons.appendChild(createMoveButton(selectedTree, availableTree, "remove"));
		chooseButtons.appendChild(createMoveButton(availableTree, selectedTree, "add"));

		return chooseButtons;
	}

	protected Button createMoveButton(final Tree from, final Tree to, final String buttonId)
	{
		final Button moveBtn = new Button(Labels.getLabel(LABEL_BUTTON + buttonId));
		UITools.addSClass(moveBtn, SCLASS_Y_ATTRIBUTE_PICKER + "-" + buttonId + "-btn");
		moveBtn.addEventListener(Events.ON_CLICK, event -> {
			moveSelectedNodes(from, to);
			moveBtn.setDisabled(from.getSelectedCount() < 1);
		});
		from.addEventListener(Events.ON_SELECT, event -> moveBtn.setDisabled(from.getSelectedCount() < 1));
		moveBtn.setDisabled(getModel(from).getSelectionCount() < 1);
		return moveBtn;
	}

	protected Tree createTree(final int visibleRows)
	{
		final Tree tree = new Tree();
		tree.setMultiple(true);
		tree.setCheckmark(true);
		tree.setRows(visibleRows);
		tree.appendChild(new Treecols());
		tree.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-values-tree");

		addLocalizedNodesSelectionListener(tree);
		tree.setItemRenderer(getTreeItemRenderer());
		return tree;
	}

	protected void populateTreeData(final Tree availableValuesTree, final Tree selectedValuesTree,
			final Set<AttributeDescriptorModel> allAttributes, final WidgetInstanceManager wim)
	{
		final Set<String> selectedQualifiers = ObjectUtils
				.defaultIfNull(wim.getModel().getValue(MODEL_SELECTED_QUALIFIERS, Set.class), SetUtils.emptySet());

		final Predicate<SelectedAttribute> selectedFilter = attribute -> {
			final boolean isAttributeMandatory = attribute.isRequired(getCommonI18NService().getCurrentLanguage().getIsocode());
			final boolean hasNoDefaultValue = attribute.getAttributeDescriptor().getDefaultValue() == null;
			return (isAttributeMandatory && hasNoDefaultValue) || selectedQualifiers.contains(toModelQualifier(attribute));
		};

		final List<DefaultTreeNode<SelectedAttribute>> availableTreeNodes = nodeOperationsHandler.createTreeNodes(allAttributes,
				selectedFilter.negate());
		availableValuesTree.setModel(createTreeModel(availableTreeNodes, getFilterText(wim, SECTION_ID_AVAILABLE)));

		final List<DefaultTreeNode<SelectedAttribute>> selectedTreeNodes = nodeOperationsHandler.createTreeNodes(allAttributes,
				selectedFilter);
		selectedValuesTree.setModel(createTreeModel(selectedTreeNodes, getFilterText(wim, SECTION_ID_SELECTED)));
	}

	protected boolean isIncludeAllSelected(final WidgetInstanceManager wim)
	{
		return BooleanUtils.isTrue(wim.getModel().getValue(MODEL_INCLUDE_ALL_SELECTED, Boolean.class));
	}

	protected AbstractTreeModel<TreeNode<SelectedAttribute>> createTreeModel(
			final List<DefaultTreeNode<SelectedAttribute>> treeNodes, final String searchText)
	{
		final DefaultTreeModel<SelectedAttribute> model = new DefaultTreeModel<>(new DefaultTreeNode<>(null, treeNodes));
		model.setMultiple(true);
		nodeOperationsHandler.sort(model, true);
		return nodeOperationsHandler.filterTreeModel(model, searchText);
	}

	protected void refreshTreeSearch(final Tree tree, final String searchText)
	{
		tree.setModel(nodeOperationsHandler.filterTreeModel(getModel(tree), searchText));
	}

	protected AbstractTreeModel<TreeNode<SelectedAttribute>> getModel(final Tree tree)
	{
		return ((AbstractTreeModel) tree.getModel());
	}

	protected AbstractTreeModel<TreeNode<SelectedAttribute>> getUnfilteredModel(final Tree tree)
	{
		if (tree.getModel() instanceof TreeUtils.FilteredTreeModel)
		{
			return (AbstractTreeModel) ((TreeUtils.FilteredTreeModel<Object>) tree.getModel()).getOriginalModel();
		}
		return (AbstractTreeModel) tree.getModel();
	}

	protected void moveSelectedNodes(final Tree sourceTree, final Tree targetTree)
	{
		nodeOperationsHandler.moveNodesBetweenTrees(getModel(sourceTree), getModel(targetTree));
	}

	protected void addLocalizedNodesSelectionListener(final Tree tree)
	{
		tree.addEventListener(Events.ON_SELECT,
				(final SelectEvent<Treeitem, TreeNode<SelectedAttribute>> event) -> nodeOperationsHandler
						.updateGroupingNodesSelection(getModel(tree), event));
	}

	public void addChosenAttributesListener(final Tree selectedTree, final AttributeChooserForm form,
			final WidgetInstanceManager wim)
	{
		getUnfilteredModel(selectedTree).addTreeDataListener(treeDataEvent -> {
			if (treeDataEvent.getType() == TreeDataEvent.INTERVAL_ADDED || treeDataEvent.getType() == TreeDataEvent.INTERVAL_REMOVED)
			{
				form.setSelectedAttributes(nodeOperationsHandler.extractAttributes(getModel(selectedTree)));
				wim.getModel().setValue(MODEL_SELECTED_QUALIFIERS,
						form.getSelectedAttributes().stream().map(this::toModelQualifier).collect(Collectors.toSet()));
			}
		});
	}

	protected String toModelQualifier(final SelectedAttribute attr)
	{
		return attr.getQualifier() + "%" + ObjectUtils.defaultIfNull(attr.getIsoCode(), "");
	}

	protected List<SelectedAttribute> getSelectedAttributes(final Tree available, final Tree selected, final boolean includeAll)
	{
		final List<SelectedAttribute> allAttributes = new ArrayList<>();
		allAttributes.addAll(nodeOperationsHandler.extractAttributes(getModel(selected)));
		if (includeAll)
		{
			allAttributes.addAll(nodeOperationsHandler.extractAttributes(getModel(available)));
		}
		allAttributes.sort(Comparator.comparing(SelectedAttribute::getName));
		return allAttributes;
	}

	public TreeitemRenderer<DefaultTreeNode<SelectedAttribute>> getTreeItemRenderer()
	{
		return treeItemRenderer;
	}

	@Required
	public void setTreeItemRenderer(final TreeitemRenderer<DefaultTreeNode<SelectedAttribute>> treeItemRenderer)
	{
		this.treeItemRenderer = treeItemRenderer;
	}

	public void setVisibleRowsNumber(final Integer visibleRowsNumber)
	{
		this.visibleRowsNumber = visibleRowsNumber;
	}

	public NodeOperationsHandler getNodeOperationsHandler()
	{
		return nodeOperationsHandler;
	}

	@Required
	public void setNodeOperationsHandler(final NodeOperationsHandler nodeOperationsHandler)
	{
		this.nodeOperationsHandler = nodeOperationsHandler;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected int getVisibleRowsNumber()
	{
		return ObjectUtils.defaultIfNull(visibleRowsNumber, 12);
	}

	protected int getVisibleRowsNumber(final WidgetInstanceManager wim)
	{
		return ObjectUtils.defaultIfNull(wim.getModel().getValue(MODE_VISIBLE_ROWS_NUMBER, Integer.class), getVisibleRowsNumber());
	}

}

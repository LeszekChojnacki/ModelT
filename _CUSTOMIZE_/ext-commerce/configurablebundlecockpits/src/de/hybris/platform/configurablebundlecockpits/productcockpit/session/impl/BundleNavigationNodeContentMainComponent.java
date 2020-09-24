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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cockpit.components.contentbrowser.AbstractContentBrowser;
import de.hybris.platform.cockpit.components.contentbrowser.AbstractMainAreaBrowserComponent;
import de.hybris.platform.cockpit.components.mvc.listbox.Listbox;
import de.hybris.platform.cockpit.components.mvc.listbox.ListboxController;
import de.hybris.platform.cockpit.components.mvc.tree.Tree;
import de.hybris.platform.cockpit.components.mvc.tree.TreeController;
import de.hybris.platform.cockpit.components.mvc.tree.TreeControllerWrapper;
import de.hybris.platform.cockpit.components.notifier.Notification;
import de.hybris.platform.cockpit.model.general.UIItemView;
import de.hybris.platform.cockpit.model.meta.BaseType;
import de.hybris.platform.cockpit.model.meta.ObjectTemplate;
import de.hybris.platform.cockpit.model.meta.ObjectType;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.session.AdvancedBrowserModel;
import de.hybris.platform.cockpit.session.UIBrowserArea;
import de.hybris.platform.cockpit.session.UICockpitPerspective;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.AbstractBrowserArea;
import de.hybris.platform.cockpit.session.impl.CreateContext;
import de.hybris.platform.cockpit.util.ListProvider;
import de.hybris.platform.cockpit.util.UITools;
import de.hybris.platform.cockpit.wizards.generic.AbstractGenericItemPage;
import de.hybris.platform.cockpit.wizards.generic.AdvancedSearchPage;
import de.hybris.platform.cockpit.wizards.generic.GenericItemMandatoryPage;
import de.hybris.platform.cockpit.wizards.generic.GenericItemWizard;
import de.hybris.platform.cockpit.wizards.generic.NewItemWizard;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.list.BundleRelatedItemListController;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.list.BundleRelatedItemListModel;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.list.BundleRelatedItemListRenderer;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree.BundleNavigationNodeController;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree.BundleNavigationNodeRenderer;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree.BundleTemplateTreeModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.fest.util.Collections;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Br;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Box;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Splitter;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Vbox;

import com.google.common.collect.Sets;


/**
 * Represents main component for navigation node perspective.
 * <p/>
 * <b>Note:</b> <br/>
 * Component contains hierarchical representation of Navigation Nodes.
 * 
 */
public class BundleNavigationNodeContentMainComponent extends AbstractMainAreaBrowserComponent
{
	private static final String MESSAGE_TEXT_SCLASS = "messageText";
	private static final String MESSAGE_TITLE_SCLASS = "messageTitle";
	private static final String MESSAGE_CONTAINER_SCLASS = "messageContainer";
	protected static final String RELATED_ITEMS_LIST_SCLASS = "relatedItemList";
	protected static final String NAVIGATION_TREE_SCLASS = "navigationNodeTree";
	protected static final String NAVIGATION_TREE_PANEL_SCLASS = "navigationTreePanel";
	protected static final String ROOT_NAVIGATION_CNT_SCLASS = "rootNavigationContainer";
	protected static final String ROOT_NAVIGATION_SUB_CNT_SCLASS = "rootNavigationSubContainer";
	protected static final String NAVIGATION_SECTION_CONTAINER_LEFT_SCLASS = "navigationSectionContainerLeft";
	protected static final String NAVIGATION_SECTION_CONTAINER_RIGHT_SCLASS = "navigationSectionContainerRight";
	protected static final String RELATED_ITEMS_CONTAINER_SCLASS = "relatedItemsContainer";
	protected static final String ADD_RELATED_ITEMS_BTG_SCLASS = "addRelatedItemsAddBtn";
	protected static final String MARGIN_HELPER_SCLASS = "marginHelper";
	protected static final String ADD_PRODUCT_LABEL = "configurablebundlecockpits.product.add";
	protected static final String ADD_BUNDLE_LABEL = "configurablebundlecockpits.bundle.add";
	protected static final String ADD_BUNDLE_TOOLTIP = "configurablebundlecockpits.bundle.add.tooltip";
	protected static final String SEARCH_PRODUCT_LABEL = "configurablebundlecockpits.product.search";
	protected static final String INFO_AREA_DIV_ID = "infoAreaContainer2";

	private TreeController treeController;
	private Tree tree;

	private Component relatedItemListContainer;
	private Button addItemButton;
	private Button searchProductButton;
	private Button addBundleButton;

	public BundleNavigationNodeContentMainComponent(final AdvancedBrowserModel model, final AbstractContentBrowser contentBrowser)
	{
		super(model, contentBrowser);
	}

	/**
	 * Initialize a whole tree component with its dependencies.
	 * 
	 * @param catalogVersion
	 *           current catalog version
	 * @param parent
	 *           a parent for component
	 */
	protected void initTreeComponent(final CatalogVersionModel catalogVersion, final Component parent)
	{

		final Set<TypedObject> selected = getModel().getSelectedNode();
		final BundleRelatedItemListController relatedItemListController = getRelatedItemListController();
		final Listbox<TypedObject> listBox = createRelatedItemList(CollectionUtils.isNotEmpty(selected) ? selected.iterator()
				.next() : null);
		listBox.setVflex(false);
		relatedItemListContainer = createRelatedItemListContainer(listBox);

		listBox.setController(relatedItemListController);

		treeController = createTreeController(relatedItemListContainer, relatedItemListController, listBox, addItemButton,
				searchProductButton);
		tree = createTree(catalogVersion);
		tree.setController(treeController);

		addItemButton = createAddItemButton();
		searchProductButton = createSearchProductButton();
		addBundleButton = createAddBundleButton(catalogVersion);

		renderTreeComponent(tree, relatedItemListContainer, createProductActionButtonContainer(searchProductButton, addItemButton),
				createButtonContainer(addBundleButton), parent);

	}

	protected void updateTreeComponent(@SuppressWarnings("unused") final CatalogVersionModel catalogVersion, final Component parent) // NOSONAR
	{
		updateTreeModel();
		getNavigationNodeController().refresh(tree);
		renderTreeComponent(tree, relatedItemListContainer, createProductActionButtonContainer(searchProductButton, addItemButton),
				createButtonContainer(addBundleButton), parent);
	}

	/**
	 * Creates left (with tree) and right (with node content) container, also 'add item' button.
	 * 
	 * @param tree
	 *           tree component
	 * @param contentItemListContainer
	 *           node content parent
	 * @param productActionButtonContainer
	 * @param parent
	 *           add page button parent
	 * 
	 */
	protected void renderTreeComponent(final Tree tree, final Component contentItemListContainer,
			final Component productActionButtonContainer, final Component addBundleActionButtonContainer, final Component parent)
	{
		final Hbox hbox = new Hbox();
		hbox.setSclass(NAVIGATION_TREE_PANEL_SCLASS);
		UITools.maximize(hbox);
		hbox.setPack("start");
		parent.appendChild(hbox);

		if (getModel().getTreeRootChildCount() == 0)
		{
			final Div noNavigationNodes = new Div();
			final Div rootNavigationContainer = new Div();
			rootNavigationContainer.setSclass(ROOT_NAVIGATION_CNT_SCLASS);
			createMessageArea(Labels.getLabel("configurablebundlecockpits.perspective.bundleList.title"),
					Labels.getLabel("configurablebundlecockpits.navigationnode.nonodes"), noNavigationNodes);
			final Div noNavigationNodesDiv = new Div();
			noNavigationNodesDiv.appendChild(noNavigationNodes);
			noNavigationNodesDiv.appendChild(addBundleActionButtonContainer);
			rootNavigationContainer.appendChild(noNavigationNodesDiv);
			final Div div = new Div();
			div.setSclass(ROOT_NAVIGATION_SUB_CNT_SCLASS);
			div.appendChild(rootNavigationContainer);
			UITools.maximize(div);
			hbox.appendChild(rootNavigationContainer);
		}
		else
		{
			final Splitter splitter = new Splitter();
			final Div leftContainer = new Div();
			leftContainer.setWidth("99%");
			leftContainer.setSclass(NAVIGATION_SECTION_CONTAINER_LEFT_SCLASS);

			final Vbox rightContainer = new Vbox();
			rightContainer.setSclass(NAVIGATION_SECTION_CONTAINER_RIGHT_SCLASS);
			rightContainer.setAlign("end");
			rightContainer.setPack("start");
			rightContainer.setWidth("99%");

			createMessageArea(Labels.getLabel("configurablebundlecockpits.perspective.bundleList.title"),
					Labels.getLabel("configurablebundlecockpits.perspective.bundleList.text"), leftContainer);
			createMessageArea(Labels.getLabel("configurablebundlecockpits.perspective.productList.title"),
					Labels.getLabel("configurablebundlecockpits.perspective.productList.text"), rightContainer);
			hbox.appendChild(leftContainer);
			hbox.appendChild(splitter);
			hbox.appendChild(rightContainer);

			hbox.setWidths("50%,50%");

			leftContainer.appendChild(tree);
			leftContainer.appendChild(addBundleActionButtonContainer);
			rightContainer.appendChild(contentItemListContainer);
			rightContainer.appendChild(productActionButtonContainer);

		}

	}

	@Override
	protected Div createMainArea()
	{
		final Div mainAreaContainer = new Div();
		UITools.maximize(mainAreaContainer);
		this.mainArea = mainAreaContainer;
		renderMainAreaComponent(this.mainArea);
		return mainAreaContainer;
	}

	/**
	 * Constructs and renders main browse area component for navigation node perspective. </p>
	 * 
	 * @param parent
	 *           a component parent
	 * 
	 */
	protected void renderMainAreaComponent(final Component parent)
	{
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final CatalogVersionModel catalogVersion = ((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion();

			UITools.detachChildren(parent);

			if (catalogVersion == null)
			{
				createBlankArea(Labels.getLabel("cmscockpit.select.catalog.version"), parent);

			}
			else
			{
				initTreeComponent(catalogVersion, this.mainArea);
			}
		}
	}

	protected void updateMainAreaCompoenent(final Component parent)
	{
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final CatalogVersionModel catalogVersion = ((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion();

			UITools.detachChildren(parent);

			if (catalogVersion == null)
			{
				createBlankArea(Labels.getLabel("cmscockpit.select.catalog.version"), parent);

			}
			else
			{
				updateTreeComponent(catalogVersion, this.mainArea);
			}
		}
	}



	@Override
	public boolean update()
	{
		updateMainAreaCompoenent(this.mainArea);
		return false;
	}

	/**
	 * Triggers adding root navigation node. </p>
	 */
	public void fireAddRootNavigationNode()
	{
		treeController.create(tree, null);
	}

	/**
	 * Triggers remove selected navigation node. </p>
	 */
	public void removeSelectedNavigationNode()
	{
		final Treeitem selectedTreeItem = tree.getSelectedItem();
		if (selectedTreeItem != null)
		{
			treeController.delete(tree, selectedTreeItem.getValue());
		}
	}

	@Override
	public BundleNavigationNodeBrowserModel getModel()
	{
		BundleNavigationNodeBrowserModel ret = null;
		if (super.getModel() instanceof BundleNavigationNodeBrowserModel)
		{
			ret = (BundleNavigationNodeBrowserModel) super.getModel();
		}
		return ret;
	}

	@Override
	protected UIItemView getCurrentItemView()
	{
		return null;
	}


	@Override
	protected void cleanup()
	{
		// nop
	}

	/**
	 * Creates a controller for a tree component. </p>
	 */
	protected TreeController createTreeController(final Component relatedItemListContainer, // NOSONAR
			final BundleRelatedItemListController relatedItemListController, final Listbox listbox, final Button addItemButton,
			final Button searchProductButton)
	{
		final TreeController<TypedObject> wrappedTreeController = new BundleTreeControllerWrapper(getNavigationNodeController(),
				relatedItemListController, listbox);
		return wrappedTreeController;
	}

	/**
	 * Just a box around button so that it can be aligned to the bottom right corner of enclosing component. </p>
	 * 
	 * @param addItemButton
	 *           add page button
	 */
	protected Component createAddItemButtonContainer(final Button addItemButton)
	{
		final Box box = new Box();
		box.setWidth("99%");
		box.setHeight("99%");
		box.setAlign("end");
		box.setPack("end");
		box.appendChild(addItemButton);
		return box;
	}

	/**
	 * Just a box around button so that it can be aligned to the bottom right corner of enclosing component. </p>
	 * 
	 * @param button
	 *           add page button
	 */
	protected Component createButtonContainer(final Button button)
	{
		final Div marginHelper = new Div();
		marginHelper.setSclass(MARGIN_HELPER_SCLASS);
		final Box box = new Box();
		box.setWidth("99%");
		box.setHeight("99%");
		box.setAlign("left");
		box.setSclass("search-product-button");
		box.setPack("left");
		box.appendChild(button);
		marginHelper.appendChild(box);
		return marginHelper;
	}

	/**
	 * Just a box around button so that it can be aligned to the bottom right corner of enclosing component. </p>
	 * 
	 * @param searchProductButton
	 *           add page button
	 * @param addProductButton
	 *           add page button
	 */
	protected Component createProductActionButtonContainer(final Button searchProductButton, final Button addProductButton)
	{
		final Box box = new Box();
		box.setWidth("99%");
		box.setHeight("99%");
		box.setAlign("right");
		box.setSclass("search-product-button");
		box.setPack("left");
		box.appendChild(searchProductButton);
		box.appendChild(addProductButton);
		box.setOrient("horizontal");
		return box;
	}


	/**
	 * Creates a content item list for right column of main area component. </p>
	 */
	protected Listbox createRelatedItemList(final TypedObject naviNode)
	{
		final Listbox<TypedObject> listBox = new Listbox<TypedObject>();
		listBox.setSclass(RELATED_ITEMS_LIST_SCLASS);
		listBox.setOddRowSclass("no");
		listBox.setItemRenderer(new BundleRelatedItemListRenderer());
		listBox.setModel(new BundleRelatedItemListModel(naviNode));
		return listBox;
	}

	/**
	 * Creates button for creating new items inside selected node. </p>
	 */
	protected Button createAddItemButton()
	{

		final Button button = new Button(Labels.getLabel(ADD_PRODUCT_LABEL));
		button.setTooltiptext(Labels.getLabel(ADD_PRODUCT_LABEL));
		UITools.modifySClass(this, "buttonDisabled", true);
		button.setVisible(getModel().getSelectedNode() != null);
		button.addEventListener(Events.ON_CLICK, new MandatorySearchPageWizardStarter());
		button.setSclass(ADD_RELATED_ITEMS_BTG_SCLASS);
		return button;

	}

	/**
	 * Creates button for opening a product search listview </p>
	 */
	protected Button createSearchProductButton()
	{
		final Button button = new Button(Labels.getLabel(SEARCH_PRODUCT_LABEL));
		button.setTooltiptext(Labels.getLabel(SEARCH_PRODUCT_LABEL));
		UITools.modifySClass(this, "buttonDisabled", true);
		button.setVisible(getModel().getSelectedNode() != null);

		button.addEventListener(Events.ON_CLICK, event -> getModel().openRelatedQueryBrowser());
		button.setSclass(ADD_RELATED_ITEMS_BTG_SCLASS);
		return button;
	}

	/**
	 * Creates button for adding a new bundle </p>
	 */
	protected Button createAddBundleButton(final CatalogVersionModel catalogVersion)
	{
		final Button button = new Button(Labels.getLabel(ADD_BUNDLE_LABEL));
		button.setTooltiptext(Labels.getLabel(ADD_BUNDLE_TOOLTIP));
		UITools.modifySClass(this, "buttonDisabled", true);

		button.setVisible(true);
		button.addEventListener(Events.ON_CLICK, new NewItemWizardStarter(catalogVersion));
		button.setSclass(ADD_RELATED_ITEMS_BTG_SCLASS);
		return button;
	}

	/**
	 * Creates container which displays node content.</p>
	 * 
	 * @param relatedItemList
	 *           content item list
	 */
	protected Component createRelatedItemListContainer(final Listbox relatedItemList)
	{
		final Div content = new Div();
		content.setWidth("100%");
		content.setHeight("80%");
		content.setSclass(RELATED_ITEMS_CONTAINER_SCLASS);
		content.appendChild(relatedItemList);
		return content;
	}

	/**
	 * Updates the tree setting actual model
	 */
	protected void updateTreeModel()
	{
		tree.setModel(new BundleTemplateTreeModel(getModel()));
	}

	/**
	 * Creates tree with. </p>
	 */
	protected Tree createTree(final CatalogVersionModel catVer)
	{
		final Tree<TypedObject> typedObjectTree = new Tree<TypedObject>();
		typedObjectTree.setSclass(NAVIGATION_TREE_SCLASS);
		typedObjectTree.setTreeitemRenderer(getNavigationNodeRenderer(treeController));
		typedObjectTree.setModel(new BundleTemplateTreeModel(getTypeService().wrapItem(catVer)));
		typedObjectTree.setWidth("100%");

		//create columns
		final Treecols treecols = new Treecols();
		final Treecol firstColumn = new Treecol("");
		treecols.appendChild(firstColumn);
		final Treecol secondColumn = new Treecol("");
		treecols.appendChild(secondColumn);
		typedObjectTree.appendChild(treecols);
		typedObjectTree.setZclass("z-dottree");

		getNavigationNodeController().refresh(typedObjectTree, getModel().getOpenedPath());

		return typedObjectTree;
	}

	/**
	 * Creates blank main area with given message. </p>
	 * 
	 * @param message
	 *           given message
	 * @param parent
	 *           a parent component
	 */
	protected void createBlankArea(final String message, final Component parent)
	{
		final Div marginHelper = new Div();
		marginHelper.setSclass(MARGIN_HELPER_SCLASS);

		UITools.maximize(marginHelper);

		final Div mainContainer = new Div();
		mainContainer.setWidth("99%");
		mainContainer.setHeight("99%");

		mainContainer.appendChild(new Br());
		mainContainer.appendChild(new Label(message));
		marginHelper.appendChild(mainContainer);
		parent.appendChild(marginHelper);
	}

	/**
	 * Creates message area with title and text </p>
	 * 
	 * @param title
	 *           title text
	 * @param message
	 *           given message
	 * @param parent
	 *           a parent component
	 */
	protected void createMessageArea(final String title, final String message, final Component parent)
	{
		final Div marginHelper = new Div();
		marginHelper.setSclass(MARGIN_HELPER_SCLASS);

		final Div messageContainer = new Div();
		messageContainer.setSclass(MESSAGE_CONTAINER_SCLASS);
		messageContainer.setWidth("99%");
		messageContainer.setHeight("99%");

		final Div messageTitle = new Div();
		messageTitle.setSclass(MESSAGE_TITLE_SCLASS);
		messageTitle.appendChild(new Label(title));

		final Div messageText = new Div();
		messageTitle.setSclass(MESSAGE_TEXT_SCLASS);
		messageText.appendChild(new Html(message));

		messageContainer.appendChild(messageTitle);
		messageContainer.appendChild(messageText);
		marginHelper.appendChild(messageContainer);
		parent.appendChild(marginHelper);
	}


	/**
	 * Refreshes a item button after selection was done in the tree. </p>
	 * 
	 * @param selectedItems
	 *           currently selected items
	 * @param addItemButton
	 *           add item button
	 */
	protected void refreshAddItemButton(final Set<Treeitem> selectedItems, final Button addItemButton)
	{
		if (addItemButton != null)
		{
			UITools.modifySClass(addItemButton, "buttonDisabled", false);
			addItemButton.setVisible(true);

			if (Collections.isEmpty(selectedItems))
			{
				addItemButton.setVisible(false);

			}
		}
	}

	/**
	 * Refreshes search product button after selection was done in the tree. </p>
	 * 
	 * @param selectedItems
	 *           currently selected items
	 * @param searchProductButton
	 *           add item button
	 */
	protected void refreshSearchProductButton(final Set<Treeitem> selectedItems, final Button searchProductButton)
	{
		if (searchProductButton != null)
		{
			UITools.modifySClass(searchProductButton, "buttonDisabled", false);
			searchProductButton.setVisible(true);

			if (Collections.isEmpty(selectedItems))
			{
				searchProductButton.setVisible(false);

			}
		}
	}


	/**
	 * Refreshes a content item list after selection was done in the tree. </p>
	 * 
	 * @param parent
	 *           a parent component
	 * @param listBox
	 *           a content item list
	 * @param controller
	 *           a content item list controller
	 * 
	 */
	protected void refreshRelatedItemList(final Component parent, final Listbox listBox,
			final BundleRelatedItemListController controller)
	{
		UITools.detachChildren(parent);
		if (controller.updateList(listBox))
		{
			parent.appendChild(listBox);
		}
		else
		{
			final Label label = new Label(Labels.getLabel("cmcockpit.navigation.node.no.content.item"));
			parent.appendChild(label);
		}
	}

	/**
	 * Returns {@link TreeitemRenderer} configured via spring.
	 * <p/>
	 */
	protected TreeitemRenderer getNavigationNodeRenderer(final TreeController controller)
	{
		final BundleNavigationNodeRenderer renderer = (BundleNavigationNodeRenderer) SpringUtil.getBean(
				"bundleNavigationNodeRenderer", BundleNavigationNodeRenderer.class);
		renderer.setController(controller);
		return renderer;
	}

	/**
	 * Returns {@link ListboxController} configured via spring.
	 * <p/>
	 */
	protected BundleRelatedItemListController getRelatedItemListController()
	{
		return (BundleRelatedItemListController) SpringUtil.getBean("bundleRelatedItemListController",
				BundleRelatedItemListController.class);
	}

	/**
	 * Returns {@link BundleNavigationNodeController} configured via spring.
	 * <p/>
	 */
	protected BundleNavigationNodeController getNavigationNodeController()
	{
		return (BundleNavigationNodeController) SpringUtil.getBean("bundleNavigationNodeController",
				BundleNavigationNodeController.class);
	}

	/**
	 * Returns a {@link Set} of product types (configured via spring bean 'allowedProductTypesList') that can be added to
	 * a bundle component.
	 * <p/>
	 */
	protected Set<ObjectType> getAllowedProductTypes()
	{
		final ArrayList allowedTyesCodes = (ArrayList) Registry.getApplicationContext().getBean("allowedProductTypesList");
		final Set<ObjectType> allowedTypesSet = new HashSet<ObjectType>();

		final Iterator<String> iterator = allowedTyesCodes.iterator();

		while (iterator.hasNext())
		{
			final BaseType baseType = getTypeService().getBaseType(iterator.next());
			allowedTypesSet.add(baseType);
		}

		if (allowedTypesSet.isEmpty())
		{
			final BaseType baseType = getTypeService().getBaseType("Product");
			allowedTypesSet.add(baseType);
		}
		return allowedTypesSet;
	}

	private class BundleTreeControllerWrapper extends TreeControllerWrapper<TypedObject>
	{

		final BundleRelatedItemListController relatedItemListController;
		final Listbox listbox;
		final BundleNavigationNodeController treeController;

		public BundleTreeControllerWrapper(final BundleNavigationNodeController treeController,
				final BundleRelatedItemListController listController, final Listbox listbox)
		{
			super(treeController);
			this.treeController = treeController;
			this.relatedItemListController = listController;
			this.listbox = listbox;
		}


		@Override
		public Object customAction(final Tree tree, final Event event, final TypedObject node)
		{
			Object ret = null;

			if (null != tree)
			{
				ret = super.customAction(tree, event, node);
			}
			if (event instanceof OpenEvent)
			{
				treeController.captureOpenedTreeitem((Treeitem) event.getTarget());
				getModel().setOpenedItems(treeController.getOpenedPath());
			}
			return ret;
		}

		@Override
		public void selected(final Tree tree, final Set<Treeitem> selectedItems)
		{
			super.selected(tree, selectedItems);
			final Set<TypedObject> selectedObjects = Sets.newHashSet();

			for (final Treeitem treeitem : selectedItems)
			{
				selectedObjects.add((TypedObject) treeitem.getValue());
			}
			getModel().setSelectedNode(selectedObjects);

			if (CollectionUtils.isNotEmpty(selectedItems))
			{
				final Treeitem selectedTreeItem = selectedItems.iterator().next();

				final UICockpitPerspective perspective = UISessionUtils.getCurrentSession().getCurrentPerspective();
				if (((TypedObject) selectedTreeItem.getValue()).getObject() == null)
				{
					final Notification notification = new Notification(
							Labels.getLabel("cmscockpit.navigationnode.error.nodedoesnotexist"));
					perspective.getNotifier().setNotification(notification);
					perspective.getBrowserArea().update();
					return;
				}

				relatedItemListController.setCurrentNavigationNode((TypedObject) selectedTreeItem.getValue());
				final AbstractBrowserArea browserArea = (AbstractBrowserArea) perspective.getBrowserArea();
				final ListProvider<TypedObject> listProvider = new TypedObjectListProvider(selectedTreeItem);
				browserArea.updateInfoArea(listProvider, false);

				refreshAddItemButton(selectedItems, addItemButton);
				refreshSearchProductButton(selectedItems, searchProductButton);
				refreshRelatedItemList(relatedItemListContainer, listbox, relatedItemListController);
			}
		}
	}

	private class MandatorySearchPageWizard extends NewItemWizard
	{
		private BundleTemplateModel bundleTemplateModel;

		private MandatorySearchPageWizard(final ObjectTemplate productType, final Component component,
			final BundleNavigationNodeBrowserModel model, final BundleTemplateModel bundleTemplateModel)
		{
			super(productType, component, model);
			this.bundleTemplateModel = bundleTemplateModel;
		}

		protected void informReferenceSelector(final Object passedValue)
		{
			if (passedValue != null)
			{
				if (passedValue instanceof Collection)
				{
					getNavigationNodeController().addProductsToNode(bundleTemplateModel,
							(Collection<TypedObject>) passedValue);
				}
				else
				{
					getNavigationNodeController().addProductsToNode(bundleTemplateModel,
							Arrays.asList((TypedObject) passedValue));
				}
			}
		}

		@Override
		public void doAfterDone(final AbstractGenericItemPage page)
		{
			Object item = page.getWizard().getItem();
			if (item == null)
			{
				if (page instanceof AdvancedSearchPage)
				{
					item = ((AdvancedSearchPage) page).getSelectedValue();
				}
				else if (page instanceof GenericItemMandatoryPage)
				{
					item = ((GenericItemMandatoryPage) page).getValue();
				}
			}
			if (item != null)
			{
				informReferenceSelector(item);
			}
			UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser()
					.updateItems();
		}
	}

	private class TypedObjectListProvider implements ListProvider<TypedObject>
	{
		private Treeitem treeItem;

		private TypedObjectListProvider(final Treeitem treeItem)
		{
			this.treeItem = treeItem;
		}

		@Override
		public List<TypedObject> getList()
		{
			return Collections.list(getFirst());
		}

		@Override
		public TypedObject getFirst()
		{
			return (TypedObject) treeItem.getValue();
		}

		@Override
		public de.hybris.platform.cockpit.util.ListProvider.ListInfo getListInfo()
		{
			return ListInfo.SINGLETON;
		}

		@Override
		public int getListSize()
		{
			return 1;
		}
	}

	private class MandatorySearchPageWizardStarter implements EventListener
	{
		private MandatorySearchPageWizardStarter() {}

		@Override
		public void onEvent(final Event event)
		{
			final Treeitem selectedItem = tree.getSelectedItem();
			if (selectedItem != null)
			{
				final BundleTemplateModel bundleTemplateModel = (BundleTemplateModel) ((TypedObject) selectedItem.getValue())
						.getObject();

				final ObjectTemplate productType = getTypeService().getObjectTemplate("Product");
				final NewItemWizard wizard = new MandatorySearchPageWizard(productType, UITools.getCurrentZKRoot(),
						getModel(), bundleTemplateModel);

				final Map<String, Object> initialValues = new HashMap<String, Object>();
				initialValues.put("Product.catalogVersion", getTypeService().wrapItem(bundleTemplateModel.getCatalogVersion()));
				wizard.setPredefinedValues(initialValues);
				final CreateContext createContext = new CreateContext(productType.getBaseType(), null, null, null);
				createContext.setAllowedTypes(getAllowedProductTypes());
				wizard.setCreateContext(createContext);
				wizard.setDisplaySubTypes(true);
				wizard.setAllowSelect(true);
				wizard.setAllowCreate(false);
				final Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("multiple", "true"); // value has to be a String and there is no static parameter for "multiple"
				parameters.put(GenericItemWizard.FORCE_CREATE_IN_WIZARD, Boolean.TRUE);
				wizard.setParameters(parameters);
				wizard.start();
			}
		}
	}

	private class NewItemWizardStarter implements EventListener
	{
		private CatalogVersionModel catalogVersion;

		private NewItemWizardStarter(final CatalogVersionModel catalogVersion)
		{
			this.catalogVersion = catalogVersion;
		}

		@Override
		public void onEvent(final Event event)
		{
			final ObjectTemplate bundleTemplateType = getTypeService().getObjectTemplate("BundleTemplate");

			final NewItemWizard wizard = new NewItemWizard(bundleTemplateType, UITools.getCurrentZKRoot(), getModel())
			{
				@Override
				public void doAfterDone(final AbstractGenericItemPage page)
				{
					Object item = page.getWizard().getItem();
					if (item == null)
					{
						if (page instanceof AdvancedSearchPage)
						{
							item = ((AdvancedSearchPage) page).getSelectedValue();
						}
						else if (page instanceof GenericItemMandatoryPage)
						{
							item = ((GenericItemMandatoryPage) page).getValue();
						}
					}

					UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();
				}
			};

			final Map<String, Object> initialValues = new HashMap<>();
			initialValues.put("BundleTemplate.catalogVersion", catalogVersion);
			wizard.setPredefinedValues(initialValues);
			final CreateContext createContext = new CreateContext(bundleTemplateType.getBaseType(), null, null, null);
			final Set<ObjectType> allowedTypesSet = new HashSet<>();
			allowedTypesSet.add(getTypeService().getBaseType("BundleTemplate"));
			createContext.setAllowedTypes(allowedTypesSet);
			wizard.setCreateContext(createContext);
			wizard.setDisplaySubTypes(true);
			wizard.setAllowSelect(false);
			wizard.setAllowCreate(true);
			final Map<String, Object> parameters = new HashMap<>();
			parameters.put("multiple", "true"); // value has to be a String and there is no static parameter for "multiple"
			parameters.put(GenericItemWizard.FORCE_CREATE_IN_WIZARD, Boolean.TRUE);
			wizard.setParameters(parameters);
			wizard.start();
		}
	}
}

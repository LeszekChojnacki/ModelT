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

package de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.cockpit.components.ComponentsHelper;
import de.hybris.platform.cockpit.components.listview.ListViewAction.Context;
import de.hybris.platform.cockpit.components.mvc.tree.Tree;
import de.hybris.platform.cockpit.components.mvc.tree.TreeController;
import de.hybris.platform.cockpit.components.notifier.Notification;
import de.hybris.platform.cockpit.components.sync.dialog.OneSourceManyTargetItemSyncDialog;
import de.hybris.platform.cockpit.events.impl.ItemChangedEvent;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.SystemService;
import de.hybris.platform.cockpit.services.dragdrop.DraggedItem;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.services.sync.SynchronizationService;
import de.hybris.platform.cockpit.services.sync.SynchronizationService.SyncContext;
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.BaseUICockpitPerspective;
import de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.BundleNavigationNodeBrowserModel;
import de.hybris.platform.configurablebundlecockpits.servicelayer.services.BundleNavigationService;
import de.hybris.platform.configurablebundleservices.bundle.BundleTemplateService;
import de.hybris.platform.configurablebundleservices.enums.BundleTemplateStatusEnum;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateStatusModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Controller of 'navigation node' tree, see related class: {@link BundleNavigationNodeBrowserModel}
 * 
 * @author Jacek
 * @spring.bean navigationNodeController
 */
public class BundleNavigationNodeController implements TreeController<TypedObject>
{
	private static final Logger LOG = Logger.getLogger(BundleNavigationNodeController.class);
	private static final String SYNCHRONIZATION_SERVICE = "synchronizationService";
	private static final String SYNC_JOBS_PARAM = "syncJobs";


	private BundleNavigationService bundleNavigationService;
	private TypeService typeService;
	private ModelService modelService;
	private SystemService systemService;
	private SynchronizationService synchronizationService;
	private String navigationNodeWizardId;
	private String relatedResourceWizardId;
	private Set<TypedObject> selectedItems = Sets.newHashSet();
	private final List<List<Integer>> openedItems = Lists.newArrayList();
	private BundleTemplateService bundleTemplateService;
	private FlexibleSearchService flexibleSearchService;

	@Override
	public void selected(final Tree tree, final Set<Treeitem> selectedTreeItems)
	{
		showNodeActions(tree, selectedTreeItems);
		selectedItems = extractSelectedItem(selectedTreeItems);

	}

	@Override
	public Set<TypedObject> getSelected()
	{
		return selectedItems;
	}


	public void captureOpenedTreeitem(final Treeitem treeitem)
	{
		final List<Integer> pathToRoot = getPathToRoot(treeitem);
		if (treeitem != null && treeitem.isOpen())
		{
			openedItems.add(pathToRoot);
		}
		else
		{

			int index = -1;
			for (final List existing : openedItems)
			{

				index++;
				if (ListUtils.isEqualList(existing, pathToRoot))
				{
					break;
				}

			}
			if (index > -1)
			{
				openedItems.remove(index);
			}
		}

	}


	public List<List<Integer>> getOpenedPath()
	{

		return openedItems;
	}

	/**
	 * Shows actions next to selected row and hides actions from previously selected row
	 */
	public void showNodeActions(final Tree tree, final Set<Treeitem> selectedItems)
	{
		// 1. hide actions for every row (what with collapsed ones?)
		final Collection<Treeitem> treeChildren = tree.getTreechildren().getItems();
		if (!CollectionUtils.isEmpty(treeChildren))
		{
			for (final Treeitem treeitem : treeChildren)
			{
				((Treecell) treeitem.getTreerow().getChildren().get(1)).getFirstChild().setVisible(false);

			}
			// 2. show actions for selected one
			if (!(selectedItems == null || selectedItems.isEmpty()))
			{
				((Treecell) selectedItems.iterator().next().getTreerow().getChildren().get(1)).getFirstChild().setVisible(true);
			}
		}
	}

	@Override
	public TypedObject create(final Tree tree, final TypedObject target)//NOPMD
	{
		//get the size of children and call 'create' method with index param.
		if (target == null)//it's root node
		{
			TypedObject treeModelRoot =  (TypedObject) tree.getModel().getRoot();
			return create(tree, treeModelRoot, 0);
		}
		if (tree.getSelectedItem() != null)
		{
			final int index = tree.getSelectedItem().indexOf();
			return create(tree, target, index);
		}
		return null;

	}

	@Override
	public TypedObject create(final Tree tree, final TypedObject target, final int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final Tree tree, final Object object, final TypedObject target)
	{

		final DraggedItem draggedItem = UISessionUtils.getCurrentSession().getCurrentPerspective().getDragAndDropWrapperService()
				.getWrapper().getDraggedItem((Component) object);


		if (draggedItem.getSingleTypedObject() != null)
		{
			final List<TypedObject> typedObjects = new ArrayList<TypedObject>(draggedItem.getAllTypedObjects());
			final List<ProductModel> draggedObjects = new ArrayList<ProductModel>();

			final Iterator<TypedObject> it = typedObjects.iterator();
			while (it.hasNext())
			{
				draggedObjects.add((ProductModel) it.next().getObject());
			}

			final BundleTemplateModel targetBundleTemplateModel = (BundleTemplateModel) target.getObject();
			bundleNavigationService.add(targetBundleTemplateModel, draggedObjects);

			UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();


		}


		//check if it is content page from "content pages" list box:
		if (object instanceof Listitem)
		{
			final Listitem listitem = (Listitem) object;
			final Object value = listitem.getValue();
			if (value instanceof TypedObject)
			{
				final Window modal = getMoveItemModalDialog(tree, (TypedObject) value, target, getBundleNavigationService());
				modal.doHighlighted();

			}
		}
	}

	@Override
	public void move(final Tree tree, final TypedObject node, final TypedObject target, final boolean addAsChild)
	{
		ComponentsHelper.displayNotification("general.error", "cmscockpit.navigationnode.drag.error");
	}

	/**
	 * Returns window dialog with options when dropping content page. Also handles click events.
	 * 
	 * @param tree
	 *           to find selected node
	 * @param typedObject
	 *           to move/copy
	 * @param targetNodeObj
	 *           - target node over which content page is dropped
	 * @param bundleNavigationService
	 * @return - dialog window
	 */
	public static Window getMoveItemModalDialog(final Tree tree, final TypedObject typedObject, final TypedObject targetNodeObj,
			final BundleNavigationService bundleNavigationService)
	{
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("title", Labels.getLabel("general.choose"));
		arguments.put("message", Labels.getLabel("configurablebundlecockpits.bundle.label.copymove"));

		final Button moveNodeButton = new Button(Labels.getLabel("general.move"));
		moveNodeButton.addEventListener(Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception//NOPMD ZK Specific
			{
				final BundleTemplateModel sourceBundleTemplateModel = (BundleTemplateModel) ((TypedObject) tree.getSelectedItem()
						.getValue()).getObject();
				final BundleTemplateModel targetBundleTemplateModel = (BundleTemplateModel) targetNodeObj.getObject();
				final ProductModel productModel = (ProductModel) typedObject.getObject();
				bundleNavigationService.move(sourceBundleTemplateModel, productModel, targetBundleTemplateModel);

				((Window) moveNodeButton.getSpaceOwner()).detach();

				UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();
			}
		});
		arguments.put("moveNodeButton", moveNodeButton);
		final Button copyNodeButton = new Button(Labels.getLabel("general.copy"));
		copyNodeButton.addEventListener(Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD ZK Specific
			{
				final BundleTemplateModel targetBundleTemplateModel = (BundleTemplateModel) targetNodeObj.getObject();
				final ProductModel productModel = (ProductModel) typedObject.getObject();
				bundleNavigationService.add(targetBundleTemplateModel, productModel);

				((Window) copyNodeButton.getSpaceOwner()).detach();

				UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();
			}
		});
		arguments.put("copyNodeButton", copyNodeButton);
		return (Window) Executions.createComponents("/productcockpit/messagedialog.zul", null, arguments);
	}

	@Override
	public void add(final Tree tree, final Object object, final TypedObject target, final int index)
	{
		// Not supported
	}

	@Override
	public void delete(final Tree tree, final TypedObject node)
	{
		if (!(getSystemService().checkPermissionOn(node.getType().getCode(), PermissionsConstants.REMOVE)))
		{
			return;
		}

		String message = "";

		final BundleTemplateModel templateModel = (BundleTemplateModel) node.getObject();
		final BundleTemplateStatusModel statusModel = templateModel.getStatus();

		if (templateModel.getParentTemplate() == null)
		{
			if (BundleTemplateStatusEnum.ARCHIVED.equals(statusModel.getStatus()))
			{
				message = Labels.getLabel("configurablebundlecockpits.bundle.restore.confirmationMessage");
			}
			else
			{
				message = Labels.getLabel("configurablebundlecockpits.bundle.archive.confirmationMessage");
			}
		}
		else
		{
			if (isBundleTemplateDeletable(templateModel))
			{
				message = Labels.getLabel("configurablebundlecockpits.bundle.delete.confirmationMessage");
			}
			else
			{
				final BaseUICockpitPerspective basePerspective = (BaseUICockpitPerspective) UISessionUtils.getCurrentSession()
						.getCurrentPerspective();
				if (basePerspective.getNotifier() == null)
				{
					return;
				}
				basePerspective.getNotifier().setDialogNotification(
						new Notification(Labels.getLabel("configurablebundlecockpits.bundle.delete.notallowedHeader"), Labels
								.getLabel("configurablebundlecockpits.bundle.delete.notallowedMessage")));
				return;
			}
		}

		ComponentsHelper.displayConfirmationPopup("", message, new DeleteListener(tree, node));

	}

	protected boolean isBundleTemplateDeletable(final BundleTemplateModel templateModel)
	{
		final List<BundleTemplateModel> bundleTemplates = getAllCatalogVersionsOfBundleTemplate(templateModel);

		if (CollectionUtils.isNotEmpty(bundleTemplates))
		{
			for (final BundleTemplateModel bundleTemplate : bundleTemplates)
			{
				if (getBundleTemplateService().isBundleTemplateUsed(bundleTemplate))
				{
					return false;
				}
			}
		}

		final BundleTemplateStatusModel statusModel = templateModel.getStatus();
		if (BundleTemplateStatusEnum.APPROVED.equals(statusModel.getStatus()))
		{
			return false;
		}

		return true;
	}

	protected List<BundleTemplateModel> getAllCatalogVersionsOfBundleTemplate(final BundleTemplateModel templateModel)
	{
		final BundleTemplateModel exampleTemplate = new BundleTemplateModel();
		exampleTemplate.setId(templateModel.getId());
		exampleTemplate.setVersion(templateModel.getVersion());

		return getFlexibleSearchService().getModelsByExample(exampleTemplate);
	}

	@Override
	public void doubleClicked(final Tree tree, final TypedObject currentNode)
	{
		UISessionUtils.getCurrentSession().getCurrentPerspective().activateItemInEditor(currentNode);
	}

	protected Set<TypedObject> extractSelectedItem(final Set<Treeitem> selectedItems)
	{
		final Set<TypedObject> selectedItem = new HashSet<TypedObject>();
		for (final Treeitem treeitem : selectedItems)
		{
			selectedItem.add((TypedObject) treeitem.getValue());
		}
		return selectedItem;
	}

	protected List<List<Integer>> getOpenedNodes(final Tree tree, final int depth)
	{
		final List<List<Integer>> ret = new ArrayList<List<Integer>>();

		for (final Object treeitem : tree.getItems())
		{
			final Treeitem treeItem = (Treeitem) treeitem;
			if (treeItem.getLevel() <= depth && treeItem.isOpen() && treeItem.getTreechildren() != null
					&& treeItem.getTreechildren().getChildren().size() > 0)
			{
				final List<Integer> path = getPathToRoot(treeItem);

				if (!path.isEmpty())
				{
					ret.add(path);
				}
			}
		}
		return ret;
	}

	public void openCreatedNode(final Tree tree, final TypedObject typedObject, final boolean addAsAChild)
	{
		if (tree == null || typedObject == null)
		{
			return;
		}
		final Treeitem treeitem = tree.getSelectedItem();
		if (treeitem != null)
		{
			treeitem.setOpen(addAsAChild);
		}
		if (selectedItems == null)
		{
			selectedItems = new HashSet<TypedObject>();
		}
		else
		{
			selectedItems.clear();
			selectedItems.add(typedObject);
		}
		refresh(tree);
	}


	protected List<Integer> getPathToRoot(final Treeitem treeItem)
	{
		final List<Integer> ret = new ArrayList<Integer>();
		Treeitem currentItem = treeItem;
		while (currentItem != null)
		{
			ret.add(Integer.valueOf(currentItem.indexOf()));
			if (!currentItem.isOpen() || !currentItem.isVisible())
			{
				ret.clear();
				break;
			}
			currentItem = currentItem.getParentItem();
		}

		Collections.reverse(ret);
		return ret;
	}

	/**
	 * Restores whatever was selected (before tree invalidation etc) Call it after tree open state is restored see
	 * {@link #restoreOpenedState(Tree, List)} because only opened nodes are search through.
	 */
	protected void restoreSelectionState(final Tree tree)
	{
		if (CollectionUtils.isNotEmpty(selectedItems))
		{
			final TypedObject selectedItem = selectedItems.iterator().next();
			final List<Treeitem> treeitems = tree.getTreechildren().getChildren();
			final Treeitem treeitem = searchForSelectedNode(treeitems, selectedItem);
			if (treeitem != null)
			{
				tree.selectItem(treeitem);
				showNodeActions(tree, Collections.singleton(treeitem));
			}

		}
	}

	/**
	 * @return treeitem to be selected, null if nothing found
	 */
	protected Treeitem searchForSelectedNode(final List<Treeitem> treeitems, final TypedObject selectedItem)
	{
		for (final Treeitem treeitem : treeitems)
		{
			if (selectedItem.equals(treeitem.getValue()))
			{
				return treeitem;
			}
			if (treeitem.isOpen() && !treeitem.isEmpty())//search only in opened nodes
			{
				final Treeitem foundToBeSelected = searchForSelectedNode(treeitem.getTreechildren().getChildren(), selectedItem);
				if (foundToBeSelected != null)
				{
					return foundToBeSelected;
				}
			}
		}
		return null;
	}

	/**
	 * Restores opened state
	 */
	protected void restoreOpenedState(final Tree tree, final List<List<Integer>> openedNodes)
	{
		for (final List<Integer> path : openedNodes)
		{
			openPath(tree, path);
		}
	}

	/**
	 * Opens specified path
	 */
	protected void openPath(final Tree tree, final List<Integer> path)
	{
		List<Treeitem> treeitems = tree.getTreechildren().getChildren();

		for (final Integer integer : path)
		{
			if (integer.intValue() < treeitems.size())
			{
				final Treeitem treeItem = treeitems.get(integer.intValue());
				treeItem.setOpen(true);
				if (treeItem.getTreechildren() == null)
				{
					break;
				}
				treeitems = treeItem.getTreechildren().getChildren();
			}
		}
	}

	public BundleNavigationService getBundleNavigationService()
	{
		if (this.bundleNavigationService == null)
		{
			this.bundleNavigationService = (BundleNavigationService) SpringUtil.getBean("bundleNavigationService");
		}
		return this.bundleNavigationService;
	}

	public void setBundleNavigationService(final BundleNavigationService bundleNavigationService)
	{
		this.bundleNavigationService = bundleNavigationService;
	}

	protected List<SyncItemJobModel>[] getSyncJobs(final Context context)
	{
		return (List<SyncItemJobModel>[]) context.getMap().get(SYNC_JOBS_PARAM);
	}

	@Override
	public Object customAction(final Tree tree, final Event event, final TypedObject node)
	{
		final SyncContext syncContext = getSynchronizationService().getSyncContext(node);
		final CatalogVersionModel sourceCatalogVersionModel = getSynchronizationService().getCatalogVersionForItem(node);

		if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_OK)
		{
			return null;
		}

		try
		{
			final List<SyncItemJobModel>[] matrixRules = syncContext.getSyncJobs();
			final int size = matrixRules[0].size() + matrixRules[1].size();
			if (size > 1)
			{
				//standard window 2 tabs [source|target] final
				final OneSourceManyTargetItemSyncDialog dialog = new OneSourceManyTargetItemSyncDialog(node,
						sourceCatalogVersionModel, matrixRules)
				{
					@Override
					public void updateBackground(final List<String> chosenRules)
					{
						Clients.showBusy(null, false);
						sendUpdateEvents(tree, node);
					}
				};
				detachDialog(dialog);
				appendHighlightedDialog(event, dialog);
			}
			else if (matrixRules[0].size() == 1 && CollectionUtils.isEmpty(matrixRules[1]))
			{
				Clients.showBusy(Labels.getLabel("busy.sync"), true);

				for (final SyncItemJobModel job : sourceCatalogVersionModel.getSynchronizations())
				{
					LOG.info(job);
				}

				getSynchronizationService().performSynchronization(Collections.singletonList(node.getObject()), null, null, null);

				sendUpdateEvents(tree, node);
			}
		}
		finally
		{
			Clients.showBusy(null, false);
		}

		return null;
	}

	protected void appendHighlightedDialog(final Event event, final OneSourceManyTargetItemSyncDialog dialog)
	{
		event.getTarget().getRoot().appendChild(dialog);
		dialog.doHighlighted();
	}

	protected void sendUpdateEvents(final Tree tree, final TypedObject node)
	{
		UISessionUtils.getCurrentSession().sendGlobalEvent(new ItemChangedEvent(tree, node, null));

		//update view after the staged item is synchronized
		final BaseUICockpitPerspective perspective = (BaseUICockpitPerspective) UISessionUtils.getCurrentSession()
				.getCurrentPerspective();
		perspective.getNavigationArea().update();
		for (final BrowserModel visBrowser : perspective.getBrowserArea().getVisibleBrowsers())
		{
			visBrowser.updateItems();
		}
	}

	public void refresh(final Tree tree)
	{
		refresh(tree, getOpenedPath());
	}

	public void refresh(final Tree tree, final List<List<Integer>> openedNodes)
	{
		tree.setModel(new BundleTemplateTreeModel(tree.getModel().getRoot()));
		tree.invalidate();
		restoreOpenedState(tree, openedNodes);
		restoreSelectionState(tree);
	}

	protected void detachDialog(final Window dialog)
	{
		dialog.addEventListener(Events.ON_OPEN, dialogEvent ->
		{
			if (dialogEvent instanceof OpenEvent && !((OpenEvent) dialogEvent).isOpen())
			{
				dialog.detach();
			}
		});
	}

	protected ModelService getModelService()
	{
		if (this.modelService == null)
		{
			this.modelService = UISessionUtils.getCurrentSession().getModelService();
		}
		return this.modelService;
	}

	protected TypeService getTypeService()
	{
		if (this.typeService == null)
		{
			this.typeService = UISessionUtils.getCurrentSession().getTypeService();
		}
		return this.typeService;
	}

	protected SystemService getSystemService()
	{
		if (this.systemService == null)
		{
			this.systemService = UISessionUtils.getCurrentSession().getSystemService();
		}
		return this.systemService;
	}

	protected SynchronizationService getSynchronizationService()
	{
		if (this.synchronizationService == null)
		{
			this.synchronizationService = (SynchronizationService) SpringUtil.getBean(SYNCHRONIZATION_SERVICE);
		}
		return this.synchronizationService;
	}

	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public void setSystemService(final SystemService systemService)
	{
		this.systemService = systemService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public String getNavigationNodeWizardId()
	{
		return navigationNodeWizardId;
	}

	public void setNavigationNodeWizardId(final String navigationNodeWizardId)
	{
		this.navigationNodeWizardId = navigationNodeWizardId;
	}

	public void setSynchronizationService(final SynchronizationService synchronizationService)
	{
		this.synchronizationService = synchronizationService;
	}


	public String getRelatedResourceWizardId()
	{
		return relatedResourceWizardId;
	}

	public void setRelatedResourceWizardId(final String relatedResourceWizardId)
	{
		this.relatedResourceWizardId = relatedResourceWizardId;
	}

	/**
	 * Adds new or existing products to the product list of a bundletemplate.
	 * 
	 * @param bundleTemplateModel
	 * @param typedProductsToAdd
	 */
	public void addProductsToNode(final BundleTemplateModel bundleTemplateModel, final Collection<TypedObject> typedProductsToAdd)
	{
		final Collection<ProductModel> products = new ArrayList<ProductModel>();
		for (final TypedObject typedObject : typedProductsToAdd)
		{
			products.add((ProductModel) typedObject.getObject());
		}

		bundleNavigationService.add(bundleTemplateModel, products);
	}

	protected BundleTemplateService getBundleTemplateService()
	{
		return bundleTemplateService;
	}

	public void setBundleTemplateService(final BundleTemplateService bundleTemplateService)
	{
		this.bundleTemplateService = bundleTemplateService;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	private class DeleteListener implements EventListener
	{
		private final Tree tree;
		private final TypedObject node;

		public DeleteListener(final Tree paramTree, final TypedObject node)
		{
			this.tree = paramTree;
			this.node = node;
		}

		@Override
		public void onEvent(final Event event) throws Exception//NOPMD ZK Specific
		{
			if (((Integer) event.getData()).intValue() != Messagebox.YES)
			{
				return;
			}

			final BundleTemplateModel templateModel = (BundleTemplateModel) node.getObject();
			if (templateModel != null)
			{
				if (templateModel.getParentTemplate() == null)
				{
					final BundleTemplateStatusModel statusModel = templateModel.getStatus();

					if (BundleTemplateStatusEnum.ARCHIVED.equals(statusModel.getStatus()))
					{
						statusModel.setStatus(BundleTemplateStatusEnum.CHECK);
					}
					else
					{
						statusModel.setStatus(BundleTemplateStatusEnum.ARCHIVED);
					}

					getModelService().save(statusModel);
				}
				else
				{
					final BundleTemplateModel parentTemplate = templateModel.getParentTemplate();
					getModelService().removeAll(getAllCatalogVersionsOfBundleTemplate(templateModel));
					getModelService().refresh(parentTemplate);
				}

				refresh(tree);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("tree invalidated after root template (un)archiving");
				}

				UISessionUtils.getCurrentSession().sendGlobalEvent(
						new ItemChangedEvent(this.tree, this.node, Collections.emptyList(), ItemChangedEvent.ChangeType.REMOVED));
			}
		}
	}
}

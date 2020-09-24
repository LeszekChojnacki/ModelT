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
import de.hybris.platform.cockpit.components.mvc.tree.TreeConstants;
import de.hybris.platform.cockpit.components.mvc.tree.TreeController;
import de.hybris.platform.cockpit.components.mvc.tree.events.ExtendedDropEvent;
import de.hybris.platform.cockpit.components.mvc.tree.listeners.CustomActionListener;
import de.hybris.platform.cockpit.components.mvc.tree.listeners.DeleteListener;
import de.hybris.platform.cockpit.components.mvc.tree.listeners.DoubleClickListener;
import de.hybris.platform.cockpit.components.mvc.tree.view.DefaultNodeWithActionsRenderer;
import de.hybris.platform.cockpit.constants.ImageUrls;
import de.hybris.platform.cockpit.model.meta.ObjectTemplate;
import de.hybris.platform.cockpit.model.meta.ObjectType;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.SystemService;
import de.hybris.platform.cockpit.services.label.LabelService;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.services.sync.SynchronizationService;
import de.hybris.platform.cockpit.services.sync.SynchronizationService.SyncContext;
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.CreateContext;
import de.hybris.platform.cockpit.util.TypeTools;
import de.hybris.platform.cockpit.util.UITools;
import de.hybris.platform.cockpit.wizards.generic.AbstractGenericItemPage;
import de.hybris.platform.cockpit.wizards.generic.GenericItemWizard;
import de.hybris.platform.cockpit.wizards.generic.NewItemWizard;
import de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.BundleNavigationNodeBrowserModel;
import de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.type.BundleRuleType;
import de.hybris.platform.configurablebundleservices.enums.BundleTemplateStatusEnum;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateStatusModel;
import de.hybris.platform.configurablebundleservices.model.ChangeProductPriceBundleRuleModel;
import de.hybris.platform.configurablebundleservices.model.DisableProductBundleRuleModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Box;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;


/**
 * Renderer for navigation nodes in tree, contains set of default actions per node
 *
 * @spring.bean bundleNavigationNodeRenderer
 */
public class BundleNavigationNodeRenderer extends DefaultNodeWithActionsRenderer
{
	protected static final String SYNCHRONIZATION_STATUS_OK = "cockpit/images/icon_status_sync.png";
	protected static final String SYNCHRONIZATION_TOOLTIP_OK = "cmscockpit.navigationnodes.synchornized";
	protected static final String SYNCHRONIZATION_STATUS_NOT_OK = "cockpit/images/icon_status_unsync.png";
	protected static final String SYNCHRONIZATION_TOOLTIP_NOT_OK = "cmscockpit.navigationnodes.notSynchornized";
	protected static final String SYNCHRONIZATION_STATUS_NA = "cockpit/images/icon_status_sync_unavailable.png";
	protected static final String SYNCHRONIZATION_TOOLTIP_NA = "cmscockpit.navigationnodes.sync_na";
	protected static final String ADD_AS_SIBLING_TOOLTIP = "configurablebundlecockpits.navigationnodes.addAsASibling";
	protected static final String ADD_AS_CHILD_TOOLTIP = "configurablebundlecockpits.navigationnodes.addAsAChild";
	protected static final String REMOVE_NN_TOOLTIP = "configurablebundlecockpits.bundle.delete.tooltip";
	protected static final String ARCHIVE_NN_TOOLTIP = "configurablebundlecockpits.bundle.archive.tooltip";
	protected static final String RESTORE_NN_TOOLTIP = "configurablebundlecockpits.bundle.restore.tooltip";
	protected static final String CLONE_NN_TOOLTIP = "configurablebundlecockpits.navigationnodes.clone";
	protected static final String EDIT_NN_TOOLTIP = "cmscockpit.navigationnodes.edit";
	protected static final String LIST_PRICE_RULES = "configurablebundlecockpits.product.list_changepricerules";
	protected static final String LIST_AVAILABILITY_RULES = "configurablebundlecockpits.product.list_availabilityrules";
	protected static final String NAVIGATION_NODE_TREE_ROW_SCLASS = "navigationTreeRow";
	protected static final String ADD_RELATED_ITEMS_BTG_SCLASS = "addRelatedItemsAddBtn";
	private static final String SYNCHRONIZATION_SERVICE = "synchronizationService";
	private final Map<String, BundleTemplateModel> templateIdsMap = new HashMap<String, BundleTemplateModel>();
	private BundleTemplateStatusModel bundleStatus;
	private TreeController controller;

	private LabelService labelService;
	private SystemService systemService;
	private SynchronizationService synchronizationService;
	private TypeService typeService;
	private KeyGenerator cloneIdGenerator;

	public BundleNavigationNodeRenderer()
	{
		super();
	}

	public BundleNavigationNodeRenderer(final EventListener dropListener)
	{
		super(dropListener);
	}

	@Override
	public void render(final Treeitem item, final Object data) throws Exception //NOPMD:ZK Specific
	{
		item.addEventListener(Events.ON_OPEN, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD:ZK Specific
			{
				controller.customAction(null, event, data);

			}
		});

		super.render(item, data);
		final Treerow treerow = item.getTreerow();
		if (treerow != null)
		{
			treerow.setDroppable("false");
			treerow.setDraggable("false");
		}
	}

	@Override
	public Treerow renderRow(final Treeitem treeItem, final Object node)
	{
		treeItem.setSclass(NAVIGATION_NODE_TREE_ROW_SCLASS);

		final TypedObject typedObject = (TypedObject) node;

		Treerow treeRow = null;
		treeItem.setValue(node);
		if (treeItem.getTreerow() == null)
		{
			treeRow = new Treerow();
			treeRow.setParent(treeItem);
		}
		else
		{
			treeRow = treeItem.getTreerow();
			treeRow.getChildren().clear();
		}

		treeRow.addEventListener(Events.ON_DOUBLE_CLICK, new DoubleClickListener());
		prepareLabelCell(typedObject, treeRow);
		prepareActionsNode(typedObject, treeRow);

		return treeRow;
	}

	protected void prepareActionsNode(final Object node, final Treerow treeRow)
	{
		final Treecell actionsCell = new Treecell();
		actionsCell.setStyle("text-align:right;");
		addActions(actionsCell, node);
		actionsCell.setParent(treeRow);
	}

	protected void prepareLabelCell(final TypedObject typedObject, final Treerow treeRow)
	{
		final boolean testIDsEnabled = UISessionUtils.getCurrentSession().isUsingTestIDs();
		final BundleTemplateModel bundleTemplateModel = (BundleTemplateModel) typedObject.getObject();

		String droppable = "false";
		if (CollectionUtils.isEmpty(bundleTemplateModel.getChildTemplates()))
		{
			droppable = "true";
		}

		final Treecell labelCell = new Treecell();
		labelCell.setDroppable(droppable);
		labelCell.setParent(treeRow);
		labelCell.addEventListener(Events.ON_DROP, event -> {
			if (event instanceof DropEvent)
			{
				sendExtendedDropEvent((DropEvent) event, true, true);
			}
		});

		final Box labelBox = new Box();
		labelBox.setParent(labelCell);
		labelBox.setOrient("horizontal");
		labelBox.setSclass("navigationNodesDropSlotVbox");

		final SyncContext nodeSyncCtx = computeSyncContext(typedObject);
		boolean activeVersion = isActiveCatalog(nodeSyncCtx);

		final String synchImageUrl = getSyncImageUrl(nodeSyncCtx);
		if (!activeVersion && StringUtils.isNotBlank(synchImageUrl))
		{
			final Toolbarbutton syncButton = new Toolbarbutton("", synchImageUrl);
			syncButton.setTooltiptext(Labels.getLabel(getSyncTooltip(nodeSyncCtx)));
			syncButton.addEventListener(Events.ON_CLICK, new CustomActionListener());
			if (testIDsEnabled)
			{
				UITools.applyTestID(syncButton, "synNN");
			}
			labelBox.appendChild(syncButton);
		}

		final Label label = new Label(getLabelService().getObjectTextLabelForTypedObject(typedObject));
		if (UISessionUtils.getCurrentSession().isUsingTestIDs())
		{
			UITools.applyTestID(label, "navigationNode#" + label.hashCode());
		}

		updateLabelIfArchived(bundleTemplateModel, label);
		label.setDroppable("false");
		labelBox.appendChild(label);
	}

	protected boolean isActiveCatalog(SyncContext nodeSyncCtx)
	{
		boolean activeVersion = false;
		for (final CatalogVersionModel catmodel : nodeSyncCtx.getSourceCatalogVersions())
		{
			if (catmodel.getActive() && !activeVersion)
			{
				activeVersion = catmodel.getActive();
			}
		}
		return activeVersion;
	}

	protected void updateLabelIfArchived(BundleTemplateModel bundleTemplateModel, Label label)
	{
		if (bundleTemplateModel.getParentTemplate() == null
				&& BundleTemplateStatusEnum.ARCHIVED.equals(bundleTemplateModel.getStatus().getStatus()))
		{
			label.setValue(Labels.getLabel("configurablebundlecockpits.bundle.archive.prefix") + " " + label.getValue());
		}
	}

	/**
	 * Creates button for adding a new bundle </p>
	 */
	protected Toolbarbutton createToolbarButton(final CatalogVersionModel catalogVersion, final BundleTemplateModel template,
			final String image, final String tooltip)
	{
		final Toolbarbutton addChild = new Toolbarbutton("", image);
		addChild.setTooltiptext(Labels.getLabel(tooltip));
		addChild.setStyle("margin-left:3px");
		addChild.addEventListener(Events.ON_CLICK, new OnClickEventListener(catalogVersion, template));
		addChild.setSclass(ADD_RELATED_ITEMS_BTG_SCLASS);
		return addChild;
	}

	@Override
	protected void addActions(final Treecell actionsCell, final Object data)
	{
		final boolean testIDsEnabled = UISessionUtils.getCurrentSession().isUsingTestIDs();

		final Div actionsCellCnt = new Div();
		actionsCellCnt.setParent(actionsCell);
		final TypedObject currentNode = (TypedObject) data;
		final TypedObject bundleTemplate = getTypeService().wrapItem(currentNode.getObject());
		final BundleTemplateModel bundleTemplateModel = (BundleTemplateModel) bundleTemplate.getObject();
		final BundleTemplateStatusModel templateStatus = bundleTemplateModel.getStatus();

		if (!BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus())
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.CREATE))
		{
			final Toolbarbutton siblingButton = addSiblingTemplateAction(bundleTemplateModel, testIDsEnabled, currentNode);
			final Toolbarbutton childButton = addChildTemplateAction(bundleTemplateModel, testIDsEnabled, currentNode);
			final Toolbarbutton cloneButton = addCloneTemplateAction(bundleTemplateModel, testIDsEnabled);

			actionsCellCnt.appendChild(siblingButton);

			if (null != childButton)
			{
				actionsCellCnt.appendChild(childButton);
			}
			if (null != cloneButton)
			{
				actionsCellCnt.appendChild(cloneButton);
			}
		}

		// change price rules button
		// this part should only be executed if we have the perms to edit and are not on a root node
		if (!BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus())
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.CHANGE)
				&& bundleTemplateModel.getParentTemplate() != null)
		{
			actionsCellCnt.appendChild(productPriceAction(bundleTemplate, testIDsEnabled));
		}

		// availability/disable rules button
		if (!BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus())
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.CHANGE)
				&& bundleTemplateModel.getParentTemplate() != null)
		{
			actionsCellCnt.appendChild(disablePrductRulesAction(bundleTemplate, testIDsEnabled));
		}

		if (!BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus())
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.CHANGE))
		{
			actionsCellCnt.appendChild(editBundleAction(testIDsEnabled, currentNode));
		}

		// Delete/archive bundle button
		if (!BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus())
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.REMOVE))
		{
			actionsCellCnt.appendChild(removeBundleAction(bundleTemplateModel, testIDsEnabled));
		}

		// Restore from archive button
		if (BundleTemplateStatusEnum.ARCHIVED.equals(templateStatus.getStatus()) && bundleTemplateModel.getParentTemplate() == null
				&& getSystemService().checkPermissionOn(currentNode.getType().getCode(), PermissionsConstants.REMOVE))
		{
			actionsCellCnt.appendChild(addRestoreBundleAction(testIDsEnabled));
		}

		actionsCellCnt.setVisible(false);
	}

	protected Toolbarbutton addSiblingTemplateAction(final BundleTemplateModel bundleTemplateModel, final boolean testIDsEnabled,
			final TypedObject currentNode)
	{
		BundleTemplateModel template = null;
		if (bundleTemplateModel.getParentTemplate() != null)
		{
			template = bundleTemplateModel.getParentTemplate();
		}

		final Toolbarbutton addButton = createToolbarButton((CatalogVersionModel) computeSyncContext(currentNode)
				.getSourceCatalogVersions().toArray()[0], template, "/productcockpit/images/node_duplicate.png",
				ADD_AS_SIBLING_TOOLTIP);

		if (testIDsEnabled)
		{
			UITools.applyTestID(addButton, "addNN");
		}
		return addButton;
	}

	protected Toolbarbutton addChildTemplateAction(final BundleTemplateModel bundleTemplateModel, final boolean testIDsEnabled,
			final TypedObject currentNode)
	{
        final Toolbarbutton addChildButton = createToolbarButton((CatalogVersionModel) computeSyncContext(currentNode)
                .getSourceCatalogVersions().toArray()[0], bundleTemplateModel, "/productcockpit/images/node_add_child.png",
                ADD_AS_CHILD_TOOLTIP);

        if (testIDsEnabled)
        {
            UITools.applyTestID(addChildButton, "addSubNN");
        }
        return addChildButton;
	}

	protected Toolbarbutton addCloneTemplateAction(final BundleTemplateModel bundleTemplateModel, final boolean testIDsEnabled)
	{
		if (bundleTemplateModel.getParentTemplate() == null)
		{
			final Toolbarbutton cloneButton = new Toolbarbutton("", "/productcockpit/images/clone_btn.png");
			cloneButton.setTooltiptext(Labels.getLabel(CLONE_NN_TOOLTIP));
			cloneButton.setStyle("margin-left:2px");
			cloneButton.addEventListener(Events.ON_CLICK, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD:ZK Specific
				{
					templateIdsMap.clear();
					final BundleTemplateModel newTemplate = cloneBundle(bundleTemplateModel);

					UISessionUtils.getCurrentSession().getModelService().save(newTemplate);

					UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();

				}
			});
			if (testIDsEnabled)
			{
				UITools.applyTestID(cloneButton, "cloNN");
			}
			return cloneButton;
		}
		return null;
	}

	protected Toolbarbutton productPriceAction(final TypedObject bundleTemplate, final boolean testIDsEnabled)
	{
		final Toolbarbutton changePriceRuleButton = new Toolbarbutton("", "/productcockpit/images/rules_chng_price_d.png");

		changePriceRuleButton.setTooltiptext(Labels.getLabel(LIST_PRICE_RULES));
		changePriceRuleButton.setStyle("margin-left:4px");

		changePriceRuleButton.setImage("/productcockpit/images/rules_chng_price.png");
		changePriceRuleButton.addEventListener(Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event)
			{
				// we open 2 browsers, one for rules search and the other for adding rules

				addRuleEventAction(BundleRuleType.CHANGE_PRODUCT_PRICE_BUNDLE_RULE, bundleTemplate,
						"BundleTemplate.changeProductPriceBundleRules");
			}
		});
		if (testIDsEnabled)
		{
			UITools.applyTestID(changePriceRuleButton, "chngPR");
		}
		return changePriceRuleButton;
	}

	protected Toolbarbutton disablePrductRulesAction(final TypedObject bundleTemplate, final boolean testIDsEnabled)
	{
		final Toolbarbutton disableBundleRuleButton = new Toolbarbutton("", "/productcockpit/images/rules_disable_d.png");

		disableBundleRuleButton.setTooltiptext(Labels.getLabel(LIST_AVAILABILITY_RULES));
		disableBundleRuleButton.setStyle("margin-left:4px");


		disableBundleRuleButton.setImage("/productcockpit/images/rules_disable.png");
		disableBundleRuleButton.addEventListener(Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event)
			{

				// we open 2 browsers, one for rules search and the other for adding rules
				addRuleEventAction(BundleRuleType.DISABLE_PRODUCT_BUNDLE_RULE, bundleTemplate,
						"BundleTemplate.disableProductBundleRules");
			}
		});
		if (testIDsEnabled)
		{
			UITools.applyTestID(disableBundleRuleButton, "disablePR");
		}

		return disableBundleRuleButton;
	}

	protected Toolbarbutton editBundleAction(final boolean testIDsEnabled, final TypedObject currentNode)
	{
		final Toolbarbutton editButton = new Toolbarbutton("", ImageUrls.EDIT_ICON);
		editButton.setTooltiptext(Labels.getLabel(EDIT_NN_TOOLTIP));
		editButton.setStyle("margin-left:4px");
		editButton.addEventListener(Events.ON_CLICK, new EventListener()
		{

			@Override
			public void onEvent(final Event event)
			{
				UISessionUtils.getCurrentSession().getCurrentPerspective().activateItemInEditor(currentNode);
			}
		});
		if (testIDsEnabled)
		{
			UITools.applyTestID(editButton, "editNN");
		}

		return editButton;
	}

	protected Toolbarbutton removeBundleAction(final BundleTemplateModel bundleTemplateModel, final boolean testIDsEnabled)
	{
		String tooltiptext;
		if (bundleTemplateModel.getParentTemplate() == null)
		{
			tooltiptext = Labels.getLabel(ARCHIVE_NN_TOOLTIP);
		}
		else
		{
			tooltiptext = Labels.getLabel(REMOVE_NN_TOOLTIP);
		}

		final Toolbarbutton deleteButton = new Toolbarbutton("", "/productcockpit/images/node_delete.png");
		deleteButton.setTooltiptext(tooltiptext);
		deleteButton.setStyle("margin-left:2px");
		deleteButton.addEventListener(Events.ON_CLICK, new DeleteListener());
		if (testIDsEnabled)
		{
			UITools.applyTestID(deleteButton, "delNN");
		}

		return deleteButton;
	}

	protected Toolbarbutton addRestoreBundleAction(final boolean testIDsEnabled)
	{
		final String tooltiptext = Labels.getLabel(RESTORE_NN_TOOLTIP);
		final Toolbarbutton restoreButton = new Toolbarbutton("", "/productcockpit/images/icon_func_refresh_available.gif");
		restoreButton.setTooltiptext(tooltiptext);
		restoreButton.setStyle("margin-left:2px");
		restoreButton.addEventListener(Events.ON_CLICK, new DeleteListener());
		if (testIDsEnabled)
		{
			UITools.applyTestID(restoreButton, "restoreNN");
		}

		return restoreButton;
	}

	protected void addRuleEventAction(final BundleRuleType bundleRuleType, final TypedObject bundleTemplate,
			final String propertyQualifier)
	{
		// open 2 browsers, one for rules search and the other for adding rules

		final BrowserModel browserModel = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea()
				.getFocusedBrowser();

		final BundleTemplateModel bundleTemplateModel = (BundleTemplateModel) bundleTemplate.getObject();
		getModel(browserModel).openRelatedBundleQueryBrowser(bundleTemplateModel, browserModel, bundleRuleType);


		final PropertyDescriptor propertyDescriptor = getTypeService().getPropertyDescriptor(propertyQualifier);
		final ObjectTemplate objectTemplate = TypeTools.getValueTypeAsObjectTemplate(propertyDescriptor, getTypeService());
		final Map<String, ? extends Object> params = new HashMap<String, Object>();

		UISessionUtils.getCurrentSession().getCurrentPerspective()
				.openReferenceCollectionInBrowserContext(new ArrayList(), objectTemplate, bundleTemplate, params);
	}

	public BundleNavigationNodeBrowserModel getModel(final BrowserModel browserModel)
	{
		BundleNavigationNodeBrowserModel ret = null;
		if (browserModel instanceof BundleNavigationNodeBrowserModel)
		{
			ret = (BundleNavigationNodeBrowserModel) browserModel;
		}
		return ret;
	}

	protected BundleTemplateModel cloneBundle(final BundleTemplateModel bundleToBeCloned)
	{
		final BundleTemplateModel clonedBundleTemplateModel = UISessionUtils.getCurrentSession().getModelService()
				.clone(bundleToBeCloned);
		final String cloneGeneratedId = (String) getCloneIdGenerator().generate();

		//only for parent templates we will append the prefix "CLONE: "
		if (null == bundleToBeCloned.getParentTemplate())
		{
			clonedBundleTemplateModel.setName("CLONE: " + clonedBundleTemplateModel.getName());

			//always set the template status to check
			bundleStatus = UISessionUtils.getCurrentSession().getModelService().create(BundleTemplateStatusModel.class);

			bundleStatus.setCatalogVersion(clonedBundleTemplateModel.getCatalogVersion());
			bundleStatus.setStatus(BundleTemplateStatusEnum.CHECK);
		}

		cloneChildBundleTemplates(bundleToBeCloned, clonedBundleTemplateModel);

		clonedBundleTemplateModel.setId("BT_" + cloneGeneratedId);
		templateIdsMap.put(bundleToBeCloned.getId(), clonedBundleTemplateModel);

		clonedBundleTemplateModel.setStatus(bundleStatus);

		if (null != clonedBundleTemplateModel.getBundleSelectionCriteria())
		{
			clonedBundleTemplateModel.getBundleSelectionCriteria().setId("BSC_" + cloneGeneratedId);
		}

		if (!CollectionUtils.isEmpty(clonedBundleTemplateModel.getDisableProductBundleRules()))
		{
			for (final DisableProductBundleRuleModel disableProductBundleRuleModel : clonedBundleTemplateModel
					.getDisableProductBundleRules())
			{
				final String dpbrGeneratedId = (String) getCloneIdGenerator().generate();
				disableProductBundleRuleModel.setId("DPBR_" + dpbrGeneratedId);
			}
		}

		if (!CollectionUtils.isEmpty(clonedBundleTemplateModel.getChangeProductPriceBundleRules()))
		{
			for (final ChangeProductPriceBundleRuleModel changeProductPriceBundleRuleModel : clonedBundleTemplateModel
					.getChangeProductPriceBundleRules())
			{
				final String cppbrGeneratedId = (String) getCloneIdGenerator().generate();
				changeProductPriceBundleRuleModel.setId("CPPBR_" + cppbrGeneratedId);
			}
		}

		cloneRequiredBundleTemplates(bundleToBeCloned, clonedBundleTemplateModel);
		cloneDependentBundleTemplates(bundleToBeCloned, clonedBundleTemplateModel);

		clonedBundleTemplateModel.setCreationtime(new Date());
		clonedBundleTemplateModel.setModifiedtime(null);
		clonedBundleTemplateModel.setOwner(UISessionUtils.getCurrentSession().getUser());
		return clonedBundleTemplateModel;
	}

	protected void cloneChildBundleTemplates(final BundleTemplateModel bundleToBeCloned,
			final BundleTemplateModel clonedBundleTemplateModel)
	{
		if (CollectionUtils.isNotEmpty(bundleToBeCloned.getChildTemplates()))
		{
			final List<BundleTemplateModel> childTemplates = new ArrayList<>();

			for (final BundleTemplateModel childTemplate : bundleToBeCloned.getChildTemplates())
			{
				final BundleTemplateModel newChildTemplateModel = cloneBundle(childTemplate);
				childTemplates.add(newChildTemplateModel);
			}
			clonedBundleTemplateModel.setChildTemplates(childTemplates);
		}
	}

	protected void cloneRequiredBundleTemplates(final BundleTemplateModel bundleToBeCloned,
			final BundleTemplateModel clonedBundleTemplateModel)
	{
		if (!CollectionUtils.isEmpty(bundleToBeCloned.getRequiredBundleTemplates()))
		{
			final List<BundleTemplateModel> requiredBundles = new ArrayList<>();
			for (final BundleTemplateModel requiredBundle : bundleToBeCloned.getRequiredBundleTemplates())
			{
				if (null != templateIdsMap.get(requiredBundle.getId()))
				{
					requiredBundles.add(templateIdsMap.get(requiredBundle.getId()));
				}
			}

			clonedBundleTemplateModel.setRequiredBundleTemplates(requiredBundles);
		}
	}

	protected void cloneDependentBundleTemplates(final BundleTemplateModel bundleToBeCloned,
			final BundleTemplateModel clonedBundleTemplateModel)
	{
		for (final BundleTemplateModel dependentBundle : bundleToBeCloned.getDependentBundleTemplates())
		{
			final List<BundleTemplateModel> dependentBundles = new ArrayList<>();
			if (null != templateIdsMap.get(dependentBundle.getId()))
			{
				dependentBundles.add(templateIdsMap.get(dependentBundle.getId()));
			}
			clonedBundleTemplateModel.setDependentBundleTemplates(dependentBundles);
		}
	}

	/**
	 * Computes correct synchronization image url for given node
	 *
	 * @param currentNode
	 *           given node
	 *
	 */
	protected String computeSynchImageUrl(final TypedObject currentNode)
	{
		if (currentNode.getObject() instanceof BundleTemplateModel)
		{
			final SyncContext syncContext = getSynchronizationService().getSyncContext(currentNode);
			return getSyncImageUrl(syncContext);
		}
		return StringUtils.EMPTY;
	}

	protected String getSyncImageUrl(final SyncContext syncContext)
	{
		if (syncContext == null)
		{
			return SYNCHRONIZATION_STATUS_NA;
		}

		if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_OK)
		{
			return SYNCHRONIZATION_STATUS_OK;
		}
		else if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_NOT_OK)
		{
			return SYNCHRONIZATION_STATUS_NOT_OK;
		}
		else if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_NOT_AVAILABLE)
		{
			return SYNCHRONIZATION_STATUS_NA;
		}
		return StringUtils.EMPTY;
	}



	protected String getSyncTooltip(final SyncContext syncContext)
	{
		if (syncContext == null)
		{
			return SYNCHRONIZATION_TOOLTIP_NA;
		}
		if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_OK)
		{
			return SYNCHRONIZATION_TOOLTIP_OK;
		}
		else if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_NOT_OK)
		{
			return SYNCHRONIZATION_TOOLTIP_NOT_OK;
		}
		else if (syncContext.isProductSynchronized() == SynchronizationService.SYNCHRONIZATION_NOT_AVAILABLE)
		{
			return SYNCHRONIZATION_TOOLTIP_NA;
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Computes sync cotext for given node.</p>
	 *
	 * @param currentNode
	 *           given node
	 *
	 */
	protected SyncContext computeSyncContext(final TypedObject currentNode)
	{
		if (currentNode.getObject() instanceof BundleTemplateModel)
		{
			return getSynchronizationService().getSyncContext(currentNode);
		}
		return null;
	}

	protected void sendExtendedDropEvent(final DropEvent dropEvent, final boolean addAsChild, final boolean append)
	{
		Tree tree = null;
		Component target = null;
		tree = ((Treerow) dropEvent.getTarget().getParent()).getTree();
		target = dropEvent.getTarget().getParent();

		final Component source = dropEvent.getDragged();
		if (target != source)
		{
			final ExtendedDropEvent extendedDropEvent = new ExtendedDropEvent(TreeConstants.DROPPED, target, source,
					dropEvent.getX(), dropEvent.getY(), dropEvent.getKeys(), addAsChild, append);
			Events.sendEvent(tree, extendedDropEvent);
		}
	}

	@Override
	protected void registerDefaultOnDoubleClickListeners(final Treerow treeRow)
	{
		//NOOP
	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	public KeyGenerator getCloneIdGenerator()
	{
		return cloneIdGenerator;
	}

	public void setCloneIdGenerator(final KeyGenerator cloneIdGenerator)
	{
		this.cloneIdGenerator = cloneIdGenerator;
	}

	public SystemService getSystemService()
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

	protected TypeService getTypeService()
	{
		if (this.typeService == null)
		{
			this.typeService = UISessionUtils.getCurrentSession().getTypeService();
		}
		return this.typeService;
	}

	public TreeController getController()
	{
		return controller;
	}

	public void setController(final TreeController controller)
	{
		this.controller = controller;
	}

	private class OnClickEventListener implements EventListener
	{
		private CatalogVersionModel catalogVersion;
		private BundleTemplateModel template;

		private OnClickEventListener(final CatalogVersionModel catalogVersion, final BundleTemplateModel template)
		{
			this.catalogVersion = catalogVersion;
			this.template = template;
		}

		@Override
		public void onEvent(final Event event)
		{
			final ObjectTemplate bundleTemplateType = getTypeService().getObjectTemplate("BundleTemplate");

			final NewItemWizard wizard = new NewItemWizard(bundleTemplateType, UITools.getCurrentZKRoot(), UISessionUtils
					.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser())
			{
				@Override
				public void doAfterDone(final AbstractGenericItemPage page)
				{
					UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea().getFocusedBrowser().updateItems();
				}
			};

			final Map<String, Object> initialValues = new HashMap<String, Object>();
			initialValues.put("BundleTemplate.catalogVersion", catalogVersion);
			if (null != template)
			{
				initialValues.put("BundleTemplate.parentTemplate", template);
			}
			wizard.setPredefinedValues(initialValues);
			final CreateContext createContext = new CreateContext(bundleTemplateType.getBaseType(), null, null, null);
			final Set<ObjectType> allowedTypesSet = new HashSet<ObjectType>();
			allowedTypesSet.add(getTypeService().getBaseType("BundleTemplate"));
			createContext.setAllowedTypes(allowedTypesSet);
			wizard.setCreateContext(createContext);
			wizard.setDisplaySubTypes(true);
			wizard.setAllowSelect(false);
			wizard.setAllowCreate(true);
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("multiple", "true"); // value has to be a String and there is no static parameter for "multiple"
			parameters.put(GenericItemWizard.FORCE_CREATE_IN_WIZARD, Boolean.TRUE);
			wizard.setParameters(parameters);
			wizard.start();
		}
	}
}

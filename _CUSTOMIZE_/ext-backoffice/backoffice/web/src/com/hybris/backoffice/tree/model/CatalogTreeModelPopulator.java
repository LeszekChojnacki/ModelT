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
package com.hybris.backoffice.tree.model;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.model.ComponentModelPopulator;
import com.hybris.cockpitng.testing.annotation.InextensibleMethod;
import com.hybris.cockpitng.tree.factory.impl.DefaultNavigationTreeFactory;
import com.hybris.cockpitng.tree.node.DynamicNode;
import com.hybris.cockpitng.tree.node.DynamicNodePopulator;
import com.hybris.cockpitng.widgets.common.explorertree.ExplorerTreeController;
import com.hybris.cockpitng.widgets.common.explorertree.data.PartitionNodeData;
import com.hybris.cockpitng.widgets.common.explorertree.model.RefreshableTreeModel;


public class CatalogTreeModelPopulator implements ComponentModelPopulator<TreeModel<TreeNode<ItemModel>>>, DynamicNodePopulator
{

	public static final String MULTI_SELECT = CatalogTreeModelPopulator.class.getName() + "_multipleChoice";
	public static final String SHOW_ALL_CATALOGS_NODE = CatalogTreeModelPopulator.class.getSimpleName() + "_showAllCatalogsNode";
	public static final String SHOW_UNCATEGORIZED_ROOT_NODE = CatalogTreeModelPopulator.class.getSimpleName()
			+ "_showUncategorizedRootNode";
	public static final String SHOW_UNCATEGORIZED_CATALOG_NODE = CatalogTreeModelPopulator.class.getSimpleName()
			+ "_showUncategorizedCatalogNode";
	public static final String SHOW_UNCATEGORIZED_CATALOG_VERSION_NODE = CatalogTreeModelPopulator.class.getSimpleName()
			+ "_showUncategorizedCatalogVersionNode";

	public static final String ALL_CATALOGS_NODE_ID = "allCatalogs";
	public static final String UNCATEGORIZED_PRODUCTS_NODE_ID = "uncategorizedProducts";
	public static final String I18N_CATALOGTREEMODELPOPULATOR_ALLCATALOGS = "catalogtreemodelpopulator.allcatalogs";
	public static final String I18N_CATALOGTREEMODELPOPULATOR_UNCATEGORIZED = "catalogtreemodelpopulator.uncategorized";

	private static final Logger LOG = LoggerFactory.getLogger(CatalogTreeModelPopulator.class);
	public static final String SIMPLE_LABELS_CTX_PARAMETERS = "simpleLabels";

	private CatalogService catalogService;

	private CatalogVersionService catalogVersionService;

	private PermissionFacade permissionFacade;

	private TypeFacade typeFacade;

	private TypeService typeService;

	private CockpitUserService cockpitUserService;

	private UserService userService;

	private Set<String> excludedTypes;
	private final Map<String, Boolean> supportedCheckedTypeCodes = new ConcurrentHashMap<>();

	private LabelService labelService;

	private CatalogTreeSimpleLabelProvider catalogTreeSimpleLabelProvider;

	private int partitionThreshold;

	@PostConstruct
	public void postConstruct()
	{
		final Iterator<String> it = getExcludedTypes().iterator();
		while (it.hasNext())
		{
			final String type = it.next();
			try
			{
				getTypeService().getTypeForCode(type);
			}
			catch (final UnknownIdentifierException uie)
			{
				LOG.warn("Misspelled or unknown type name {}, excluding from filtered types", type);
				LOG.debug(uie.getMessage(), uie);
				it.remove();
			}
		}
		supportedCheckedTypeCodes.clear();
	}

	@Override
	public TreeModel<TreeNode<ItemModel>> createModel(final CockpitContext context)
	{
		final CatalogTreeModel model = new CatalogTreeModel(getRoot(context));
		if (context.containsParameter(MULTI_SELECT))
		{
			model.setMultiple(context.getParameterAsBoolean(MULTI_SELECT, false));
		}
		return model;
	}

	@Override
	public List<NavigationNode> getChildren(final NavigationNode node)
	{
		if (!(node instanceof DynamicNode))
		{
			throw new IllegalArgumentException("Only Dynamic Nodes are supported");
		}

		final List<NavigationNode> children = findChildrenNavigationNodes(node);
		if (children.size() > getPartitionThreshold())
		{
			return partitionNodes(node, children);
		}
		return children;
	}

	protected List<NavigationNode> findChildrenNavigationNodes(final NavigationNode node)
	{
		final Object nodeData = node.getData();
		if (nodeData instanceof CatalogModel)
		{
			return prepareCatalogVersionNodes(node, (CatalogModel) nodeData);
		}
		else if (nodeData instanceof CatalogVersionModel)
		{
			return prepareRootCategoryNodes(node, (CatalogVersionModel) nodeData);
		}
		else if (nodeData instanceof CategoryModel)
		{
			return prepareSubcategoryNodes(node, (CategoryModel) nodeData);
		}
		else if (nodeData instanceof PartitionNodeData)
		{
			return ((PartitionNodeData) nodeData).getChildren();
		}
		else
		{
			return prepareCatalogNodes(node);
		}
	}

	protected List<NavigationNode> prepareCatalogNodes(final NavigationNode node)
	{
		final TreeModel<TreeNode<ItemModel>> model = createModel(node.getContext());

		final List<NavigationNode> rootNodes = model.getRoot().getChildren().stream().filter(treeNode -> treeNode.getData() != null)
				.map(getRegularNodeCreatorFunction(node)).collect(Collectors.toList());

		if (node.getContext().getParameterAsBoolean(SHOW_UNCATEGORIZED_ROOT_NODE, false))
		{
			rootNodes.add(0, prepareUncategorizedProductsNode(node, null));
		}
		if (node.getContext().getParameterAsBoolean(SHOW_ALL_CATALOGS_NODE, true))
		{
			rootNodes.add(0, prepareAllCatalogsNode(node));
		}
		return rootNodes;
	}

	protected List<NavigationNode> prepareCatalogVersionNodes(final NavigationNode node, final CatalogModel nodeData)
	{
		final List<NavigationNode> catalogVersionsNodes = getAllReadableCatalogVersions(nodeData).stream()
				.filter(catalogVersion -> isCatalogVersionAvailableInContext(catalogVersion, node.getContext()))
				.map(DefaultTreeNode::new).map(getRegularNodeCreatorFunction(node)).collect(Collectors.toList());
		if (!(nodeData instanceof ClassificationSystemModel)
				&& node.getContext().getParameterAsBoolean(SHOW_UNCATEGORIZED_CATALOG_NODE, false))
		{
			catalogVersionsNodes.add(0, prepareUncategorizedProductsNode(node, nodeData));
		}
		return catalogVersionsNodes;
	}

	protected List<NavigationNode> prepareRootCategoryNodes(final NavigationNode node, final CatalogVersionModel nodeData)
	{
		final List<NavigationNode> categoriesNodes = filterAvailableCategories(getCategoryDynamicNodeCreatorFunction(node),
				nodeData.getRootCategories());
		if (!(nodeData instanceof ClassificationSystemVersionModel)
				&& node.getContext().getParameterAsBoolean(SHOW_UNCATEGORIZED_CATALOG_VERSION_NODE, true))
		{
			categoriesNodes.add(0, prepareUncategorizedProductsNode(node, nodeData));
		}
		return categoriesNodes;
	}

	protected List<NavigationNode> prepareSubcategoryNodes(final NavigationNode node, final CategoryModel nodeData)
	{
		return filterAvailableCategories(getCategoryDynamicNodeCreatorFunction(node), nodeData.getCategories());
	}

	protected Function<TreeNode, DynamicNode> getRegularNodeCreatorFunction(final NavigationNode node)
	{
		return treeNode -> {
			final String label = prepareNodeLabel(treeNode, node, getLabelService()::getObjectLabel);
			return createDynamicNode(node, treeNode, label);
		};
	}

	protected Function<TreeNode, DynamicNode> getCategoryDynamicNodeCreatorFunction(final NavigationNode node)
	{
		return treeNode -> {
			final String label = prepareNodeLabel(treeNode, node, getLabelService()::getShortObjectLabel);
			return createDynamicNode(node, treeNode, label);
		};
	}

	protected String prepareNodeLabel(final TreeNode treeNode, final NavigationNode parentNode,
			final Function<Object, String> labelServiceFn)
	{
		if (parentNode.getContext().getParameterAsBoolean(SIMPLE_LABELS_CTX_PARAMETERS, false))
		{
			final Object currentNodeData = treeNode.getData();
			final Object parentNodeData = parentNode.getData();
			final Optional<String> calculatedLabel = getCatalogTreeSimpleLabelProvider().getNodeLabel(parentNodeData,
					currentNodeData);
			if (calculatedLabel.isPresent())
			{
				return calculatedLabel.get();
			}
		}
		return labelServiceFn.apply(treeNode.getData());
	}

	protected DynamicNode createDynamicNode(final NavigationNode node, final TreeNode treeNode, final String label)
	{
		final int index = ((DynamicNode) node).getIndexingDepth() - 1;
		final DynamicNode dynamicNode = new DynamicNode(createDynamicNodeId(node, label), this, index);
		dynamicNode.setData(treeNode.getData());
		dynamicNode.setName(label);
		dynamicNode.setContext(createCockpitContext(node));
		dynamicNode.setSelectable(true);
		return dynamicNode;
	}

	protected DynamicNode prepareAllCatalogsNode(final NavigationNode rootNode)
	{
		final DynamicNode allCatalogsNode = new DynamicNode(createDynamicNodeId(rootNode, ALL_CATALOGS_NODE_ID),
				node -> Collections.emptyList(), 1);
		allCatalogsNode.setSelectable(true);
		allCatalogsNode.setActionAware(false);
		allCatalogsNode.setName(Labels.getLabel(I18N_CATALOGTREEMODELPOPULATOR_ALLCATALOGS));
		return allCatalogsNode;
	}

	protected DynamicNode prepareUncategorizedProductsNode(final NavigationNode rootNode, final ItemModel parentObject)
	{
		final DynamicNode uncategorizedNode = new DynamicNode(createDynamicNodeId(rootNode, UNCATEGORIZED_PRODUCTS_NODE_ID),
				node -> Collections.emptyList(), 1);
		uncategorizedNode.setSelectable(true);
		uncategorizedNode.setData(new UncategorizedNode(parentObject));
		uncategorizedNode.setName(Labels.getLabel(I18N_CATALOGTREEMODELPOPULATOR_UNCATEGORIZED));
		uncategorizedNode.setActionAware(false);
		return uncategorizedNode;
	}

	protected String createDynamicNodeId(final NavigationNode node, final String postFix)
	{
		final boolean isRoot = node != null && StringUtils.startsWith(node.getId(), DefaultNavigationTreeFactory.ROOT_NODE_ID);
		final String prefix = isRoot ? node.getId() : createParentNodesIdPrefix(node);
		return prefix + postFix;
	}

	protected String createParentNodesIdPrefix(final NavigationNode node)
	{
		final StringBuilder prefix = new StringBuilder();
		NavigationNode current = node;
		while (current != null)
		{
			final String id = current.getId();
			if (id != null && !isParentIdAppended(id + "_", prefix))
			{
				prefix.insert(0, '_');
				prefix.insert(0, id.toLowerCase());
			}
			current = current.getParent();
		}
		return prefix.toString();
	}

	protected boolean isParentIdAppended(final String parentId, final StringBuilder childId)
	{
		return (childId.length() >= parentId.length()) && (parentId.equals(childId.substring(0, parentId.length())));
	}

	protected List<NavigationNode> filterAvailableCategories(final Function<TreeNode, DynamicNode> nodeCreator,
			final Collection<CategoryModel> categories)
	{
		final Map<String, Boolean> supportedCategories = new HashMap<>();
		final Collection<CatalogVersionModel> allReadableCatalogVersions = getAllReadableCatalogVersionsForCurrentUser();

		return categories.stream().filter(category -> allReadableCatalogVersions.contains(category.getCatalogVersion()))
				.filter(category -> supportedCategories.computeIfAbsent(category.getItemtype(),
						typeCode -> isSupportedType(typeCode) && getPermissionFacade().canReadType(typeCode)))
				.map(DefaultTreeNode::new).map(nodeCreator).collect(Collectors.toList());
	}

	protected List<NavigationNode> partitionNodes(final NavigationNode parent, final List<NavigationNode> nodes)
	{
		final List<List<NavigationNode>> partitions = Lists.partition(nodes, getPartitionThreshold());
		final List<NavigationNode> portionsNodes = new ArrayList<>();
		for (int i = 0; i < partitions.size(); i++)
		{
			final List<NavigationNode> partition = partitions.get(i);
			final int from = i * getPartitionThreshold();
			final int to = from + partition.size();
			final String nodeName = String.format("%d ... %d", Integer.valueOf(from + 1), Integer.valueOf(to));
			final DynamicNode partitionNode = new DynamicNode(nodeName, this, getIndexingDepth(parent));
			partitionNode.setData(new PartitionNodeData(parent, partition));
			partitionNode.setSelectable(true);
			portionsNodes.add(partitionNode);
			partitionNode.setName(nodeName);
		}
		return portionsNodes;
	}

	protected int getIndexingDepth(final NavigationNode node)
	{
		return node instanceof DynamicNode ? ((DynamicNode) node).getIndexingDepth() : 0;
	}

	protected CockpitContext createCockpitContext(final NavigationNode node)
	{
		final CockpitContext context = new DefaultCockpitContext();
		context.addAllParameters(node.getContext());
		return context;
	}

	public Collection<CatalogModel> getAllReadableCatalogs(final CockpitContext context)
	{
		final Map<String, Boolean> typePermissions = new HashMap<>();
		return getCatalogService().getAllCatalogs().stream()
				.filter(catalog -> typePermissions.computeIfAbsent(catalog.getItemtype(),
						typeCode -> isSupportedType(typeCode) && getPermissionFacade().canReadType(typeCode)))
				.filter(catalog -> isCatalogAvailableInContext(catalog, context)).collect(Collectors.toList());
	}

	protected boolean isCatalogAvailableInContext(final CatalogModel catalogModel, final CockpitContext context)
	{
		if (context == null)
		{
			return true;
		}

		final Collection<Object> selectedItems = (Collection) context
				.getParameter(ExplorerTreeController.DYNAMIC_NODE_SELECTION_CONTEXT);

		if (selectedItems == null || selectedItems.isEmpty())
		{
			return true;
		}
		if (selectedItems.contains(catalogModel))
		{
			return true;
		}
		for (final CatalogVersionModel catalogVersion : catalogModel.getCatalogVersions())
		{
			if (selectedItems.contains(catalogVersion))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean isCatalogVersionAvailableInContext(final CatalogVersionModel catalogVersionModel,
			final CockpitContext context)
	{
		if (context == null)
		{
			return true;
		}

		final Collection<Object> selectedItems = (Collection<Object>) context
				.getParameter(ExplorerTreeController.DYNAMIC_NODE_SELECTION_CONTEXT);

		if (selectedItems == null || selectedItems.isEmpty())
		{
			return true;
		}

		if (selectedItems.contains(catalogVersionModel))
		{
			return true;
		}

		return selectedItems.contains(catalogVersionModel.getCatalog());
	}

	public synchronized TreeNode<ItemModel> getRoot(final CockpitContext context)
	{
		final List<DefaultTreeNode<CatalogModel>> nodes = getAllReadableCatalogs(context).stream().map(DefaultTreeNode::new)
				.collect(Collectors.toList());

		nodes.add(new AllCatalogsTreeNode(null));
		return new DefaultTreeNode(null, nodes);
	}

	public CatalogService getCatalogService()
	{
		return catalogService;
	}

	@Required
	public void setCatalogService(final CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	@Required
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected List<CatalogVersionModel> getAllReadableCatalogVersions(final CatalogModel data)
	{
		final TreeSet<CatalogVersionModel> versions = new TreeSet<>(
				(o1, o2) -> -ObjectUtils.compare(o1.getVersion(), o2.getVersion()));
		final Set<CatalogVersionModel> catalogVersions = data.getCatalogVersions();
		if (catalogVersions != null)
		{
			versions.addAll(catalogVersions);
		}

		final Collection<CatalogVersionModel> readableCatalogVersions = getAllReadableCatalogVersionsForCurrentUser();
		return versions.stream().filter(readableCatalogVersions::contains).filter(cv -> isSupportedType(cv.getItemtype()))
				.collect(Collectors.toList());
	}

	protected Collection<CatalogVersionModel> getAllReadableCatalogVersionsForCurrentUser()
	{
		final UserModel user = getUserService().getCurrentUser();
		if (getUserService().isAdmin(user))
		{
			return getCatalogVersionService().getAllCatalogVersions();
		}
		else
		{
			return getCatalogVersionService().getAllReadableCatalogVersions(user);
		}
	}

	protected boolean isSupportedType(final String typeCode)
	{
		if (StringUtils.isEmpty(typeCode))
		{
			return false;
		}
		else if (supportedCheckedTypeCodes.get(typeCode) == null)
		{
			synchronized (supportedCheckedTypeCodes)
			{
				if (supportedCheckedTypeCodes.get(typeCode) == null)
				{
					supportedCheckedTypeCodes.put(typeCode, computeTypeSupported(typeCode));
				}
			}
		}
		return supportedCheckedTypeCodes.get(typeCode);
	}

	@InextensibleMethod
	private boolean computeTypeSupported(final String typeCode)
	{
		for (final String type : getExcludedTypes())
		{
			if (getTypeService().isAssignableFrom(type, typeCode))
			{
				return false;
			}
		}
		return true;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public int getPartitionThreshold()
	{
		return partitionThreshold;
	}

	@Required
	public void setPartitionThreshold(final int partitionThreshold)
	{
		this.partitionThreshold = partitionThreshold;
	}

	public CatalogTreeSimpleLabelProvider getCatalogTreeSimpleLabelProvider()
	{
		return catalogTreeSimpleLabelProvider;
	}

	@Required
	public void setCatalogTreeSimpleLabelProvider(final CatalogTreeSimpleLabelProvider catalogTreeSimpleLabelProvider)
	{
		this.catalogTreeSimpleLabelProvider = catalogTreeSimpleLabelProvider;
	}

	public Set<String> getExcludedTypes()
	{
		if (excludedTypes == null)
		{
			excludedTypes = Collections.emptySet();
		}
		return excludedTypes;
	}

	public void setExcludedTypes(final Set<String> excludedTypes)
	{
		this.excludedTypes = excludedTypes;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public class CatalogTreeModel extends AbstractTreeModel<TreeNode<ItemModel>> implements RefreshableTreeModel
	{

		private final transient LoadingCache<CatalogModel, List<CatalogVersionModel>> catalogVersionsCache = CacheBuilder
				.newBuilder().build(new CacheLoader<CatalogModel, List<CatalogVersionModel>>()
				{
					@Override
					public List<CatalogVersionModel> load(final CatalogModel data)
					{
						return getAllReadableCatalogVersions(data);
					}
				});

		public CatalogTreeModel(final TreeNode<ItemModel> root)
		{
			super(root);
		}

		@Override
		public boolean isLeaf(final TreeNode<ItemModel> node)
		{

			if (node == super.getRoot())
			{
				return node.getChildCount() == 0;
			}

			final ItemModel data = node.getData();
			if (data instanceof CatalogModel)
			{
				return getCatalogVersions((CatalogModel) data).isEmpty();
			}

			return true;
		}

		@Override
		public TreeNode<ItemModel> getChild(final TreeNode<ItemModel> node, final int i)
		{
			if (node == super.getRoot())
			{
				return node.getChildAt(i);
			}
			final ItemModel data = node.getData();
			if (data instanceof CatalogModel)
			{
				return new DefaultTreeNode<>(getCatalogVersions((CatalogModel) data).get(i));
			}
			return null;
		}

		@Override
		public int getChildCount(final TreeNode<ItemModel> node)
		{
			if (node == super.getRoot())
			{
				return node.getChildren().size();
			}
			final ItemModel data = node.getData();
			if (data instanceof CatalogModel)
			{
				return getCatalogVersions((CatalogModel) data).size();
			}
			return 0;
		}

		private List<CatalogVersionModel> getCatalogVersions(final CatalogModel data)
		{
			try
			{
				return catalogVersionsCache.get(data);
			}
			catch (final ExecutionException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Cannot get catalog versions", e);
				}
				return Collections.emptyList();
			}
		}

		@Override
		public int[] getPath(final TreeNode<ItemModel> child)
		{
			final Deque<Integer> indexes = new ArrayDeque<>();
			TreeNode current = child;
			while (current != null)
			{
				final TreeNode parent = current.getParent();
				if (parent != null)
				{
					final int indexOfNode = parent.getChildren().indexOf(current);
					if (indexOfNode >= 0)
					{
						indexes.push(Integer.valueOf(indexOfNode));
					}
					else
					{
						break;
					}
				}
				current = parent;
			}
			return indexes.stream().mapToInt(Integer::intValue).toArray();
		}

		@Override
		public void refreshChildren(final Object node, final List children)
		{
			catalogVersionsCache.invalidate(node);
		}

		/**
		 * Always throws {@link UnsupportedOperationException}.
		 *
		 * @return nothing.
		 * @throws UnsupportedOperationException
		 *            always.
		 */
		@Override
		public List findNodesByData(final Object data)
		{
			throw new UnsupportedOperationException();
		}
	}
}

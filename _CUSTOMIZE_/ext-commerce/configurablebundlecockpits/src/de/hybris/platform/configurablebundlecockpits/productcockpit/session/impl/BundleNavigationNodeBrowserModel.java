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

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cockpit.components.contentbrowser.AbstractContentBrowser;
import de.hybris.platform.cockpit.model.advancedsearch.ConditionValue;
import de.hybris.platform.cockpit.model.advancedsearch.ConditionValueContainer;
import de.hybris.platform.cockpit.model.advancedsearch.impl.AdvancedSearchHelper;
import de.hybris.platform.cockpit.model.advancedsearch.impl.SimpleConditionValue;
import de.hybris.platform.cockpit.model.browser.BrowserModelFactory;
import de.hybris.platform.cockpit.model.meta.ObjectTemplate;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.model.meta.impl.ItemAttributePropertyDescriptor;
import de.hybris.platform.cockpit.model.search.ExtendedSearchResult;
import de.hybris.platform.cockpit.model.search.Operator;
import de.hybris.platform.cockpit.model.search.Query;
import de.hybris.platform.cockpit.model.search.SearchParameterValue;
import de.hybris.platform.cockpit.model.search.SearchType;
import de.hybris.platform.cockpit.services.label.LabelService;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.services.search.SearchProvider;
import de.hybris.platform.cockpit.services.search.impl.GenericSearchParameterDescriptor;
import de.hybris.platform.cockpit.services.search.impl.ItemAttributeSearchDescriptor;
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.UIBrowserArea;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.DefaultSearchBrowserModel;
import de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree.BundleTemplateTreeModel;
import de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.type.BundleRuleType;
import de.hybris.platform.configurablebundlecockpits.servicelayer.services.BundleNavigationService;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.productcockpit.session.impl.QueryBrowserCatalogVersionFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zkoss.spring.SpringUtil;

import com.google.common.collect.Lists;


// NOSONAR
public class BundleNavigationNodeBrowserModel extends DefaultSearchBrowserModel
{

	private static final String BUNDLE_TEMPLATE_PARENT_TEMPLATE = BundleTemplateModel._TYPECODE + "."
			+ BundleTemplateModel.PARENTTEMPLATE;
	private static final String BUNDLE_TEMPLATE_CATALOG_VERSION = BundleTemplateModel._TYPECODE + "."
			+ BundleTemplateModel.CATALOGVERSION;
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(BundleNavigationNodeBrowserModel.class);
	private static final String BUNDLE_NAVIGATION_SERVICE = "bundleNavigationService";
	private static final String BROWSER_MODEL = "BundleProductSearchBrowserModel";

	private Set<TypedObject> selectedNode;
	private List<List<Integer>> openedItems = Lists.newArrayList();
	private BundleTemplateTreeModel treeModel;
	private LabelService labelService;
	private TypeService typeService;
	private BundleNavigationService bundleNavigationService;

	public BundleNavigationNodeBrowserModel()
	{
		super(UISessionUtils.getCurrentSession().getTypeService().getObjectTemplate(BundleTemplateModel._TYPECODE));
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final CatalogVersionModel catVer = ((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion();

			if (catVer != null)
			{
				final CatalogModel catalog = catVer.getCatalog();
				final String catalogLabel = getLabelServiceInternal().getObjectTextLabelForTypedObject(
						getTypeServiceInternal().wrapItem(catalog));
				final String catVerLabel = getLabelServiceInternal().getObjectTextLabelForTypedObject(
						getTypeServiceInternal().wrapItem(catVer));
				final String arrows = " >> ";
				setLabel(catalogLabel + arrows + catVerLabel);
			}
		}
	}

	@Override
	public void removeItems(final Collection<Integer> indexes)
	{
		//
	}

	@Override
	public void blacklistItems(final Collection<Integer> indexes)
	{
		//
	}

	@Override
	public AbstractContentBrowser createViewComponent()
	{
		return new BundleNavigationNodeContentBrowser();
	}

	@Override
	public TypedObject getItem(final int index)
	{
		return null;
	}

	@Override
	public List<TypedObject> getItems()
	{
		return Collections.emptyList();
	}

	@Override
	// NOSONAR
	public Object clone() throws CloneNotSupportedException
	{
		return null;
	}

	public void clearPreservedState()
	{
		this.selectedNode = null;
		this.openedItems.clear();
	}

	public void removeSelectedNavigationNode(final BundleNavigationNodeContentBrowser content)
	{
		content.removeSelectedNavigationNode();
	}

	public void fireAddNewNavigationNode(final BundleNavigationNodeContentBrowser content)
	{
		content.fireAddRootNavigatioNode();
	}

	public int getTreeRootChildCount()
	{
		int ret = 0;
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final BundleNavigationNodeBrowserArea navigationBrowserArea = (BundleNavigationNodeBrowserArea) browserArea;
			final CatalogVersionModel catalogVersionModel = navigationBrowserArea.getActiveCatalogVersion();
			if (catalogVersionModel != null)
			{
				final List<BundleTemplateModel> rootNavigationNodes = getBundleNavigationNodeService().getRootNavigationNodes(
						navigationBrowserArea.getActiveCatalogVersion());
				ret = CollectionUtils.isEmpty(rootNavigationNodes) ? 0 : CollectionUtils.size(rootNavigationNodes);
			}
		}
		return ret;
	}

	public void openRelatedQueryBrowser()
	{
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final BundleNavigationNodeBrowserArea bArea = (BundleNavigationNodeBrowserArea) browserArea;
			this.focus();
			bArea.setSplittable(true);
			bArea.setSplitModeActiveDirectly(true);

			final BrowserModelFactory factory = (BrowserModelFactory) SpringUtil.getBean(BrowserModelFactory.BEAN_ID);
			final BundleProductSearchBrowserModel browserModel = (BundleProductSearchBrowserModel) factory
					.createBrowserModel(BROWSER_MODEL);
			browserModel.setLastQuery(new Query(null, "*", 0, 0));
			browserModel.setBrowserFilterFixed(new QueryBrowserCatalogVersionFilter(bArea.getActiveCatalogVersion()));
			bArea.addVisibleBrowser(1, browserModel);
			browserModel.updateItems();

			browserModel.focus();
		}
	}

	//responsible for showing search browser for bundle rules
	// NOSONAR
	public void openRelatedBundleQueryBrowser(final BundleTemplateModel bundleTemplateModel, final BrowserModel model,
			final BundleRuleType bundleRuleType)
	{
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final BundleNavigationNodeBrowserArea bArea = (BundleNavigationNodeBrowserArea) browserArea;
			this.focus();
			bArea.setSplittable(true);
			bArea.setSplitModeActiveDirectly(true);

			//put the template information as a search filter
			final BrowserModelFactory factory = (BrowserModelFactory) SpringUtil.getBean(BrowserModelFactory.BEAN_ID);
			final BundleRulesSearchBrowserModel browserModel = (BundleRulesSearchBrowserModel) factory
					.createBrowserModel(bundleRuleType.getModelName());
			final Query query = new Query(null, "*", 0, 0);

			final PropertyDescriptor bundleTemplatePropertyDesc = UISessionUtils.getCurrentSession().getTypeService()
					.getPropertyDescriptor(bundleRuleType.getTemplateBundleName());

			//creating the and condition
			final ConditionValueContainer condition = AdvancedSearchHelper.createSimpleConditionValue(bundleTemplateModel);

			final GenericSearchParameterDescriptor genericSearchParameterDesc = new ItemAttributeSearchDescriptor(
					(ItemAttributePropertyDescriptor) bundleTemplatePropertyDesc);

			final List<SearchParameterValue> paramValues = new ArrayList<SearchParameterValue>();
			final List<List<SearchParameterValue>> orValues = new ArrayList<List<SearchParameterValue>>();

			for (final ConditionValue conditionValue : condition.getConditionValues())
			{
				paramValues.add(new SearchParameterValue(genericSearchParameterDesc, conditionValue, Operator.EQUALS));
			}

			query.setParameterValues(paramValues);
			query.setParameterOrValues(orValues);

			browserModel.setLastQuery(query);
			browserModel.setBrowserFilterFixed(new QueryBrowserCatalogVersionFilter(bArea.getActiveCatalogVersion()));
			bArea.addVisibleBrowser(1, browserModel);
			browserModel.updateItems();
		}
	}

	protected BundleNavigationService getBundleNavigationNodeService()
	{
		if (bundleNavigationService == null)
		{
			bundleNavigationService = (BundleNavigationService) SpringUtil.getBean(BUNDLE_NAVIGATION_SERVICE);
		}
		return bundleNavigationService;
	}

	@Override
	public void updateLabels()
	{

		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		if (browserArea instanceof BundleNavigationNodeBrowserArea)
		{
			final CatalogVersionModel catVer = ((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion();

			if (catVer != null)
			{
				final CatalogModel catalog = catVer.getCatalog();
				final String catalogLabel = getLabelService().getObjectTextLabelForTypedObject(getTypeService().wrapItem(catalog));
				final String catVerLabel = getLabelService().getObjectTextLabelForTypedObject(getTypeService().wrapItem(catVer));
				final String arrows = " >> ";
				setLabel(catalogLabel + arrows + catVerLabel);
			}
		}
	}

	@Override
	protected ExtendedSearchResult doSearchInternal(final Query query)
	{
		if (query == null)
		{
			throw new IllegalArgumentException("Query can not be null.");
		}

		ExtendedSearchResult result = null;

		final SearchProvider searchProvider = getSearchProvider();
		if (searchProvider != null)
		{
			Query searchQuery = null;

			final int pageSize = (query.getCount() > 0) ? query.getCount() : getPageSize();

			final SearchType selectedType = getSelectedType(query);

			searchQuery = new Query(Collections.singletonList(selectedType), query.getSimpleText(), query.getStart(), pageSize);
			searchQuery.setNeedTotalCount(!(isSimplePaging()));
			searchQuery.setParameterOrValues(query.getParameterOrValues());

			setParamValuesToSearchQuery(query, searchQuery);


			final ObjectTemplate selTemplate = (ObjectTemplate) query.getContextParameter("objectTemplate");
			if (selTemplate != null)
			{
				searchQuery.setContextParameter("objectTemplate", selTemplate);
			}

			setSortPropertiesToSearchQuery(query, searchQuery);

			try
			{
				final Query clonedQuery = (Query) searchQuery.clone();
				setLastQuery(clonedQuery);
			}
			catch (final CloneNotSupportedException localCloneNotSupportedException)
			{
				LOG.error("Cloning the query is not supported");
				LOG.debug("Cloning exception", localCloneNotSupportedException);
			}

			if (getBrowserFilter() != null)
			{
				getBrowserFilter().filterQuery(searchQuery);
			}

			result = searchProvider.search(searchQuery);
			updateLabels();
		}

		return result;
	}

	/**
	 * @param query
	 * @return
	 */
	protected SearchType getSelectedType(final Query query)
	{
		SearchType selectedType = null;
		if (query.getSelectedTypes().size() == 1)
		{
			selectedType = query.getSelectedTypes().iterator().next();
		}
		else if (!(query.getSelectedTypes().isEmpty()))
		{
			selectedType = query.getSelectedTypes().iterator().next();
			LOG.warn("Query has ambigious search types. Using '" + selectedType.getCode() + "' for searching.");
		}

		if (selectedType == null)
		{
			selectedType = getSearchType();
		}
		return selectedType;
	}

	/**
	 * Sets search parameter values to searchQuery according to original ones in originalQuery adding restrictions to
	 * parentTemplate and catalogVersion fields
	 * 
	 * @param originalQuery
	 * @param searchQuery
	 */
	protected void setParamValuesToSearchQuery(final Query originalQuery, final Query searchQuery)
	{
		final UIBrowserArea browserArea = UISessionUtils.getCurrentSession().getCurrentPerspective().getBrowserArea();
		final List<SearchParameterValue> paramValues = new ArrayList<SearchParameterValue>();

		for (final SearchParameterValue spv : originalQuery.getParameterValues())
		{
			if (!BUNDLE_TEMPLATE_CATALOG_VERSION.equals(spv.getParameterDescriptor().getQualifier())
					&& !BUNDLE_TEMPLATE_PARENT_TEMPLATE.equals(spv.getParameterDescriptor().getQualifier()))
			{
				paramValues.add(spv);
			}
		}
		final TypeService currentTypeService = UISessionUtils.getCurrentSession().getTypeService();
		final ConditionValue catalogVersionConditionValue = new SimpleConditionValue(
				((BundleNavigationNodeBrowserArea) browserArea).getActiveCatalogVersion());
		final ItemAttributePropertyDescriptor catalogVersionPropertyDescriptor = (ItemAttributePropertyDescriptor) currentTypeService
				.getPropertyDescriptor(BUNDLE_TEMPLATE_CATALOG_VERSION);
		paramValues.add(new SearchParameterValue(new ItemAttributeSearchDescriptor(catalogVersionPropertyDescriptor),
				catalogVersionConditionValue, Operator.EQUALS));

		final ItemAttributePropertyDescriptor parentTemplatePropertyDescriptor = (ItemAttributePropertyDescriptor) currentTypeService
				.getPropertyDescriptor(BUNDLE_TEMPLATE_PARENT_TEMPLATE);
		paramValues.add(new SearchParameterValue(new ItemAttributeSearchDescriptor(parentTemplatePropertyDescriptor),
				new SimpleConditionValue("", new Operator("isEmpty")), new Operator("isEmpty")));

		searchQuery.setParameterValues(paramValues);
	}

	/**
	 * Sets sort properties to searchQuery according to original ones in originalQuery
	 * 
	 * @param originalQuery
	 * @param searchQuery
	 */
	// NOSONAR
	protected void setSortPropertiesToSearchQuery(final Query originalQuery, final Query searchQuery)
	{
		final TypeService sessionTypeService = UISessionUtils.getCurrentSession().getTypeService();
		searchQuery.addSortCriterion(sessionTypeService.getPropertyDescriptor("BundleTemplate.name"), true);
	}

	public List<List<Integer>> getOpenedPath()
	{
		return openedItems;
	}

	public void setOpenedItems(final List<List<Integer>> openedItems)
	{
		this.openedItems = openedItems;
	}

	public BundleTemplateTreeModel getTreeModel()
	{
		return treeModel;
	}

	public void setTreeModel(final BundleTemplateTreeModel treeModel)
	{
		this.treeModel = treeModel;
	}

	public Set<TypedObject> getSelectedNode()
	{
		return selectedNode;
	}

	public void setSelectedNode(final Set<TypedObject> selectedNode)
	{
		this.selectedNode = selectedNode;
	}

	protected LabelService getLabelService()
	{
		return getLabelServiceInternal();
	}

	protected TypeService getTypeService()
	{
		return getTypeServiceInternal();
	}

	protected final LabelService getLabelServiceInternal()
	{
		if (labelService == null)
		{
			labelService = UISessionUtils.getCurrentSession().getLabelService();
		}
		return labelService;
	}

	protected final TypeService getTypeServiceInternal()
	{
		if (typeService == null)
		{
			typeService = UISessionUtils.getCurrentSession().getTypeService();
		}
		return typeService;
	}
}

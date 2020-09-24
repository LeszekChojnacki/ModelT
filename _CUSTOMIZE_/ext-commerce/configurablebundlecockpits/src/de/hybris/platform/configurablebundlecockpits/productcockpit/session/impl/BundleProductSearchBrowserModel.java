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
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cockpit.components.contentbrowser.ListMainAreaComponentFactory;
import de.hybris.platform.cockpit.components.contentbrowser.MainAreaComponentFactory;
import de.hybris.platform.cockpit.model.advancedsearch.SearchField;
import de.hybris.platform.cockpit.model.browser.BrowserModelFactory;
import de.hybris.platform.cockpit.model.meta.ObjectTemplate;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.search.ExtendedSearchResult;
import de.hybris.platform.cockpit.model.search.Query;
import de.hybris.platform.cockpit.model.search.SearchType;
import de.hybris.platform.cockpit.services.search.SearchProvider;
import de.hybris.platform.cockpit.session.BrowserModelListener;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.DefaultSearchBrowserModel;
import de.hybris.platform.productcockpit.services.catalog.CatalogService;
import de.hybris.platform.productcockpit.services.search.impl.ProductPerspectiveQueryProvider;
import de.hybris.platform.productcockpit.session.ProductSearchBrowserModelListener;
import de.hybris.platform.productcockpit.session.impl.DefaultProductSearchBrowserModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.BeansException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zkplus.spring.SpringUtil;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


public class BundleProductSearchBrowserModel extends DefaultSearchBrowserModel
{
	private static final Logger LOG = Logger.getLogger(DefaultProductSearchBrowserModel.class);

	private CatalogService productCockpitCatalogService = null;

	public BundleProductSearchBrowserModel()
	{
		super(UISessionUtils.getCurrentSession().getTypeService().getObjectTemplate("Product"));
	}

	public BundleProductSearchBrowserModel(String templateCode)
	{
		super(UISessionUtils.getCurrentSession().getTypeService().getObjectTemplate(templateCode));
	}

	@Override
	public void addBrowserModelListener(final BrowserModelListener listener)
	{
		if (listener instanceof ProductSearchBrowserModelListener)
		{
			super.addBrowserModelListener(listener);
		}
		else
		{
			LOG.warn("Not adding listener. Reason: Listener not of type '"
					+ ProductSearchBrowserModelListener.class.getCanonicalName() + "'");
		}
	}


	@Override
	public Object clone() throws CloneNotSupportedException // NOSONAR
	{
		final BrowserModelFactory factory = (BrowserModelFactory) SpringUtil.getBean(BrowserModelFactory.BEAN_ID);
		final BundleProductSearchBrowserModel browserModel = (BundleProductSearchBrowserModel) factory
				.createBrowserModel("BundleProductSearchBrowserModel");
		browserModel.setSearchProvider(this.getSearchProvider());
		browserModel.setResult(this.getResult());
		browserModel.setLastQuery(this.getLastQuery() == null ? null : (Query) this.getLastQuery().clone());
		browserModel.setSortableProperties(this.getAdvancedSearchModel().getSortableProperties());
		browserModel.setSortAsc(this.getAdvancedSearchModel().isSortAscending());
		browserModel.setOffset(this.getOffset());
		final List<Integer> pageSizes = new ArrayList<Integer>();
		pageSizes.addAll(this.getPageSizes());
		browserModel.setPageSizes(pageSizes);
		browserModel.setPageSize(this.getPageSize());
		browserModel.setTotalCount(this.getTotalCount());
		browserModel.setBrowserFilterFixed(this.getBrowserFilterFixed());
		browserModel.setBrowserFilter(this.getBrowserFilter());
		browserModel.updateLabels();
		browserModel.setViewMode(this.getViewMode());

		return browserModel;
	}

	@SuppressWarnings("unchecked")
	public Collection<CatalogVersionModel> getSelectedCatalogVersions()
	{
		final Collection<CatalogVersionModel> ret = (Collection<CatalogVersionModel>) getLastQuery().getContextParameter(
				ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS);
		return ret == null ? Collections.emptySet() : ret;
	}

	protected void setSelectedCatalogVersions(final Collection<CatalogVersionModel> selectedCatalogVersions)
	{
		this.getLastQuery().setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS, selectedCatalogVersions);
	}

	@SuppressWarnings("unchecked")
	public Collection<CategoryModel> getSelectedCategories()
	{
		final Collection<CategoryModel> ret = (Collection<CategoryModel>) getLastQuery().getContextParameter(
				ProductPerspectiveQueryProvider.SELECTED_CATEGORIES);
		return ret == null ? Collections.emptySet() : ret;
	}

	protected void setSelectedCategories(final Collection<CategoryModel> selectedCategories)
	{
		this.getLastQuery().setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES, selectedCategories);
	}

	@Override
	public void setRootType(final ObjectTemplate rootType)
	{
		if (rootType != null && (this.rootType == null || !this.rootType.equals(rootType))
				&& UISessionUtils.getCurrentSession().getTypeService().getBaseType("Product").isAssignableFrom(rootType))
		{
			super.setRootType(rootType);
		}
	}

	@Override
	protected ExtendedSearchResult doSearchInternal(final Query query)
	{
		validateParameterNotNull(query, "Query can not be null.");

		final SearchProvider searchProvider = this.getSearchProvider();
		if (searchProvider == null)
		{
			return null;
		}

		final int pageSize = query.getCount() > 0 ? query.getCount() : getPageSize();

		SearchType selectedType = getSelectedType(query);

		Query searchQuery = new Query(Collections.singletonList(selectedType), query.getSimpleText(), query.getStart(), pageSize);
		searchQuery.setParameterValues(query.getParameterValues());
		searchQuery.setParameterOrValues(query.getParameterOrValues());
		searchQuery.setExcludeSubTypes(query.isExcludeSubTypes());

		final ObjectTemplate selTemplate = (ObjectTemplate) query.getContextParameter(SearchProvider.SELECTED_OBJECT_TEMPLATE);
		if (selTemplate != null)
		{
			searchQuery.setContextParameter(SearchProvider.SELECTED_OBJECT_TEMPLATE, selTemplate);
		}

		// get catalog versions / categories from query
		updateCatalogVersionsFromQuery(query);
		updateSelectedCategoriesFromQuery(query);

		// catalog version / category
		Collection<CatalogVersionModel> catalogVersions = getSelectedCatalogVersions();
		if (catalogVersions.isEmpty() && getSelectedCategories().isEmpty())
		{
			catalogVersions = getProductCockpitCatalogService().getAvailableCatalogVersions();
		}
		searchQuery.setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS, catalogVersions);
		if (!getSelectedCategories().isEmpty())
		{
			searchQuery.setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES, getSelectedCategories());
		}

		// sort
		final Map<PropertyDescriptor, Boolean> sortCriterion = getSortCriterion(query);
		addCriterionToSearchQuery(sortCriterion, searchQuery);

		try
		{
			final Query clonedQuery = (Query) searchQuery.clone();
			setLastQuery(clonedQuery);
		}
		catch (final CloneNotSupportedException e)
		{
			LOG.error("Cloning the query is not supported");
			LOG.debug("Cloning exception", e);
		}
		// update filter
		if (getBrowserFilter() != null)
		{
			getBrowserFilter().filterQuery(searchQuery);
		}

		ExtendedSearchResult result = searchProvider.search(searchQuery);

		this.updateLabels();

		return result;
	}

	protected void updateSelectedCategoriesFromQuery(final Query query)
	{
		if (query.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES) != null)
		{
			this.setSelectedCategories((Collection<CategoryModel>) query
					.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES));
		}
	}

	protected void updateCatalogVersionsFromQuery(final Query query)
	{
		if (query.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS) != null)
		{
			this.setSelectedCatalogVersions((Collection<CatalogVersionModel>) query
					.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS));
		}
	}

	protected SearchType getSelectedType(final Query query)
	{
		SearchType selectedType = null;
		if (query.getSelectedTypes().size() == 1)
		{
			selectedType = query.getSelectedTypes().iterator().next();
		}
		else if (!query.getSelectedTypes().isEmpty())
		{
			selectedType = query.getSelectedTypes().iterator().next();
			LOG.warn("Query has ambigious search types. Using '" + selectedType.getCode() + "' for searching.");
		}

		if (selectedType == null)
		{
			selectedType = this.getSearchType();
		}
		return selectedType;
	}

	protected void addCriterionToSearchQuery(final Map<PropertyDescriptor, Boolean> sortCriterion, final Query searchQuery)
	{
		PropertyDescriptor sortProperties = null;
		boolean asc = true;

		if (MapUtils.isNotEmpty(sortCriterion))
		{
			sortProperties = sortCriterion.keySet().iterator().next();
			if (sortProperties == null)
			{
				LOG.warn("Could not add sort criterion (Reason: Specified sort property is null).");
			}
			else
			{
				if (sortCriterion.get(sortProperties) != null)
				{
					asc = sortCriterion.get(sortProperties);
				}
				searchQuery.addSortCriterion(sortProperties, asc);
			}
		}

		// update advanced search model
		this.updateAdvancedSearchModel(searchQuery, sortProperties, asc);
	}

	@Required
	public void setProductCockpitCatalogService(final CatalogService productCockpitCatalogService)
	{
		this.productCockpitCatalogService = productCockpitCatalogService;
	}

	public CatalogService getProductCockpitCatalogService()
	{
		if (this.productCockpitCatalogService == null)
		{
			try
			{
				this.productCockpitCatalogService = (CatalogService) SpringUtil.getBean("productCockpitCatalogService");
			}
			catch (final BeansException e)
			{
				LOG.error("Could not get catalog service", e);
			}
		}
		return this.productCockpitCatalogService;
	}


	@Override
	public List<MainAreaComponentFactory> getAvailableViewModes()
	{
		List<MainAreaComponentFactory> viewModes;

		viewModes = new ArrayList<MainAreaComponentFactory>();
		viewModes.add(new ListMainAreaComponentFactory());

		return viewModes;
	}

	@Override
	protected String getAllItemLabel()
	{
		return Labels.getLabel("general.all_products");
	}

	@Override
	protected void updateLabels()
	{
		final StringBuilder sBuff = new StringBuilder();

		// advanced search specific stuff
		final StringBuilder advancedSearchBuilder = new StringBuilder();

		if (this.getAdvancedSearchModel().getParameterContainer().hasValues(true))
		{
			// search conditions available
			appendSearchFieldValueFilter(advancedSearchBuilder);
			appendSortProperties(advancedSearchBuilder);
		}
		else
		{
			sBuff.append(this.getSimpleQuery().length() > 0 ? this.getSimpleQuery() : getAllItemLabel());
		}

		// catalog/category stuff
		if (hasExactlyOneSearchQueryItem())
		{
			updateLabelSingleItem(sBuff, advancedSearchBuilder);
		}
		else if (hasMulipleSearchQueryItems())
		{
			// multiple selection
			updateLabelMultipleSelection(advancedSearchBuilder);
		}

		if (advancedSearchBuilder.length() > 0)
		{
			final boolean useBrackets = sBuff.length() > 0;
			sBuff.append((useBrackets ? " (" : "") + advancedSearchBuilder.toString() + (useBrackets ? ")" : ""));
		}

		this.setLabel(sBuff.toString());
	}

	protected void appendSortProperties(final StringBuilder advancedSearchBuilder)
	{
		if (getAdvancedSearchModel().getParameterContainer().getSortProperty() != null)
		{
			// sort criterion available

			if (advancedSearchBuilder.length() > 0)
			{
				advancedSearchBuilder.append(QUERY_PATH_DELIMITER);
			}

			advancedSearchBuilder.append("Sort: ");
			advancedSearchBuilder.append(getAdvancedSearchModel().getParameterContainer().getSortProperty().getName());
			advancedSearchBuilder.append('/');
			advancedSearchBuilder.append(getAdvancedSearchModel().getParameterContainer().isSortAscending() ? "ASC" : "DESC");
		}
	}

	protected void appendSearchFieldValueFilter(final StringBuilder advancedSearchBuilder)
	{
		if (!getAdvancedSearchModel().getParameterContainer().getSearchFieldValueMap().isEmpty())
		{
			// search parameter values available

			advancedSearchBuilder.append("Filter: ");

			final Iterator<SearchField> iterator = getAdvancedSearchModel().getParameterContainer().getSearchFieldValueMap()
					.keySet().iterator();
			while (iterator.hasNext())
			{
				final SearchField field = iterator.next();
				advancedSearchBuilder.append(field.getLabel());
				if (iterator.hasNext())
				{
					advancedSearchBuilder.append(PATH_LIST_DELIMITER);
				}
			}
		}
	}

	protected boolean hasExactlyOneSearchQueryItem()
	{
		return (this.getSelectedCatalogVersions().size() == 1 && this.getSelectedCategories().isEmpty())
				|| (this.getSelectedCatalogVersions().isEmpty() && this.getSelectedCategories().size() == 1);
	}

	protected boolean hasMulipleSearchQueryItems()
	{
		return !this.getSelectedCatalogVersions().isEmpty() || !this.getSelectedCategories().isEmpty();
	}

	protected void updateLabelSingleItem(final StringBuilder sBuff, final StringBuilder advancedSearchBuilder)
	{
		if (sBuff.length() > 0)
		{
			sBuff.append(QUERY_PATH_DELIMITER);
		}
		if (this.getSelectedCatalogVersions().size() == 1)
		{
			// a catalog version is the only thing selected
			sBuff.append(this.getPathAsString(this.getSelectedCatalogVersions().iterator().next()));
		}
		else
		{
			// a category is the only thing selected
			sBuff.append(this.getPathAsString(this.getSelectedCategories().iterator().next()));
		}
		this.setExtendedLabel(sBuff + (advancedSearchBuilder.length() > 0 ? advancedSearchBuilder.toString() : ""));
	}

	protected void updateLabelMultipleSelection(final StringBuilder advancedSearchBuilder)
	{
		String delimiter = QUERY_PATH_DELIMITER;
		String extendedLabel = "";

		for (final CatalogVersionModel uicv : this.getSelectedCatalogVersions())
		{
			final String path = this.getPathAsString(uicv);
			if (path.length() > 0)
			{
				extendedLabel += delimiter + path;
				delimiter = PATH_LIST_DELIMITER;
			}
		}

		for (final CategoryModel uic : this.getSelectedCategories())
		{
			final String path = this.getPathAsString(uic);
			if (path.length() > 0)
			{
				extendedLabel += delimiter + path;
				delimiter = PATH_LIST_DELIMITER;
			}
		}

		if (advancedSearchBuilder.length() > 0)
		{
			extendedLabel += QUERY_PATH_DELIMITER + " (" + advancedSearchBuilder.toString() + ")";
		}

		this.setExtendedLabel(extendedLabel);
	}

	protected String getPathAsString(final CategoryModel uic)
	{
		String path = "";

		String category = "";

		if (uic != null)
		{
			category = uic.getName() != null ? uic.getName() : "";

			final String tmp = this.getPathAsString(this.getProductCockpitCatalogService().getCatalogVersion(uic));

			if (tmp.length() > 0 && category.length() > 0)
			{
				path = tmp + PATH_DELIMITER + category;
			}
		}

		return path;
	}

	protected String getPathAsString(final CatalogVersionModel uicv)
	{
		String path = "";

		String catalogVersion = "";
		String catalog = "";

		if (uicv != null)
		{
			catalogVersion = uicv.getVersion() == null ? "" : uicv.getVersion();

			final CatalogModel c = this.getProductCockpitCatalogService().getCatalog(uicv);
			if (c != null)
			{
				catalog = c.getName() != null ? c.getName() : "";
			}
		}

		path += catalog;
		if (catalog.length() > 0 && catalogVersion.length() > 0)
		{
			path += PATH_DELIMITER;
		}
		path += catalogVersion;

		return path;
	}

	@Override
	protected SearchProvider getSearchProvider()
	{
		if (this.searchProvider == null)
		{
			this.searchProvider = (SearchProvider) SpringUtil.getBean("productSearchProvider");
		}
		return this.searchProvider;
	}
}

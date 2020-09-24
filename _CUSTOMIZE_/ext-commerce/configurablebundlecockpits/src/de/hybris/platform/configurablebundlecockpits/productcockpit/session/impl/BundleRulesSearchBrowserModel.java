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
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cockpit.model.browser.BrowserModelFactory;
import de.hybris.platform.cockpit.model.meta.ObjectTemplate;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.search.ExtendedSearchResult;
import de.hybris.platform.cockpit.model.search.Query;
import de.hybris.platform.cockpit.model.search.SearchParameterValue;
import de.hybris.platform.cockpit.model.search.SearchType;
import de.hybris.platform.cockpit.services.search.SearchProvider;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.type.BundleRuleType;
import de.hybris.platform.productcockpit.services.search.impl.ProductPerspectiveQueryProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zkoss.zkplus.spring.SpringUtil;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/***
 * Class that is responsible for doing searching on any bundle rule (change product price rule or disabled products
 * rules )
 */
public class BundleRulesSearchBrowserModel extends BundleProductSearchBrowserModel
{
	private static final Logger LOG = Logger.getLogger(BundleRulesSearchBrowserModel.class);
	private BundleRuleType bundleRuleType = null;


	public BundleRulesSearchBrowserModel(final String templateCode)
	{
		super(templateCode); //if the template code is invalid then an exp will be thrown when resolving the template code
		bundleRuleType = BundleRuleType.fromValue(templateCode);
	}


	@Override
	public Object clone() throws CloneNotSupportedException // NOSONAR
	{
		final BrowserModelFactory factory = (BrowserModelFactory) SpringUtil.getBean(BrowserModelFactory.BEAN_ID);
		final BundleRulesSearchBrowserModel browserModel = (BundleRulesSearchBrowserModel) factory
				.createBrowserModel(bundleRuleType.getModelName());
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


	@Override
	public void setSimpleQuery(final String simpleQuery)
	{
		final List<SearchParameterValue> paramValues = new ArrayList<SearchParameterValue>();
		for (final SearchParameterValue searchParameterValue : this.getLastQuery().getParameterValues())
		{
			if (bundleRuleType.getTemplateBundleName()
					.equalsIgnoreCase(searchParameterValue.getParameterDescriptor().getQualifier()))
			{
				paramValues.add(searchParameterValue);
			}
		}
		super.setSimpleQuery(simpleQuery);
		this.getLastQuery().setParameterValues(paramValues);
	}

	@Override
	public void setRootType(final ObjectTemplate rootType)
	{
		if (rootType != null
				&& (this.rootType == null || !this.rootType.equals(rootType))
				&& UISessionUtils.getCurrentSession().getTypeService().getBaseType(bundleRuleType.getRuleName())
						.isAssignableFrom(rootType))
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

		Query searchQuery = createSearchQuery(query);
		searchQuery = setupQueryCatalog(searchQuery, query);

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

	protected Query createSearchQuery(final Query query)
	{
		final int pageSize = query.getCount() > 0 ? query.getCount() : getPageSize();

		SearchType selectedType = getSelectedTypeFromQuery(query);

		Query searchQuery = new Query(Collections.singletonList(selectedType), query.getSimpleText(), query.getStart(), pageSize);
		searchQuery.setParameterValues(query.getParameterValues());
		searchQuery.setParameterOrValues(query.getParameterOrValues());
		searchQuery.setExcludeSubTypes(query.isExcludeSubTypes());

		if (CollectionUtils.isNotEmpty(searchQuery.getParameterValues()))
		{
			if (!bundleForQueryExists(searchQuery))
			{
				searchQuery.setParameterValues(updateSearchParameters(searchQuery.getParameterValues()));
			}
		}
		else
		{
			searchQuery.setParameterValues(updateSearchParameters(searchQuery.getParameterValues()));
		}

		final ObjectTemplate selTemplate = (ObjectTemplate) query.getContextParameter(SearchProvider.SELECTED_OBJECT_TEMPLATE);
		if (selTemplate != null)
		{
			searchQuery.setContextParameter(SearchProvider.SELECTED_OBJECT_TEMPLATE, selTemplate);
		}
		return searchQuery;
	}

	protected boolean bundleForQueryExists(final Query searchQuery)
	{
		boolean bundleExists = false;

		for (final SearchParameterValue searchParameterValue : searchQuery.getParameterValues())
		{
			if (bundleRuleType.getTemplateBundleName().equalsIgnoreCase(
					searchParameterValue.getParameterDescriptor().getQualifier()))
			{
				bundleExists = true;
			}
		}
		return bundleExists;
	}

	protected SearchType getSelectedTypeFromQuery(final Query query)
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

	protected Query setupQueryCatalog(final Query searchQuery, final Query userQuery)
	{
		// get catalog versions / categories from userQuery
		if (userQuery.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS) != null)
		{
			this.setSelectedCatalogVersions((Collection<CatalogVersionModel>) userQuery
					.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS));
		}
		if (userQuery.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES) != null)
		{
			this.setSelectedCategories((Collection<CategoryModel>) userQuery
					.getContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES));
		}

		// catalog version / category
		Collection<CatalogVersionModel> catver = getSelectedCatalogVersions();
		if (catver.isEmpty() && getSelectedCategories().isEmpty())
		{
			catver = getProductCockpitCatalogService().getAvailableCatalogVersions();
		}
		searchQuery.setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATALOG_VERSIONS, catver);
		if (!getSelectedCategories().isEmpty())
		{
			searchQuery.setContextParameter(ProductPerspectiveQueryProvider.SELECTED_CATEGORIES, getSelectedCategories());
		}

		return searchQuery;
	}

	/**
	 * method used to add bundle rule as a search parameter only if it doesn't exist
	 *
	 * @param parameterValues
	 *           already existing search parameters
	 * @return updated search parameters
	 */
	protected List<SearchParameterValue> updateSearchParameters(final List<SearchParameterValue> parameterValues)
	{
		final List<SearchParameterValue> paramValues = new ArrayList<SearchParameterValue>(parameterValues);

		for (final SearchParameterValue searchParameterValue : this.getLastQuery().getParameterValues())
		{
			if (bundleRuleType.getTemplateBundleName()
					.equalsIgnoreCase(searchParameterValue.getParameterDescriptor().getQualifier()))
			{
				paramValues.add(searchParameterValue);
			}
		}
		return paramValues;
	}

	@Override
	protected String getAllItemLabel()
	{
		return "";
	}

	@Override
	protected SearchProvider getSearchProvider()
	{
		if (this.searchProvider == null)
		{
			this.searchProvider = (SearchProvider) SpringUtil.getBean("genericSearchProvider");
		}
		return this.searchProvider;
	}
}

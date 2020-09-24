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
package de.hybris.platform.adaptivesearch.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.adaptivesearch.daos.AsSearchProfileDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearch.strategies.AsCloneStrategy;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileService}.
 */
public class DefaultAsSearchProfileService implements AsSearchProfileService
{
	private AsSearchProfileDao asSearchProfileDao;
	private AsCloneStrategy asCloneStrategy;

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T> getAllSearchProfiles()
	{
		return asSearchProfileDao.findAllSearchProfiles();
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T> getSearchProfilesForIndexTypesAndCatalogVersions(
			final List<String> indexTypes, final List<CatalogVersionModel> catalogVersions)
	{
		return asSearchProfileDao.findSearchProfilesByIndexTypesAndCatalogVersions(indexTypes, catalogVersions);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T> getSearchProfilesForCatalogVersion(
			final CatalogVersionModel catalogVersion)
	{
		return asSearchProfileDao.findSearchProfilesByCatalogVersion(catalogVersion);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> Optional<T> getSearchProfileForCode(final CatalogVersionModel catalogVersion,
			final String code)
	{
		validateParameterNotNullStandardMessage("code", code);

		return asSearchProfileDao.findSearchProfileByCode(catalogVersion, code);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T> getSearchProfiles(final String query,
			final Map<String, Object> filters)
	{
		ServicesUtil.validateParameterNotNull(filters, "filters must not be null");

		return asSearchProfileDao.getSearchProfiles(query, filters);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> SearchPageData<T> getSearchProfiles(final String query,
			final Map<String, Object> filters, final SearchPageData<?> pagination)
	{
		ServicesUtil.validateParameterNotNull(filters, "filters must not be null");
		ServicesUtil.validateParameterNotNull(pagination, "pagination must not be null");

		return asSearchProfileDao.getSearchProfiles(query, filters, pagination);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> T cloneSearchProfile(final T original)
	{
		return asCloneStrategy.clone(original);
	}

	public AsSearchProfileDao getAsSearchProfileDao()
	{
		return asSearchProfileDao;
	}

	@Required
	public void setAsSearchProfileDao(final AsSearchProfileDao asSearchProfileDao)
	{
		this.asSearchProfileDao = asSearchProfileDao;
	}

	public AsCloneStrategy getAsCloneStrategy()
	{
		return asCloneStrategy;
	}

	@Required
	public void setAsCloneStrategy(final AsCloneStrategy asCloneStrategy)
	{
		this.asCloneStrategy = asCloneStrategy;
	}
}

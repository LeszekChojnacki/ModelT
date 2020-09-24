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
package de.hybris.platform.adaptivesearch.daos.impl;

import de.hybris.platform.adaptivesearch.daos.AsSearchConfigurationDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchConfigurationDao}.
 */
public class DefaultAsSearchConfigurationDao extends AbstractAsGenericDao<AbstractAsSearchConfigurationModel>
		implements AsSearchConfigurationDao
{
	protected static final String BASE_QUERY = "SELECT {" + AbstractAsSearchConfigurationModel.PK + "} FROM {"
			+ AbstractAsSearchConfigurationModel._TYPECODE + "} WHERE";

	private TypeService typeService;

	/**
	 * Creates DAO for {@link AbstractAsSearchConfigurationModel}.
	 */
	public DefaultAsSearchConfigurationDao()
	{
		super(AbstractAsSearchConfigurationModel._TYPECODE);
	}

	@Override
	public List<AbstractAsSearchConfigurationModel> findAllSearchConfigurations()
	{
		return find();
	}

	@Override
	public List<AbstractAsSearchConfigurationModel> findSearchConfigurationsByCatalogVersion(
			final CatalogVersionModel catalogVersion)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap();

		appendClause(query, parameters, AbstractAsSearchConfigurationModel.CATALOGVERSION, catalogVersion);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		return getFlexibleSearchService().<AbstractAsSearchConfigurationModel> search(searchQuery).getResult();
	}

	@Override
	public Optional<AbstractAsSearchConfigurationModel> findSearchConfigurationByUid(final CatalogVersionModel catalogVersion,
			final String uid)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map parameters = new HashMap();

		appendClause(query, parameters, AbstractAsSearchConfigurationModel.CATALOGVERSION, catalogVersion);
		appendAndClause(query);
		appendClause(query, parameters, AbstractAsSearchConfigurationModel.UID, uid);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		final List<AbstractAsSearchConfigurationModel> searchConfigurations = getFlexibleSearchService()
				.<AbstractAsSearchConfigurationModel> search(searchQuery).getResult();

		return searchConfigurations.isEmpty() ? Optional.empty() : Optional.of(searchConfigurations.get(0));
	}

	@Override
	public <T extends AbstractAsSearchConfigurationModel> List<T> findSearchConfigurations(final Class<T> type,
			final Map<String, Object> filters)
	{
		final String typeCode = typeService.getComposedTypeForClass(type).getCode();

		final StringBuilder query = new StringBuilder(256);
		query.append("SELECT {");
		query.append(AbstractAsSearchConfigurationModel.PK);
		query.append("} FROM {");
		query.append(typeCode);
		query.append("} WHERE");

		final Map<String, Object> parameters = new HashMap();

		boolean firstParam = true;

		for (final Entry<String, Object> filter : filters.entrySet())
		{
			if (!firstParam)
			{
				appendAndClause(query);
			}

			appendClause(query, parameters, filter.getKey(), filter.getValue());
			firstParam = false;
		}

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		return getFlexibleSearchService().<T> search(searchQuery).getResult();
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
}
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

import de.hybris.platform.adaptivesearch.daos.AsConfigurationDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsConfigurationDao}.
 */
public class DefaultAsConfigurationDao extends AbstractAsGenericDao<AbstractAsConfigurationModel> implements AsConfigurationDao
{
	private TypeService typeService;

	/**
	 * Creates DAO for {@link AbstractAsConfigurationModel}.
	 */
	public DefaultAsConfigurationDao()
	{
		super(AbstractAsConfigurationModel._TYPECODE);
	}

	@Override
	public <T extends AbstractAsConfigurationModel> Optional<T> findConfigurationByUid(final Class<T> type,
			final CatalogVersionModel catalogVersion, final String uid)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(type);

		final StringBuilder query = createQuery(composedType.getCode());
		final Map parameters = new HashMap();

		appendWhereClause(query);
		appendClause(query, parameters, AbstractAsConfigurationModel.CATALOGVERSION, catalogVersion);
		appendAndClause(query);
		appendClause(query, parameters, AbstractAsConfigurationModel.UID, uid);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		final List<T> configurations = getFlexibleSearchService().<T> search(searchQuery).getResult();

		return configurations.isEmpty() ? Optional.empty() : Optional.of(configurations.get(0));
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
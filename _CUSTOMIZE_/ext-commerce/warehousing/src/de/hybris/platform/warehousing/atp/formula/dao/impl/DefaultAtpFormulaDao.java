/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.atp.formula.dao.impl;

import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.warehousing.atp.formula.dao.AtpFormulaDao;
import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.util.Collection;


/**
 * Default implementation of the {@link AtpFormulaDao}
 */
public class DefaultAtpFormulaDao extends AbstractItemDao implements AtpFormulaDao
{

	protected static final String ALL_ATPFORMULA_QUERY = "SELECT {" + AtpFormulaModel.PK + "} FROM {" + AtpFormulaModel._TYPECODE + "}";

	@Override
	public Collection<AtpFormulaModel> getAllAtpFormula()
	{
		final SearchResult<AtpFormulaModel> searchResult = search(new FlexibleSearchQuery(ALL_ATPFORMULA_QUERY));
		ServicesUtil.validateIfAnyResult(searchResult.getResult(),"No AtpFormula found");
		return searchResult.getResult();
	}
}

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
package de.hybris.platform.ruleengine.dao.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;


/**
 * Provides a default dao implementation for rules module objects
 */
public class DefaultRulesModuleDao extends AbstractItemDao implements RulesModuleDao
{

	private static final String FIND_ACTIVE_MODULE_BY_NAME = "select {" + AbstractRulesModuleModel.PK + "} from {"    // NOSONAR
			+ AbstractRulesModuleModel._TYPECODE + "} where {" + AbstractRulesModuleModel.NAME + "} = ?"                // NOSONAR
			+ AbstractRulesModuleModel.NAME + " and {"                                                                  // NOSONAR
			+ AbstractRulesModuleModel.ACTIVE + "} = ?" + AbstractRulesModuleModel.ACTIVE;

	private static final String FIND_ALL_ACTIVE_MODULES = "select {" + AbstractRulesModuleModel.PK + "} from {"
			+ AbstractRulesModuleModel._TYPECODE + "} where {" + AbstractRulesModuleModel.ACTIVE + "} = ?"
			+ AbstractRulesModuleModel.ACTIVE;

	private static final String FIND_ALL_ACTIVE_MODULES_BY_RULE_TYPE = FIND_ALL_ACTIVE_MODULES + " and {"
			+ AbstractRulesModuleModel.RULETYPE + "} = ?" + AbstractRulesModuleModel.RULETYPE + "";

	private static final String FIND_MODULE_BY_NAME_AND_VERSION = "select {" + AbstractRulesModuleModel.PK + "} from {"
			+ AbstractRulesModuleModel._TYPECODE + "} where {" + AbstractRulesModuleModel.NAME + "} = ?"
			+ AbstractRulesModuleModel.NAME + " and {"
			+ AbstractRulesModuleModel.VERSION + "} = ?" + AbstractRulesModuleModel.VERSION;

	@Override
	public <T extends AbstractRulesModuleModel> T findByName(final String name)
	{
		final Map<String, Object> queryParams = Maps.newHashMap();
		queryParams.put(AbstractRulesModuleModel.NAME, name);
		queryParams.put(AbstractRulesModuleModel.ACTIVE, Boolean.TRUE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ACTIVE_MODULE_BY_NAME, queryParams);
		return getFlexibleSearchService().searchUnique(query);
	}

	@Override
	public List<AbstractRulesModuleModel> findAll()
	{
		final Map<String, Object> queryParams = Maps.newHashMap();
		queryParams.put(AbstractRulesModuleModel.ACTIVE, Boolean.TRUE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ALL_ACTIVE_MODULES, queryParams);
		final SearchResult<AbstractRulesModuleModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<AbstractRulesModuleModel> findActiveRulesModulesByRuleType(final RuleType ruleType)
	{
		validateParameterNotNull(ruleType, "Rule type must be not null");
		final Map<String, Object> queryParams = Maps.newHashMap();
		queryParams.put(AbstractRulesModuleModel.ACTIVE, Boolean.TRUE);
		queryParams.put(AbstractRulesModuleModel.RULETYPE, ruleType);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ALL_ACTIVE_MODULES_BY_RULE_TYPE, queryParams);
		final SearchResult<AbstractRulesModuleModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public <T extends AbstractRulesModuleModel> T findByNameAndVersion(final String name, final long version)
	{
		validateParameterNotNullStandardMessage("name", name);
		checkArgument(version >= 0, "provided [version] must be positive: %s", version);

		final Map<String, Object> queryParams = Maps.newHashMap();
		queryParams.put(AbstractRulesModuleModel.NAME, name);
		queryParams.put(AbstractRulesModuleModel.VERSION, version);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_MODULE_BY_NAME_AND_VERSION, queryParams);
		T ruleModule = null;
		try
		{
			ruleModule = getFlexibleSearchService().searchUnique(query);
		}
		catch (final ModelNotFoundException e)
		{
			ruleModule = null;
		}
		return ruleModule;
	}

}

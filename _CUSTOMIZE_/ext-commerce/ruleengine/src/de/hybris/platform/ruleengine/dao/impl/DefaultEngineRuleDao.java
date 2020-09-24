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

import com.google.common.collect.ImmutableMap;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearchException;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.time.TimeService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * The default implementation of the Engine Rule Dao.
 */
@SuppressWarnings("squid:S1192")
public class DefaultEngineRuleDao extends AbstractItemDao implements EngineRuleDao
{
	protected static final String MODULE_NAME = "moduleName";

	protected static final String RULES_MODULE = "ruleModule";

	protected static final String FROM_ALL_WITH_MODULE_NAME = "from {" + DroolsRuleModel._TYPECODE + " AS r JOIN "
			+ DroolsKIEBaseModel._TYPECODE + " AS b ON {r." + DroolsRuleModel.KIEBASE + "} = {b." + DroolsKIEBaseModel.PK + "} JOIN "
			+ DroolsKIEModuleModel._TYPECODE + " AS m ON {b." + DroolsKIEBaseModel.KIEMODULE + "} = {m." + DroolsKIEModuleModel.PK
			+ "}} where {m." + DroolsKIEModuleModel.NAME + "} = ?moduleName";

	protected static final String GET_ALL_RULES_QUERY = "select {" + DroolsRuleModel.PK + "} " + FROM_ALL_WITH_MODULE_NAME
			+ " AND {" + DroolsRuleModel.ACTIVE + "} = ?active and {" + DroolsRuleModel.CURRENTVERSION + "} = ?currentVersion";

	protected static final String GET_ALL_RULES_FOR_MODULE_QUERY = "select {" + DroolsRuleModel.PK + "} "
			+ FROM_ALL_WITH_MODULE_NAME + " AND {" + DroolsRuleModel.ACTIVE + "} = ?active and {" + DroolsRuleModel.CURRENTVERSION
			+ "} = ?currentVersion";

	protected static final String GET_RULE_BY_UUID = "select {" + DroolsRuleModel.PK + "} from {" + DroolsRuleModel._TYPECODE
			+ "} where " + " {" + DroolsRuleModel.UUID + "} = ?uuid";

	protected static final String GET_RULES_BY_MULTIPLE_UUID = "select {" + DroolsRuleModel.PK + "} from {"
			+ DroolsRuleModel._TYPECODE + "} where {" + DroolsRuleModel.UUID + "} IN (?uuids)";

	protected static final String GET_RULES_BY_CODE = "select {" + DroolsRuleModel.PK + "} from {" + DroolsRuleModel._TYPECODE
			+ "} where {" + DroolsRuleModel.CODE + "} = ?code";

	protected static final String GET_RULE_BY_CODE_AND_MODULE = "select {" + DroolsRuleModel.PK + "} " + FROM_ALL_WITH_MODULE_NAME
			+ " AND {" + DroolsRuleModel.CODE + "} = ?code";

	protected static final String GET_MAX_VERSION_FOR_CODE = "select MAX({" + DroolsRuleModel.VERSION + "}) "
			+ FROM_ALL_WITH_MODULE_NAME + " AND {" + DroolsRuleModel.CODE + "} = ?code";

	protected static final String GET_MAX_VERSION_WITH_MODULE = "select MAX( {r." + DroolsRuleModel.VERSION + "} ) "
			+ FROM_ALL_WITH_MODULE_NAME;

	protected static final String GET_ALL_RULES_FOR_VERSION = "select {r." + DroolsRuleModel.PK + "} " + FROM_ALL_WITH_MODULE_NAME
			+ " AND {" + DroolsRuleModel.VERSION + "} <= ?version";

	protected static final String GET_ALL_AVAILABLE_KIE_MODULES = "select {" + AbstractRulesModuleModel.PK + "} " + "from {"
			+ AbstractRulesModuleModel._TYPECODE + "}";

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEngineRuleDao.class);

	private TimeService timeService;

	@Override
	public AbstractRuleEngineRuleModel getRuleByUuid(final String uuid)
	{
		validateParameterNotNullStandardMessage("uuid", uuid);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put("uuid", uuid);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULE_BY_UUID, queryParams);
		return getWithMaximumVersion(query, -1);
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> Collection<T> getRulesByUuids(final Collection<String> ruleUuids)
	{
		checkArgument(CollectionUtils.isNotEmpty(ruleUuids), "A collection of UUIDs shouldn't be null or empty");

		final Map<String, Object> queryParams = ImmutableMap.of("uuids", ruleUuids);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULES_BY_MULTIPLE_UUID, queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<AbstractRuleEngineRuleModel> findRulesByCode(final String code)
	{
		validateParameterNotNullStandardMessage(AbstractRuleEngineRuleModel.CODE, code);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.CODE, code);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULES_BY_CODE, queryParams);
		final SearchResult<AbstractRuleEngineRuleModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleByCode(final String code, final String moduleName)
	{
		validateParameterNotNullStandardMessage(AbstractRuleEngineRuleModel.CODE, code);
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.CODE, code);
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULE_BY_CODE_AND_MODULE, queryParams);
		return getWithMaximumVersion(query, -1);
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleByCodeAndMaxVersion(final String code, final String moduleName, final long version)
	{
		validateParameterNotNullStandardMessage(AbstractRuleEngineRuleModel.CODE, code);
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.CODE, code);
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULE_BY_CODE_AND_MODULE, queryParams);
		return getWithMaximumVersion(query, version);
	}

	@Override
	public AbstractRuleEngineRuleModel getActiveRuleByCodeAndMaxVersion(final String code, final String moduleName,
			final long version)
	{
		validateParameterNotNullStandardMessage(AbstractRuleEngineRuleModel.CODE, code);
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);
		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.CODE, code);
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULE_BY_CODE_AND_MODULE, queryParams);

		final AbstractRuleEngineRuleModel ruleForModuleVersion = getWithMaximumVersion(query, version);
		return nonNull(ruleForModuleVersion) && ruleForModuleVersion.getActive() ? ruleForModuleVersion : null;
	}

	@Override
	public List<AbstractRuleEngineRuleModel> getActiveRules(final String moduleName)
	{
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put("active", Boolean.TRUE);
		queryParams.put("currentVersion", Boolean.TRUE);
		queryParams.put("tmsp", getRoundedTimestamp());
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY, queryParams);
		final SearchResult<AbstractRuleEngineRuleModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<AbstractRuleEngineRuleModel> getActiveRules(final AbstractRulesModuleModel ruleModule)
	{
		validateParameterNotNullStandardMessage(RULES_MODULE, ruleModule);
		final String moduleName = ruleModule.getName();
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.ACTIVE, Boolean.TRUE);
		queryParams.put(AbstractRuleEngineRuleModel.CURRENTVERSION, Boolean.TRUE);
		queryParams.put(MODULE_NAME, moduleName);
		queryParams.put("tmsp", getRoundedTimestamp());

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_FOR_MODULE_QUERY, queryParams);
		final SearchResult<AbstractRuleEngineRuleModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public Long getCurrentRulesSnapshotVersion(final AbstractRulesModuleModel ruleModule)
	{

		validateParameterNotNullStandardMessage(RULES_MODULE, ruleModule);
		final String moduleName = ruleModule.getName();
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_MAX_VERSION_WITH_MODULE, queryParams);
		query.setResultClassList(singletonList(Long.class));
		Long nextRuleVersion;
		try
		{
			nextRuleVersion = getFlexibleSearchService().searchUnique(query);
		}
		catch (final ModelNotFoundException e)
		{
			nextRuleVersion = 0l;
		}
		return nextRuleVersion;
	}

	@Override
	public Long getRuleVersion(final String code, final String moduleName)
	{
		validateParameterNotNullStandardMessage(AbstractRuleEngineRuleModel.CODE, code);
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = newHashMap();
		queryParams.put(AbstractRuleEngineRuleModel.CODE, code);
		queryParams.put(MODULE_NAME, moduleName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_MAX_VERSION_FOR_CODE, queryParams);

		query.setResultClassList(singletonList(Long.class));
		Long nextRuleVersion;
		try
		{
			nextRuleVersion = getFlexibleSearchService().searchUnique(query);
		}
		catch (final ModelNotFoundException e)
		{
			nextRuleVersion = null;
		}
		return nextRuleVersion;
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> List<T> getRulesForVersion(final String moduleName, final long version)
	{
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = ImmutableMap.of(AbstractRuleEngineRuleModel.VERSION, Long.valueOf(version),
				MODULE_NAME, moduleName, "tmsp", getRoundedTimestamp());

		return getRulesForVersion(GET_ALL_RULES_FOR_VERSION, queryParams, version);
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> List<T> getActiveRulesForVersion(final String moduleName, final long version)
	{
		validateParameterNotNullStandardMessage(MODULE_NAME, moduleName);

		final Map<String, Object> queryParams = ImmutableMap.of(AbstractRuleEngineRuleModel.VERSION, Long.valueOf(version),
				MODULE_NAME, moduleName, "tmsp", getRoundedTimestamp());

		final List<T> rulesForModuleVersion = getRulesForVersion(GET_ALL_RULES_FOR_VERSION, queryParams, version);
		return rulesForModuleVersion.stream().filter(AbstractRuleEngineRuleModel::getActive).collect(toList());
	}

	protected <T extends AbstractRuleEngineRuleModel> List<T> getRulesForVersion(final String query,
			final Map<String, Object> queryParams, final long version)
	{
		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query, queryParams);

		try
		{
			final SearchResult<T> search = getFlexibleSearchService().search(flexibleSearchQuery);

			final List<T> rulesForVersion = search.getResult();

			if (isNotEmpty(rulesForVersion))
			{
				final List<T> rules = new ArrayList<>();

				final Map<String, List<T>> twinRulesMap = rulesForVersion.stream()
						.collect(groupingBy(AbstractRuleEngineRuleModel::getCode));

				twinRulesMap.values().stream().map(List::stream)
						.forEach(l -> l.max(comparing(AbstractRuleEngineRuleModel::getVersion)).ifPresent(rules::add));

				return rules;
			}
		}
		catch (final FlexibleSearchException e)
		{
			LOG.warn("Exception {} occurred as no rules were found for the version [{}]", e, version);
		}

		return emptyList();
	}

	protected AbstractRuleEngineRuleModel getWithMaximumVersion(final FlexibleSearchQuery query, final long version)
	{
		final SearchResult<AbstractRuleEngineRuleModel> search = getFlexibleSearchService().search(query);
		final List<AbstractRuleEngineRuleModel> ruleModels = search.getResult();
		Stream<AbstractRuleEngineRuleModel> ruleStream = ruleModels.stream();
		if (version >= 0)
		{
			ruleStream = ruleStream.filter(r -> r.getVersion() <= version);
		}
		return ruleStream.sorted(comparing(AbstractRuleEngineRuleModel::getVersion).reversed()).findFirst().orElse(null);
	}

	/**
	 * @return the current time, but with seconds set to zero (for FlexibleSearchQuery caching benefits to the minute).
	 */
	protected Date getRoundedTimestamp()
	{
		final Date currentTime = getTimeService().getCurrentTime();
		final Calendar c = Calendar.getInstance();
		c.setTime(currentTime);
		c.set(Calendar.SECOND, 0);
		return currentTime;
	}

	protected AbstractRulesModuleModel getRulesModuleIfOneAvailable()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_AVAILABLE_KIE_MODULES);
		final SearchResult<AbstractRulesModuleModel> search = getFlexibleSearchService().search(query);
		final List<AbstractRulesModuleModel> searchResult = search.getResult();
		if (CollectionUtils.isNotEmpty(searchResult) && searchResult.size() == 1)
		{
			return searchResult.get(0);
		}
		throw new IllegalStateException(
				"This method could be called in the case of only one AbstractRulesModuleModel available in the system, "
						+ "otherwise the explicit reference to a module should be provided");
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}

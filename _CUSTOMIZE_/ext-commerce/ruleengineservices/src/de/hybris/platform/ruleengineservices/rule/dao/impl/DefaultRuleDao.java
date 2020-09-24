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
package de.hybris.platform.ruleengineservices.rule.dao.impl;


import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.RuleToEngineRuleTypeMappingModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.type.TypeService;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


@SuppressWarnings("squid:S1192")
public class DefaultRuleDao extends AbstractItemDao implements RuleDao
{

	protected static final String GET_ALL_RULES_QUERY = "select {" + AbstractRuleModel.PK + "} from {" // NOSONAR
			+ AbstractRuleModel._TYPECODE + "}";

	protected static final String GET_ALL_ACTIVE_RULES_QUERY = " where ({" + AbstractRuleModel.STARTDATE + "} <= ?startDate OR {"
			+ AbstractRuleModel.STARTDATE + "} IS NULL) AND ({" + AbstractRuleModel.ENDDATE + "} >= ?endDate OR {"
			+ AbstractRuleModel.ENDDATE + "} IS NULL)";

	protected static final String GET_RULE_BY_CODE = " where {" + AbstractRuleModel.CODE + "} = ?code"; // NOSONAR

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected static final String GET_ALL_NOT_ARCHIVED_RULES_QUERY = " where {" + AbstractRuleModel.STATUS + "} != ?status";

	protected static final String GET_ENGINE_RULE_TYPE_FOR_RULE_TYPE = "select {" + RuleToEngineRuleTypeMappingModel.ENGINERULETYPE
			+ "} from {" + RuleToEngineRuleTypeMappingModel._TYPECODE + "} " + "WHERE {" + RuleToEngineRuleTypeMappingModel.RULETYPE
			+ "} = ?ruleType ";

	protected static final String GET_MAX_VERSION_FOR_CODE = "select MAX( {" + AbstractRuleModel.VERSION + "} ) from {"
			+ AbstractRuleModel._TYPECODE + "}" + " where {" + AbstractRuleModel.CODE + "} = ?code";

	protected static final String GET_RULE_BY_CODE_STATUS = " where {" + AbstractRuleModel.CODE + "} = ?code and {"
			+ AbstractRuleModel.STATUS + "} = ?status";

	protected static final String GET_RULE_BY_CODE_STATUSES = " where {" + AbstractRuleModel.CODE + "} = ?code and {"
			+ AbstractRuleModel.STATUS + "} IN (?statuses)";

	protected static final String GET_RULE_BY_STATUSES = " where {" + AbstractRuleModel.STATUS + "} IN (?statuses)";

	protected static final String GET_RULE_BY_CODE_VERSION = " where {" + AbstractRuleModel.CODE + "} = ?code and {"
			+ AbstractRuleModel.VERSION + "} = ?version";

	protected static final String GET_RULE_BY_STATUS_AND_VERSION = " where {" + SourceRuleModel.STATUS + "} in (?" + SourceRuleModel.STATUS + ") and  {" + SourceRuleModel.VERSION
				 + "} = ?" + SourceRuleModel.VERSION;

	private I18NService i18NService;
	private TypeService typeService;

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRules()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY);

		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return filterByLastVersion(search.getResult());
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRulesByType(final Class<T> type)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(type);
		final String typecode = composedType.getCode();

		final StringBuilder sb = createSelectStatement(typecode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(sb.toString());
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return filterByLastVersion(search.getResult());
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllActiveRules()
	{
		final Calendar now = Calendar.getInstance(getI18NService().getCurrentTimeZone(), getI18NService().getCurrentLocale());
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);

		final Map queryParams = of("startDate", now.getTime(), "endDate", now.getTime());

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_ALL_ACTIVE_RULES_QUERY, queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return filterByLastVersion(search.getResult());
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllActiveRulesByType(final Class<T> type)
	{
		final Calendar now = Calendar.getInstance(getI18NService().getCurrentTimeZone(), getI18NService().getCurrentLocale());
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);

		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(type);
		final String typecode = composedType.getCode();

		final Map queryParams = of("startDate", now.getTime(), "endDate", now.getTime());

		final StringBuilder sb = createSelectStatement(typecode);
		sb.append(GET_ALL_ACTIVE_RULES_QUERY);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(sb.toString(), queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return filterByLastVersion(search.getResult());
	}


	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public <T extends AbstractRuleModel> List<T> findAllToBePublishedRules()
	{
		return findAllRules();
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public <T extends AbstractRuleModel> List<T> findAllToBePublishedRulesByType(final Class<T> type)
	{
		return findAllRulesByType(type);
	}

	@Override
	public <T extends AbstractRuleModel> T findRuleByCode(final String code)
	{
		final List<T> versionRules = findAllRuleVersionsByCode(code);
		final List<T> lastVersionRules = filterByLastVersion(versionRules);
		return isNotEmpty(lastVersionRules) ? lastVersionRules.get(0) : null;
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRuleVersionsByCode(final String code)
	{
		final Map queryParams = of("code", code);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_CODE, queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);

		return search.getResult();
	}

	@Override
	public <T extends AbstractRuleModel> T findRuleByCodeAndType(final String code, final Class<T> type)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(type);
		final String typecode = composedType.getCode();

		final Map queryParams = of("code", code);

		final StringBuilder sb = createSelectStatement(typecode);
		sb.append(GET_RULE_BY_CODE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(sb.toString(), queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		final List<T> lastVersionRules = filterByLastVersion(search.getResult());
		return isNotEmpty(lastVersionRules) ? lastVersionRules.get(0) : null;
	}

	@Override
	public RuleType findEngineRuleTypeByRuleType(final Class<?> type)
	{
		final ComposedTypeModel typeModel = typeService.getComposedTypeForClass(type);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ENGINE_RULE_TYPE_FOR_RULE_TYPE);
		query.addQueryParameter("ruleType", typeModel);
		query.setResultClassList(singletonList(RuleType.class));

		try
		{
			return getFlexibleSearchService().searchUnique(query);
		}
		catch (final ModelNotFoundException e)
		{
			return null;
		}
	}

	@Override
	public Long getRuleVersion(final String code)
	{
		validateParameterNotNullStandardMessage("code", code);

		final Map queryParams = of("code", code);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_MAX_VERSION_FOR_CODE, queryParams);

		query.setResultClassList(singletonList(Long.class));
		Long nextRuleVersion;
		try
		{
			nextRuleVersion = getFlexibleSearchService().searchUnique(query);
		}
		catch (final ModelNotFoundException e)
		{
			nextRuleVersion = 0L;
		}
		return nextRuleVersion;
	}

	@Override
	public Optional<AbstractRuleModel> findRuleByCodeAndStatus(final String code, final RuleStatus ruleStatus)
	{
		final List<AbstractRuleModel> rules = findAllRuleVersionsByCodeAndStatus(code, ruleStatus);
		final List<AbstractRuleModel> lastVersionRules = filterByLastVersion(rules);
		return lastVersionRules.stream().findFirst();
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRuleVersionsByCodeAndStatus(final String code, final RuleStatus ruleStatus)
	{
		final Map queryParams = of(AbstractRuleModel.CODE, code, AbstractRuleModel.STATUS, ruleStatus);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_CODE_STATUS, queryParams);

		final SearchResult<T> search = getFlexibleSearchService().search(query);

		return search.getResult();
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRuleVersionsByCodeAndStatuses(final String code,
			final RuleStatus... ruleStatuses)
	{
		if (Objects.nonNull(ruleStatuses) && ruleStatuses.length > 0)
		{
			final Map queryParams = of(AbstractRuleModel.CODE, code, "statuses", newArrayList(ruleStatuses));

			final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_CODE_STATUSES, queryParams);

			final SearchResult<T> search = getFlexibleSearchService().search(query);

			return search.getResult();
		}
		return findAllRuleVersionsByCode(code);
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findAllRulesWithStatuses(final RuleStatus... ruleStatuses)
	{
		final Map queryParams = of("statuses", newArrayList(ruleStatuses));

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_STATUSES, queryParams);

		final SearchResult<T> search = getFlexibleSearchService().search(query);

		return search.getResult();
	}

	@Override
	public <T extends AbstractRuleModel> List<T> findByVersionAndStatuses(final Long version, final RuleStatus... ruleStatuses)
	{
		final Map queryParams = of(AbstractRuleModel.VERSION, version, AbstractRuleModel.STATUS, newArrayList(ruleStatuses));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_STATUS_AND_VERSION, queryParams);
		final SearchResult<T> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public Optional<AbstractRuleModel> findRuleByCodeAndVersion(final String code, final Long version)
	{
		final Map queryParams = of("code", code, "version", version);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULES_QUERY + GET_RULE_BY_CODE_VERSION, queryParams);

		final SearchResult<AbstractRuleModel> search = getFlexibleSearchService().search(query);
		final List<AbstractRuleModel> rules = search.getResult();
		return rules.stream().findFirst();
	}

	protected StringBuilder createSelectStatement(final String typecode)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("select {").append(AbstractRuleModel.PK).append("} from {").append(typecode).append('}');
		return sb;
	}

	protected <T extends AbstractRuleModel> List<T> filterByLastVersion(final List<T> rulesWithVersion)
	{
		final List<T> filteredRules = new ArrayList<>();
		if (isNotEmpty(rulesWithVersion))
		{
			final Map<String, List<T>> twinRulesMap = rulesWithVersion.stream().collect(groupingBy(AbstractRuleModel::getCode));
			twinRulesMap.values().stream().map(List::stream)
					.forEach(l -> l.max(comparing(AbstractRuleModel::getVersion)).ifPresent(filteredRules::add));
		}
		return filteredRules;
	}

	protected I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

}

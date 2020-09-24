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
package de.hybris.platform.ruleengineservices.setup.tasks.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;


/**
 * Update {@link de.hybris.platform.ruleengineservices.jalo.SourceRule}s by changing:
 * <ul>
 * <li>status to {@link RuleStatus#INACTIVE} where status equals {@link RuleStatus#PUBLISHED}</li>
 * <li>status to {@link RuleStatus#UNPUBLISHED} where status equals {@link RuleStatus#MODIFIED}</li>
 * </ul>
 */
public class UpdateRuleStatusTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(UpdateRuleStatusTask.class);
	protected static final String SELECT_RULES_BY_STATUS = "SELECT {Pk} FROM {" + AbstractRuleModel._TYPECODE + "} WHERE {"
			+ AbstractRuleModel.STATUS + "} = ?" + AbstractRuleModel.STATUS + " AND {"+ AbstractRuleModel.VERSION + "} is null";

	private FlexibleSearchService flexibleSearchService;
	private ModelService modelService;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Updating Source Rules status");
		final List<AbstractRuleModel> updatedRules = newArrayList();
		updatedRules.addAll(changeStatus(RuleStatus.PUBLISHED, RuleStatus.INACTIVE));
		updatedRules.addAll(changeStatus(RuleStatus.MODIFIED, RuleStatus.UNPUBLISHED));
		getModelService().saveAll(updatedRules);
	}

	protected List<AbstractRuleModel> changeStatus(final RuleStatus fromStatus, final RuleStatus toStatus)
	{
		return selectRulesByStatus(fromStatus).map(rule -> changeStatus(rule, toStatus)).collect(toList());
	}

	protected AbstractRuleModel changeStatus(final AbstractRuleModel rule, final RuleStatus status)
	{
		rule.setStatus(status);
		return rule;
	}

	protected Stream<SourceRuleModel> selectRulesByStatus(final RuleStatus status)
	{
		final Map<String, Object> parameters = ImmutableMap.of(SourceRuleModel.STATUS, status);
		return getFlexibleSearchService().<SourceRuleModel> search(SELECT_RULES_BY_STATUS, parameters).getResult().stream();
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}

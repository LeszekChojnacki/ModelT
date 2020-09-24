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
package de.hybris.platform.ruleengineservices.jobs.impl;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;

import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobDAO;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default implementation of {@link RuleEngineCronJobDAO}
 */
public class DefaultRuleEngineCronJobDAO implements RuleEngineCronJobDAO
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRuleEngineCronJobDAO.class);
	private FlexibleSearchService flexibleSearchService;

	private static final String GET_NUM_CRON_JOBS_BY_JOB_AND_STATUSES = "select {" + RuleEngineCronJobModel.PK + "} from {"
			+ RuleEngineCronJobModel._TYPECODE + " as cj JOIN " + RuleEngineJobModel._TYPECODE + " as j ON {cj."
			+ RuleEngineCronJobModel.JOB + "} = {j." + RuleEngineJobModel.PK + "}}"
			+ " where {j."+RuleEngineJobModel.CODE+"} = ?jobCode";
	private static final String STATUSES_CONDITION = " and {cj." + RuleEngineCronJobModel.STATUS + "} in (?statuses)";


	@Override
	public int countCronJobsByJob(final String jobCode, final CronJobStatus... statuses)
	{
		Preconditions.checkArgument(Objects.nonNull(jobCode), "Job code should be specified");

		Map queryParams;
		String queryString = GET_NUM_CRON_JOBS_BY_JOB_AND_STATUSES;
		if(Objects.nonNull(statuses) && statuses.length > 0)
		{
			queryParams = of("jobCode", jobCode, "statuses", newArrayList(statuses));
			queryString += STATUSES_CONDITION;
		}
		else
		{
			queryParams = of("jobCode", jobCode);
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString, queryParams);
		final SearchResult<Long> search = getFlexibleSearchService().search(query);
		return search.getCount();
	}

	@Override
	public RuleEngineJobModel findRuleEngineJobByCode(final String jobCode)
	{
		RuleEngineJobModel ruleEngineJob = new RuleEngineJobModel();
		ruleEngineJob.setCode(jobCode);
		try
		{
			ruleEngineJob = getFlexibleSearchService().getModelByExample(ruleEngineJob);
		}
		catch (final ModelNotFoundException e)
		{
			LOG.warn("No RuleEngineJob was found for code [{}]", jobCode);
			ruleEngineJob = null;
		}

		return ruleEngineJob;
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
}

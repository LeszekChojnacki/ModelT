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

import com.google.common.collect.Lists;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.jobs.RuleEngineJobExecutionSynchronizer;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.Lists.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Default implementation of {@link RuleEngineJobExecutionSynchronizer} contract
 */
public class DefaultRuleEngineJobExecutionSynchronizer implements RuleEngineJobExecutionSynchronizer
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultRuleEngineJobExecutionSynchronizer.class);

	private RulesModuleDao rulesModuleDao;

	private ModelService modelService;

	private Lock lock = new ReentrantLock();

	@Override
	public boolean acquireLock(final RuleEngineCronJobModel cronJob)
	{
		try
		{
			lock.lock();

			final List<AbstractRulesModuleModel> modules = getRulesModules(cronJob);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Trying to acquire lock on rule modules [{}] for cron job [{}]", ruleModulesAsString(modules),
						cronJob.getCode());
			}

			final boolean isLockAlreadyAcquired = modules.stream().anyMatch(m -> toBoolean(m.getLockAcquired()));

			if (isLockAlreadyAcquired)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Not able to acquire all necessary locks for the cron job [{}]", cronJob.getCode());
				}
				return false;
			}

			setLockAcquired(cronJob, modules, true);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Acquired locks on rule modules [{}] for the cron job [{}]", ruleModulesAsString(modules), cronJob.getCode());
			}

			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void releaseLock(final RuleEngineCronJobModel cronJob)
	{
		try
		{
			lock.lock();

			final List<AbstractRulesModuleModel> modules = getRulesModules(cronJob);

			setLockAcquired(cronJob, modules, false);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Released locks on rule modules [{}] for the cron job [{}]", ruleModulesAsString(modules), cronJob.getCode());
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	protected void setLockAcquired(final RuleEngineCronJobModel cronJob, final List<AbstractRulesModuleModel> modules,
			boolean value)
	{
		modules.forEach(m -> m.setLockAcquired(value));
		cronJob.setLockAcquired(value);
		getModelService().saveAll(asList(cronJob, modules.toArray()));
	}

	protected List<AbstractRulesModuleModel> getRulesModules(final RuleEngineCronJobModel cronJob)
	{
		final List<AbstractRulesModuleModel> modules = new ArrayList<>();

		if (isNotEmpty(cronJob.getSrcModuleName()))
		{
			modules.add(getRulesModuleDao().findByName(cronJob.getSrcModuleName()));
		}

		modules.addAll(getRulesModules(cronJob.getTargetModuleName()));

		return modules;
	}

	protected List<AbstractRulesModuleModel> getRulesModules(final String moduleName)
	{
		return isEmpty(moduleName) ? getRulesModuleDao().findAll() : singletonList(getRulesModuleDao().findByName(moduleName));
	}

	protected String ruleModulesAsString(final List<AbstractRulesModuleModel> modules)
	{
		return modules.stream().map(AbstractRulesModuleModel::getName).collect(joining(", "));
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
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

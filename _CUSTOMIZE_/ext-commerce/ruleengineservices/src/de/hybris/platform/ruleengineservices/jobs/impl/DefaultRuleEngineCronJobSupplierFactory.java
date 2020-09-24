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

import static java.util.Collections.singletonList;

import de.hybris.platform.ruleengineservices.jobs.RuleEngineCronJobSupplierFactory;
import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.cluster.ClusterService;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of the rule engine cron job supplier factory
 */
public class DefaultRuleEngineCronJobSupplierFactory implements RuleEngineCronJobSupplierFactory
{
	private ClusterService clusterService;

	private Integer nodeId;

	private String nodeGroup;

	@Override
	public Supplier<RuleEngineCronJobModel> createCompileAndPublishSupplier(final List<SourceRuleModel> rules,
			final String moduleName, final boolean enableIncrementalUpdate)
	{
		return () ->
		{

			final RuleEngineCronJobModel cronJob = newCronJob();
			cronJob.setSourceRules(rules);
			cronJob.setTargetModuleName(moduleName);
			cronJob.setEnableIncrementalUpdate(enableIncrementalUpdate);
			return cronJob;
		};
	}

	@Override
	public Supplier<RuleEngineCronJobModel> createUndeploySupplier(final List<SourceRuleModel> rules, final String moduleName)
	{
		return () ->
		{

			final RuleEngineCronJobModel cronJob = newCronJob();
			cronJob.setSourceRules(rules);
			cronJob.setTargetModuleName(moduleName);
			return cronJob;
		};
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public Supplier<RuleEngineCronJobModel> createArchiveSupplier(final SourceRuleModel rule)
	{
		return () ->
		{
			final RuleEngineCronJobModel cronJob = newCronJob();
			cronJob.setSourceRules(singletonList(rule));
			return cronJob;
		};
	}

	@Override
	public Supplier<RuleEngineCronJobModel> createSynchronizeSupplier(final String srcModuleName, final String targetModuleName)
	{
		return () ->
		{

			final RuleEngineCronJobModel cronJob = newCronJob();
			cronJob.setSrcModuleName(srcModuleName);
			cronJob.setTargetModuleName(targetModuleName);
			return cronJob;
		};
	}

	@Override
	public Supplier<RuleEngineCronJobModel> createModuleInitializationSupplier(final String moduleName)
	{
		return () ->
		{

			final RuleEngineCronJobModel cronJob = newCronJob();
			cronJob.setTargetModuleName(moduleName);
			return cronJob;
		};
	}

	@Override
	public Supplier<RuleEngineCronJobModel> createAllModulesInitializationSupplier()
	{
		return this::newCronJob;
	}

	protected RuleEngineCronJobModel newCronJob()
	{
		final RuleEngineCronJobModel cronJob = new RuleEngineCronJobModel();
		if (getClusterService().isClusteringEnabled())
		{
			cronJob.setNodeID(getNodeId());
			cronJob.setNodeGroup(getNodeGroup());
		}
		return cronJob;
	}

	protected Integer getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(final Integer nodeId)
	{
		this.nodeId = nodeId;
	}

	protected String getNodeGroup()
	{
		return nodeGroup;
	}

	public void setNodeGroup(final String nodeGroup)
	{
		this.nodeGroup = nodeGroup;
	}

	protected ClusterService getClusterService()
	{
		return clusterService;
	}

	@Required
	public void setClusterService(final ClusterService clusterService)
	{
		this.clusterService = clusterService;
	}
}

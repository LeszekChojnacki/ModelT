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

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Deactivate all {@link de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel} that are related to
 * {@link de.hybris.platform.ruleengineservices.model.SourceRuleModel}
 */
public class DeactivateDroolsRulesTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(DeactivateDroolsRulesTask.class);
	protected static final String FIND_QUALIFYING_RULES = "SELECT {engine_rule:Pk} FROM {" + AbstractRuleEngineRuleModel._TYPECODE
			+ " as engine_rule JOIN " + AbstractRuleModel._TYPECODE + " as rule ON {rule:" + AbstractRuleModel.UUID
			+ "}={engine_rule:" + AbstractRuleEngineRuleModel.UUID + "}} ";
	private FlexibleSearchService flexibleSearchService;
	private ModelService modelService;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Deactivating existing drools rules");
		final List<AbstractRuleEngineRuleModel> sourcedDroolsRules = getSourcedDroolsRules();
		final List<AbstractRuleEngineRuleModel> modifiedRules = sourcedDroolsRules.stream()
				.filter(isRuleValidToProcess())
				.map(this::deactivate).collect(Collectors.toList());
		getModelService().saveAll(modifiedRules);
	}

	protected Predicate<AbstractRuleEngineRuleModel> isRuleValidToProcess()
	{
		return rule -> BooleanUtils.isTrue(rule.getActive()) && BooleanUtils.isTrue(rule.getCurrentVersion());
	}

	protected List<AbstractRuleEngineRuleModel> getSourcedDroolsRules()
	{
		return getFlexibleSearchService()
				.<AbstractRuleEngineRuleModel> search(FIND_QUALIFYING_RULES).getResult();
	}

	protected AbstractRuleEngineRuleModel deactivate(final AbstractRuleEngineRuleModel ruleEngineRule)
	{
		ruleEngineRule.setActive(Boolean.FALSE);
		ruleEngineRule.setCurrentVersion(Boolean.FALSE);
		return ruleEngineRule;
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

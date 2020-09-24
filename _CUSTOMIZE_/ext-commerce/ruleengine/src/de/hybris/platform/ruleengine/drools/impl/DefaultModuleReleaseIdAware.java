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
package de.hybris.platform.ruleengine.drools.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.drools.ModuleReleaseIdAware;
import de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.rule.AgendaFilter;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link ModuleReleaseIdAware}
 */
public class DefaultModuleReleaseIdAware implements ModuleReleaseIdAware
{

	private static final String KIE_MODULE_DUMMY_VERSION = "DUMMY_VERSION";
	private static final String KIE_MODULE_DUMMY_GROUPID = "DUMMY_GROUP";
	private static final String KIE_MODULE_DUMMY_ARTIFACTID = "DUMMY_ARTIFACT";

	private RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper;

	@Override
	public ReleaseId getDeployedKieModuleReleaseId(final RuleEvaluationContext context)
	{
		final DroolsRuleEngineContextModel ruleEngineContext = validateRuleEvaluationContext(context);
		final DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
		final DroolsKIEBaseModel kieBase = kieSession.getKieBase();
		final DroolsKIEModuleModel kieModule = kieBase.getKieModule();
		return getRuleEngineKieModuleSwapper()
				.getDeployedReleaseId(kieModule, null)
				.orElse(getDummyReleaseId(kieModule));
	}

	protected ReleaseId getDummyReleaseId(final DroolsKIEModuleModel module)
	{
		final String groupId = module.getMvnGroupId();
		final String artifactId = module.getMvnArtifactId();

		return new ReleaseIdImpl(nonNull(groupId) ? groupId : KIE_MODULE_DUMMY_GROUPID,
				nonNull(artifactId) ? artifactId : KIE_MODULE_DUMMY_ARTIFACTID, KIE_MODULE_DUMMY_VERSION);
	}

	protected DroolsRuleEngineContextModel validateRuleEvaluationContext(final RuleEvaluationContext context)
	{
		validateParameterNotNull(context, "rule evaluation context must not be null");
		final AbstractRuleEngineContextModel abstractREContext = context.getRuleEngineContext();
		validateParameterNotNull(abstractREContext, "rule engine context must not be null");
		if (!(abstractREContext instanceof DroolsRuleEngineContextModel))
		{
			throw new IllegalArgumentException("rule engine context " + abstractREContext.getName()
					+ " must be of type DroolsRuleEngineContext. " + abstractREContext.getItemtype() + " is not supported.");
		}
		final DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel) abstractREContext;

		if (nonNull(context.getFilter()) && !(context.getFilter() instanceof AgendaFilter))
		{
			throw new IllegalArgumentException("context.filter attribute must be of type org.kie.api.runtime.rule.AgendaFilter");
		}
		return ruleEngineContext;
	}

	protected RuleEngineKieModuleSwapper getRuleEngineKieModuleSwapper()
	{
		return ruleEngineKieModuleSwapper;
	}

	@Required
	public void setRuleEngineKieModuleSwapper(final RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper)
	{
		this.ruleEngineKieModuleSwapper = ruleEngineKieModuleSwapper;
	}
}

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
package de.hybris.platform.ruleengine.monitor.impl;

import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.monitor.RuleEngineRulesModuleMonitor;

import java.util.Objects;

import javax.annotation.PostConstruct;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * default implementation of the {@link RuleEngineRulesModuleMonitor}
 */
public class DefaultRuleEngineRulesModuleMonitor implements RuleEngineRulesModuleMonitor<DroolsKIEModuleModel>
{

	private KieServices kieServices;
	private RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap;

	@Override
	public boolean isRulesModuleDeployed(final DroolsKIEModuleModel rulesModule)
	{
		Preconditions.checkArgument(nonNull(rulesModule), "Provided rules module cannot be NULL");
		
		final String deployedMvnVersion = rulesModule.getDeployedMvnVersion();
		if (nonNull(deployedMvnVersion))
		{
			final ReleaseId releaseId = getKieServices()
					.newReleaseId(rulesModule.getMvnGroupId(), rulesModule.getMvnArtifactId(), deployedMvnVersion);
			return nonNull(getKieServices().getRepository().getKieModule(releaseId));
		}
		return false;
	}

	@PostConstruct
	private void setUp()
	{
		if (Objects.isNull(kieServices))
		{
			kieServices = getRuleEngineBootstrap().getEngineServices();
		}
	}

	protected KieServices getKieServices()
	{
		return kieServices;
	}

	public void setKieServices(final KieServices kieServices)
	{
		this.kieServices = kieServices;
	}

	protected RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> getRuleEngineBootstrap()
	{
		return ruleEngineBootstrap;
	}

	@Required
	public void setRuleEngineBootstrap(final RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap)
	{
		this.ruleEngineBootstrap = ruleEngineBootstrap;
	}
}

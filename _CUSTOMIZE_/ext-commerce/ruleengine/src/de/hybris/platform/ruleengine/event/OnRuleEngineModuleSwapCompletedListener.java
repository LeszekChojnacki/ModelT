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
package de.hybris.platform.ruleengine.event;

import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.drools.KieSessionHelper;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

import org.springframework.beans.factory.annotation.Required;


/**
 * triggered after the (local) rule publication has been completed. If the publication was successful, this listener
 * shuts down and removes any old kie session pool.
 */
public class OnRuleEngineModuleSwapCompletedListener<T> extends AbstractEventListener<RuleEngineModuleSwapCompletedEvent>
{
	private KieSessionHelper<T> kieSessionHelper;

	private RulesModuleDao rulesModuleDao;

	@Override
	protected void onEvent(final RuleEngineModuleSwapCompletedEvent event)
	{
		if (!event.isFailed())
		{
			final DroolsKIEModuleModel module = getRulesModuleDao().findByName(event.getRulesModuleName());
			getKieSessionHelper().shutdownKieSessionPools(module.getMvnArtifactId(), event.getRulesModuleVersion());
		}
	}

	protected KieSessionHelper<T> getKieSessionHelper()
	{
		return kieSessionHelper;
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setKieSessionHelper(final KieSessionHelper<T> kieSessionHelper)
	{
		this.kieSessionHelper = kieSessionHelper;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

}
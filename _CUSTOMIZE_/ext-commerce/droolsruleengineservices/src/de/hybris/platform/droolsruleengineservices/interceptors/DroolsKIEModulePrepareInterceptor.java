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
package de.hybris.platform.droolsruleengineservices.interceptors;

import com.google.common.collect.Sets;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import org.springframework.beans.factory.annotation.Required;

import java.util.Objects;
import java.util.Set;


/**
 * Drools module specific prepare interceptor. Changes the version of the module according to maximum version of the
 * rules in the module
 **/
public class DroolsKIEModulePrepareInterceptor implements PrepareInterceptor<AbstractRulesModuleModel>
{
	private ModuleVersioningService moduleVersioningService;
	private EngineRuleDao engineRuleDao;

	@Override
	public void onPrepare(final AbstractRulesModuleModel rulesModuleModel, final InterceptorContext context)
			throws InterceptorException
	{
		if (rulesModuleModel instanceof DroolsKIEModuleModel)
		{
			final DroolsKIEModuleModel droolsKIEModule = (DroolsKIEModuleModel) rulesModuleModel;
			final DroolsKIEBaseModel defaultKIEBase = droolsKIEModule.getDefaultKIEBase();
			Set droolRules = null;
			if (Objects.nonNull(defaultKIEBase))
			{
				droolRules = Sets.newHashSet(getEngineRuleDao().getActiveRules(defaultKIEBase.getKieModule()));
			}
			getModuleVersioningService().assertRuleModuleVersion(rulesModuleModel, droolRules);
		}
	}

	protected ModuleVersioningService getModuleVersioningService()
	{
		return moduleVersioningService;
	}

	@Required
	public void setModuleVersioningService(final ModuleVersioningService moduleVersioningService)
	{
		this.moduleVersioningService = moduleVersioningService;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}
}

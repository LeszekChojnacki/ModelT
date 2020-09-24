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

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Prepare intercepter for DroolsKIEBase. Assigns the correct module version, based on associated rules set. The module
 * version "adjustments" are implemented both on module->base->rules and rule->base->module relation ends to make sure
 * we always have the consistent version situation all over the chain
 */
public class DroolsKIEBasePrepareInterceptor implements PrepareInterceptor<DroolsKIEBaseModel>
{

	private ModuleVersioningService moduleVersioningService;

	@Override
	public void onPrepare(final DroolsKIEBaseModel base, final InterceptorContext context) throws InterceptorException
	{
		final DroolsKIEModuleModel kieModule = base.getKieModule();
		final Set rules = base.getRules();
		// if the module is associated with the base, make it sure it has a correct version, derived from the associated rules version
		if (nonNull(kieModule) && isNotEmpty(rules))
		{
			getModuleVersioningService().assertRuleModuleVersion(kieModule, rules);
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

}

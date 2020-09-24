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
package de.hybris.platform.ruleengineservices.rule.interceptors;

import de.hybris.platform.ruleengineservices.model.RuleGroupModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import org.springframework.beans.factory.annotation.Required;


/**
 * @deprecated since 6.8. the interceptor is not functional anymore after DEFAULT_RULEGROUP_CODE_PROPERTY is deprecated.
 */
@Deprecated
public class RuleGroupValidateInterceptor implements ValidateInterceptor<RuleGroupModel>
{

	private ConfigurationService configurationService;

	@Override
	public void onValidate(final RuleGroupModel model, final InterceptorContext ctx) throws InterceptorException
	{
		// empty
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

}

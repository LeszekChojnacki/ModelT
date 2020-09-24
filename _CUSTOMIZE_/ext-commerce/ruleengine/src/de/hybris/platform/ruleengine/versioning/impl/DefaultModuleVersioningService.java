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
package de.hybris.platform.ruleengine.versioning.impl;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;

import java.util.Optional;
import java.util.Set;


/**
 * Default (unsupported) implementation of module versioning service
 */
public class DefaultModuleVersioningService implements ModuleVersioningService
{

	@Override
	public Optional<Long> getModuleVersion(final AbstractRuleEngineRuleModel ruleModel)
	{
		throw new UnsupportedOperationException(getUnsupportedMessage());
	}

	@Override
	public void assertRuleModuleVersion(final AbstractRuleEngineRuleModel ruleModel,
			final AbstractRulesModuleModel rulesModule)
	{
		throw new UnsupportedOperationException(getUnsupportedMessage());
	}

	@Override
	public void assertRuleModuleVersion(final AbstractRulesModuleModel moduleModel, final Set<AbstractRuleEngineRuleModel> rules)
	{
		throw new UnsupportedOperationException(getUnsupportedMessage());
	}

	@Override
	public Optional<Long> getDeployedModuleVersionForRule(final String ruleCode, final String moduleName)
	{
		throw new UnsupportedOperationException(getUnsupportedMessage());
	}

	protected String getUnsupportedMessage()
	{
		return "This method is not implemented in the default implementation";
	}

}

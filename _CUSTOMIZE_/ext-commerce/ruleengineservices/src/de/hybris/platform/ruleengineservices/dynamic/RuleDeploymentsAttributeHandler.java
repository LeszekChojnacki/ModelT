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
package de.hybris.platform.ruleengineservices.dynamic;

import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.strategies.RulesModuleResolver;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static java.util.Collections.emptyList;


/**
 * Dynamic attribute handler for {@link AbstractRuleModel#getDeployments()} that returns rules modules provided by
 * {@link RulesModuleResolver#lookupForRulesModules(AbstractRuleModel)}
 *
 * @deprecated since 18.08
 */
@Deprecated
public class RuleDeploymentsAttributeHandler implements DynamicAttributeHandler<List<AbstractRulesModuleModel>, AbstractRuleModel>
{
	private RulesModuleResolver rulesModuleResolver;

	@Override
	public List<AbstractRulesModuleModel> get(final AbstractRuleModel rule)
	{
		return RuleStatus.PUBLISHED.equals(rule.getStatus()) ? getRulesModuleResolver().lookupForRulesModules(rule)
				: emptyList();
	}

	@Override
	public void set(final AbstractRuleModel model, final List<AbstractRulesModuleModel> abstractRulesModuleModels)
	{
		throw new UnsupportedOperationException("AbstractRulesModuleModel.deployments is readonly attribute");
	}

	protected RulesModuleResolver getRulesModuleResolver()
	{
		return rulesModuleResolver;
	}

	@Required
	public void setRulesModuleResolver(final RulesModuleResolver rulesModuleResolver)
	{
		this.rulesModuleResolver = rulesModuleResolver;
	}
}

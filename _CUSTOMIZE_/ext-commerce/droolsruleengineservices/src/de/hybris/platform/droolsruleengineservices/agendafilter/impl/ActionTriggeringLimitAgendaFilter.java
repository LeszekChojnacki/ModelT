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
package de.hybris.platform.droolsruleengineservices.agendafilter.impl;

import de.hybris.platform.droolsruleengineservices.impl.DefaultCommerceRuleEngineService;
import de.hybris.platform.ruleengineservices.rao.providers.impl.DefaultRuleConfigurationRRDProvider;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;

import org.kie.api.runtime.rule.Match;


/**
 * The ActionTriggeringLimitAgendaFilter prevents a rule from being activated if the rule has exceeded the limit defined
 * in {@code AbstractRuleEngineRuleModel#getMaxAllowedRuns()}. It requires {@code RuleConfigurationRRD} facts to be
 * inserted and declared as variable in the "when" clause for it to work properly (see
 * {@link DefaultRuleConfigurationRRDProvider} configured at the {@link DefaultCommerceRuleEngineService}). Note that
 * this filter only checks the {@code RuleConfigurationRRD#getMaxAllowedRuns()} and
 * {@code RuleConfigurationRRD#getCurrentRuns()} fields, the incrementing of the counter has to be done somewhere else
 * (e.g. in AbstractRuleExecutableSupport).
 *
 * @deprecated since 18.08 no longer used
 */
@Deprecated
public class ActionTriggeringLimitAgendaFilter extends AbstractRuleConfigurationAgendaFilter
{

	@Override
	public boolean accept(final Match match, final RuleConfigurationRRD config)
	{
		final Integer maxAllowedRuns = config.getMaxAllowedRuns();
		final Integer currentRuns = config.getCurrentRuns();
		return currentRuns == null || maxAllowedRuns == null || currentRuns.compareTo(maxAllowedRuns) < 0;
	}
}

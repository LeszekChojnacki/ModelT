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

import de.hybris.platform.ruleengineservices.rule.evaluation.impl.RuleAndRuleGroupExecutionTracker;

import java.util.Optional;

import org.drools.core.common.InternalFactHandle;
import org.kie.api.definition.KieDefinition.KnowledgeType;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;


/**
 * checks whether a given rule is allowed to be executed (using the tracker object).
 */
public class RuleAndRuleGroupTrackingAgendaFilter implements AgendaFilter
{

	@Override
	public boolean accept(final Match match)
	{
		final Optional<RuleAndRuleGroupExecutionTracker> tracker = getTracker(match);
		if (tracker.isPresent())
		{
			final Rule rule = match.getRule();
			return tracker.get().allowedToExecute(rule);
		}
		return true;
	}

	/**
	 * returns the RuleAndRuleGroupExecutionTracker
	 */
	protected Optional<RuleAndRuleGroupExecutionTracker> getTracker(final Match match)
	{
		final Rule rule = match.getRule();
		if (rule.getKnowledgeType() != KnowledgeType.RULE)
		{
			return Optional.empty();
		}
		return match.getFactHandles().stream().filter(fact -> fact instanceof InternalFactHandle)
				.map(fact -> ((InternalFactHandle) fact).getObject()).filter(fact -> fact instanceof RuleAndRuleGroupExecutionTracker)
				.map(fact -> (RuleAndRuleGroupExecutionTracker) fact).findAny();
	}
}

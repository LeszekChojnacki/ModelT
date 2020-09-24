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
package de.hybris.platform.droolsruleengineservices.agendafilter;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import org.kie.api.runtime.rule.AgendaFilter;


/**
 * The AgendaFilterFactory provides a factory to create {@code AgendaFilter}s.
 */
public interface AgendaFilterFactory
{
	/**
	 * creates an AgendaFilter based on the given rule engine context.
	 *
	 * @param context
	 *           the rule engine context
	 * @return an AgendaFilter (to be applied during rule evaluation)
	 */
	AgendaFilter createAgendaFilter(AbstractRuleEngineContextModel context);
}

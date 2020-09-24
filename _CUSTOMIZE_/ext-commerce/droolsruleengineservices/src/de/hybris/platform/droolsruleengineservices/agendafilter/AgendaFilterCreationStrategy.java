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

import de.hybris.platform.droolsruleengineservices.agendafilter.impl.DefaultAgendaFilterFactory;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import org.kie.api.runtime.rule.AgendaFilter;


/**
 * The AgendaFilterCreationStrategy is used in the {@link DefaultAgendaFilterFactory} to create AgendaFilters.
 */
public interface AgendaFilterCreationStrategy
{

	/**
	 * creates an AgendaFilter based on the given rule engine context.
	 *
	 * @param context
	 *           the rule engine context
	 * @return an AgendaFilter (to be registered with a drools session)
	 */
	AgendaFilter createAgendaFilter(final AbstractRuleEngineContextModel context);
}

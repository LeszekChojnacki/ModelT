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
package de.hybris.platform.droolsruleengineservices.eventlisteners;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import java.util.Set;

import org.kie.api.event.rule.AgendaEventListener;


/**
 * Provides a factory for creating drools {@link AgendaEventListener}s.
 */
public interface AgendaEventListenerFactory
{
	/**
	 * returns a set of {@link AgendaEventListener}s based on the given context
	 *
	 * @param ctx
	 *           the context
	 * @return a set of AgendaEventListener objects
	 */
	Set<AgendaEventListener> createAgendaEventListeners(AbstractRuleEngineContextModel ctx);
}

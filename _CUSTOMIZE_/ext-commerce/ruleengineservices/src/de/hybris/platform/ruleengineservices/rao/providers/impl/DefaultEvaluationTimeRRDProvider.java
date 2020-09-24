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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import static java.util.Collections.emptySet;

import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rrd.EvaluationTimeRRD;

import java.util.Collections;
import java.util.Date;
import java.util.Set;


/**
 * DefaultEvaluationTimeRRDProvider is the default implementation for the evaluation time {@code RRD} provider
 * used by the default drools rule engine.
 */
public class DefaultEvaluationTimeRRDProvider implements RAOProvider
{
	@Override
	public Set expandFactModel(final Object modelFact)
	{
		if (modelFact instanceof Date)
		{
			final EvaluationTimeRRD evaluationTimeRRD = new EvaluationTimeRRD();
			evaluationTimeRRD.setEvaluationTime(Long.valueOf(((Date) modelFact).getTime()));
			return Collections.singleton(evaluationTimeRRD);
		}
		else
		{
			return emptySet();
		}
	}
}

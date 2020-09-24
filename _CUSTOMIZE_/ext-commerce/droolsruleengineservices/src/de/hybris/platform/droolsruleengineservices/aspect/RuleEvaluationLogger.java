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
package de.hybris.platform.droolsruleengineservices.aspect;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;

import de.hybris.platform.ruleengineservices.aspect.AbstractLoggingAspect;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.RAOAction;

import java.util.List;
import java.util.function.Predicate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.definition.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logging Aspect for intercepting calls of public class methods in
 * de.hybris.platform.droolsruleengineservices.rule.evaluation package.
 *
 */
@Aspect
public class RuleEvaluationLogger extends AbstractLoggingAspect
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluationLogger.class);

	private static final Predicate<Object> ELIGIBLE_FOR_TARGET = obj -> obj instanceof RAOAction;

	@Before("execution(public * de.hybris.platform.droolsruleengineservices.commerce.impl.*.*(..))")
	public void decideBefore(final JoinPoint joinPoint)
	{
		logJoinPoint(joinPoint);
	}

	@Override
	protected String getLogInfoFromArgs(final List<?> args)
	{
		for (final Object arg : args)
		{
			if (arg instanceof KnowledgeHelper)
			{
				return String.format("%s : %s", getDebugInfo((KnowledgeHelper) arg), args.toString());
			}
		}
		return "[no debug info from ruleeval context] : " + args.toString();
	}

	@Override
	protected Logger getLogger()
	{
		return LOGGER;
	}

	@Override
	protected boolean isEligibleForJoinPoint(final JoinPoint joinPoint)
	{
		return ELIGIBLE_FOR_TARGET.test(joinPoint.getTarget());
	}

	/**
	 * Aggregates debug info from rule eval context into String.
	 */
	protected String getDebugInfo(final KnowledgeHelper context)
	{
		return String.format("%s: %s", RULEMETADATA_RULECODE, getMetaDataFromRule(context.getRule(), RULEMETADATA_RULECODE));
	}

	/**
	 * returns the rule's meta-data for the given key (or {@code null}). Calls {@code toString()} on the meta-data
	 * object.
	 *
	 * @param rule
	 *           the rule
	 * @param key
	 *           the key of the meta-data
	 * @return the string representation of the meta-data (or null)
	 */
	protected String getMetaDataFromRule(final Rule rule, final String key)
	{
		final Object value = rule.getMetaData().get(key);
		return value == null ? null : value.toString();
	}
}

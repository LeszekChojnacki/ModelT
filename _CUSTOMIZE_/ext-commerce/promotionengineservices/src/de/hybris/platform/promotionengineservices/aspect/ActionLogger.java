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
package de.hybris.platform.promotionengineservices.aspect;

import de.hybris.platform.promotionengineservices.action.impl.AbstractRuleActionStrategy;
import de.hybris.platform.ruleengineservices.aspect.AbstractLoggingAspect;

import java.util.function.Predicate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logging Aspect for intercepting calls of public class methods in
 * de.hybris.platform.promotionengineservices.action.impl package.
 *
 */
@Aspect
public class ActionLogger extends AbstractLoggingAspect
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogger.class);

	private static final Predicate<Object> eligibleForTarget = obj -> obj instanceof AbstractRuleActionStrategy<?>;

	@Before("execution(public * de.hybris.platform.promotionengineservices.action.impl.*.*(..))")
	public void decideBefore(final JoinPoint joinPoint)
	{
		logJoinPoint(joinPoint);
	}

	@Override
	protected boolean isEligibleForJoinPoint(final JoinPoint joinPoint)
	{
		return eligibleForTarget.test(joinPoint.getTarget());
	}

	@Override
	protected Logger getLogger()
	{
		return LOGGER;
	}
}

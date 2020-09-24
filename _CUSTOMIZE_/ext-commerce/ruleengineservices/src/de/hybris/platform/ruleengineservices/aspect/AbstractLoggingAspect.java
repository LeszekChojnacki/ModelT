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
package de.hybris.platform.ruleengineservices.aspect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class of Logging Aspect for intercepting calls of class methods and log it attributes. Method
 * isEligibleForJoinPoint must be implemented in subclasses. Method(s) with Aspect annotations (ex. {@link Before}) have
 * to be implemented in subclasses to specify joint-points to intercept and call logJoinPoint(...) in it. Subclasses
 * have to have {@link Aspect} annotation to be able to act as Spring Aspect.
 */
public abstract class AbstractLoggingAspect
{
	public static final String DEBUG = "DEBUG";
	public static final String WARN = "WARN";
	public static final String TRACE = "TRACE";
	public static final String INFO = "INFO";
	public static final String DEFAULT_LOG_LEVEL = DEBUG;
	private static final String LOGGING_TEMPLATE = "{} triggered : {}";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoggingAspect.class);
	private String logLevel = DEFAULT_LOG_LEVEL;


	protected Logger getLogger()
	{
		return LOGGER;
	}

	/**
	 * Does logging for the intercepted {@code joinPoint}.
	 */
	protected void logJoinPoint(final JoinPoint joinPoint)
	{
		if (isLoggingEligibleForJoinPoint(joinPoint))
		{
			final String signatureName = getSignatureName(joinPoint);
			final List<?> args = joinPoint.getArgs() == null ? Collections.emptyList() : Arrays.asList(joinPoint.getArgs());
			final String logInfo = getLogInfoFromArgs(args);
			logAtDebugLevel(signatureName, logInfo);
			logAtTraceLevel(signatureName, logInfo);
			logAtInfoLevel(signatureName, logInfo);
			logAtWarnLevel(signatureName, logInfo);
		}
	}


	protected String getSignatureName(final JoinPoint joinPoint)
	{
		return String.format("%s.%s", joinPoint.getSignature().getDeclaringType().getSimpleName(),
				joinPoint.getSignature().getName());
	}

	protected String getLongSignatureName(final JoinPoint joinPoint)
	{
		return String.format("%s.%s", joinPoint.getSignature().getDeclaringType().getName(),
				joinPoint.getSignature().getName());
	}

	protected void logAtDebugLevel(final String signatureName, final String logInfo)
	{
		if (DEBUG.equalsIgnoreCase(getLogLevel()) && getLogger().isDebugEnabled())
		{
			getLogger().debug(LOGGING_TEMPLATE, signatureName, logInfo);
		}
	}

	protected void logAtTraceLevel(final String signatureName, final String logInfo)
	{
		if (TRACE.equalsIgnoreCase(getLogLevel()) && getLogger().isTraceEnabled())
		{
			getLogger().trace(LOGGING_TEMPLATE, signatureName, logInfo);
		}
	}

	protected void logAtInfoLevel(final String signatureName, final String logInfo)
	{
		if (INFO.equalsIgnoreCase(getLogLevel()) && getLogger().isInfoEnabled())
		{
			getLogger().info(LOGGING_TEMPLATE, signatureName, logInfo);
		}
	}

	protected void logAtWarnLevel(final String signatureName, final String logInfo)
	{
		if (WARN.equalsIgnoreCase(getLogLevel()) && getLogger().isWarnEnabled())
		{
			getLogger().warn(LOGGING_TEMPLATE, signatureName, logInfo);
		}
	}

	/**
	 * Checks if the {@code joinPoint} is eligible for logging.
	 */
	protected boolean isLoggingEligibleForJoinPoint(final JoinPoint joinPoint)
	{
		return isEligibleForJoinPoint(joinPoint);
	}

	/**
	 * Checks if the {@code joinPoint} is eligible to process.
	 */
	protected abstract boolean isEligibleForJoinPoint(final JoinPoint joinPoint);

	/**
	 * Returns String representation of argument set of intercepted method.
	 */
	protected String getLogInfoFromArgs(final List<?> args)
	{
		return args.stream().map(o -> ToStringBuilder.reflectionToString(o)).collect(Collectors.joining(", "));
	}

	protected String getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel(final String logLevel)
	{
		this.logLevel = logLevel;
	}
}

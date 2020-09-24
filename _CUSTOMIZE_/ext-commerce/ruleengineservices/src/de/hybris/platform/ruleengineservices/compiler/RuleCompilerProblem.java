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
package de.hybris.platform.ruleengineservices.compiler;

import java.io.Serializable;


/**
 * This interface represents the problem which may occur during compilation process.
 */
public interface RuleCompilerProblem extends Serializable
{
	enum Severity
	{
		WARNING, ERROR
	}

	/**
	 * Returns problem severity.
	 *
	 * @return severity
	 */
	Severity getSeverity();

	/**
	 * Returns problem message.
	 *
	 * @return message
	 */
	String getMessage();
}

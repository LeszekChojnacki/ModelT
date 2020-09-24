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
import java.util.List;


/**
 * This interface represents result of rule compilation process.
 */
public interface RuleCompilerResult extends Serializable
{
	/**
	 * Returns code of the source rule that has been compiled.
	 * @return source rule code
	 */
	String getRuleCode();

	/**
	 * Returns version of the source rule that has been compiled.
	 * @return rule version
	 */
	long getRuleVersion();

	/**
	 * Returns outcome of compilation process.
	 * @return outcome (error/success)
	 */
	Result getResult();

	/**
	 * Returns problems occurred during compilation process.
	 * @return problems
	 */
	List<RuleCompilerProblem> getProblems();

	enum Result
	{
		SUCCESS, ERROR
	}
}

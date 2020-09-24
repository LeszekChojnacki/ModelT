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
package de.hybris.platform.ruleengineservices.maintenance;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;

import java.util.List;


/**
 * This interface represents result of rule compilation and publication process.
 */
public interface RuleCompilerPublisherResult
{
	enum Result
	{
		SUCCESS, COMPILER_ERROR, PUBLISHER_ERROR
	}

	/**
	 * Returns outcome of compilation and publication process.
	 *
	 * @return outcome (error/success)
	 */
	Result getResult();

	/**
	 * Returns results from the compilation.
	 *
	 * @return results
	 */
	List<RuleCompilerResult> getCompilerResults();

	/**
	 * Returns results from the publication.
	 *
	 * @return results
	 */
	List<RuleEngineActionResult> getPublisherResults();
}

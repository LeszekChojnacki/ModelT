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

import java.util.List;


/**
 * Returns the list of applicable {@link RuleIrProcessor}s.
 */
public interface RuleIrProcessorFactory
{
	/**
	 * Returns instances of {@link RuleIrProcessor}.
	 *
	 * @return the processors
	 */
	List<RuleIrProcessor> getRuleIrProcessors();
}

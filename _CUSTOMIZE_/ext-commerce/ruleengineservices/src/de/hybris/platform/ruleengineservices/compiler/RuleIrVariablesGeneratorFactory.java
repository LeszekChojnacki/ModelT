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

/**
 * Implementations of this interface are responsible for creating instances of {@link RuleIrVariablesGenerator}.
 */
public interface RuleIrVariablesGeneratorFactory
{
	/**
	 * Creates a new variables generator.
	 *
	 * @return the new variables generator
	 */
	RuleIrVariablesGenerator createVariablesGenerator();
}

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
package de.hybris.platform.droolsruleengineservices.compiler;

/**
 * Implementations for this interface can format metadata to its drools representation.
 */
public interface DroolsRuleMetadataGenerator
{
	/**
	 * Generates the metadata for the Drools rule engine.
	 *
	 * @param context
	 *           - the drools rule generator context
	 * @param indentation
	 *           - the indentation
	 *
	 * @return the String representation
	 *
	 */
	String generateMetadata(final DroolsRuleGeneratorContext context, String indentation);
}

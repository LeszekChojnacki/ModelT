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
package de.hybris.platform.ruleengineservices.constants;



/**
 * Global class for all RuleEngineServices constants. You can add global constants for your extension into this class.
 */
public final class RuleEngineServicesConstants extends GeneratedRuleEngineServicesConstants
{
	public static final String EXTENSIONNAME = "ruleengineservices";

	public static final int DEFAULT_MAX_ALLOWED_RUNS = 1000;
	public static final Long DEFAULT_RULE_VERSION = 0L;
	/**
	 * @deprecated since 6.8. the property is not used and not functional anymore
	 * the property name for looking up the default rule group (defined e.g. in project.properties)
	 */
	@Deprecated
	public static final String DEFAULT_RULEGROUP_CODE_PROPERTY = "ruleengineservices.rulegroups.defaultRuleGroupCode";

	public static final String CUSTOMER_CONDITION_USE_PK_PROPERTY = "ruleengineservices.target.customer.condition.use.pk";

	private RuleEngineServicesConstants()
	{
		//empty to avoid instantiating this constant class
	}
}

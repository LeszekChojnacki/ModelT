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
package de.hybris.platform.ruleengine.constants;

import java.io.File;


/**
 * Global class for all RuleEngine constants. You can add global constants for your extension into this class.
 */
public final class RuleEngineConstants extends GeneratedRuleEngineConstants // NOSONAR
{

	public static final String RULE_ENGINE_ACTIVE = "ruleengine.engine.active";

	public static final String RULE_ENGINE_INIT_MODE = "ruleengine.engine.init.mode";

	public enum RuleEngineInitMode
	{
		SYNC, ASYNC
	}

	public static final String MEDIA_CODE_POSTFIX = "RuleMedia";

	public static final String MEDIA_DRL_FILE_EXTENSION = ".drl";

	public static final String DROOLS_DATE_FORMAT_KEY = "drools.dateformat";

	public static final String VALIDATE_DROOLSRULE_RULECODE = "droolsruleengineservices.validate.droolsrule.rulecode";

	public static final String VALIDATE_DROOLSRULE_MODULENAME = "droolsruleengineservices.validate.droolsrule.modulename";

	public static final String VALIDATE_DROOLSRULE_RULENAME = "droolsruleengineservices.validate.droolsrule.rulename";

	public static final String VALIDATE_DROOLSRULE_RULEPACKAGE = "droolsruleengineservices.validate.droolsrule.rulepackage";

	public static final boolean VALIDATE_DROOLSRULE_DEFAULT_FLAG = true;

	/**
	 * @deprecated since 6.6
	 */
	@Deprecated
	public static final String RULEENGINE_EVALUATION_RETRY_LIMIT_PROPERTY = "ruleengine.evaluation.retry.limit";   // NOSONAR

	/**
	 * @deprecated since 6.6
	 */
	@Deprecated
	public static final int RULEENGINE_EVALUATION_RETRY_LIMIT_DEFAULT_VALUE = 5;                                  // NOSONAR

	/**
	 * the default drools date format (used if no other one is set via the {@code drools.dateformat} property
	 */
	public static final String DEFAULT_DROOLS_DATE_FORMAT = "dd-MMM-yyyy";

	/**
	 * drools base path for resources {@code /src/main/resources/} declared using {@link File#separatorChar}
	 */
	public static final String DROOLS_BASE_PATH = "src" + File.separatorChar + "main" + File.separatorChar + "resources"
			+ File.separatorChar;

	public static final String RULEMETADATA_RULECODE = "ruleCode";
	public static final String RULEMETADATA_RULEGROUP_CODE = "ruleGroupCode";
	public static final String RULEMETADATA_RULEGROUP_EXCLUSIVE = "ruleGroupExclusive";
	public static final String RULEMETADATA_MODULENAME = "moduleName";
	public static final String RULEMETADATA_MAXIMUM_RULE_EXECUTIONS = "maxRuleExecutions";

	public static final String KIE_MODULE_MEDIA_FOLDER_QUALIFIER = "ruleengine.kie.module.media.folder.qualifier";

	public static final String KIE_MODULE_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE = "kie-modules";

	private RuleEngineConstants()
	{
		//empty to avoid instantiating this constant class
	}
}

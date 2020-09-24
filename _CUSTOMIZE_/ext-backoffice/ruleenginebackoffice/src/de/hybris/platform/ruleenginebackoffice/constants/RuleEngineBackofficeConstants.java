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
package de.hybris.platform.ruleenginebackoffice.constants;

/**
 * Global class for all RuleEngineBackoffice constants. You can add global constants for your extension into this class.
 */
@SuppressWarnings("squid:S1214")
public final class RuleEngineBackofficeConstants extends GeneratedRuleEngineBackofficeConstants
{
	/**
	 * @deprecated since 6.6 Please use {@link #EXTENSIONNAME} instead
	 */
	@Deprecated
	public static final String MY_EXTENSIONNAME = "ruleenginebackoffice";

	private RuleEngineBackofficeConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension
	public interface NotificationSource
	{
		String MESSAGE_SOURCE = EXTENSIONNAME + "-ruleComposer";

		interface EventType
		{
			String EXCEPTION = "Exception";
			String TRIGGER = "Trigger";
		}

		/**
		 * @deprecated since 6.6 The hybris job framework is used therefore all notifications are no longer displayed for the backoffice user
		 */
		@Deprecated
		interface CompileAndPublish
		{
			String ABSTRACT_RULE_MESSAGE_SOURCE = EXTENSIONNAME + "-abstractRuleCompileForModuleComposer";
			String RULE_COMPILE_FOR_MODULE_COMPOSER_MESSAGE_SOURCE = EXTENSIONNAME + "-ruleCompileForModuleComposer";
			String RULE_COMPILE_ALL_FOR_MODULE_COMPOSER_MESSAGE_SOURCE = EXTENSIONNAME + "-ruleCompileAllForModuleComposer";
			String RULE_UNDEPLOY_FROM_MODULE_COMPOSER_MESSAGE_SOURCE = EXTENSIONNAME + "-ruleUndeployFromModuleComposer";
			String RULE_MODULE_SWAP_MESSAGE_SOURCE = EXTENSIONNAME + "-ruleModuleSwap";
			String RULE_INITIALIZE_MESSAGE_SOURCE = EXTENSIONNAME + "-ruleModuleInitialize";
			/**
			 * @deprecated since 6.6 A job framework is used therefore all notification are not longer displayed to the backoffice user
			 */
			@Deprecated
			interface EventType
			{
				String COMPILE = "Compile";
				String PUBLISH = "Publish";
				String UNDEPLOY = "Undeploy";
				String SWAP = "Swap";
				String EXCEPTION = "Exception";
				String INIT = "Init";
			}
		}

		interface CreateFromTemplate
		{
			String MESSAGE_SOURCE = EXTENSIONNAME + "-createFromTemplate";

			interface EventType
			{
				String CREATE = "Create";
			}
		}
		/**
		 * @deprecated since 6.6 A job framework is used therefore all notification are not longer displayed to the backoffice user
		 */
		@Deprecated
		interface Archive
		{
			String MESSAGE_SOURCE = EXTENSIONNAME + "-ruleArchive";
			/**
			 * @deprecated since 6.6 A job framework is used therefore all notification are not longer displayed to the backoffice user
			 */
			interface EventType
			{
				String ARCHIVE = "Archive";
			}
		}

		interface Clone
		{
			String MESSAGE_SOURCE = EXTENSIONNAME + "-ruleClone";

			interface EventType
			{
				String CLONE = "Clone";
			}
		}

		/**
		 * @deprecated since 6.6 A job framework is used therefore all notification are not longer displayed to the backoffice user
		 */
		@Deprecated
		interface RulesModuleSync
		{
			String RULES_MODULE_SYNC_MESSAGE_SOURCE = EXTENSIONNAME + "-rulesModuleSyncComposer";
			/**
			 * @deprecated since 6.6 A job framework is used therefore all notification are not longer displayed to the backoffice user
			 */
			@Deprecated
			interface EventType
			{
				String COMPILE = "Compile";
				String PUBLISH = "Publish";
				String START_SYNC = "StartSync";
				String SYNCHRONIZE = "Synchronization";
				String EXCEPTION = "Exception";
			}
		}
	}
}

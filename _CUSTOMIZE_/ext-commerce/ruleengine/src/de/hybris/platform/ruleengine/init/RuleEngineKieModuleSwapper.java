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
package de.hybris.platform.ruleengine.init;

import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.enums.DroolsEqualityBehavior;
import de.hybris.platform.ruleengine.enums.DroolsEventProcessingMode;
import de.hybris.platform.ruleengine.enums.DroolsSessionType;
import de.hybris.platform.ruleengine.impl.KieContainerListener;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;


/**
 * Drools - specific interface incapsulating the logic of swapping to a new KieContainer during the RuleEngine
 * initialization phase
 */
public interface RuleEngineKieModuleSwapper
{

	/**
	 * Swaps synchroneously to a new KieModule. It's a blocking call
	 *
	 * @param module
	 * 			 instance of the {@link de.hybris.platform.ruleengine.model.AbstractRulesModuleModel} module
	 * @param listener
	 * 			 instance of {@link KieContainerListener} that fires when the switch of Kie Container is complete
	 * @param postTaskList
	 * 			 chain of post-task operations incapsulated as a linked list of {@link Supplier} instances
	 * @param enableIncrementalUpdate
	 * 			 flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 * @return the list that will contain the method execution results including the post-tasks
	 */
	List<Object> switchKieModule(DroolsKIEModuleModel module, KieContainerListener listener,
				 LinkedList<Supplier<Object>> postTaskList, boolean enableIncrementalUpdate, RuleEngineActionResult result);

	/**
	 * Swaps asynchroneously to a new KieModule. It's a non-blocking call
	 *
	 * @param moduleName
	 * 			 kie module name
	 * @param listener
	 * 			 instance of {@link KieContainerListener} that fires when the switch of Kie Container is complete
	 * @param resultsAccumulator
	 * 			 the list that will contain the method execution results including the post-tasks
	 * @param resetFlagSupplier
	 * 			 the task to perform after the sync call finishes the task (whether it was successfull or not )
	 * @param postTaskList
	 * 			 chain of post-task operations incapsulated as a linked list of {@link Supplier} instances
	 * @param enableIncrementalUpdate
	 * 			 flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 */
	void switchKieModuleAsync(String moduleName,
				 KieContainerListener listener, List<Object> resultsAccumulator, Supplier<Object> resetFlagSupplier,
				 List<Supplier<Object>> postTaskList, boolean enableIncrementalUpdate, RuleEngineActionResult result);


	/**
	 * Creates the new instance of {@link KieContainer} for a given {@link KieModule}
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel}
	 * @param kieModule
	 *      instance of {@link KieModule}
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 * @return new {@link KieContainer} instance
	 */
	KieContainer initializeNewKieContainer(DroolsKIEModuleModel module, KieModule kieModule, RuleEngineActionResult result);

	/**
	 * Creates the new instance of {@link KieModule}, based on information contained in {@link DroolsKIEModuleModel}
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel}
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 * @return pair of the newly created instance of {@link KieModule} and the corresponding caching structure for the
	 * created module
	 */
	Pair<KieModule, KIEModuleCacheBuilder> createKieModule(DroolsKIEModuleModel module, RuleEngineActionResult result);

	/**
	 * Adds new {@link KieBaseModel} to a {@link KieModuleModel} with all rules
	 *
	 * @param module
	 * 			 instance of {@link KieModuleModel} to add the {@link KieBaseModel} to
	 * @param kfs
	 * 			 instance of {@link KieFileSystem}
	 * @param base
	 * 			 instance of {@link DroolsKIEBaseModel} that keeps the information for a {@link KieBaseModel} to be
	 * 			 created
	 * @param cache
	 * 			 the caching structure for the module being initialized
	 */
	void addKieBase(KieModuleModel module, KieFileSystem kfs, DroolsKIEBaseModel base, KIEModuleCacheBuilder cache);

	/**
	 * Adds new {@link KieBaseModel} to a {@link KieModuleModel} with all rules
	 *
	 * @param module
	 * 			 instance of {@link KieModuleModel} to add the {@link KieBaseModel} to
	 * @param base
	 * 			 instance of {@link DroolsKIEBaseModel} that keeps the information for a {@link KieBaseModel} to be
	 * 			 created
	 */
	void addKieBase(KieModuleModel module, DroolsKIEBaseModel base);


	/**
	 * Updates the instance of {@link DroolsKIEModuleModel} with information about affectively deployed {@link ReleaseId}
	 * version
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel}
	 * @return version of deployed {@link ReleaseId}
	 */
	String activateKieModule(DroolsKIEModuleModel module);

	/**
	 * Tries to remove the {@link KieModule} with given {@link ReleaseId} from {@link org.kie.api.builder.KieRepository}
	 *
	 * @param releaseId
	 * 			 the instance of {@link ReleaseId} corresponding to a {@link KieModule} to be removed
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 * 			 removal
	 * @return true if the module was found and removed, false otherwise
	 */
	boolean removeKieModuleIfPresent(final ReleaseId releaseId, RuleEngineActionResult result);

	/**
	 * Tries to remove the old {@link KieModule} from {@link org.kie.api.builder.KieRepository}
	 *
	 * @param result
	 * 			 instance of {@link RuleEngineActionResult} to be used in cluster nodes notification
	 * 			 removal
	 * @return true if the module was found and removed, false otherwise
	 */
	boolean removeOldKieModuleIfPresent(final RuleEngineActionResult result);

	/**
	 * Adds instance of new {@link KieSessionModel} to {@link KieBaseModel}
	 *
	 * @param base
	 * 			 instance of {@link KieBaseModel}
	 * @param session
	 * 			 instance of {@link DroolsKIESessionModel} containing the information for new {@link KieSessionModel}
	 */
	void addKieSession(KieBaseModel base, DroolsKIESessionModel session);

	/**
	 * Adds rules from a given {@link DroolsKIEBaseModel} to {@link KieFileSystem}
	 *
	 * @param kfs
	 * 			 instance of {@link KieFileSystem}
	 * @param base
	 * 			 instance of {@link DroolsKIEBaseModel} containing the reference to the rules to publish
	 * @param cache
	 * 			 the caching structure for the module being initialized
	 */
	void addRules(KieFileSystem kfs, DroolsKIEBaseModel base, KIEModuleCacheBuilder cache);

	/**
	 * Creates the XML representation of {@link KieModuleModel} and writes it to {@link KieFileSystem}
	 *
	 * @param module
	 * 			 instance of {@link KieModuleModel}
	 * @param kfs
	 * 			 instance of {@link KieFileSystem}
	 */
	void writeKModuleXML(KieModuleModel module, KieFileSystem kfs);

	/**
	 * Write the building POM XML to {@link KieFileSystem}
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel} to be used for {@link ReleaseId} creation
	 * @param kfs
	 * 			 instance of {@link KieFileSystem}
	 */
	void writePomXML(DroolsKIEModuleModel module, KieFileSystem kfs);

	/**
	 * Creates the new instance of {@link ReleaseId} based on {@link DroolsKIEModuleModel}
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel}
	 * @return newly created {@link ReleaseId}
	 */
	ReleaseId getReleaseId(DroolsKIEModuleModel module);

	/**
	 * Returns (optional) {@link ReleaseId} for a deployed version of the {@link KieModuleModel}
	 *
	 * @param module
	 * 			 instance of {@link DroolsKIEModuleModel}
	 * @param deployedMvnVersion
	 * 			 currently deployed releaseId version of the Kie Module, if known
	 * @return instance of {@link Optional}.of({@link ReleaseId}) if the {@link ReleaseId} could be created,
	 * {@link Optional}.empty() otherwise
	 */
	Optional<ReleaseId> getDeployedReleaseId(DroolsKIEModuleModel module, String deployedMvnVersion);


	/**
	 * Initializes the {@link org.kie.api.KieServices} instance
	 */
	void setUpKieServices();

	/**
	 * Block until the whole swapping task/tasks are finished
	 */
	void waitForSwappingToFinish();
	
	/**
	 * Add cacheable data of rules of the latest versions belonging to the KieBase into the cache.
	 * 
	 * @param base
	 * 			KieBase of the rules to be processed
	 * @param cache
	 * 			CacheBuilder to cache rule data 
	 */
	void addRulesToCache(final DroolsKIEBaseModel base, final KIEModuleCacheBuilder cache);

	/**
	 * converts between hybris and drools session type
	 */
	static KieSessionModel.KieSessionType getSessionType(final DroolsSessionType sessionType)
	{
		switch (sessionType)
		{
			case STATEFUL:
				return KieSessionModel.KieSessionType.STATEFUL;
			case STATELESS:
				return KieSessionModel.KieSessionType.STATELESS;
			default:
				return null;
		}
	}

	/**
	 * converts between hybris and drools equality behavior
	 */
	static EqualityBehaviorOption getEqualityBehaviorOption(final DroolsEqualityBehavior behavior)
	{
		switch (behavior)
		{
			case EQUALITY:
				return EqualityBehaviorOption.EQUALITY;
			case IDENTITY:
				return EqualityBehaviorOption.IDENTITY;
			default:
				return null;
		}
	}

	/**
	 * converts between hybris and drools event processing mode
	 */
	static EventProcessingOption getEventProcessingOption(final DroolsEventProcessingMode eventProcessingMode)
	{
		switch (eventProcessingMode)
		{
			case STREAM:
				return EventProcessingOption.STREAM;
			case CLOUD:
				return EventProcessingOption.CLOUD;
			default:
				return null;
		}
	}

	/**
	 * converts a drools message {@link Level} to a {@link MessageLevel}.
	 */
	static MessageLevel convertLevel(final Message.Level level)
	{
		if (level == null)
		{
			return null;
		}
		switch (level)
		{
			case ERROR:
				return MessageLevel.ERROR;
			case WARNING:
				return MessageLevel.WARNING;
			case INFO:
				return MessageLevel.INFO;
			default:
				return null;
		}
	}


}

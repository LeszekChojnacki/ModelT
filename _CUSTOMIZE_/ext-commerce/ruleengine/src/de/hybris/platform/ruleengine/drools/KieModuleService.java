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
package de.hybris.platform.ruleengine.drools;

import java.util.Optional;

import org.kie.api.builder.KieModule;


/**
 * The interface provides methods that allow serialization and deserialization of {@link KieModule}.
 */
public interface KieModuleService
{
	/**
	 * Stores a {@link KieModule} which is identified by its name and version.
	 *
	 * @param kieModuleName
	 *           name of being stored KieModule
	 * @param kieModuleVersion
	 *           string representation of a version of being stored KieModule
	 * @param kieModule
	 *           {@link KieModule} instance to store
	 */
	void saveKieModule(String kieModuleName, String kieModuleVersion, KieModule kieModule);

	/**
	 * Finds and restores a {@link KieModule} by its name and version.
	 *
	 * @param kieModuleName
	 *           name of being restored KieModule
	 * @param kieModuleVersion
	 *           string representation of a version of being restored KieModule
	 * @return found instance of {@link KieModule} wrapped into {@Optional} or empty Optional.
	 */
	Optional<KieModule> loadKieModule(String kieModuleName, String kieModuleVersion);
}
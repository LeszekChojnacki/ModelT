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
package de.hybris.platform.ruleengine.dao;

import de.hybris.platform.ruleengine.model.DroolsKIEModuleMediaModel;

import java.util.Optional;


/**
 * Provides dao functionality for {@code DroolsKIEModuleMediaModel}.
 *
 */
public interface DroolsKIEModuleMediaDao
{
	/**
	 * Returns an {@link Optional} of {@link DroolsKIEModuleMediaModel}
	 *
	 * @param kieModuleName
	 *           KIE Module name
	 * @param releaseId
	 *           String representation of the KIE Module ReleaseId
	 * @return Optional of AbstractRuleEngineRuleModel by its identifiers.
	 */
	Optional<DroolsKIEModuleMediaModel> findKIEModuleMedia(String kieModuleName, String releaseId);
}

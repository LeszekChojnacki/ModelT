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

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;

import java.util.List;


/**
 * Provides dao functionality for {@code DroolsKIEBaseModel}.
 *
 */
public interface DroolsKIEBaseDao
{
	/**
	 * Returns a list with all {@link DroolsKIEBaseModel}s
	 */
	List<DroolsKIEBaseModel> findAllKIEBases();
}

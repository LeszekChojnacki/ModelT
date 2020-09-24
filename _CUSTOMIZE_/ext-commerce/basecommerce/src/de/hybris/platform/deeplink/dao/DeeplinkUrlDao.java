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
package de.hybris.platform.deeplink.dao;

import de.hybris.platform.deeplink.model.rules.DeeplinkUrlModel;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlRuleModel;

import java.util.List;


/**
 * The Interface DeeplinkUrlDao. Provides methods for interaction with DB in DeeplinkUrl functionality
 * 
 *
 * 
 * @spring.bean deeplinUrlDao
 */
public interface DeeplinkUrlDao
{

	/**
	 * Find deeplink url model.
	 * 
	 * @param code
	 *           the code
	 * @return the deeplink url model
	 */
	DeeplinkUrlModel findDeeplinkUrlModel(final String code);

	/**
	 * Find deeplink url rules.
	 * 
	 * @return the list
	 */
	List<DeeplinkUrlRuleModel> findDeeplinkUrlRules();

	/**
	 * Find object.
	 * 
	 * @param pkString
	 *           the pk string
	 * @return the object
	 */
	Object findObject(final String pkString);

}

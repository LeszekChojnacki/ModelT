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
package de.hybris.platform.couponservices.dao;

import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Optional;


/**
 * Data Access Object for CodeGenerationConfiguration Model.
 */
public interface CodeGenerationConfigurationDao extends Dao
{
	/**
	 * Finds the CodeGenerationConfiguration by its couponId.
	 *
	 * @param name
	 *           the name of the configuration
	 *
	 * @return CodeGenerationConfiguration by its name.
	 */
	Optional<CodeGenerationConfigurationModel> findCodeGenerationConfigurationByName(final String name);


}

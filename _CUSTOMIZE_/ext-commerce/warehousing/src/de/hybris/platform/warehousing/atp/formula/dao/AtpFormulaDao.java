/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.atp.formula.dao;

import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.util.Collection;


/**
 * Dao for {@link AtpFormulaModel}
 */
public interface AtpFormulaDao
{
	/**
	 * Retrieves all {@link AtpFormulaModel} in the system.
	 *
	 * @return the collection of all {@link AtpFormulaModel}.
	 */
	Collection<AtpFormulaModel> getAllAtpFormula();
}

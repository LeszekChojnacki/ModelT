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
package de.hybris.platform.warehousing.atp.formula.services;

import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.util.Collection;
import java.util.Map;



/**
 * Services to interact with ATP formulas.
 */
public interface AtpFormulaService
{
	/**
	 * Retrieves an {@link AtpFormulaModel} by its code.
	 *
	 * @param formulaCode
	 * 		The formula code for which we want to retrieve a specific {@link AtpFormulaModel}.
	 * @return the corresponding {@link AtpFormulaModel}.
	 */
	AtpFormulaModel getAtpFormulaByCode(String formulaCode);

	/**
	 * Retrieves collection of all {@link AtpFormulaModel} in the system.
	 *
	 * @return the collection of all {@link AtpFormulaModel}.
	 */
	Collection<AtpFormulaModel> getAllAtpFormula();

	/**
	 * Creates an {@link AtpFormulaModel}
	 *
	 * @param atpFormula
	 * 		The ATP formula to be created.
	 * @return the created {@link AtpFormulaModel}.
	 */
	AtpFormulaModel createAtpFormula(AtpFormulaModel atpFormula);

	/**
	 * Updates an {@link AtpFormulaModel} based on its code
	 *
	 * @param atpFormula
	 * 		The ATP formula to be updated.
	 * @return the up to date {@link AtpFormulaModel}.
	 */
	AtpFormulaModel updateAtpFormula(AtpFormulaModel atpFormula);

	/**
	 * Deletes an {@link AtpFormulaModel} from its code. Only possible if the formula is not assign to any base store
	 *
	 * @param formulaCode
	 * 		The formula code that we want to delete
	 */
	void deleteAtpFormula(String formulaCode);

	/**
	 * Retrieves the ATP value from a specific formula
	 * @param atpFormula
	 * 		The {@link AtpFormulaModel} from which we want to retrieve the ATP value
	 * @param params
	 * 		The list of {@link de.hybris.platform.ordersplitting.model.StockLevelModel} to gather in order to define the ATP value
	 * @return the number of available item according to the params
	 */
	Long getAtpValueFromFormula(AtpFormulaModel atpFormula, Map<String, Object> params);
}

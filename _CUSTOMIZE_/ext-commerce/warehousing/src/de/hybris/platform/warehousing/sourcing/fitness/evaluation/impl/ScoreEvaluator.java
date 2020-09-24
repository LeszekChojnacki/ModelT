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
package de.hybris.platform.warehousing.sourcing.fitness.evaluation.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.fitness.evaluation.FitnessEvaluator;

/**
 * Score implementation of {@link FitnessEvaluator} interface.
 * This simply returns the score of warehouse in provided {@link SourcingLocation}
 */
public class ScoreEvaluator implements FitnessEvaluator {

	@Override
	public Double evaluate(SourcingLocation sourcingLocation) {
		WarehouseModel warehouse = sourcingLocation.getWarehouse();

		return (warehouse != null && warehouse.getScore() != null) ? warehouse.getScore() : Double.NaN;
	}
}

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
package de.hybris.platform.ruleengineservices.rule.evaluation;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;



/**
 * ChangeDeliveryModeRAOAction changes the delivery mode of a cart.
 * @deprecated since 6.6
 */
@Deprecated
public interface ChangeDeliveryModeRAOAction
{

	/**
	 * Changes the current delivery mode to the given values, adds it to the cartRao and recalculates the carRao totals.
	 * The {@code ruleContext} object can be used to enhance the returned DiscountRAO.
	 *
	 * @param cartRao
	 *           the cartRao to change the delivery mode for
	 * @param mode
	 *           the new delivery mode
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 */
	ShipmentRAO changeDeliveryMode(CartRAO cartRao, DeliveryModeRAO mode, RuleEngineResultRAO result, Object ruleContext);

	/**
	 * Changes the current delivery mode to the given values, adds it to the cartRao and recalculates the carRao totals.
	 * The {@code ruleContext} object can be used to enhance the returned DiscountRAO.
	 *
	 * @param cartRao
	 *           the cartRao to change the delivery mode for
	 * @param deliveryModeCode
	 *           the new delivery mode code
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DiscountRAO
	 * @return the newly applied ShipmentRAO or null if the given deliveryModeCode cannot be found
	 */
	ShipmentRAO changeDeliveryMode(CartRAO cartRao, String deliveryModeCode, RuleEngineResultRAO result, Object ruleContext);

}

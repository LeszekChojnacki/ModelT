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
package de.hybris.platform.promotionengineservices.promotionengine.report;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResults;


/**
 * Provides information about applied promotions based on an order
 */
public interface ReportPromotionService
{
	/**
	 * Provides comprehensive promotions report against an order
	 * @param order - subject to report
	 * @return @{link PromotionEngineResults} that contains insights on promotions applied against the order
	 */
	PromotionEngineResults report(AbstractOrderModel order);
}

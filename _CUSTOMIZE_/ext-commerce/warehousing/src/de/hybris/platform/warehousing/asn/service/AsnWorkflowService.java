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
package de.hybris.platform.warehousing.asn.service;

import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;


/**
 * Warehousing service to manage workflow for {@link AdvancedShippingNoticeModel}.
 */
public interface AsnWorkflowService
{
	/**
	 * Starts a cancellation workflow for the given consignment {@link AdvancedShippingNoticeModel}.
	 *
	 * @param asnModel
	 * 		{@link AdvancedShippingNoticeModel} for which a workflow needs to be started
	 */
	void startAsnCancellationWorkflow(AdvancedShippingNoticeModel asnModel);

}

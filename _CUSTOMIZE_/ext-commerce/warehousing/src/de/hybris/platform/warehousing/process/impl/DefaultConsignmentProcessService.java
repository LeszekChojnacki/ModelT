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
package de.hybris.platform.warehousing.process.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.warehousing.constants.WarehousingConstants;
import de.hybris.platform.warehousing.process.AbstractWarehousingBusinessProcessService;
import de.hybris.platform.warehousing.process.BusinessProcessException;

import org.apache.commons.collections.CollectionUtils;


/**
 * Business process service for handling consignments.
 */
public class DefaultConsignmentProcessService extends AbstractWarehousingBusinessProcessService<ConsignmentModel>
{
	@Override
	public String getProcessCode(final ConsignmentModel consignment)
	{
		final BusinessProcessModel consignmentProcess = getConsignmentProcess(consignment);
		return consignmentProcess.getCode();
	}

	/**
	 * Returns the {@link ConsignmentProcessModel} asscoiated by Ordermanagement, to the {@link ConsignmentModel}
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} which has the {@link ConsignmentProcessModel}
	 * @return the associated {@link ConsignmentProcessModel}
	 */
	public BusinessProcessModel getConsignmentProcess(final ConsignmentModel consignmentModel)
	{
		if (CollectionUtils.isEmpty(consignmentModel.getConsignmentProcesses()))
		{
			throw new BusinessProcessException("Unable to process event for consignment [" + consignmentModel.getCode()
					+ "]. No processes associated to the consignment.");
		}

		final String expectedCode = consignmentModel.getCode() + WarehousingConstants.CONSIGNMENT_PROCESS_CODE_SUFFIX;

		return consignmentModel.getConsignmentProcesses().stream().filter(process -> expectedCode.equals(process.getCode()))
				.findFirst().orElseThrow(() -> new BusinessProcessException(
						"No business process found for consignment [" + consignmentModel.getCode() + "]."));
	}

}

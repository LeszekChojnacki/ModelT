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

import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.warehousing.process.AbstractWarehousingBusinessProcessService;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Business process service for handling orders.
 */
public class DefaultReturnProcessService extends AbstractWarehousingBusinessProcessService<ReturnRequestModel>
{
	private transient BaseStoreService baseStoreService;

	@Override
	public String getProcessCode(final ReturnRequestModel returnRequest)
	{
		if (CollectionUtils.isEmpty(returnRequest.getReturnProcess()))
		{
			throw new BusinessProcessException("Unable to process event for return [" + returnRequest.getCode()
					+ "]. No processes associated to the return.");
		}

		// Get base store for return
		final BaseStoreModel store = Optional.ofNullable(Optional.ofNullable(returnRequest.getOrder().getStore()).orElseGet(() -> getBaseStoreService().getCurrentBaseStore()))
				.orElseThrow(
						() -> new BusinessProcessException("Unable to process event for return [" + returnRequest.getCode()
								+ "]. No base store associated to the return."));

		// Get return process name
		final String returnProcessDefinitionName = Optional.ofNullable(store.getCreateReturnProcessCode()).orElseThrow(
				() -> new BusinessProcessException("Unable to process event for return [" + returnRequest.getCode()
						+ "]. No return process definition for base store."));

		final String expectedCodePrefix = returnProcessDefinitionName + "-" + returnRequest.getCode();
		final Collection<String> codes = returnRequest.getReturnProcess().stream().map(ReturnProcessModel::getCode)
				.filter(code -> code.startsWith(expectedCodePrefix)).collect(Collectors.toList());

		// Validate that we have 1 valid process
		if (CollectionUtils.isEmpty(codes))
		{
			throw new BusinessProcessException("Unable to process event for return [" + returnRequest.getCode()
					+ "]. No return processes associated to the return with prefix [" + returnProcessDefinitionName + "].");
		}
		if (codes.size() > 1)
		{
			throw new BusinessProcessException("Unable to process event for return [" + returnRequest.getCode()
					+ "]. Expected only 1 process with prefix [" + returnProcessDefinitionName + "] but there were "
					+ codes.size() + ".");
		}

		return codes.iterator().next();
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

}

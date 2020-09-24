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
package de.hybris.platform.warehousing.interceptor;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.warehousing.model.PackagingInfoModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populate the packaging info with default data when preparing the {@link ConsignmentModel}.
 */
public class ConsignmentPackagingInfoPrepareInterceptor implements PrepareInterceptor<ConsignmentModel>
{
	protected static final String DEFAULT_DIMENSION_UNIT = "cm";
	protected static final String DEFAULT_VALUE = "0";
	protected static final String DEFAULT_WEIGHT_UNIT = "kg";

	private ModelService modelService;
	private TimeService timeService;

	@Override
	public void onPrepare(final ConsignmentModel consignment, final InterceptorContext context) throws InterceptorException
	{
		if (context.isNew(consignment))
		{
			final PackagingInfoModel packagingInfo = getModelService().create(PackagingInfoModel.class);
			packagingInfo.setConsignment(consignment);
			packagingInfo.setGrossWeight(DEFAULT_VALUE);
			packagingInfo.setHeight(DEFAULT_VALUE);
			packagingInfo.setInsuredValue(DEFAULT_VALUE);
			packagingInfo.setLength(DEFAULT_VALUE);
			packagingInfo.setWidth(DEFAULT_VALUE);
			packagingInfo.setDimensionUnit(DEFAULT_DIMENSION_UNIT);
			packagingInfo.setWeightUnit(DEFAULT_WEIGHT_UNIT);
			packagingInfo.setCreationtime(getTimeService().getCurrentTime());
			packagingInfo.setModifiedtime(getTimeService().getCurrentTime());

			context.registerElementFor(packagingInfo, PersistenceOperation.SAVE);

			consignment.setPackagingInfo(packagingInfo);
		}
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}

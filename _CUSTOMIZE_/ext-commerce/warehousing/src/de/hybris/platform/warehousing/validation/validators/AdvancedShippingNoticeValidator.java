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
package de.hybris.platform.warehousing.validation.validators;

import de.hybris.platform.core.Registry;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.validation.annotations.AdvancedShippingNoticeValid;
import de.hybris.platform.warehousing.warehouse.service.WarehousingWarehouseService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validates if the {@link WarehouseModel} belongs to the given {@link PointOfServiceModel}.
 */
public class AdvancedShippingNoticeValidator
		implements ConstraintValidator<AdvancedShippingNoticeValid, AdvancedShippingNoticeModel>
{
	private WarehousingWarehouseService warehousingWarehouseService;

	@Override
	public void initialize(final AdvancedShippingNoticeValid advancedShippingNoticeValid)
	{
		warehousingWarehouseService = Registry.getApplicationContext()
				.getBean("warehousingWarehouseService", WarehousingWarehouseService.class);
	}

	@Override
	public boolean isValid(final AdvancedShippingNoticeModel advancedShippingNotice,
			final ConstraintValidatorContext constraintValidatorContext)
	{
		final PointOfServiceModel pointOfService = advancedShippingNotice.getPointOfService();
		final WarehouseModel warehouse = advancedShippingNotice.getWarehouse();

		return pointOfService != null && (warehouse == null || getWarehousingWarehouseService()
				.isWarehouseInPoS(warehouse, pointOfService));
	}

	protected WarehousingWarehouseService getWarehousingWarehouseService()
	{
		return warehousingWarehouseService;
	}

}

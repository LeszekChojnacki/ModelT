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
package de.hybris.platform.ordercancel.impl.denialstrategies;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.Constants;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.servicelayer.type.TypeService;


/**
 * Strategy that forbids cancel, when the end-customer makes the cancel request and configuration prevents cancels by
 * end-customers (only CSA is allowed to make request in that case). To use this strategy, please make sure, that
 * PrincipalModel passed to
 * {@link #getCancelDenialReason(OrderCancelConfigModel, OrderModel, PrincipalModel, boolean, boolean)} represents the
 * end-customer. This object is considered to be an end-customer when it's type code is equal to
 * {@link de.hybris.platform.core.Constants.TYPES#Customer}
 */
public class DefaultRequestOriginDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy
{
	private TypeService typeService;

	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requestor, final boolean partialCancel, final boolean partialEntryCancel)
	{
		validateParameterNotNull(configuration, "Parameter configuration must not be null");
		final ComposedTypeModel ctm = typeService.getComposedTypeForClass(requestor.getClass());

		final boolean isCalledByCustomer = Constants.TYPES.Customer.equals(ctm.getCode());

		if (!configuration.isOrderCancelAllowed() && isCalledByCustomer)
		{
			/*
			 * Order cancel request is issued by end customer. However, our configuration prevents end-users from canceling
			 * orders, only CSA is authorized to do it.
			 */
			return getReason();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return the typeService
	 */
	public TypeService getTypeService()
	{
		return typeService;
	}

	/**
	 * @param typeService
	 *           the typeService to set
	 */
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}

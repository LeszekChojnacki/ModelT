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
package de.hybris.platform.customercouponbackoffice.widgets;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.customercouponservices.model.CustomerCouponModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;


/**
 * Checks the product validity date before saving a coupon
 */
public class CustomerCouponEditorAreaLogicHandler extends DefaultEditorAreaLogicHandler
{
	private static final Logger LOG = Logger.getLogger(CustomerCouponEditorAreaLogicHandler.class);

	private ModelService modelService;

	@Override
	public List<ValidationInfo> performValidation(final WidgetInstanceManager widgetInstanceManager, final Object currentObject,
			final ValidationContext validationContext)
	{
		final List<ValidationInfo> validationInfos = super.performValidation(widgetInstanceManager, currentObject,
				validationContext);
		if (currentObject instanceof CustomerCouponModel)
		{
			return validationInfos.stream().filter(vi -> !AbstractCouponModel.ENDDATE.equals(vi.getInvalidPropertyPath()))
					.collect(Collectors.toList());
		}
		return validationInfos;
	}

	@Override
	public void beforeEditorAreaRender(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
	{
		if (currentObject instanceof CustomerModel)
		{
			final CustomerModel customer = (CustomerModel) currentObject;
			final List<CustomerCouponModel> validCoupons = customer.getCustomerCoupons().stream()
					.filter(coupon -> Objects.isNull(coupon.getEndDate()) || !new DateTime(coupon.getEndDate()).isBeforeNow())
					.collect(Collectors.toList());
			customer.setCustomerCoupons(validCoupons);
			getModelService().save(customer);

		}
		else if (currentObject instanceof CustomerCouponModel)
		{
			final CustomerCouponModel coupon = (CustomerCouponModel) currentObject;
			if (Objects.nonNull(coupon.getEndDate()) && new DateTime(coupon.getEndDate()).isBeforeNow())
			{
				coupon.setCustomers(null);
				getModelService().save(coupon);
			}
		}
		else
		{
			LOG.debug("Do Nothing for other instances!");

		}
		super.beforeEditorAreaRender(widgetInstanceManager, currentObject);
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
}

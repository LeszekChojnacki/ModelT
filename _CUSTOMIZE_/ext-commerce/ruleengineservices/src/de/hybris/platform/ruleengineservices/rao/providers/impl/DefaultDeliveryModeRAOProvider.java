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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import static java.util.stream.Collectors.toSet;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.ruleengineservices.calculation.DeliveryCostEvaluationStrategy;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultDeliveryModeRAOProvider implements RAOProvider
{
	private DeliveryModeDao deliveryModeDao;
	private DeliveryCostEvaluationStrategy deliveryCostEvaluationStrategy;
	private Converter<DeliveryModeModel, DeliveryModeRAO> deliveryModeRaoConverter;

	@Override
	public Set expandFactModel(final Object modelFact)
	{
		if (modelFact instanceof AbstractOrderModel)
		{
			final AbstractOrderModel orderModel = (AbstractOrderModel) modelFact;
			final Collection<DeliveryModeModel> availableDeliveryModes = getDeliveryModeDao().findAllDeliveryModes();
			if (CollectionUtils.isNotEmpty(availableDeliveryModes))
			{
				return availableDeliveryModes.stream().map(dm -> {
					final DeliveryModeRAO deliveryModeRao = getDeliveryModeRaoConverter().convert(dm);
					final BigDecimal cost = getDeliveryCostEvaluationStrategy().evaluateCost(orderModel, dm);
					deliveryModeRao.setCost(cost);
					deliveryModeRao.setCurrencyIsoCode(orderModel.getCurrency().getIsocode());
					return deliveryModeRao;
				}).collect(toSet());
			}
		}
		return Collections.emptySet();
	}



	protected DeliveryModeDao getDeliveryModeDao()
	{
		return deliveryModeDao;
	}

	@Required
	public void setDeliveryModeDao(final DeliveryModeDao deliveryModeDao)
	{
		this.deliveryModeDao = deliveryModeDao;
	}

	@Required
	public void setDeliveryModeRaoConverter(final Converter<DeliveryModeModel, DeliveryModeRAO> deliveryModeRaoConverter)
	{
		this.deliveryModeRaoConverter = deliveryModeRaoConverter;
	}

	protected Converter<DeliveryModeModel, DeliveryModeRAO> getDeliveryModeRaoConverter()
	{
		return deliveryModeRaoConverter;
	}

	protected DeliveryCostEvaluationStrategy getDeliveryCostEvaluationStrategy()
	{
		return deliveryCostEvaluationStrategy;
	}

	@Required
	public void setDeliveryCostEvaluationStrategy(final DeliveryCostEvaluationStrategy deliveryCostEvaluationStrategy)
	{
		this.deliveryCostEvaluationStrategy = deliveryCostEvaluationStrategy;
	}
}

/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.renderers;

import de.hybris.platform.payment.jalo.PaymentTransactionEntry;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Listcell;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * This renderer renders the {@link PaymentTransactionEntry#AMOUNT}
 */
public class PaymentTransactionAmountRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(PaymentTransactionAmountRenderer.class);
	protected static final String PAYMENT_TRANSCTION = "PaymentTransaction";

	private TypeFacade typeFacade;
	private PropertyValueService propertyValueService;
	private LabelService labelService;
	private PermissionFacade permissionFacade;

	@Override
	public void render(final Listcell listcell, final ListColumn columnConfiguration, final Object object, DataType unused,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final String qualifier = columnConfiguration.getQualifier();
		try
		{
			final DataType dataType = getTypeFacade().load(PAYMENT_TRANSCTION);
			if (dataType == null || getPermissionFacade().canReadProperty(dataType.getCode(), qualifier))
			{
				final Object value = getPropertyValueService().readValue(object, qualifier);
				final BigDecimal paymentTransactionAmount = ((BigDecimal) value).setScale(
						((PaymentTransactionModel) object).getEntries().get(0).getCurrency().getDigits().intValue(),
						BigDecimal.ROUND_HALF_DOWN);
				String amount = getLabelService().getObjectLabel(paymentTransactionAmount);
				if (StringUtils.isBlank(amount))
				{
					amount = value.toString();
				}
				listcell.setLabel(amount);
			}
		}
		catch (final TypeNotFoundException e)
		{
			LOG.error("Could not render row......", e);
		}
	}

	protected PropertyValueService getPropertyValueService()
	{
		return propertyValueService;
	}

	@Required
	public void setPropertyValueService(final PropertyValueService propertyValueService)
	{
		this.propertyValueService = propertyValueService;
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	protected PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}
}

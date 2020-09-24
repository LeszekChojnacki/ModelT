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

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.returns.model.RefundEntryModel;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Listcell;

import java.math.BigDecimal;


/**
 * This renderer renders the price formatted
 */
public class PriceRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(PriceRenderer.class);

	private TypeFacade typeFacade;
	private PropertyValueService propertyValueService;
	private LabelService labelService;
	private PermissionFacade permissionFacade;
	private String myEntry;

	@Override
	public void render(final Listcell listcell, final ListColumn columnConfiguration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final String qualifier = columnConfiguration.getQualifier();
		try
		{
			final DataType objectDataType = getTypeFacade().load(getMyEntry());
			if (objectDataType != null && !getPermissionFacade().canReadProperty(objectDataType.getCode(), qualifier))
			{
				return;
			}
			final Object value = getPropertyValueService().readValue(object, qualifier);
			if (value == null)
			{
				return;
			}
			BigDecimal amountValue;
			if (value instanceof Double)
			{
				amountValue = BigDecimal.valueOf((Double) value);
			}
			else
			{
				amountValue = (BigDecimal) value;
			}
			final BigDecimal entryAmount = amountValue.setScale(getDigitsNumber(object), BigDecimal.ROUND_HALF_DOWN);
			String price = getLabelService().getObjectLabel(entryAmount);
			if (StringUtils.isBlank(price))
			{
				price = value.toString();
			}
			listcell.setLabel(price);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.info("Could not render row.", e);
		}
	}

	/**
	 * Retrieves the number of digits to display for the amount. Default number is 2
	 *
	 * @param object
	 * 		the object from which we can define how many digits to display
	 * @return the number of digits to display
	 */
	protected int getDigitsNumber(final Object object)
	{
		int digitsNumber = 2;
		if (object instanceof RefundEntryModel)
		{
			digitsNumber = ((RefundEntryModel) object).getOrderEntry().getOrder().getCurrency().getDigits();
		}
		else if (object instanceof AbstractOrderEntryModel)
		{
			digitsNumber = ((AbstractOrderEntryModel) object).getOrder().getCurrency().getDigits();
		}
		else if (object instanceof PaymentTransactionEntryModel)
		{
			digitsNumber = ((PaymentTransactionEntryModel) object).getPaymentTransaction().getOrder().getCurrency().getDigits();
		}
		return digitsNumber;
	}

	protected String getMyEntry()
	{
		return myEntry;
	}

	@Required
	public void setMyEntry(final String myEntry)
	{
		this.myEntry = myEntry;
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

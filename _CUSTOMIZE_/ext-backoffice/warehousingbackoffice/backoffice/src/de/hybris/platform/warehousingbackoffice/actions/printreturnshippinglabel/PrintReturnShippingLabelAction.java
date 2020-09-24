/*
 * [y] hybris Platform
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.warehousingbackoffice.actions.printreturnshippinglabel;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import javax.annotation.Resource;

import java.util.NoSuchElementException;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action responsible for generating a {@link ConsignmentModel#RETURNLABEL}
 */
public class PrintReturnShippingLabelAction implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(PrintReturnShippingLabelAction.class);

	protected static final String CAN_PERFORM_PROP_KEY = "warehousing.printreturnshippinglabel.active";

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintReturnShippingLabelStrategy;
	@Resource
	private ConfigurationService configurationService;

	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final ConsignmentModel consignment = consignmentModelActionContext.getData();
		LOG.info("Generate return shipping label for consignment {}", consignment.getCode());

		getConsignmentPrintReturnShippingLabelStrategy().printDocument(consignment);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		boolean result = false;
		final Configuration configuration = getConfigurationService().getConfiguration();
		try
		{
			if (configuration != null)
			{
				result = configuration.getBoolean(CAN_PERFORM_PROP_KEY);
			}
		}
		catch (final ConversionException | NoSuchElementException e)//NOSONAR
		{
			LOG.error(String.format(
					"No or incorrect property defined for [%s]. Value has to be 'true' or 'false' - any other value will be treated as a false",
					CAN_PERFORM_PROP_KEY));//NOSONAR
		}


		return result && (consignmentModelActionContext.getData() != null) && (
				consignmentModelActionContext.getData().getFulfillmentSystemConfig() == null);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return null;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintReturnShippingLabelStrategy()
	{
		return consignmentPrintReturnShippingLabelStrategy;
	}
}

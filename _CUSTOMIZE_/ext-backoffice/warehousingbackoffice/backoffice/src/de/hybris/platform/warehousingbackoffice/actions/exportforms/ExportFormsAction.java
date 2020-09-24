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
package de.hybris.platform.warehousingbackoffice.actions.exportforms;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Backoffice action displaying popup with export form for current consignment
 */
public class ExportFormsAction implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(ExportFormsAction.class);

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintExportFormStrategy;

	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{

		final ConsignmentModel consignment = consignmentModelActionContext.getData();

		LOG.info("Generating Export Form for consignment {}", consignment.getCode());

		getConsignmentPrintExportFormStrategy().printDocument(consignment);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return (consignmentModelActionContext.getData() != null) && (
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

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintExportFormStrategy()
	{
		return consignmentPrintExportFormStrategy;
	}
}

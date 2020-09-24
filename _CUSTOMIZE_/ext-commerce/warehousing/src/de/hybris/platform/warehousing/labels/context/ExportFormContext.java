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
package de.hybris.platform.warehousing.labels.context;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.warehousing.data.shipping.ExportForm;
import de.hybris.platform.warehousing.data.shipping.ExportFormEntry;
import de.hybris.platform.warehousing.labels.strategy.ExportFormPriceStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Context specific to Export Form providing methods for extracting item price and total price of the products
 */
public class ExportFormContext extends CommonPrintLabelContext
{
	private ExportFormPriceStrategy exportFormPriceStrategy;
	private ExportForm exportForm;

	@Override
	public void init(final ConsignmentProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel, documentPageModel);
		setExportForm(createExportFormContent(businessProcessModel.getConsignment()));
	}

	/**
	 * Creates {@link ExportForm} object with item and total prices calculated according to
	 * {@link ExportFormPriceStrategy}
	 *
	 * @param consignment
	 *           the {@link ConsignmentModel} to retrieve entries from
	 * @return {@link ExportForm} object with calculated price values or null, if the consignment doesn't contain any
	 *         entries
	 */
	protected ExportForm createExportFormContent(final ConsignmentModel consignment)
	{
		validateParameterNotNull(consignment, "Consignment cannot be null");
		ExportForm exportFormContent = null;

		if (CollectionUtils.isNotEmpty(consignment.getConsignmentEntries()))
		{
			final List<ExportFormEntry> exportFormEntries = consignment.getConsignmentEntries().stream()
					.map(this::createExportFormEntry).collect(Collectors.toList());
			final BigDecimal totalPrice = exportFormEntries.stream().map(ExportFormEntry::getTotalPrice).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			exportFormContent = new ExportForm();
			exportFormContent.setFormEntries(exportFormEntries);
			exportFormContent.setTotalPrice(totalPrice);
		}

		return exportFormContent;
	}

	/**
	 * Creates {@link ExportFormEntry} with unit price and total price calculated according to
	 * {@link ExportFormPriceStrategy}, based on {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntry
	 *           the {@link ConsignmentEntryModel} to calculate prices for
	 * @return {@link ExportFormEntry} with proper data
	 */
	protected ExportFormEntry createExportFormEntry(final ConsignmentEntryModel consignmentEntry)
	{
		validateParameterNotNull(consignmentEntry, "Consignment entry cannot be null");

		final ExportFormEntry entry = new ExportFormEntry();
		entry.setConsignmentEntry(consignmentEntry);
		entry.setItemPrice(getExportFormPriceStrategy().calculateProductPrice(consignmentEntry));
		entry.setTotalPrice(getExportFormPriceStrategy().calculateTotalPrice(entry.getItemPrice(), consignmentEntry));

		return entry;
	}

	protected ExportFormPriceStrategy getExportFormPriceStrategy()
	{
		return exportFormPriceStrategy;
	}

	@Required
	public void setExportFormPriceStrategy(final ExportFormPriceStrategy exportFormPriceStrategy)
	{
		this.exportFormPriceStrategy = exportFormPriceStrategy;
	}

	public ExportForm getExportForm()
	{
		return exportForm;
	}

	public void setExportForm(final ExportForm exportForm)
	{
		this.exportForm = exportForm;
	}
}

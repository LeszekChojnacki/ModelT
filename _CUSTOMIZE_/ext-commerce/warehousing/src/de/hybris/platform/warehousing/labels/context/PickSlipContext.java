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
package de.hybris.platform.warehousing.labels.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.AllocationEventModel;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Context specific to PickLabel providing additional methods for extracting bincode and image url of the product
 */
public class PickSlipContext extends CommonPrintLabelContext
{
	private Set<ConsignmentEntryModel> consignmentEntries;
	private AddressModel shippingAddress;

	private InventoryEventService inventoryEventService;

	@Override
	public void init(final ConsignmentProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel, documentPageModel);
		final ConsignmentModel consignment = businessProcessModel.getConsignment();
		consignmentEntries = consignment.getConsignmentEntries();
		shippingAddress = consignment.getShippingAddress();
	}

	/**
	 * Extracts the bin location of the product for the selected consignmentEntry
	 *
	 * @param consignmentEntryModel
	 * 		the consignment model for which we request the Pick Slip label
	 * @return The binCode location of the product
	 */
	public String extractBin(final ConsignmentEntryModel consignmentEntryModel)
	{
		String binLocation = "";
		final StringBuilder bins = new StringBuilder();

		final Collection<AllocationEventModel> events = inventoryEventService
				.getAllocationEventsForConsignmentEntry(consignmentEntryModel);
		events.stream().filter(e -> e.getStockLevel().getBin() != null)
				.forEach(e -> bins.append(e.getStockLevel().getBin()).append(","));

		if (bins.length() > 0)
		{
			binLocation = bins.substring(0, bins.length() - 1);
		}

		return binLocation;
	}

	/**
	 * Extracts the image url for the thumbnail of the product for the selected consignmentEntry
	 *
	 * @param consignmentEntryModel
	 * 		the consignment model for which we request the Pick Slip label
	 * @return the source url for the product thumbnail (expecting the relative path as a string)
	 */
	public String getProductImageURL(final ConsignmentEntryModel consignmentEntryModel)
	{
		String path = null;
		final MediaModel media = consignmentEntryModel.getOrderEntry().getProduct().getThumbnail();
		if (media != null)
		{
			path = media.getDownloadURL();
		}
		return path;
	}

	public AddressModel getShippingAddress()
	{
		return shippingAddress;
	}

	public Set<ConsignmentEntryModel> getConsignmentEntries()
	{
		return consignmentEntries;
	}

	protected InventoryEventService getInventoryEventService()
	{
		return inventoryEventService;
	}

	@Required
	public void setInventoryEventService(final InventoryEventService inventoryEventService)
	{
		this.inventoryEventService = inventoryEventService;
	}

}

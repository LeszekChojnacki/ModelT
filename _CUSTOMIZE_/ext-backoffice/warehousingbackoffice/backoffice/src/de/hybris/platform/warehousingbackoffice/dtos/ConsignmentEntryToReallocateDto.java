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
package de.hybris.platform.warehousingbackoffice.dtos;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.enums.DeclineReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.zkoss.zul.ListModelArray;


/**
 * DTO used to display consignment entry to reallocate.
 */
public class ConsignmentEntryToReallocateDto
{
	private ConsignmentEntryModel consignmentEntry = null;
	private Long quantityToReallocate;
	private String declineConsignmentEntryComment;
	private DeclineReason selectedReason;
	private WarehouseModel selectedLocation;

	private ListModelArray declineReasonsModel = new ListModelArray(new ArrayList<>());
	private ListModelArray possibleLocationsModel = new ListModelArray(new ArrayList<>());

	public ConsignmentEntryToReallocateDto(final ConsignmentEntryModel consignmentEntry, final List<String> reasons,
			final Set<WarehouseModel> locations)
	{
		this.consignmentEntry = consignmentEntry;
		this.quantityToReallocate = 0L;

		declineReasonsModel = new ListModelArray(reasons);
		possibleLocationsModel = new ListModelArray(locations.toArray());
	}

	public ConsignmentEntryModel getConsignmentEntry()
	{
		return consignmentEntry;
	}

	public void setConsignmentEntry(final ConsignmentEntryModel consignmentEntry)
	{
		this.consignmentEntry = consignmentEntry;
	}

	public Long getQuantityToReallocate()
	{
		return quantityToReallocate;
	}

	public void setQuantityToReallocate(final Long quantityToReallocate)
	{
		this.quantityToReallocate = quantityToReallocate;
	}

	public ListModelArray getDeclineReasonsModel()
	{
		return declineReasonsModel;
	}

	public void setDeclineReasonsModel(final ListModelArray declineReasonsModel)
	{
		this.declineReasonsModel = declineReasonsModel;
	}

	public ListModelArray getPossibleLocationsModel()
	{
		return possibleLocationsModel;
	}

	public void setPossibleLocationsModel(final ListModelArray possibleLocationsModel)
	{
		this.possibleLocationsModel = possibleLocationsModel;
	}

	public DeclineReason getSelectedReason()
	{
		return selectedReason;
	}

	public void setSelectedReason(final DeclineReason selectedReason)
	{
		this.selectedReason = selectedReason;
	}

	public WarehouseModel getSelectedLocation()
	{
		return selectedLocation;
	}

	public void setSelectedLocation(final WarehouseModel selectedLocation)
	{
		this.selectedLocation = selectedLocation;
	}

	public String getDeclineConsignmentEntryComment()
	{
		return declineConsignmentEntryComment;
	}

	public void setDeclineConsignmentEntryComment(final String declineConsignmentEntryComment)
	{
		this.declineConsignmentEntryComment = declineConsignmentEntryComment;
	}
}

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

import de.hybris.platform.warehousing.enums.StockLevelAdjustmentReason;

import org.zkoss.zul.ListModelArray;

import java.util.ArrayList;
import java.util.List;


/**
 * DTO used to display stock level adjustment to create.
 */
public class StockAdjustmentDto
{
	private StockLevelAdjustmentReason selectedReason;
	private String localizedStringReason;
	private Long quantity;
	private String comment;
	private Boolean underEdition;

	private ListModelArray stockAdjustmentReasonsModel = new ListModelArray(new ArrayList<>());

	public StockAdjustmentDto(final List<String> reasons)
	{
		quantity = 0L;
		underEdition = Boolean.TRUE;
		stockAdjustmentReasonsModel = new ListModelArray(reasons);
	}

	public ListModelArray getStockAdjustmentReasonsModel()
	{
		return stockAdjustmentReasonsModel;
	}

	public void setStockAdjustmentReasonsModel(final ListModelArray stockAdjustmentReasonsModel)
	{
		this.stockAdjustmentReasonsModel = stockAdjustmentReasonsModel;
	}

	public StockLevelAdjustmentReason getSelectedReason()
	{
		return selectedReason;
	}

	public void setSelectedReason(final StockLevelAdjustmentReason selectedReason)
	{
		this.selectedReason = selectedReason;
	}

	public Long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(final Long quantity)
	{
		this.quantity = quantity;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(final String comment)
	{
		this.comment = comment;
	}


	public Boolean getUnderEdition()
	{
		return underEdition;
	}

	public void setUnderEdition(final Boolean underEdition)
	{
		this.underEdition = underEdition;
	}

	public String getLocalizedStringReason()
	{
		return localizedStringReason;
	}

	public void setLocalizedStringReason(String localizedStringReason)
	{
		this.localizedStringReason = localizedStringReason;
	}
}

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
package de.hybris.platform.omsbackoffice.widgets.returns.dtos;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.returns.model.RefundEntryModel;

import org.zkoss.zul.ListModelArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * DTO used to display return entry to create.
 */
public class ReturnEntryToCreateDto
{
	private RefundEntryModel refundEntry = null;
	private int returnableQuantity;
	private int quantityToReturn;
	private String refundEntryComment;
	private boolean discountApplied;
	private BigDecimal tax;

	private ListModelArray reasonsModel = new ListModelArray(new ArrayList<>());

	public ReturnEntryToCreateDto(final AbstractOrderEntryModel orderEntry, final int returnableQuantity,
			final List<String> reasons)
	{
		final RefundEntryModel defaultRefundEntry = new RefundEntryModel();
		defaultRefundEntry.setOrderEntry(orderEntry);
		defaultRefundEntry.setAmount(BigDecimal.ZERO);
		setRefundEntry(defaultRefundEntry); //NOSONAR
		setReturnableQuantity(returnableQuantity); //NOSONAR
		setQuantityToReturn(0); //NOSONAR
		setReasonsModel(new ListModelArray(reasons)); //NOSONAR

		setDiscountApplied(orderEntry.getDiscountValues() != null && orderEntry.getDiscountValues().size() > 0); //NOSONAR
	}

	public RefundEntryModel getRefundEntry()
	{
		return refundEntry;
	}

	public void setRefundEntry(final RefundEntryModel refundEntry)
	{
		this.refundEntry = refundEntry;
	}

	public String getRefundEntryComment()
	{
		return refundEntryComment;
	}

	public void setRefundEntryComment(final String refundEntryComment)
	{
		this.refundEntryComment = refundEntryComment;
	}

	public ListModelArray getReasonsModel()
	{
		return reasonsModel;
	}

	public void setReasonsModel(final ListModelArray reasonsModel)
	{
		this.reasonsModel = reasonsModel;
	}

	public int getReturnableQuantity()
	{
		return returnableQuantity;
	}

	public void setReturnableQuantity(final int returnableQuantity)
	{
		this.returnableQuantity = returnableQuantity;
	}

	public int getQuantityToReturn()
	{
		return quantityToReturn;
	}

	public void setQuantityToReturn(final int quantityToReturn)
	{
		this.quantityToReturn = quantityToReturn;
	}

	public boolean isDiscountApplied()
	{
		return discountApplied;
	}

	public void setDiscountApplied(final boolean discountApplied)
	{
		this.discountApplied = discountApplied;
	}

	public BigDecimal getTax()
	{
		return tax;
	}

	public void setTax(final BigDecimal tax)
	{
		this.tax = tax;
	}
}

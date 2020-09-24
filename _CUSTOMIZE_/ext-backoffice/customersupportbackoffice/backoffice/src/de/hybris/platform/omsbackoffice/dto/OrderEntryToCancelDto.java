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
package de.hybris.platform.omsbackoffice.dto;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zul.ListModelArray;


/**
 * DTO used to display order entries for cancellation.
 */
public class OrderEntryToCancelDto implements Comparable<OrderEntryToCancelDto>
{
	private AbstractOrderEntryModel orderEntry;
	private Long quantityToCancel;
	private Long quantityAvailableToCancel;
	private String cancelOrderEntryComment;
	private CancelReason selectedReason;
	private String deliveryModeName;

	private ListModelArray<String> cancelReasonsModel = new ListModelArray<>(new ArrayList<>());

	public OrderEntryToCancelDto(final AbstractOrderEntryModel orderEntry, final List<String> reasons,
			final Long quantityAvailableToCancel, final String deliveryModeName)
	{
		this.orderEntry = orderEntry;
		this.quantityToCancel = Long.valueOf(0L);
		this.quantityAvailableToCancel = quantityAvailableToCancel;
		this.cancelReasonsModel = new ListModelArray<>(reasons);
		this.deliveryModeName = deliveryModeName;
	}

	@Override
	public int compareTo(final OrderEntryToCancelDto orderEntryToCancelDto)
	{
		return Long.compare(this.getOrderEntry().getProduct().getPk().getLong(), orderEntryToCancelDto.getOrderEntry().getProduct().getPk().getLong());
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final OrderEntryToCancelDto that = (OrderEntryToCancelDto) o;

		if ((orderEntry != null) ? !orderEntry.equals(that.orderEntry) : (that.orderEntry != null))
		{
			return false;
		}
		if ((quantityToCancel != null) ? !quantityToCancel.equals(that.quantityToCancel) : (that.quantityToCancel != null))
		{
			return false;
		}
		if ((quantityAvailableToCancel != null) ? !quantityAvailableToCancel.equals(that.quantityAvailableToCancel)
				: (that.quantityAvailableToCancel != null))
		{
			return false;
		}
		if ((cancelOrderEntryComment != null) ? !cancelOrderEntryComment.equals(that.cancelOrderEntryComment)
				: (that.cancelOrderEntryComment != null))
		{
			return false;
		}
		if ((selectedReason != null) ? !selectedReason.equals(that.selectedReason) : (that.selectedReason != null))
		{
			return false;
		}
		if ((deliveryModeName != null) ? !deliveryModeName.equals(that.deliveryModeName) : (that.deliveryModeName != null))
		{
			return false;
		}
		return (cancelReasonsModel != null) ? cancelReasonsModel.equals(that.cancelReasonsModel) : (that.cancelReasonsModel == null);
	}

	@Override
	public int hashCode()
	{
		int result = orderEntry != null ? orderEntry.hashCode() : 0;
		result = 31 * result + (quantityToCancel != null ? quantityToCancel.hashCode() : 0);
		result = 31 * result + (quantityAvailableToCancel != null ? quantityAvailableToCancel.hashCode() : 0);
		result = 31 * result + (cancelOrderEntryComment != null ? cancelOrderEntryComment.hashCode() : 0);
		result = 31 * result + (selectedReason != null ? selectedReason.hashCode() : 0);
		result = 31 * result + (deliveryModeName != null ? deliveryModeName.hashCode() : 0);
		result = 31 * result + (cancelReasonsModel != null ? cancelReasonsModel.hashCode() : 0);
		return result;
	}

	public void setCancelReasonsModel(final ListModelArray<String> cancelReasonsModel)
	{
		this.cancelReasonsModel = cancelReasonsModel;
	}

	public AbstractOrderEntryModel getOrderEntry()
	{
		return orderEntry;
	}

	public void setOrderEntry(final AbstractOrderEntryModel orderEntry)
	{
		this.orderEntry = orderEntry;
	}

	public CancelReason getSelectedReason()
	{
		return selectedReason;
	}

	public void setSelectedReason(final CancelReason selectedReason)
	{
		this.selectedReason = selectedReason;
	}

	public ListModelArray<String> getCancelReasonsModel()
	{
		return cancelReasonsModel;
	}

	public String getCancelOrderEntryComment()
	{
		return cancelOrderEntryComment;
	}

	public void setCancelOrderEntryComment(final String cancelOrderEntryComment)
	{
		this.cancelOrderEntryComment = cancelOrderEntryComment;
	}

	public void setQuantityToCancel(final Long quantityToCancel)
	{
		this.quantityToCancel = quantityToCancel;
	}

	public Long getQuantityToCancel()
	{
		return quantityToCancel;
	}

	public Long getQuantityAvailableToCancel()
	{
		return quantityAvailableToCancel;
	}

	public void setQuantityAvailableToCancel(final Long quantityAvailableToCancel)
	{
		this.quantityAvailableToCancel = quantityAvailableToCancel;
	}

	public String getDeliveryModeName()
	{
		return deliveryModeName;
	}

	public void setDeliveryModeName(final String deliveryModeName)
	{
		this.deliveryModeName = deliveryModeName;
	}
}

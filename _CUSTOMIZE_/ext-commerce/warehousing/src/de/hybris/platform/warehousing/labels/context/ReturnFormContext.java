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
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.warehousing.data.shipping.ReturnForm;
import de.hybris.platform.warehousing.data.shipping.ReturnFormEntry;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.velocity.tools.generic.NumberTool;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Context specific to Return Form providing methods for extracting returned items.
 */
public class ReturnFormContext extends CommonReturnDocumentContext
{
	private ReturnForm returnForm;
	private NumberTool number;
	private boolean showQuantityPurchased;

	@Override
	public void init(final BusinessProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel, documentPageModel);

		setNumber(new NumberTool());

		if (businessProcessModel instanceof ConsignmentProcessModel)
		{
			final ConsignmentModel consignment = ((ConsignmentProcessModel) businessProcessModel).getConsignment();
			setReturnForm(createReturnFormForConsignment(consignment));
			setShowQuantityPurchased(true);
		}
		else
		{
			final ReturnRequestModel returnRequest = ((ReturnProcessModel) businessProcessModel).getReturnRequest();
			setReturnForm(createReturnFormForReturnRequest(returnRequest));
			setShowQuantityPurchased(false);
		}
	}

	/**
	 * Creates {@link ReturnForm} which holds lists of {@link ReturnFormEntry}(s)
	 *
	 * @param consignment
	 * 		the given {@link ConsignmentModel} from which {@link ReturnFormEntry}(s) need to be retrieved
	 * @return {@link ReturnForm}. <br>
	 * Or null, if given {@link ConsignmentModel} does not have any {@link ConsignmentEntryModel}.
	 */
	protected ReturnForm createReturnFormForConsignment(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);
		validateParameterNotNullStandardMessage("consignmentEntries", consignment.getConsignmentEntries());

		ReturnForm newReturnForm = null;
		if (CollectionUtils.isNotEmpty(consignment.getConsignmentEntries()))
		{
			newReturnForm = new ReturnForm();
			newReturnForm.setFormEntries(consignment.getConsignmentEntries().stream().filter(Objects::nonNull)
					.map(entry -> createReturnFormEntry(entry.getOrderEntry(), entry.getQuantity(), null))
					.collect(Collectors.toList()));
		}
		return newReturnForm;
	}


	/**
	 * Creates {@link ReturnForm} which holds lists of {@link ReturnFormEntry}(s)
	 *
	 * @param returnRequest
	 * 		the given {@link ReturnRequestModel} from which {@link ReturnFormEntry}(s) need to be retrieved
	 * @return {@link ReturnForm}. <br>
	 * Or null, if given {@link ReturnRequestModel} does not have any {@link de.hybris.platform.returns.model.ReturnEntryModel}(s).
	 */
	protected ReturnForm createReturnFormForReturnRequest(final ReturnRequestModel returnRequest)
	{
		validateParameterNotNullStandardMessage("returnRequest", returnRequest);
		validateParameterNotNullStandardMessage("returnEntries", returnRequest.getReturnEntries());

		ReturnForm newReturnForm = null;
		if (CollectionUtils.isNotEmpty(returnRequest.getReturnEntries()))
		{
			newReturnForm = new ReturnForm();
			newReturnForm.setFormEntries(returnRequest.getReturnEntries().stream().filter(Objects::nonNull)
					.map(entry -> createReturnFormEntry(entry.getOrderEntry(), null, entry.getExpectedQuantity()))
					.collect(Collectors.toList()));
		}
		return newReturnForm;
	}

	/**
	 * Creates {@link ReturnFormEntry} from given {@link AbstractOrderEntryModel}
	 *
	 * @param orderEntry
	 * 		the {@link AbstractOrderEntryModel} which need to be transformed to {@link ReturnFormEntry}
	 * @param purchasedQuantity
	 * 		quantity of shipped items
	 * @param returnedQuantity
	 * 		quantity of items being returned
	 * @return {@link ReturnFormEntry}
	 */
	protected ReturnFormEntry createReturnFormEntry(final AbstractOrderEntryModel orderEntry, final Long purchasedQuantity,
			final Long returnedQuantity)
	{
		validateParameterNotNullStandardMessage("orderEntry", orderEntry);
		validateParameterNotNullStandardMessage("order", orderEntry.getOrder());

		final ReturnFormEntry entry = new ReturnFormEntry();
		entry.setProduct(orderEntry.getProduct());
		entry.setBasePrice(orderEntry.getBasePrice());
		entry.setQuantityPurchased(purchasedQuantity);
		entry.setQuantityReturned(returnedQuantity);
		return entry;
	}

	public void setReturnForm(final ReturnForm returnForm)
	{
		this.returnForm = returnForm;
	}

	public void setNumber(final NumberTool number)
	{
		this.number = number;
	}

	public void setShowQuantityPurchased(final boolean flag)
	{
		this.showQuantityPurchased = flag;
	}

	public ReturnForm getReturnForm()
	{
		return this.returnForm;
	}

	public NumberTool getNumber()
	{
		return number;
	}

	public boolean getShowQuantityPurchased()
	{
		return showQuantityPurchased;
	}
}

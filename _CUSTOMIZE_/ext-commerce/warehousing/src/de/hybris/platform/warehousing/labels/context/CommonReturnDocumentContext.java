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

import de.hybris.platform.acceleratorservices.document.context.AbstractDocumentContext;
import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.sourcing.context.PosSelectionStrategy;

import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Common context for velocity templates related to return form attached to either {@link ConsignmentModel} or {@link ReturnRequestModel}
 */
public class CommonReturnDocumentContext extends AbstractDocumentContext<BusinessProcessModel>
{
	private DateTool date;
	private AbstractOrderModel order;
	private AddressModel address;
	private PosSelectionStrategy posSelectionStrategy;
	private EscapeTool escapeTool;

	@Override
	public void init(final BusinessProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		validateProcessType(businessProcessModel);

		super.init(businessProcessModel, documentPageModel);
		setDate(new DateTool());

		final AbstractOrderModel orderForProcess = getOrder(businessProcessModel);

		final WarehouseModel warehouse;
		if (businessProcessModel instanceof ConsignmentProcessModel)
		{
			warehouse = ((ConsignmentProcessModel) businessProcessModel).getConsignment().getWarehouse();
		}
		else
		{
			warehouse = ((ReturnProcessModel) businessProcessModel).getReturnRequest().getReturnWarehouse();
		}

		setAddress(getPosAddress(orderForProcess, warehouse));
		setOrder(orderForProcess);
		this.escapeTool = new EscapeTool();
	}

	/**
	 * Escapes String to generate safe HTML
	 *
	 * @param stringToEscape
	 * 		String to escape
	 * @return escaped String
	 */
	public String escapeHtml(final String stringToEscape)
	{
		return escapeTool.html(stringToEscape);
	}

	/**
	 * Extracts the {@link AddressModel} where the returned goods need to be shipped by the customer
	 *
	 * @param order
	 * 		the {@link AbstractOrderModel} for which address needs to be calculated
	 * @param returnWarehouse
	 * 		the {@link WarehouseModel} for which address needs to be extracted
	 * @return the {@link AddressModel} of {@link PointOfServiceModel}
	 */
	protected AddressModel getPosAddress(final AbstractOrderModel order, final WarehouseModel returnWarehouse)
	{
		validateParameterNotNullStandardMessage("order", order);
		validateParameterNotNullStandardMessage("returnWarehouse", returnWarehouse);

		final PointOfServiceModel pointOfService = getPosSelectionStrategy().getPointOfService(order, returnWarehouse);
		AddressModel posAddress = null;
		if (pointOfService != null && pointOfService.getAddress() != null)
		{
			posAddress = pointOfService.getAddress();
		}
		return posAddress;
	}

	/**
	 * Get {@link AbstractOrderModel} from given {@link BusinessProcessModel}
	 *
	 * @param businessProcessModel
	 * 		the {@link BusinessProcessModel}, for which {@link AbstractOrderModel} needs be retrieved
	 * @return {@link AbstractOrderModel} assigned to given {@link BusinessProcessModel}
	 */
	protected AbstractOrderModel getOrder(final BusinessProcessModel businessProcessModel)
	{
		validateProcessType(businessProcessModel);

		final AbstractOrderModel orderForProcess;
		if (businessProcessModel instanceof ConsignmentProcessModel)
		{
			final ConsignmentModel consignment = ((ConsignmentProcessModel) businessProcessModel).getConsignment();
			validateParameterNotNullStandardMessage("consignment", consignment);
			orderForProcess = consignment.getOrder();
		}
		else
		{
			final ReturnRequestModel returnRequest = ((ReturnProcessModel) businessProcessModel).getReturnRequest();
			validateParameterNotNullStandardMessage("returnRequest", returnRequest);
			orderForProcess = returnRequest.getOrder();
		}

		return orderForProcess;
	}

	@Override
	protected BaseSiteModel getSite(final BusinessProcessModel businessProcessModel)
	{
		final AbstractOrderModel orderForProcess = getOrder(businessProcessModel);
		validateParameterNotNullStandardMessage("orderForProcess", orderForProcess);
		return orderForProcess.getSite();
	}

	@Override
	protected LanguageModel getDocumentLanguage(final BusinessProcessModel businessProcessModel)
	{
		final BaseSiteModel site = getSite(businessProcessModel);
		validateParameterNotNullStandardMessage("site", site);
		return site.getDefaultLanguage();
	}

	/**
	 * Validates that given {@link BusinessProcessModel} is either instance of {@link ConsignmentProcessModel} or {@link ReturnProcessModel}
	 *
	 * @param businessProcessModel
	 * 		the {@link BusinessProcessModel} to be validated
	 */
	protected void validateProcessType(final BusinessProcessModel businessProcessModel)
	{
		if (!(businessProcessModel instanceof ConsignmentProcessModel) && !(businessProcessModel instanceof ReturnProcessModel))
		{
			throw new IllegalArgumentException(
					"businessProcessModel is not an instance of ConsignmentProcessModel nor ReturnProcessModel");
		}
	}

	public void setDate(final DateTool date)
	{
		this.date = date;
	}

	public void setOrder(final AbstractOrderModel order)
	{
		this.order = order;
	}

	public void setAddress(final AddressModel address)
	{
		this.address = address;
	}

	public AbstractOrderModel getOrder()
	{
		return order;
	}

	public DateTool getDate()
	{
		return date;
	}

	public AddressModel getAddress()
	{
		return address;
	}

	protected PosSelectionStrategy getPosSelectionStrategy()
	{
		return posSelectionStrategy;
	}

	@Required
	public void setPosSelectionStrategy(final PosSelectionStrategy posSelectionStrategy)
	{
		this.posSelectionStrategy = posSelectionStrategy;
	}
}

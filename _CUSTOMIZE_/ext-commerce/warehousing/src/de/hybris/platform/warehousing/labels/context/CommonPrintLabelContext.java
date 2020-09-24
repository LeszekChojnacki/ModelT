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
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;

import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.NumberTool;


/**
 * Default context passed to velocity to populate the template
 */
public class CommonPrintLabelContext extends AbstractDocumentContext<ConsignmentProcessModel>
{
	private OrderModel order;
	private ConsignmentModel consignment;
	private DateTool date;
	private NumberTool number;
	private EscapeTool escapeTool;

	/**
	 * Initialization of the model passed in parameter
	 *
	 * @param businessProcessModel
	 * @param documentPageModel
	 */
	@Override
	public void init(final ConsignmentProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel,documentPageModel);
		final ConsignmentModel currentConsignment = businessProcessModel.getConsignment();
		this.order = (OrderModel) currentConsignment.getOrder();
		this.consignment = currentConsignment;
		this.date = new DateTool();
		this.number = new NumberTool();
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

	@Override
	protected BaseSiteModel getSite(final ConsignmentProcessModel consignmentProcessModel)
	{
		return consignmentProcessModel.getConsignment().getOrder().getSite();
	}

	@Override
	protected LanguageModel getDocumentLanguage(final ConsignmentProcessModel businessProcessModel)
	{
		return businessProcessModel.getConsignment().getOrder().getSite().getDefaultLanguage();
	}

	/**
	 * @return the date
	 */
	public DateTool getDate() {
		return date;
	}

	/**
	 * @return the order
	 */
	public OrderModel getOrder() {
		return order;
	}

	/**
	 * @return the consignment
	 */
	public ConsignmentModel getConsignment() {
		return consignment;
	}

	public NumberTool getNumber()
	{
		return number;
	}

}

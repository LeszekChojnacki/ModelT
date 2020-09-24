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
package de.hybris.platform.customersupportbackoffice.dataaccess.facades.type;

import com.google.common.base.Preconditions;
import com.hybris.backoffice.cockpitng.dataaccess.facades.type.DefaultPlatformTypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.CollectionDataType;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ticket.model.CsTicketModel;


/**
 *
 * Facade strategy responsible for extending the CsTicket with extra parameter
 */
public class TicketTypeFacadeStrategy extends DefaultPlatformTypeFacadeStrategy
{
	private static final String TICKETS_ATTR = "tickets";
	private static final String CS_TICKET_LIST = "CsTicketList";
	private DataType ticketDataType;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return CsTicketModel._TYPECODE.equals(typeCode);
	}

	@Override
	public DataType load(final String code, final Context ctx) throws TypeNotFoundException
	{
		Preconditions.checkArgument(code != null, "code is null");

		if (ticketDataType == null)
		{
			//load standard attributes
			final DataType orginalTicketType = super.load(code, ctx);

			//build the new attribute to add to the CsTicket
			final CollectionDataType.CollectionBuilder csTicketListBuilder = new CollectionDataType.CollectionBuilder(
					CS_TICKET_LIST);

			csTicketListBuilder.valueType(orginalTicketType).supertype(ItemModel._TYPECODE);

			final DataAttribute.Builder attributeBuilder = new DataAttribute.Builder(TICKETS_ATTR).primitive(false).ordered(true);
			attributeBuilder.searchable(false).localized(false).unique(false).writable(true).mandatory(false)
					.valueType(csTicketListBuilder.build());

			final DataType.Builder finalTicketTypeBuilder = new DataType.Builder(code);

			for (final DataAttribute dataAttr : orginalTicketType.getAttributes())
			{
				finalTicketTypeBuilder.attribute(dataAttr);
			}

			finalTicketTypeBuilder.labels(orginalTicketType.getAllLabels());
			finalTicketTypeBuilder.clazz(orginalTicketType.getClazz());
			finalTicketTypeBuilder.supertype(orginalTicketType.getSuperType());
			finalTicketTypeBuilder.type(orginalTicketType.getType());

			//build the attribute after setting it up
			ticketDataType = finalTicketTypeBuilder.attribute(attributeBuilder.build()).build();

		}
		return ticketDataType;
	}
}

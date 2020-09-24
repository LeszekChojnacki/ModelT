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
package de.hybris.platform.ticket.strategies.impl;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelContextImpl;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.strategies.TicketAttributeChangeEventStrategy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class DefaultTicketAttributeChangeEventStrategy implements
		TicketAttributeChangeEventStrategy
{
	@SuppressWarnings(
	{ "unused" })
	private static final Logger LOG = Logger.getLogger(DefaultTicketAttributeChangeEventStrategy.class);

	private TypeService typeService;
	private ModelService modelService;
	private Map<String, String> valueType2ChangeRecordType;
	private String defaultChangeRecordType;

	@Override
	public Set<CsTicketChangeEventEntryModel> getEntriesForChangedAttributes(final CsTicketModel ticket)
	{
		final Set<CsTicketChangeEventEntryModel> changedEntries = new HashSet<CsTicketChangeEventEntryModel>(getContext(ticket)
				.getValueHistory().getDirtyAttributes().size());
		// before updating a ticket we want to find all of the changed attributes and create a change record for each one
		for (final String attr : getContext(ticket).getValueHistory().getDirtyAttributes())
		{
			final CsTicketChangeEventEntryModel changeEntry = createChangeEntryForAttribute(ticket, attr);
			if (changeEntry != null)
			{
				changedEntries.add(changeEntry);
			}
		}

		return changedEntries;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setValueType2ChangeRecordType(final Map<String, String> valueType2ChangeRecordType)
	{
		this.valueType2ChangeRecordType = valueType2ChangeRecordType;
	}

	@Required
	public void setDefaultChangeRecordType(final String defaultChangeRecordType)
	{
		this.defaultChangeRecordType = defaultChangeRecordType;
	}

	protected CsTicketChangeEventEntryModel createChangeEntryForAttribute(final CsTicketModel ticket, final String attribute)
	{
		final AttributeDescriptorModel attributeDescriptor = typeService.getAttributeDescriptor(
				typeService.getComposedType(ticket.getClass()), attribute);
		String changeAttributeTypeCode = valueType2ChangeRecordType.get(attributeDescriptor.getAttributeType().getCode());
		if (changeAttributeTypeCode == null)
		{
			LOG.warn("Not able to find change attribute type for attribute [" + attributeDescriptor + "], using default");
			changeAttributeTypeCode = defaultChangeRecordType;
		}

		final Object originalValue = getOriginalAttributeValue(ticket, attribute);
		final Object newValue = getNewAttributeValue(ticket, attribute);

		if (ObjectUtils.equals(originalValue, newValue))
		{
			// nothing to do - the value has not changed.
			return null;
		}

		final ComposedTypeModel changeAttributeType = typeService.getComposedType(changeAttributeTypeCode);
		final CsTicketChangeEventEntryModel changeEventEntry = getModelService().create(changeAttributeType.getCode());

		changeEventEntry.setAlteredAttribute(attributeDescriptor);

		if (!changeAttributeTypeCode.equals(defaultChangeRecordType))
		{
			if (typeService.getAttributeDescriptor(changeAttributeType, "oldValue") == null
					|| typeService.getAttributeDescriptor(changeAttributeType, "newValue") == null)
			{
				LOG.error("Cannot find oldval or newval methods on type changeEventEntry for types ["
						+ attributeDescriptor.getAttributeType().getItemtype() + "]");
				return null;
			}

			getModelService().setAttributeValue(changeEventEntry, "oldValue", originalValue);
			getModelService().setAttributeValue(changeEventEntry, "newValue", newValue);
		}

		changeEventEntry.setOldStringValue(stringValueOf(originalValue));
		changeEventEntry.setNewStringValue(stringValueOf(newValue));

		changeEventEntry.setOldBinaryValue(originalValue);
		changeEventEntry.setNewBinaryValue(newValue);

		return changeEventEntry;
	}

	protected Object getOriginalAttributeValue(final ItemModel item, final String attribute)
	{
		if (!getContext(item).getValueHistory().isValueLoaded(attribute))
		{
			return getContext(item).getAttributeProvider().getAttribute(attribute);
		}

		return getContext(item).getValueHistory().getOriginalValue(attribute);
	}

	protected Object getNewAttributeValue(final ItemModel item, final String attribute)
	{
		return getModelService().getAttributeValue(item, attribute);
	}

	protected String stringValueOf(final Object object)
	{
		if (object == null)
		{
			return "None";
		}

		if (object instanceof EmployeeModel)
		{
			return StringUtils.isNotEmpty(((EmployeeModel) object).getName()) ? ((EmployeeModel) object).getName()
					: ((EmployeeModel) object).getUid();
		}

		if (object instanceof CsAgentGroupModel)
		{
			return StringUtils.isNotEmpty(((CsAgentGroupModel) object).getName()) ? ((CsAgentGroupModel) object).getName()
					: ((CsAgentGroupModel) object).getUid();
		}

		if (object instanceof HybrisEnumValue)
		{
			return ((HybrisEnumValue) object).getCode();
		}

		if (object instanceof AbstractOrderModel)
		{
			return ((AbstractOrderModel) object).getCode();
		}

		return object.toString();
	}

	protected ItemModelContextImpl getContext(final AbstractItemModel model)
	{
		return (ItemModelContextImpl) ModelContextUtils.getItemModelContext(model);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService)
	{
		this.modelService = modelService;
	}
}

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
package de.hybris.platform.customersupportbackoffice.widgets;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.customersupportbackoffice.data.CsCloseTicketForm;
import de.hybris.platform.customersupportbackoffice.data.CsCreateAddressForm;
import de.hybris.platform.customersupportbackoffice.data.CsCreateCustomerForm;
import de.hybris.platform.customersupportbackoffice.data.CsCreateTicketForm;
import de.hybris.platform.customersupportbackoffice.data.CsReopenTicketForm;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsResolutionType;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.model.CsAgentGroupModel;
import de.hybris.platform.ticket.service.TicketService;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.util.CockpitSessionService;


/**
 * Factory sets some default values for customer support create-wizards.
 */
public class DefaultCsFormInitialsFactory
{
	private static final Logger LOG = Logger.getLogger(DefaultCsFormInitialsFactory.class);

	private TicketService ticketService;
	private UserService userService;
	private AddressModel lastSavedAddress;
	private EnumerationService enumerationService;
	private CockpitSessionService cockpitSessionService;

	//ticket creation defaults
	private String defaultPriority;
	private String defaultCategory;
	private String defaultIntervention;
	private String defaultReason;
	private String defaultAgentGroup;
	private String defaultRootGroup;

	/**
	 * Factory-method for TicketForm defaults creation.
	 */
	public CsCreateTicketForm getTicketForm()
	{
		final CsCreateTicketForm ticketForm = new CsCreateTicketForm();

		try
		{
			ticketForm.setCategory(enumerationService.getEnumerationValue(CsTicketCategory._TYPECODE, getDefaultCategory()));
			ticketForm.setPriority(enumerationService.getEnumerationValue(CsTicketPriority._TYPECODE, getDefaultPriority()));
			ticketForm
					.setIntervention(enumerationService.getEnumerationValue(CsInterventionType._TYPECODE, getDefaultIntervention()));
			ticketForm.setReason(enumerationService.getEnumerationValue(CsEventReason._TYPECODE, getDefaultReason()));
			ticketForm.setAssignedAgent((EmployeeModel) userService.getCurrentUser());

			// special field for filtering Agent Groups
			ticketForm.setRootGroup(userService.getUserGroupForUID(defaultRootGroup));

			final Object currentSessionCustomerId = cockpitSessionService
					.getAttribute(CustomersupportbackofficeConstants.SESSION_CONTEXT_UID_SESSION_ATTR);

			if (null != currentSessionCustomerId)
			{
				ticketForm.setCustomer((CustomerModel) userService.getUserForUID(currentSessionCustomerId.toString()));
			}
			final List<CsAgentGroupModel> group = ticketService.getAgentGroups().stream()
					.filter(m -> m.getUid().equalsIgnoreCase(getDefaultAgentGroup())).collect(Collectors.toList());
			ticketForm.setAssignedGroup(CollectionUtils.isNotEmpty(group) ? group.get(0) : null);
		}
		catch (final Exception exp)
		{
			LOG.error("Can't load ticket defaults, please check your conifuration.", exp);
		}
		return ticketForm;
	}

	public CsCreateCustomerForm getCustomerForm()
	{
		final CsCreateCustomerForm customerForm = new CsCreateCustomerForm();
		final AddressModel addressModel = new AddressModel();
		if (getLastSavedAddress() != null)
		{
			addressModel.setCountry(getLastSavedAddress().getCountry());
		}
		customerForm.setAddress(addressModel);
		return customerForm;
	}

	public CsCreateAddressForm getAddressForm()
	{
		final CsCreateAddressForm addressForm = new CsCreateAddressForm();
		final Object currentSessionCustomerId = cockpitSessionService
				.getAttribute(CustomersupportbackofficeConstants.SESSION_CONTEXT_UID_SESSION_ATTR);

		if (null != currentSessionCustomerId)
		{
			final CustomerModel customer = (CustomerModel) userService.getUserForUID(currentSessionCustomerId.toString());
			final String[] cName = customer.getName().split(" ");
			addressForm.setFirstName(cName[0]);
			if (cName.length > 1 && StringUtils.isNotEmpty(cName[1]))
			{
				addressForm.setLastName(cName[1]);
			}
		}
		addressForm.setShippingAddress(Boolean.TRUE);
		addressForm.setBillingAddress(Boolean.FALSE);
		return addressForm;
	}

	public CsCloseTicketForm getCloseTicketForm()
	{
		final CsCloseTicketForm closeTicketForm = new CsCloseTicketForm();
		closeTicketForm.setResolution(CsResolutionType.CLOSED);
		return closeTicketForm;
	}

	public CsReopenTicketForm getReopenTicketForm()
	{
		return new CsReopenTicketForm();
	}

	protected TicketService getTicketService()
	{
		return ticketService;
	}

	@Required
	public void setTicketService(final TicketService ticketService)
	{
		this.ticketService = ticketService;
	}

	public void setLastSavedAddress(final AddressModel addressModel)
	{
		if (addressModel != null && addressModel.getCountry() != null)
		{
			synchronized (this)
			{
				this.lastSavedAddress = addressModel;
			}
		}
	}

	protected AddressModel getLastSavedAddress()
	{
		return this.lastSavedAddress;
	}


	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	public String getDefaultPriority()
	{
		return defaultPriority;
	}

	@Required
	public void setDefaultPriority(final String defaultPriority)
	{
		this.defaultPriority = defaultPriority;
	}

	public String getDefaultCategory()
	{
		return defaultCategory;
	}

	@Required
	public void setDefaultCategory(final String defaultCategory)
	{
		this.defaultCategory = defaultCategory;
	}

	public String getDefaultIntervention()
	{
		return defaultIntervention;
	}

	@Required
	public void setDefaultIntervention(final String defaultIntervention)
	{
		this.defaultIntervention = defaultIntervention;
	}

	public String getDefaultReason()
	{
		return defaultReason;
	}

	@Required
	public void setDefaultReason(final String defaultReason)
	{
		this.defaultReason = defaultReason;
	}

	protected CockpitSessionService getCockpitSessionService()
	{
		return cockpitSessionService;
	}

	public String getDefaultRootGroup()
	{
		return defaultRootGroup;
	}

	@Required
	public void setDefaultRootGroup(final String defaultRootGroup)
	{
		this.defaultRootGroup = defaultRootGroup;
	}

	@Required
	public void setCockpitSessionService(final CockpitSessionService cockpitSessionService)
	{
		this.cockpitSessionService = cockpitSessionService;
	}

	public String getDefaultAgentGroup()
	{
		return defaultAgentGroup;
	}

	@Required
	public void setDefaultAgentGroup(final String defaultAgentGroup)
	{
		this.defaultAgentGroup = defaultAgentGroup;
	}
}

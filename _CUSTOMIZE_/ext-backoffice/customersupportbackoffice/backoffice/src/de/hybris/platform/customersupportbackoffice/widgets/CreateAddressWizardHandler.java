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

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.customersupportbackoffice.data.CsCreateAddressForm;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class CreateAddressWizardHandler implements FlowActionHandler
{

	private CustomerAccountService customerAccountService;
	private ModelService modelService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter flowActionHandlerAdapter,
			final Map<String, String> map)
	{
		final CsCreateAddressForm form = flowActionHandlerAdapter.getWidgetInstanceManager().getModel()
				.getValue("customersupport_backoffice_addressForm", CsCreateAddressForm.class);

		customerAccountService.saveAddressEntry(form.getOwner(), populateAddress(form));

		flowActionHandlerAdapter.getWidgetInstanceManager().getModel().removeAllObservers();

		final ConfigurableFlowController controller = (ConfigurableFlowController) flowActionHandlerAdapter
				.getWidgetInstanceManager().getWidgetslot().getAttribute("widgetController");
		controller.setValue("finished", Boolean.TRUE);
		controller.getBreadcrumbDiv().invalidate();
		flowActionHandlerAdapter.done();
	}


	/**
	 * @param form
	 * @return AddressModel
	 */
	protected AddressModel populateAddress(final CsCreateAddressForm form)
	{
		final AddressModel address = getModelService().create(AddressModel.class);
		address.setTitle(form.getTitle());
		address.setFirstname(form.getFirstName());
		address.setLastname(form.getLastName());
		address.setStreetname(form.getAddressLine1());
		address.setStreetnumber(form.getAddressLine2());
		address.setTown(form.getTown());
		address.setCountry(form.getCountry());
		address.setRegion(form.getRegion());
		address.setPostalcode(form.getPostalcode());
		address.setPhone1(form.getPhone1());
		address.setShippingAddress(form.getShippingAddress());
		address.setBillingAddress(form.getBillingAddress());
		address.setVisibleInAddressBook(Boolean.TRUE);
		return address;
	}


	/**
	 * @return the customerAccountService
	 */
	protected CustomerAccountService getCustomerAccountService()
	{
		return customerAccountService;
	}

	@Required
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}


	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}


	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}

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
package de.hybris.platform.customersupportbackoffice.strategies;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customersupportbackoffice.data.CsCreateCustomerForm;
import de.hybris.platform.customersupportbackoffice.widgets.DefaultCsFormInitialsFactory;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of create customer strategy.
 */
public class DefaultCsCreateCustomerStrategy implements CsCreateCustomerStrategy
{
	private CustomerAccountService customerAccountService;
	private BaseSiteService baseSiteService;
	private ModelService modelService;
	private CommonI18NService commonI18NService;
	private DefaultCsFormInitialsFactory csFormInitialsFactory;

	@Override
	public void createCustomer(final CsCreateCustomerForm createCustomerForm) throws DuplicateUidException
	{
		final CustomerModel customerModel = getModelService().create(CustomerModel.class);

		// Block below is logic that being copied from DefaultCustomerFacade.
		//      Can be removed since Facade-Service refactoring will be done.
		customerModel.setName(createCustomerForm.getName());
		customerModel.setTitle(createCustomerForm.getTitle());
		customerModel.setSessionLanguage(getCommonI18NService().getCurrentLanguage());
		customerModel.setSessionCurrency(getCommonI18NService().getCurrentCurrency());
		customerModel.setUid(createCustomerForm.getEmail().toLowerCase());
		customerModel.setOriginalUid(createCustomerForm.getEmail());

		getBaseSiteService().setCurrentBaseSite(createCustomerForm.getSite(), false);
		getCustomerAccountService().register(customerModel, null);

		if (createCustomerForm.getAddress().getCountry() != null)
		{
			getCustomerAccountService().saveAddressEntry(customerModel, createCustomerForm.getAddress());
			getCsFormInitialsFactory().setLastSavedAddress(createCustomerForm.getAddress());
		}
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
	 * @return the baseSiteService
	 */
	protected BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * @return the modelService
	 */
	protected ModelService getModelService()
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

	/**
	 * @return the commonI18NService
	 */
	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the csFormInitialsFactory
	 */
	protected DefaultCsFormInitialsFactory getCsFormInitialsFactory()
	{
		return csFormInitialsFactory;
	}

	/**
	 * @param csFormInitialsFactory
	 *           the csFormInitialsFactory to set
	 */
	@Required
	public void setCsFormInitialsFactory(final DefaultCsFormInitialsFactory csFormInitialsFactory)
	{
		this.csFormInitialsFactory = csFormInitialsFactory;
	}
}

/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.payment.strategy.impl;

import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.payment.strategy.PaymentInfoCreatorStrategy;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Required;


/**
 * This class is used by {@link de.hybris.platform.payment.impl.DefaultPaymentServiceImpl} to attaches a PaymentInfo to
 * the payment transaction instance during authorization.
 */
public class DefaultCreditCardPaymentInfoStrategy implements PaymentInfoCreatorStrategy
{
	private KeyGenerator keyGenerator;
	private ModelService modelService;
	private I18NService i18nService;
	private CommonI18NService commonI18NService;

	/**
	 * Attaches PaymentInfo to the assigned PaymentTransactionModel instance.<br>
	 *
	 * @param paymentTransactionModel
	 *           the payment transaction
	 * @param userModel
	 *           the user
	 * @param cardInfo
	 *           the card info
	 * @param amount
	 *           the amount
	 */
	@Override
	public void attachPaymentInfo(final PaymentTransactionModel paymentTransactionModel, final UserModel userModel,
			final CardInfo cardInfo, final BigDecimal amount)
	{
		final CreditCardPaymentInfoModel creditCardModel = getModelService().create(CreditCardPaymentInfoModel.class);
		creditCardModel.setUser(userModel);
		creditCardModel.setCode(getKeyGenerator().generate().toString());

		creditCardModel.setType(cardInfo.getCardType());
		creditCardModel.setNumber(cardInfo.getCardNumber());
		creditCardModel.setValidFromMonth(cardInfo.getIssueMonth() != null ? String.valueOf(cardInfo.getIssueMonth()) : "");
		creditCardModel.setValidFromYear(cardInfo.getIssueYear() != null ? String.valueOf(cardInfo.getIssueYear()) : "");
		creditCardModel.setValidToMonth(String.valueOf(cardInfo.getExpirationMonth()));
		creditCardModel.setValidToYear(String.valueOf(cardInfo.getExpirationYear()));
		creditCardModel.setCcOwner(cardInfo.getCardHolderFullName());

		if (cardInfo.getBillingInfo() != null)
		{
			final BillingInfo billingInfo = cardInfo.getBillingInfo();
			final AddressModel addressModel = getModelService().create(AddressModel.class);
			addressModel.setOwner(userModel);

			addressModel.setFirstname(billingInfo.getFirstName());
			addressModel.setLastname(billingInfo.getLastName());
			addressModel.setStreetnumber(billingInfo.getStreet1());
			addressModel.setStreetname(billingInfo.getStreet2());
			addressModel.setTown(billingInfo.getCity());
			addressModel.setDistrict(billingInfo.getState());
			addressModel.setPostalcode(billingInfo.getPostalCode());
			addressModel.setCountry(getCommonI18NService().getCountry(billingInfo.getCountry()));
			addressModel.setEmail(billingInfo.getEmail());
			addressModel.setPhone1(billingInfo.getPhoneNumber());

			creditCardModel.setBillingAddress(addressModel);
			getModelService().save(addressModel);
		}

		getModelService().save(creditCardModel);
		getModelService().refresh(userModel);

		paymentTransactionModel.setInfo(creditCardModel);
		getModelService().save(paymentTransactionModel);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @deprecated since 6.4 use {@link #getCommonI18NService()}
	 */
	@Deprecated
	protected I18NService getI18nService() //NOSONAR
	{
		return i18nService;
	}

	/**
	 * @deprecated since 6.4 use {@link #setCommonI18NService(CommonI18NService)}
	 */
	@Required
	@Deprecated
	public void setI18nService(final I18NService i18nService) //NOSONAR
	{
		this.i18nService = i18nService;
	}

	protected KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

}

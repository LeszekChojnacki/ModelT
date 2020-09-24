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
package de.hybris.platform.deeplink.servicelayer.interceptors;

import de.hybris.platform.deeplink.dao.DeeplinkUrlDao;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.util.localization.Localization;


/**
 * The Class DeeplinkUrlValidateInterceptor. Validates {@link DeeplinkUrlModel} model.
 *
 *
 */
public class DeeplinkUrlValidateInterceptor implements ValidateInterceptor
{
	private static final String INVALID_CODE_CHAR = "-";
	private DeeplinkUrlDao deeplinkUrlDao;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.interceptor.ValidateInterceptor#onValidate(java.lang.Object,
	 * de.hybris.platform.servicelayer.interceptor.InterceptorContext)
	 */
	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof DeeplinkUrlModel && ((DeeplinkUrlModel) model).getCode().contains(INVALID_CODE_CHAR))
		{
			throw new InterceptorException(
					getLocalizedString("validation.cant_contains_char", DeeplinkUrlModel.CODE, INVALID_CODE_CHAR));

		}
	}

	/**
	 * Gets the deeplink url dao.
	 *
	 * @return the deeplinkUrlDao
	 */
	public DeeplinkUrlDao getDeeplinkUrlDao()
	{
		return deeplinkUrlDao;
	}

	/**
	 * Sets the deeplink url dao.
	 *
	 * @param deeplinkUrlDao
	 *           the deeplinkUrlDao to set
	 */
	public void setDeeplinkUrlDao(final DeeplinkUrlDao deeplinkUrlDao)
	{
		this.deeplinkUrlDao = deeplinkUrlDao;
	}

	/**
	 * Gets the localized string.
	 *
	 * @param key
	 *           the key
	 * @param arguments
	 *           the arguments
	 *
	 * @return the localized string
	 */
	protected String getLocalizedString(final String key, final Object... arguments)
	{
		return Localization.getLocalizedString(key, arguments);
	}
}

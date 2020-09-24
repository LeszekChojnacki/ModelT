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
package de.hybris.platform.warehousing.interceptor;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Sets {@link AdvancedShippingNoticeModel#INTERNALID} for {@link AdvancedShippingNoticeModel}.
 */
public class AdvancedShippingNoticePrepareInterceptor implements PrepareInterceptor<AdvancedShippingNoticeModel>
{
	private KeyGenerator keyGenerator;

	@Override
	public void onPrepare(final AdvancedShippingNoticeModel advancedShippingNotice, final InterceptorContext context)
			throws InterceptorException
	{
		if (context.isNew(advancedShippingNotice) && StringUtils.isEmpty(advancedShippingNotice.getInternalId()))
		{
			advancedShippingNotice.setInternalId(getKeyGenerator().generate().toString());
		}
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
}

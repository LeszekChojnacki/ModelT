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
package de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.model.SessionContextModel;
import de.hybris.platform.util.Config;

import org.apache.commons.collections.CollectionUtils;


/**
 * Contains methods for asm-related functionality
 */
public class AsmUtils
{

	// param defines visibility of asm-buttons
	public static final String ASM_DEEPLINK_SHOW_PARAM = "cscokpit.assistedservice.deeplink";
	// path for asm-emulation request
	public static final String ASM_DEEPLINK_PARAM = "assistedservicestorefront.deeplink.link";


	private AsmUtils()
	{
		// utility class. Prevent creation.
	}

	/**
	 * Method loads ASM_DEEPLINK_SHOW_PARAM from properties, false is default value
	 *
	 * @return boolean
	 */
	public static boolean showAsmButton()
	{
		return Config.getBoolean(ASM_DEEPLINK_SHOW_PARAM, false) && Config.getParameter(ASM_DEEPLINK_PARAM) != null;
	}


	/**
	 * Method constructs deep link for asm as href
	 *
	 * @param currentSite
	 *           the site to use as a storefront
	 * @param SessionContextModel
	 *           the session context to get all needed info from
	 * @return deep link generated
	 */
	public static String getAsmDeepLink(final BaseSiteModel currentSite, final SessionContextModel sessionContext)
	{
		final StringBuilder deepLink = new StringBuilder();
		deepLink.append(Config.getParameter("website." + currentSite.getUid() + ".https"));

		if (null == sessionContext || null == sessionContext.getCurrentCustomer())
		{
			deepLink.append("?asm=true");
			return deepLink.toString();
		}
		
		final CustomerType type = ((CustomerModel) sessionContext.getCurrentCustomer()).getType();
		String customerUid = sessionContext.getCurrentCustomer().getUid();
		if(type == CustomerType.GUEST)
		{
			customerUid = customerUid.replace("|","%7C");
		}
		deepLink.append(Config.getParameter(ASM_DEEPLINK_PARAM) + "?customerId=" + customerUid);

		if (sessionContext.getCurrentOrder() != null)
		{
			deepLink.append("&orderId=");
			deepLink.append(sessionContext.getCurrentOrder().getGuid());
			return deepLink.toString();
		}

		if (CollectionUtils.isEmpty(sessionContext.getCurrentCustomer().getCarts()))
		{
			return deepLink.toString();
		}

		for (final CartModel cart : sessionContext.getCurrentCustomer().getCarts())
		{
			if (cart.getSite().getUid().equals(currentSite.getUid()))
			{
				deepLink.append("&cartId=");
				deepLink.append(cart.getCode());
				break;
			}
		}

		return deepLink.toString();
	}
}

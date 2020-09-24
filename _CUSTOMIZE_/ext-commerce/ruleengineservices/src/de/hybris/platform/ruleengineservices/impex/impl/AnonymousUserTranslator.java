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
package de.hybris.platform.ruleengineservices.impex.impl;

import de.hybris.platform.core.Registry;
import de.hybris.platform.impex.jalo.header.StandardColumnDescriptor;
import de.hybris.platform.impex.jalo.translators.SingleValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.CUSTOMER_CONDITION_USE_PK_PROPERTY;
import static de.hybris.platform.servicelayer.user.UserConstants.ANONYMOUS_CUSTOMER_UID;

/**
 * Translator used for promotion rules that rely on 'anonymous' user within their definition.
 * Based on the {@link de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants#CUSTOMER_CONDITION_USE_PK_PROPERTY}
 * configuration flag performs conversion of 'anonymous' string value to the respective PK representation that matches
 * that of the anonymous user in the system.
 *
 *  <tt>INSERT_UPDATE PromotionSourceRule;code[unique=true];conditions[translator=de.hybris.platform.ruleengineservices.impex.impl.AnonymousUserTranslator]</tt>
 *
 */
public class AnonymousUserTranslator extends SingleValueTranslator
{
	protected static final String USER_SERVICE = "userService";

	private boolean usePk;

	@Override
	public void init(final StandardColumnDescriptor descriptor)
	{
		super.init(descriptor);
		usePk = Boolean.valueOf(Config.getParameter(CUSTOMER_CONDITION_USE_PK_PROPERTY));
	}

	@Override
	protected Object convertToJalo(final String value, final Item item)
	{
		return isUsePk() ? value
				.replace(ANONYMOUS_CUSTOMER_UID, getUserService().getAnonymousUser().getPk().getLongValueAsString()) : value;
	}

	@Override
	protected String convertToString(final Object object)
	{
		return object.toString();
	}

	protected boolean isUsePk()
	{
		return usePk;
	}

	protected UserService getUserService()
	{
		return Registry.getApplicationContext().getBean(USER_SERVICE, UserService.class);
	}
}

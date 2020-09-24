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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


public class UserGroupRuleParameterValueMapper implements RuleParameterValueMapper<UserGroupModel>
{

	private UserService userService;

	@Override
	public String toString(final UserGroupModel userGroup)
	{
		ServicesUtil.validateParameterNotNull(userGroup, "Object cannot be null");
		return userGroup.getUid();
	}

	@Override
	public UserGroupModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		final UserGroupModel userGroup = userService.getUserGroupForUID(value);

		if (userGroup == null)
		{
			throw new RuleParameterValueMapperException("Cannot find user group with the UID: " + value);
		}

		return userGroup;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

}

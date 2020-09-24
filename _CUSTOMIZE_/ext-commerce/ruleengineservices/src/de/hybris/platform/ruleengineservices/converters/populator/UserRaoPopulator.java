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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ruleengineservices.rao.UserGroupRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Populates UserRAO from UserModel.
 *
 */
public class UserRaoPopulator implements Populator<UserModel, UserRAO>
{
	private Converter<UserGroupModel, UserGroupRAO> userGroupConverter;
	private UserService userService;
	
	@Override
	public void populate(final UserModel source, final UserRAO target)
	{
		target.setId(source.getUid());
		target.setPk(source.getPk().getLongValueAsString());
		final Set<UserGroupModel> userGroups = getUserService().getAllUserGroupsForUser(source);
		if (CollectionUtils.isNotEmpty(userGroups))
		{
			target.setGroups(new LinkedHashSet<>(Converters.convertAll(userGroups, getUserGroupConverter())));
		}
	}

	protected Converter<UserGroupModel, UserGroupRAO> getUserGroupConverter()
	{
		return userGroupConverter;
	}

	@Required
	public void setUserGroupConverter(final Converter<UserGroupModel, UserGroupRAO> userGroupConverter)
	{
		this.userGroupConverter = userGroupConverter;
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
}

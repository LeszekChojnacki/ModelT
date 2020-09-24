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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.ruleengineservices.rao.UserGroupRAO;


/**
 * Populates UserGroupRAO from PrincipalGroupModel
 *
 */
public class UserGroupRaoPopulator implements Populator<UserGroupModel, UserGroupRAO>
{

	@Override
	public void populate(final UserGroupModel source, final UserGroupRAO target)
	{
		target.setId(source.getUid());

	}

}

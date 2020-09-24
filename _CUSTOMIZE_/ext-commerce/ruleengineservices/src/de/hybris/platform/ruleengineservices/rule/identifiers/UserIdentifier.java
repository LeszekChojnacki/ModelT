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
package de.hybris.platform.ruleengineservices.rule.identifiers;

import de.hybris.platform.core.model.user.UserModel;
import org.springframework.beans.factory.annotation.Required;

import java.util.function.Function;


/**
 * Provides identifier for {@link UserModel} entities based on the 'usePk' flag.
 * In case flag is 'true', string representation of PK is used, otherwise - user's UID value
 */
public class UserIdentifier implements Function<UserModel, String>
{
	private boolean usePk;

	@Override
	public String apply(final UserModel userModel)
	{
		return isUsePk() ? userModel.getPk().toString() : userModel.getUid();
	}

	protected boolean isUsePk()
	{
		return usePk;
	}

	@Required
	public void setUsePk(final boolean usePk)
	{
		this.usePk = usePk;
	}
}

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
package com.hybris.backoffice.spring.security;

import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.spring.security.CoreAuthenticationProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * Extends {@link CoreAuthenticationProvider}, additionally checks if user is an employee.
 */
public class BackofficeAuthenticationProvider extends CoreAuthenticationProvider
{

	private UserService userService;


	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException
	{
		try
		{
			final EmployeeModel employee = userService.getUserForUID(authentication.getName(), EmployeeModel.class);
			checkBackofficeAccess(employee);
		}
		catch (final ClassMismatchException e)
		{
			throw new BadCredentialsException("Bad credentials", e);
		}
		catch (final IllegalArgumentException | UnknownIdentifierException e)
		{
			throw new UsernameNotFoundException("Username not found", e);
		}

		return coreAuthenticate(authentication);
	}

	/**
	 * Checks if user has permissions to access Backoffice application. Should throw DisabledException if access is
	 * denied.
	 *
	 * @param employee
	 *           user to check
	 * @throws DisabledException
	 *            if user has no access to Backoffice
	 */
	protected void checkBackofficeAccess(final EmployeeModel employee) throws DisabledException
	{
		if (!userService.isAdmin(employee))
		{
			Boolean disabled = employee.getBackOfficeLoginDisabled();
			if (disabled == null && CollectionUtils.isNotEmpty(employee.getGroups()))
			{
				disabled = Boolean.valueOf(checkBackofficeAccessDisabledForGroups(employee.getGroups()));
			}
			if (BooleanUtils.isNotFalse(disabled))
			{
				throw new DisabledException("User access denied");
			}
		}
	}

	private boolean checkBackofficeAccessDisabledForGroups(final Collection<PrincipalGroupModel> groups)
	{
		Boolean disabled = null;
		final Set<PrincipalGroupModel> parentGroups = new HashSet<>();
		for (final PrincipalGroupModel group : groups)
		{
			if (group.getBackOfficeLoginDisabled() != null)
			{
				disabled = group.getBackOfficeLoginDisabled();
			}
			if (BooleanUtils.isTrue(disabled))
			{
				break;
			}
			if (CollectionUtils.isNotEmpty(group.getGroups()))
			{
				parentGroups.addAll(group.getGroups());
			}
		}
		if (disabled == null && !parentGroups.isEmpty())
		{
			disabled = Boolean.valueOf(checkBackofficeAccessDisabledForGroups(parentGroups));
		}
		return BooleanUtils.isNotFalse(disabled);
	}

	protected Authentication coreAuthenticate(final Authentication authentication)
	{
		return super.authenticate(authentication);
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

}

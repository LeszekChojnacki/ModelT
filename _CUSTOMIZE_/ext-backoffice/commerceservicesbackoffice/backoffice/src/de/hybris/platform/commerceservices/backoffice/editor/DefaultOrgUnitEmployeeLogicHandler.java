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
package de.hybris.platform.commerceservices.backoffice.editor;

import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.commerceservices.organization.strategies.OrgUnitAuthorizationStrategy;
import de.hybris.platform.commerceservices.organization.utils.OrgUtils;
import de.hybris.platform.commerceservices.util.CommerceSearchUtils;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;


/**
 * Organization Unit Employee extension of {@link DefaultEditorAreaLogicHandler}
 */
public class DefaultOrgUnitEmployeeLogicHandler extends DefaultEditorAreaLogicHandler
{
	private static final Logger LOG = Logger.getLogger(DefaultOrgUnitEmployeeLogicHandler.class);

	private static final String ORG_ROLES = "orgRoles";
	private static final String ORG_UNITS = "orgUnits";

	private OrgUnitService orgUnitService;
	private ModelService modelService;
	private UserService userService;
	private OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy;

	@Override
	public void beforeEditorAreaRender(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
	{
		// Populate organization roles and units to employee object
		final EmployeeModel employeeModel = (EmployeeModel) currentObject;
		final Set<PrincipalGroupModel> orgRoles = new HashSet<>();
		final Set<PrincipalGroupModel> orgUnits = new HashSet<>();
		for (final PrincipalGroupModel group : employeeModel.getGroups())
		{
			if (OrgUtils.getRoleUids().contains(group.getUid()))
			{
				orgRoles.add(group);
			}
			else if (group instanceof OrgUnitModel)
			{
				orgUnits.add(group);
			}
		}
		widgetInstanceManager.getModel().setValue(ORG_ROLES, orgRoles);
		widgetInstanceManager.getModel().setValue(ORG_UNITS, orgUnits);
	}

	@Override
	public Object performSave(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
			throws ObjectSavingException
	{
		final EmployeeModel employeeModel = (EmployeeModel) currentObject;
		final Set<UserGroupModel> orgRoles = widgetInstanceManager.getModel().getValue(ORG_ROLES, Set.class);
		final Set<OrgUnitModel> orgUnits = widgetInstanceManager.getModel().getValue(ORG_UNITS, Set.class);
		final Set<PrincipalGroupModel> userGroups = new HashSet<>(employeeModel.getGroups());

		// Validate permissions to update the employee
		if (getUserService().getCurrentUser() instanceof EmployeeModel)
		{
			final EmployeeModel currentUser = (EmployeeModel) getUserService().getCurrentUser();

			if (!OrgUtils.isAdmin(currentUser) && OrgUtils.isAdmin(employeeModel))
			{
				throw new ObjectSavingException(employeeModel.getUid(), "Only an admin of an organization can edit another admin.",
						null);
			}

			if (!OrgUtils.isAdmin(currentUser) && OrgUtils.containsOrgAdminGroup(orgRoles))
			{
				throw new ObjectSavingException(employeeModel.getUid(), "Only an admin of an organization can assign an admin role.",
						null);
			}

			if (!OrgUtils.isAdmin(currentUser) && !getOrgUnitAuthorizationStrategy().canEditUnit(currentUser))
			{
				throw new ObjectSavingException(employeeModel.getUid(),
						"Only an admin or edit permission granted group of an organization can edit.", null);
			}
		}
		else
		{
			throw new ObjectSavingException(employeeModel.getUid(), "Only an employee of an organization can edit other employees.",
					null);
		}

		try
		{
			// Disable save notification bar since it was already displayed in a super class
			final Context ctx = new DefaultContext();
			ctx.addAttribute(ObjectFacade.CTX_DISABLE_CRUD_COCKPIT_EVENT_NOTIFICATION, Boolean.TRUE);

			// update roles and sales units of employee
			final Set<OrgUnitModel> orgUnitsToRemove = new HashSet<>();
			final Set<OrgUnitModel> orgUnitsToKeep = new HashSet<>();
			for (final PrincipalGroupModel group : employeeModel.getGroups())
			{
				if (OrgUtils.getRoleUids().contains(group.getUid()) && !orgRoles.contains(group))
				{
					userGroups.remove(group);
				}
				else if (group instanceof OrgUnitModel)
				{
					if (orgUnits.contains(group))
					{
						orgUnitsToKeep.add((OrgUnitModel) group);
					}
					else
					{
						orgUnitsToRemove.add((OrgUnitModel) group);
					}
				}
			}
			userGroups.addAll(orgRoles);
			employeeModel.setGroups(userGroups);
			getModelService().save(employeeModel);

			// remove employee from removed org unit
			for (final PrincipalGroupModel principalGroupModel : orgUnitsToRemove)
			{
				getOrgUnitService().removeMembers(OrgUtils.createOrgUnitMemberParameter(principalGroupModel.getUid(),
						Collections.singleton(employeeModel), EmployeeModel.class, CommerceSearchUtils.getAllOnOnePagePageableData()));
			}

			// add employee to new org unit
			for (final OrgUnitModel orgUnitModel : orgUnits)
			{
				if (!orgUnitsToKeep.contains(orgUnitModel))
				{
					getOrgUnitService().addMembers(
							OrgUtils.createOrgUnitMemberParameter(orgUnitModel.getUid(), Collections.singleton(employeeModel),
									EmployeeModel.class, CommerceSearchUtils.getAllOnOnePagePageableData()));
				}
			}
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			throw new ObjectSavingException(employeeModel.getUid(), e);
		}
		return currentObject;
	}

	protected OrgUnitService getOrgUnitService()
	{
		return orgUnitService;
	}

	@Required
	public void setOrgUnitService(final OrgUnitService orgUnitService)
	{
		this.orgUnitService = orgUnitService;
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

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected OrgUnitAuthorizationStrategy getOrgUnitAuthorizationStrategy()
	{
		return orgUnitAuthorizationStrategy;
	}

	@Required
	public void setOrgUnitAuthorizationStrategy(final OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy)
	{
		this.orgUnitAuthorizationStrategy = orgUnitAuthorizationStrategy;
	}
}

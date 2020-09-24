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
import de.hybris.platform.commerceservices.organization.services.OrgUnitMemberParameter;
import de.hybris.platform.commerceservices.organization.services.OrgUnitParameter;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.commerceservices.organization.utils.OrgUtils;
import de.hybris.platform.commerceservices.util.CommerceSearchUtils;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;


/**
 * Organization Unit extension of {@link DefaultEditorAreaLogicHandler}
 */
public class OrgUnitLogicHandler extends DefaultEditorAreaLogicHandler
{
	private static final Logger LOG = Logger.getLogger(OrgUnitLogicHandler.class);

	private OrgUnitService orgUnitService;
	private UserService userService;

	@Override
	public void beforeEditorAreaRender(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
	{
		// Assign parent unit to model
		final OrgUnitModel unit = (OrgUnitModel) currentObject;
		final Optional<OrgUnitModel> optionalParentUnit = getOrgUnitService().getParent(unit);
		widgetInstanceManager.getModel().setValue("parentUnit", optionalParentUnit.isPresent() ? optionalParentUnit.get() : null);

		final OrgUnitMemberParameter<EmployeeModel> param = OrgUtils.createOrgUnitMemberParameter(unit.getUid(), null,
				EmployeeModel.class, CommerceSearchUtils.getAllOnOnePagePageableData());

		// Assign employees to model
		widgetInstanceManager.getModel().setValue("employeesChanged", getOrgUnitService().getMembers(param).getResults());
	}

	@Override
	public Object performSave(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
			throws ObjectSavingException
	{
		final OrgUnitModel unit = (OrgUnitModel) currentObject;

		try
		{
			// Disable save notification bar since it was already displayed in a super class
			final Context ctx = new DefaultContext();
			ctx.addAttribute(ObjectFacade.CTX_DISABLE_CRUD_COCKPIT_EVENT_NOTIFICATION, Boolean.TRUE);

			// Update organization unit
			final OrgUnitModel parentUnit = widgetInstanceManager.getModel().getValue("parentUnit", OrgUnitModel.class);
			final OrgUnitParameter param = new OrgUnitParameter();
			param.setOrgUnit(unit);
			param.setParentUnit(parentUnit);
			getOrgUnitService().updateUnit(param);

			// Update members
			handleSaveMembers(widgetInstanceManager, param.getOrgUnit());
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Saving org unit failed", e);
			}
			if (e instanceof ObjectSavingException)
			{
				throw e;
			}
			throw new ObjectSavingException(unit.getUid(), e.getMessage(), e);
		}
		return currentObject;
	}

	protected void handleSaveMembers(final WidgetInstanceManager widgetInstanceManager, final OrgUnitModel unit)
			throws ObjectSavingException
	{
		// Get original employees
		final OrgUnitMemberParameter<EmployeeModel> empParam = OrgUtils.createOrgUnitMemberParameter(unit.getUid(), null,
				EmployeeModel.class, CommerceSearchUtils.getAllOnOnePagePageableData());
		final List<EmployeeModel> employeesBefore = getOrgUnitService().getMembers(empParam).getResults();
		final List<EmployeeModel> employeesAfter = widgetInstanceManager.getModel().getValue("employeesChanged", List.class);

		// Remove employees
		final Set<EmployeeModel> employeesToRemove = new HashSet(employeesBefore);
		employeesToRemove.removeAll(employeesAfter);

		final EmployeeModel currentUser = (EmployeeModel) getUserService().getCurrentUser();

		for (final EmployeeModel employeeToRemove : employeesToRemove)
		{
			if (employeeToRemove.equals(currentUser))
			{
				throw new ObjectSavingException(unit.getUid(), "Employees may not remove themselves from a unit.", null);
			}
			if (!OrgUtils.isAdmin(currentUser) && OrgUtils.isAdmin(employeeToRemove))
			{
				throw new ObjectSavingException(unit.getUid(), "Only an admin of an organization can remove another admin.", null);
			}
		}

		if (CollectionUtils.isNotEmpty(employeesToRemove))
		{
			empParam.setMembers(employeesToRemove);
			getOrgUnitService().removeMembers(empParam);
		}

		// Add new employees
		final Set<EmployeeModel> employeesToAdd = new HashSet(employeesAfter);
		employeesToAdd.removeAll(employeesBefore);

		for (final EmployeeModel employeeToAdd : employeesToAdd)
		{
			if (!OrgUtils.isAdmin(currentUser) && OrgUtils.isAdmin(employeeToAdd))
			{
				throw new ObjectSavingException(unit.getUid(), "Only an admin of an organization can add another admin.", null);
			}
		}

		if (CollectionUtils.isNotEmpty(employeesToAdd))
		{
			empParam.setMembers(new HashSet<EmployeeModel>(employeesToAdd));
			getOrgUnitService().addMembers(empParam);
		}
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

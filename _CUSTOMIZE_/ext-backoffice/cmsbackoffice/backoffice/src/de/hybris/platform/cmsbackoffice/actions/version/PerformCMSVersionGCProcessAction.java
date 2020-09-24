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
package de.hybris.platform.cmsbackoffice.actions.version;

import de.hybris.platform.cms2.model.processing.CMSVersionGCProcessModel;
import de.hybris.platform.cms2.version.processengine.service.CMSVersionGCProcessService;
import de.hybris.platform.processengine.enums.ProcessState;

import javax.annotation.Resource;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;

public class PerformCMSVersionGCProcessAction implements CockpitAction<CMSVersionGCProcessModel, Object>
{
	private static final Logger LOG = Logger.getLogger(PerformCMSVersionGCProcessAction.class);

	@Resource
	private CMSVersionGCProcessService cmsVersionGCProcessService;

	@Override
	public ActionResult<Object> perform(final ActionContext<CMSVersionGCProcessModel> ctx)
	{
		final CMSVersionGCProcessModel cmsVersionGCProcessModel = ctx.getData();

		LOG.info("Performing Business Process  [" + cmsVersionGCProcessModel.getCode() + "] from Backoffice!");

		getCmsVersionGCProcessService().startProcess(cmsVersionGCProcessModel);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<CMSVersionGCProcessModel> ctx)
	{
		return Objects.nonNull(ctx) && Objects.nonNull(ctx.getData()) && ProcessState.CREATED.equals(ctx.getData().getState());
	}

	@Override
	public boolean needsConfirmation(final ActionContext<CMSVersionGCProcessModel> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<CMSVersionGCProcessModel> ctx)
	{
		return ctx.getLabel("runcmsversiongcprocess.perform.confirm");
	}

	protected CMSVersionGCProcessService getCmsVersionGCProcessService()
	{
		return cmsVersionGCProcessService;
	}
}

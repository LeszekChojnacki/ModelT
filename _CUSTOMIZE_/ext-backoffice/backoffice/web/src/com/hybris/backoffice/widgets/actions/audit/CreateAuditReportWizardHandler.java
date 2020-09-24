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
package com.hybris.backoffice.widgets.actions.audit;

import de.hybris.platform.auditreport.model.CreateAuditReportCronJobModel;
import de.hybris.platform.auditreport.model.CreateAuditReportJobModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Wizard handler responsible for executing cron job which will create audit report.
 */
public class CreateAuditReportWizardHandler implements FlowActionHandler
{
	public static final String MODEL_WIZARD_DATA = "auditReportWizardData";

	private static final Logger LOG = Logger.getLogger(CreateAuditReportWizardHandler.class);

	private ModelService modelService;
	private KeyGenerator keyGenerator;
	private UserService userService;
	private CommonI18NService commonI18NService;
	private CronJobService cronJobService;
	private String cronJobPerformableSpringId;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		try
		{
			final AuditReportWizardData reportWizardData = adapter.getWidgetInstanceManager().getModel().getValue(MODEL_WIZARD_DATA,
					AuditReportWizardData.class);
			adapter.done();
			getCronJobService().performCronJob(createCronJobModel(reportWizardData));
			notifyUserGenerationInvoked(adapter.getWidgetInstanceManager(), NotificationEvent.Level.SUCCESS);
		}
		catch (final Exception e)
		{
			LOG.error("Cannot generate audit report", e);
			notifyUserGenerationInvoked(adapter.getWidgetInstanceManager(), NotificationEvent.Level.FAILURE);
		}
	}

	/**
	 * Invokes notification containing information about the triggered process of report generation.
	 *
	 * @param wim
	 *           widget instance manager.
	 * @param level
	 *           notification level.
	 *
	 * @deprecated since 6.7, use the
	 *             {@link NotificationService#notifyUser(String , String, NotificationEvent.Level, Object...)} instead.
	 */
	@Deprecated
	protected void notifyUserGenerationInvoked(final WidgetInstanceManager wim, final NotificationEvent.Level level)
	{
		getNotificationService().notifyUser(wim, "AuditReportGenerationInvoked", level);
	}

	/**
	 * Creates {@link CronJobModel} which will be executed.
	 *
	 * @param wizardData
	 *           the wizard's data.
	 * @return the instance of {@link CronJobModel}.
	 */
	protected CronJobModel createCronJobModel(final AuditReportWizardData wizardData)
	{
		final String id = String.valueOf(getKeyGenerator().generate());

		final CreateAuditReportJobModel jobModel = getModelService().create(CreateAuditReportJobModel.class);
		jobModel.setCode(id);
		jobModel.setSpringId(getCronJobPerformableSpringId());
		getModelService().save(jobModel);

		final CreateAuditReportCronJobModel cronJobModel = getModelService().create(CreateAuditReportCronJobModel.class);
		cronJobModel.setCode(id);
		cronJobModel.setActive(Boolean.TRUE);
		cronJobModel.setJob(jobModel);

		cronJobModel.setSessionUser(getUserService().getCurrentUser());
		cronJobModel.setSessionLanguage(getCommonI18NService().getCurrentLanguage());
		cronJobModel.setSessionCurrency(getCommonI18NService().getCurrentCurrency());

		cronJobModel.setRootItem(wizardData.getSourceItem());
		cronJobModel.setConfigName(wizardData.getAuditReportConfig());
		cronJobModel.setReportId(wizardData.getReportName());
		cronJobModel.setAudit(wizardData.isAudit());
		cronJobModel.setIncludedLanguages(wizardData.getExportedLanguages());
		cronJobModel.setAuditReportTemplate(wizardData.getAuditReportTemplate());

		getModelService().save(cronJobModel);

		return cronJobModel;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
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

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	public String getCronJobPerformableSpringId()
	{
		return cronJobPerformableSpringId;
	}

	@Required
	public void setCronJobPerformableSpringId(final String cronJobPerformableSpringId)
	{
		this.cronJobPerformableSpringId = cronJobPerformableSpringId;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}

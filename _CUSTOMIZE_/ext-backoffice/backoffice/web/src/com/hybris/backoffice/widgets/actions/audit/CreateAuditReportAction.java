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

import de.hybris.platform.audit.internal.config.AuditConfigService;
import de.hybris.platform.audit.internal.config.AuditReportConfig;
import de.hybris.platform.auditreport.model.AuditReportDataModel;
import de.hybris.platform.core.model.ItemModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;


/**
 * Action responsible for creating audit reports for given item.
 */
public class CreateAuditReportAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<ItemModel, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(CreateAuditReportAction.class);

	protected static final String PARAMETER_REPORT_NAME = "reportName";
	protected static final String PARAMETER_AUDIT = "audit";
	protected static final String PARAMETER_ALLOWED_CONFIG_CODES = "allowedConfigCodes";
	protected static final String PARAMETER_PRESELECTED_CONFIG = "preselectedConfig";
	protected static final String REPORT_NAME_FORMAT = "PDR %s (%s)";
	protected static final String SOCKET_OUT_OPEN_WIZARD = "openWizard";
	protected static final String SETTING_REPORT_DATE_FORMAT = "reportDateFormat";
	protected static final String SETTING_PRESELECTED_REPORT_CONFIG = "preselectedReportConfigName";

	@Resource
	private TypeFacade typeFacade;
	@Resource
	private AuditConfigService auditConfigService;
	@Resource
	private LabelService labelService;

	@Override
	public ActionResult<Object> perform(final ActionContext<ItemModel> ctx)
	{
		final ItemModel selectedObject = ctx.getData();
		final String objectLabel = getLabelService().getObjectLabel(selectedObject);
		final String type = getTypeFacade().getType(selectedObject);

		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), AuditReportDataModel._TYPECODE);
		parameters.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT.getName(), selectedObject);
		parameters.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT_TYPE.getName(), type);

		final String reportName = prepareReportName(ctx, objectLabel);
		parameters.put(PARAMETER_REPORT_NAME, reportName);
		parameters.put(PARAMETER_AUDIT, false);
		final List<AuditReportConfig> configs = getAuditConfigService().getConfigsForRootType(type);
		final List<String> configStrings = configs != null
				? configs.stream().map(AuditReportConfig::getName).collect(Collectors.toList())
				: Collections.emptyList();
		parameters.put(PARAMETER_ALLOWED_CONFIG_CODES, configStrings);

		final String preselectedConfig = resolvePreselectedConfig(ctx, configStrings);
		parameters.put(PARAMETER_PRESELECTED_CONFIG, preselectedConfig);
		sendOutput(SOCKET_OUT_OPEN_WIZARD, parameters);

		return new ActionResult<>(ActionResult.SUCCESS, null);
	}

	protected String resolvePreselectedConfig(final ActionContext<ItemModel> ctx, final List<String> configs)
	{
		String preselectedConfig = null;
		final String preselectedConfigSetting = (String) ctx.getParameter(SETTING_PRESELECTED_REPORT_CONFIG);
		if (!CollectionUtils.isEmpty(configs) && StringUtils.isNotBlank(preselectedConfigSetting))
		{
			if (configs.stream().anyMatch(cfg -> cfg.equalsIgnoreCase(preselectedConfigSetting)))
			{
				preselectedConfig = preselectedConfigSetting;
			}
			else
			{
				LOG.debug("Preselected config not found on list", preselectedConfigSetting);
			}
		}
		return preselectedConfig;
	}

	protected String prepareReportName(final ActionContext<ItemModel> ctx, final String objectLabel)
	{
		final String formattedDate = prepareFormattedDate(ctx);
		return String.format(REPORT_NAME_FORMAT, objectLabel, formattedDate);
	}

	protected String prepareFormattedDate(final ActionContext<ItemModel> ctx)
	{
		final String dateFormatParameter = (String) ctx.getParameter(SETTING_REPORT_DATE_FORMAT);
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormatParameter));
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public AuditConfigService getAuditConfigService()
	{
		return auditConfigService;
	}

	public void setAuditConfigService(final AuditConfigService auditConfigService)
	{
		this.auditConfigService = auditConfigService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	@Override
	public boolean canPerform(final ActionContext<ItemModel> ctx)
	{
		return ctx.getData() != null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ItemModel> ctx)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ItemModel> ctx)
	{
		return null;
	}
}

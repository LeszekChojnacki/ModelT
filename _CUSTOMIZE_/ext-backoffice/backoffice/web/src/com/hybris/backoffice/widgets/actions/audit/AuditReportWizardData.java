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

import de.hybris.platform.auditreport.model.AuditReportTemplateModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.Collection;



/**
 * POJO used to populate wizard's fields for audit report.
 */
public class AuditReportWizardData
{
	private String reportName;
	private String auditReportConfig;
	private ItemModel sourceItem;
	private boolean audit = false;
	private Collection<LanguageModel> exportedLanguages;
	private AuditReportTemplateModel auditReportTemplate;

	public String getReportName()
	{
		return reportName;
	}

	public void setReportName(final String reportName)
	{
		this.reportName = reportName;
	}

	public ItemModel getSourceItem()
	{
		return sourceItem;
	}

	public void setSourceItem(final ItemModel sourceItem)
	{
		this.sourceItem = sourceItem;
	}

	public boolean isAudit()
	{
		return audit;
	}

	public void setAudit(final boolean audit)
	{
		this.audit = audit;
	}

	public Collection<LanguageModel> getExportedLanguages()
	{
		return exportedLanguages;
	}

	public void setExportedLanguages(final Collection<LanguageModel> exportedLanguages)
	{
		this.exportedLanguages = exportedLanguages;
	}

	public String getAuditReportConfig()
	{
		return auditReportConfig;
	}

	public void setAuditReportConfig(final String auditReportConfig)
	{
		this.auditReportConfig = auditReportConfig;
	}

	public AuditReportTemplateModel getAuditReportTemplate()
	{
		return auditReportTemplate;
	}

	public void setAuditReportTemplate(final AuditReportTemplateModel auditReportTemplate)
	{
		this.auditReportTemplate = auditReportTemplate;
	}
}

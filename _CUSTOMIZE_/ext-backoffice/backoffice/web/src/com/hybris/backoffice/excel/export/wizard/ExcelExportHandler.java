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
package com.hybris.backoffice.excel.export.wizard;

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.internal.i18n.LocalizationService;
import de.hybris.platform.servicelayer.type.AttributeModifierCriteria;
import de.hybris.platform.servicelayer.type.AttributeModifiers;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Filedownload;

import com.google.common.collect.Lists;
import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.classification.ExcelClassificationAttributeFactory;
import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelExportParams;
import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.exporting.ExcelExportPreProcessor;
import com.hybris.backoffice.excel.exporting.ExcelExportService;
import com.hybris.backoffice.excel.exporting.ExcelExportWorkbookPostProcessor;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Exports selected attributes to excel file.
 * <ul>
 * <li>{@value PARAM_EXCEL_EXPORT_FORM_MODEL_KEY} - defines where in model excel export form is
 * {@link ExcelExportWizardForm} - required</li>
 * <li>{@value PARAM_EXCEL_INCLUDE_CLASSIFICATION} - "true" or "false". Defines whether include classification
 * attributes to the file or not. Default value is "true"</li>
 * </ul>
 */
public class ExcelExportHandler implements FlowActionHandler
{
	public static final String PARAM_EXCEL_EXPORT_FORM_MODEL_KEY = "excelExportFormModelKey";
	public static final String PARAM_EXCEL_INCLUDE_CLASSIFICATION = "excelIncludeClassification";
	private static final Logger LOG = Logger.getLogger(ExcelExportHandler.class);

	private ExcelExportService exportService;
	private NotificationService notificationService;
	private TypeService typeService;
	private LocalizationService localizationService;
	private ExcelExportPreProcessor excelExportPreProcessor;
	private ExcelExportWorkbookPostProcessor excelExportWorkbookPostProcessor;
	private ObjectFacade objectFacade;
	private ExcelClassificationAttributeFactory excelClassificationAttributeFactory;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final WidgetInstanceManager wim = adapter.getWidgetInstanceManager();
		final ExcelExportWizardForm form = getExcelExportForm(wim, params);
		if (form == null)
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_EXPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_MISSING_FORM, NotificationEvent.Level.FAILURE);
			return;
		}

		if ((!form.isExportTemplate() && !form.getAttributesForm().hasSelectedAttributes()))
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_EXPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_MISSING_ATTRIBUTES, NotificationEvent.Level.FAILURE);
			return;
		}

		final List<SelectedAttribute> selectedAttributes = getSelectedAttributes(form);
		final List<ExcelAttribute> additionalAttributes = toIncludeClassification(params) ? getAdditionalAttributes(form)
				: Collections.emptyList();

		final int chosenAttributesCount = selectedAttributes.size() + additionalAttributes.size();
		final int exportAttributesMaxCount = getExportAttributesMaxCount();
		if (exportAttributesMaxCount < chosenAttributesCount)
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_EXPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_ATTRIBUTES_MAX_COUNT_EXCEEDED, NotificationEvent.Level.FAILURE,
					exportAttributesMaxCount, chosenAttributesCount);
			return;
		}

		Workbook template = null;
		try
		{
			final List<ItemModel> itemsToExport = form.isExportTemplate() ? new ArrayList<ItemModel>() : form.getPageable().getAllResults();

			final ExcelExportParams excelExportParams = excelExportPreProcessor
					.process(new ExcelExportParams(itemsToExport, selectedAttributes, additionalAttributes));

			template = exportData(form, excelExportParams.getItemsToExport(), excelExportParams.getSelectedAttributes());
			if (template == null)
			{
				getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_EXPORT,
						ExcelConstants.NOTIFICATION_EVENT_CANNOT_GENERATE_WORKBOOK, NotificationEvent.Level.FAILURE);
				return;
			}

			final ExcelExportResult excelExportResult = new ExcelExportResult(template, excelExportParams.getItemsToExport(),
					excelExportParams.getSelectedAttributes(), excelExportParams.getAdditionalAttributes(),
					getAvailableAdditionalAttributes(form));

			excelExportWorkbookPostProcessor.process(excelExportResult);

			triggerDownloading(template, getFilename(form));
			adapter.done();
		}
		finally
		{
			IOUtils.closeQuietly(template);
		}
	}

	protected List<SelectedAttribute> getSelectedAttributes(final ExcelExportWizardForm excelForm)
	{
		if (excelForm == null || excelForm.getAttributesForm() == null)
		{
			return Collections.emptyList();
		}

		return toSelectedAttributes(excelForm.getTypeCode(), excelForm.getAttributesForm().getSelectedAttributes());
	}

	protected Workbook exportData(final ExcelExportWizardForm form, final List<ItemModel> itemsToExport,
			final List<SelectedAttribute> selectedAttributes)
	{
		if (form.isExportTemplate())
		{
			return exportService.exportTemplate(form.getPageable().getTypeCode());
		}

		return CollectionUtils.isNotEmpty(itemsToExport) ? exportService.exportData(itemsToExport, selectedAttributes)
				: exportService.exportData(form.getPageable().getTypeCode(), selectedAttributes);
	}

	protected final List<ExcelAttribute> getAdditionalAttributes(final ExcelExportWizardForm excelForm)
	{
		if (excelForm == null || excelForm.getClassificationAttributesForm() == null)
		{
			return Collections.emptyList();
		}

		final AttributeChooserForm attributeChooserForm = excelForm.getClassificationAttributesForm();

		return toClassificationAttributes(attributeChooserForm.isIncludeAll() ? //
				attributeChooserForm.getSelectedAttributes() : attributeChooserForm.getChosenAttributes() //
		);
	}

	protected final List<ExcelAttribute> getAvailableAdditionalAttributes(final ExcelExportWizardForm excelForm)
	{
		if (excelForm == null || excelForm.getClassificationAttributesForm() == null)
		{
			return Collections.emptyList();
		}

		return toClassificationAttributes(SetUtils.union(excelForm.getClassificationAttributesForm().getSelectedAttributes(),
				excelForm.getClassificationAttributesForm().getAvailableAttributes()));
	}

	protected List<SelectedAttribute> toSelectedAttributes(final String typeCode, final Set<Attribute> selectedAttributes)
	{
		final AttributeModifierCriteria criteria = new AttributeModifierCriteria(0, AttributeModifiers.ALL,
				AttributeModifiers.PRIVATE);
		final Map<String, AttributeDescriptorModel> descriptors = getTypeService().getAttributesForModifiers(typeCode, criteria)
				.stream().collect(Collectors.toMap(AttributeDescriptorModel::getQualifier, attr -> attr, (a, b) -> a));

		final Map<String, String> langTagToLanguageIsoCode = new HashMap<>();

		return selectedAttributes.stream()//
				.map(attribute -> toSelectedAttributes(attribute, descriptors.get(attribute.getQualifier()),
						langTagToLanguageIsoCode))//
				.flatMap(List::stream)//
				.map(SelectedAttribute.class::cast)//
				.sorted(this::comparingSelectedAttributes)//
				.collect(Collectors.toList());
	}

	protected List<SelectedAttribute> toSelectedAttributes(final Attribute attribute, final AttributeDescriptorModel descriptor,
			final Map<String, String> langTagToLanguageIsoCodeCache)
	{
		if (descriptor == null)
		{
			return Collections.emptyList();
		}

		final boolean isLocalized = attribute.hasSubAttributes();
		if (!isLocalized)
		{
			return Lists.newArrayList(new SelectedAttribute(descriptor));
		}
		else
		{
			return attribute.getSubAttributes().stream()
					.filter(localizedAttribute -> StringUtils.isNotBlank(localizedAttribute.getIsoCode())).map(localizedAttribute -> {
						final String isoCode = langTagToLanguageIsoCodeCache.computeIfAbsent(localizedAttribute.getIsoCode(),
								this::getLanguageIsoCodeForLocaleLangTag);
						return new SelectedAttribute(isoCode, descriptor);
					}).collect(Collectors.toList());
		}
	}

	protected List<ExcelAttribute> toClassificationAttributes(final Set<Attribute> attributes)
	{
		final Map<String, String> langTagToLanguageIsoCode = new HashMap<>();

		return extractLeafs(attributes).stream()//
				.map(attribute -> toClassificationAttribute(attribute, getClassAttributeAssignmentModel(attribute),
						langTagToLanguageIsoCode))//
				.map(ExcelAttribute.class::cast)//
				.sorted(this::comparingExcelAttribute)//
				.collect(Collectors.toList());
	}

	protected List<Attribute> extractLeafs(final Set<Attribute> attributes)
	{
		final List<Attribute> leafs = new ArrayList<>();
		attributes.forEach(attribute -> {
			if (attribute.hasSubAttributes())
			{
				leafs.addAll(extractLeafs(attribute.getSubAttributes()));
			}
			else
			{
				leafs.add(attribute);
			}
		});
		return leafs;
	}

	protected ClassAttributeAssignmentModel getClassAttributeAssignmentModel(final Attribute attribute)
	{
		try
		{
			return objectFacade.load(attribute.getQualifier());
		}
		catch (final ObjectNotFoundException e)
		{
			LOG.error(String.format("Cannot load %s for attribute '%s'", ClassAttributeAssignmentModel.class.getName(),
					attribute.getDisplayName()), e);
			return null;
		}
	}

	protected ExcelAttribute toClassificationAttribute(final Attribute attribute, final ClassAttributeAssignmentModel descriptor,
			final Map<String, String> langTagToLanguageIsoCodeCache)
	{

		if (StringUtils.isNotBlank(attribute.getIsoCode()))
		{
			final String isoCode = langTagToLanguageIsoCodeCache.computeIfAbsent(attribute.getIsoCode(),
					this::getLanguageIsoCodeForLocaleLangTag);
			return excelClassificationAttributeFactory.create(descriptor, isoCode);
		}
		else
		{
			return excelClassificationAttributeFactory.create(descriptor);
		}
	}

	private String getLanguageIsoCodeForLocaleLangTag(final String langTag)
	{
		return localizationService.getDataLanguageIsoCode(Locale.forLanguageTag(langTag));
	}

	protected int comparingSelectedAttributes(final SelectedAttribute a, final SelectedAttribute b)
	{
		if (StringUtils.equals(a.getName(), b.getName()))
		{
			return StringUtils.compare(a.getIsoCode(), b.getIsoCode());
		}
		return StringUtils.compare(a.getName(), b.getName());
	}

	protected int comparingExcelAttribute(final ExcelAttribute a, final ExcelAttribute b)
	{
		if (StringUtils.equals(a.getName(), b.getName()))
		{
			return StringUtils.compare(a.getIsoCode(), b.getIsoCode());
		}
		return StringUtils.compare(a.getName(), b.getName());
	}


	protected String getFilename(final ExcelExportWizardForm form)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd_HHmm");
		return String.format("%s_%s.xlsx", form.getTypeCode(), dateFormat.format(new Date()));
	}

	protected void triggerDownloading(final Workbook template, final String filename)
	{
		try
		{
			final ByteArrayOutputStream convertedExcel = new ByteArrayOutputStream();
			template.write(convertedExcel);
			Filedownload.save(convertedExcel.toByteArray(), Config.getParameter("mediatype.by.fileextension.xlsx"), filename);
		}
		catch (final IOException e)
		{
			LOG.error("Could not export excel file", e);
		}
	}

	protected int getExportAttributesMaxCount()
	{
		return Config.getInt("backoffice.excel.export.max.attributes", 200);
	}

	protected ExcelExportWizardForm getExcelExportForm(final WidgetInstanceManager wim, final Map<String, String> parameters)
	{
		final String excelFormModelKey = parameters.getOrDefault(PARAM_EXCEL_EXPORT_FORM_MODEL_KEY,
				ExcelConstants.EXCEL_FORM_PROPERTY);
		return wim.getModel().getValue(excelFormModelKey, ExcelExportWizardForm.class);
	}

	protected boolean toIncludeClassification(final Map<String, String> params)
	{
		return Boolean.parseBoolean(params.getOrDefault(PARAM_EXCEL_INCLUDE_CLASSIFICATION, Boolean.TRUE.toString()));
	}

	public ExcelExportService getExportService()
	{
		return exportService;
	}

	@Required
	public void setExportService(final ExcelExportService exportService)
	{
		this.exportService = exportService;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}

	@Required
	public void setLocalizationService(final LocalizationService localizationService)
	{
		this.localizationService = localizationService;
	}

	public ExcelExportWorkbookPostProcessor getExcelExportWorkbookPostProcessor()
	{
		return excelExportWorkbookPostProcessor;
	}

	@Required
	public void setExcelExportWorkbookPostProcessor(final ExcelExportWorkbookPostProcessor excelExportWorkbookPostProcessor)
	{
		this.excelExportWorkbookPostProcessor = excelExportWorkbookPostProcessor;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	@Required
	public void setExcelClassificationAttributeFactory(
			final ExcelClassificationAttributeFactory excelClassificationAttributeFactory)
	{
		this.excelClassificationAttributeFactory = excelClassificationAttributeFactory;
	}

	@Required
	public void setExcelExportPreProcessor(final ExcelExportPreProcessor excelExportPreProcessor)
	{
		this.excelExportPreProcessor = excelExportPreProcessor;
	}
}

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
package com.hybris.backoffice.excel.export.wizard.renderer;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.type.AttributeModifierCriteria;
import de.hybris.platform.servicelayer.type.AttributeModifiers;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;

import com.google.common.collect.Sets;
import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.attributechooser.AttributeChooserRenderer;
import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.export.wizard.ExcelExportWizardForm;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;
import com.hybris.backoffice.renderer.attributeschooser.AbstractAttributesExportRenderer;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * Renders components that allow to pick a list of attributes to be exported to excel for given items.
 * <p/>
 * Displays a list of all available attributes of an item (e.g. a Product), and a second list of selected attributes.
 * Mandatory and unique attributes are selected by default. Params:
 * <ul>
 * <li>{@value PARAM_EXCEL_EXPORT_FORM_MODEL_KEY} - defines where in model excel export form is
 * {@link ExcelExportWizardForm} - required</li>
 * <li>{@value PARAM_INCLUDE_ALL_SUPPORTED} - defines if include all should be available</li>
 * </ul>
 */
public class ExcelExportRenderer extends AbstractAttributesExportRenderer<AttributeDescriptorModel>
{
	/**
	 * @deprecated since 1808 use {@link #PARAM_INCLUDE_ALL_SUPPORTED}
	 */
	@Deprecated
	public static final String PARAM_EXCEL_INCLUDE_ALL_SUPPORTED = "includeAllSupported";
	public static final String PARAM_EXCEL_EXPORT_FORM_MODEL_KEY = "excelExportFormModelKey";

	private ExcelFilter<AttributeDescriptorModel> requiredFilters;
	private ExcelFilter<AttributeDescriptorModel> supportedFilters;
	private LabelService labelService;
	private TypeService typeService;
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	/**
	 * @deprecated since 6.7 no longer used please see {@link #attributesChooserRenderer}
	 */
	@Deprecated
	private WidgetComponentRenderer<Component, Object, com.hybris.backoffice.excel.export.wizard.renderer.attributechooser.AttributeChooserForm> attributeChooserRenderer;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> params,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final ExcelExportWizardForm form = getExcelExportForm(wim, params);
		if (form == null)
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_EXPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_MISSING_FORM, NotificationEvent.Level.FAILURE);
			return;
		}

		parent.addForward(AttributeChooserRenderer.EVENT_ATTRIBUTES_SELECTED, parent, Editor.ON_VALUE_CHANGED);

		final AttributeChooserForm attributesForm = form.getAttributesForm();
		if (!attributesForm.hasPopulatedAttributes())
		{
			populateAttributesChooserForm(attributesForm, form.getTypeCode());
		}
		getAttributesChooserRenderer().render(parent, createAttributesChooserConfig(wim, params), attributesForm, dataType, wim);
	}

	protected void populateAttributesChooserForm(final AttributeChooserForm attributesForm, final String typeCode)
	{
		final Set<String> supportedLanguages = getSupportedLanguages();
		final String currentLanguage = getCockpitLocaleService().getCurrentLocale().toLanguageTag();
		final Set<String> otherLanguages = new HashSet<>(supportedLanguages);
		otherLanguages.remove(currentLanguage);

		final Set<Attribute> available = new HashSet<>();
		final Set<Attribute> mandatory = new HashSet<>();
		for (final AttributeDescriptorModel attrDesc : getSupportedAttributes(typeCode))
		{
			if (getRequiredFilters().test(attrDesc))
			{
				final Attribute attribute;
				if (isLocalized(attrDesc))
				{
					attribute = createAttributeWithLocalizedChildren(attrDesc, Sets.newHashSet(currentLanguage), true);
					attribute.setMandatory(false);
					available.add(createAttributeWithLocalizedChildren(attrDesc, otherLanguages, false));
				}
				else
				{
					attribute = new Attribute(createAttributeQualifier(attrDesc), createAttributeName(attrDesc), true);
				}
				mandatory.add(attribute);
			}
			else
			{
				available.add(createAttributeWithLocalizedChildren(attrDesc, supportedLanguages, false));
			}
		}

		attributesForm.setAvailableAttributes(available);
		attributesForm.setChosenAttributes(mandatory);
	}

	protected Set<AttributeDescriptorModel> getSupportedAttributes(final String dataType)
	{
		final AttributeModifierCriteria criteria = new AttributeModifierCriteria(0, AttributeModifiers.ALL,
				AttributeModifiers.PRIVATE);

		return getTypeService().getAttributesForModifiers(dataType, criteria).stream()//
				.filter(getSupportedFilters())//
				.collect(Collectors.toSet());
	}

	/**
	 * @deprecated since 1808. Not used anymore, it is replaced by {@link ExcelFilter}
	 */
	@Deprecated
	protected boolean isSupported(final String dataType, final AttributeDescriptorModel attr)
	{
		return attr.getWritable() && attr.getReadable() && getExcelTranslatorRegistry().canHandle(attr)
				&& getPermissionFacade().canReadProperty(dataType, attr.getQualifier());
	}

	@Override
	protected String createAttributeQualifier(final AttributeDescriptorModel attrDesc)
	{
		return attrDesc.getQualifier();
	}

	@Override
	protected String createAttributeName(final AttributeDescriptorModel attrDesc)
	{
		return attrDesc.getName();
	}

	@Override
	protected boolean isLocalized(final AttributeDescriptorModel attr)
	{
		return BooleanUtils.isTrue(attr.getLocalized());
	}

	/**
	 * @deprecated since 1808. Not used anymore, it is replaced by {@link ExcelFilter}
	 */
	@Deprecated
	protected boolean isRequired(final AttributeDescriptorModel attributeDescriptor)
	{
		return BooleanUtils.isTrue(attributeDescriptor.getUnique())
				|| (BooleanUtils.isFalse(attributeDescriptor.getOptional()) && attributeDescriptor.getDefaultValue() == null);
	}

	protected String getLocalizedAttributeName(final String dataType, final String attr)
	{
		return getLabelService().getObjectLabel(ObjectValuePath.getPath(dataType, attr));
	}


	protected ExcelExportWizardForm getExcelExportForm(final WidgetInstanceManager wim, final Map<String, String> parameters)
	{
		final String excelFormModelKey = parameters.getOrDefault(ExcelExportRenderer.PARAM_EXCEL_EXPORT_FORM_MODEL_KEY,
				ExcelConstants.EXCEL_FORM_PROPERTY);
		return wim.getModel().getValue(excelFormModelKey, ExcelExportWizardForm.class);
	}

	public ExcelFilter<AttributeDescriptorModel> getRequiredFilters()
	{
		return requiredFilters;
	}

	@Required
	public void setRequiredFilters(final ExcelFilter<AttributeDescriptorModel> requiredFilters)
	{
		this.requiredFilters = requiredFilters;
	}

	public ExcelFilter<AttributeDescriptorModel> getSupportedFilters()
	{
		return supportedFilters;
	}

	@Required
	public void setSupportedFilters(final ExcelFilter<AttributeDescriptorModel> supportedFilters)
	{
		this.supportedFilters = supportedFilters;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
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

	public ExcelTranslatorRegistry getExcelTranslatorRegistry()
	{
		return excelTranslatorRegistry;
	}

	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}

	/**
	 * @deprecated since 6.7 no longer used - please see {@link #getAttributesChooserRenderer()} (WidgetComponentRenderer)}
	 */
	@Deprecated
	public WidgetComponentRenderer<Component, Object, com.hybris.backoffice.excel.export.wizard.renderer.attributechooser.AttributeChooserForm> getAttributeChooserRenderer()
	{
		return attributeChooserRenderer;
	}

	/**
	 * @deprecated since 6.7 no longer used - please see {@link #setAttributesChooserRenderer(WidgetComponentRenderer)}
	 */
	@Deprecated
	public void setAttributeChooserRenderer(
			final WidgetComponentRenderer<Component, Object, com.hybris.backoffice.excel.export.wizard.renderer.attributechooser.AttributeChooserForm> attributeChooserRenderer)
	{
		this.attributeChooserRenderer = attributeChooserRenderer;
	}

}

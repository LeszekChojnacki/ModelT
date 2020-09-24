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
package com.hybris.backoffice.bulkedit.renderer;

import de.hybris.platform.core.enums.RelationEndCardinalityEnum;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.core.model.type.RelationMetaTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.attributechooser.AttributeChooserRenderer;
import com.hybris.backoffice.attributechooser.AttributesChooserConfig;
import com.hybris.backoffice.bulkedit.BulkEditConstants;
import com.hybris.backoffice.bulkedit.BulkEditForm;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


/**
 * Renderers attributes selector for bulk edit. Params:
 * <ul>
 * <li>{@value PARAM_BULK_EDIT_FORM_MODEL_KEY} - path to bulk edit for {@link BulkEditForm} in widget model -
 * required</li>
 * <li>excludedQualifiers - comma separated list of qualifiers which should not be shown to the user</li>
 * <li>includeAllSupported - defines if include all attributes switch is available</li>
 * </ul>
 */
public class BulkEditAttributesSelectorRenderer extends DefaultCustomViewRenderer
{
	public static final String PARAM_EXCLUDED_QUALIFIERS = "excludedQualifiers";
	public static final String PARAM_BULK_EDIT_FORM_MODEL_KEY = "bulkEditFormModelKey";
	public static final String PARAM_INCLUDE_ALL_SUPPORTED = "includeAllSupported";
	private static final String LABEL_MISSING_FORM = "bulkedit.missing.form";

	private WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> attributeChooserRenderer;
	private CockpitLocaleService localeService;
	private PermissionFacade permissionFacade;
	private TypeFacade typeFacade;
	private NotificationService notificationService;
	private TypeService typeService;

	@Override
	public void render(final Component component, final ViewType viewType, final Map<String, String> params,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final BulkEditForm form = getBulkEditForm(wim, params);
		if (form == null)
		{
			component.appendChild(new Label(Labels.getLabel(LABEL_MISSING_FORM)));
			getNotificationService().notifyUser(BulkEditConstants.NOTIFICATION_SOURCE_BULK_EDIT,
					BulkEditConstants.NOTIFICATION_EVENT_TYPE_MISSING_FORM, NotificationEvent.Level.FAILURE);
			return;
		}

		final AttributeChooserForm attributeForm = form.getAttributesForm();
		if (!attributeForm.hasPopulatedAttributes())
		{
			attributeForm.setAvailableAttributes(
					populateSelectableAttributes(dataType, getEditedTypes(form), extractExcludedQualifiers(params)));
		}
		component.addForward(AttributeChooserRenderer.EVENT_ATTRIBUTES_SELECTED, component, Editor.ON_VALUE_CHANGED);
		getAttributeChooserRenderer().render(component, getAttributesChooserConfig(params), attributeForm, dataType, wim);
	}

	protected Set<Attribute> populateSelectableAttributes(final DataType dataType, final Set<String> editedTypes,
			final Set<String> excludedQualifiers)
	{
		final Set<String> locales = getPermissionFacade().getAllWritableLocalesForCurrentUser().stream().map(Locale::toLanguageTag)
				.collect(Collectors.toSet());

		final Locale currentLocale = getLocaleService().getCurrentLocale();
		final Predicate<DataAttribute> isNotExcluded = attr -> !excludedQualifiers.contains(attr.getQualifier());

		return dataType.getAttributes().stream()
				.filter(isNotExcluded.and(this::isEditable).and(attr -> canChangeProperty(editedTypes, attr.getQualifier()))
						.and(attr -> canBeAppliedForManyItems(dataType, attr)))
				.map(dataAttribute -> {
					final Attribute attr = new Attribute(dataAttribute.getQualifier(), dataAttribute.getLabel(currentLocale), false);

					if (dataAttribute.isLocalized())
					{
						attr.setSubAttributes(locales.stream().map(lang -> new Attribute(attr, lang)).collect(Collectors.toSet()));
					}
					return attr;
				}).collect(Collectors.toSet());
	}

	protected Set<String> getEditedTypes(final BulkEditForm bulkEditForm)
	{
		return bulkEditForm.getItemsToEdit().stream().map(getTypeFacade()::getType).collect(Collectors.toSet());
	}

	protected boolean canChangeProperty(final Set<String> types, final String property)
	{
		return types.stream().allMatch(type -> getPermissionFacade().canChangeProperty(type, property));
	}

	protected boolean isEditable(final DataAttribute dataAttribute)
	{
		return dataAttribute.isWritable();
	}

	protected boolean canBeAppliedForManyItems(final DataType dataType, final DataAttribute attribute)
	{
		return !attribute.isPartOf() && !isManyToOneRelation(dataType, attribute);
	}

	protected boolean isManyToOneRelation(final DataType dataType, final DataAttribute attribute)
	{
		final AttributeDescriptorModel attributeDescriptor = getTypeService().getAttributeDescriptor(dataType.getCode(),
				attribute.getQualifier());
		if (attributeDescriptor instanceof RelationDescriptorModel)
		{
			final RelationMetaTypeModel relationType = ((RelationDescriptorModel) attributeDescriptor).getRelationType();

			if (StringUtils.equals(relationType.getSourceTypeRole(), attribute.getQualifier()))
			{
				return RelationEndCardinalityEnum.MANY.equals(relationType.getSourceTypeCardinality())
						&& RelationEndCardinalityEnum.ONE.equals(relationType.getTargetTypeCardinality());
			}
			else
			{
				return RelationEndCardinalityEnum.ONE.equals(relationType.getSourceTypeCardinality())
						&& RelationEndCardinalityEnum.MANY.equals(relationType.getTargetTypeCardinality());
			}

		}
		return false;
	}

	protected BulkEditForm getBulkEditForm(final WidgetInstanceManager wim, final Map<String, String> params)
	{
		final String attributesFormModelKey = params.get(PARAM_BULK_EDIT_FORM_MODEL_KEY);
		return wim.getModel().getValue(attributesFormModelKey, BulkEditForm.class);
	}

	protected Set<String> extractExcludedQualifiers(final Map<String, String> params)
	{
		final String exclude = params.get(PARAM_EXCLUDED_QUALIFIERS);
		if (StringUtils.isNotBlank(exclude))
		{
			return Arrays.stream(StringUtils.split(exclude, ',')).map(String::trim).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	protected AttributesChooserConfig getAttributesChooserConfig(final Map<String, String> params)
	{
		final AttributesChooserConfig config = new AttributesChooserConfig();
		config.setIncludeAllSupported(Boolean.valueOf(params.get(PARAM_INCLUDE_ALL_SUPPORTED)));
		return config;
	}

	public WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> getAttributeChooserRenderer()
	{
		return attributeChooserRenderer;
	}

	public void setAttributeChooserRenderer(
			final WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> attributeChooserRenderer)
	{
		this.attributeChooserRenderer = attributeChooserRenderer;
	}

	public CockpitLocaleService getLocaleService()
	{
		return localeService;
	}

	public void setLocaleService(final CockpitLocaleService localeService)
	{
		this.localeService = localeService;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
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
}

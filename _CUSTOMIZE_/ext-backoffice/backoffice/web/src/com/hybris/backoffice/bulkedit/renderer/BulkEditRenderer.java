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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Span;

import com.google.common.collect.Maps;
import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.bulkedit.BulkEditConstants;
import com.hybris.backoffice.bulkedit.BulkEditForm;
import com.hybris.backoffice.bulkedit.BulkEditValidationHelper;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.common.EditorBuilder;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.components.validation.EditorValidation;
import com.hybris.cockpitng.components.validation.ValidatableContainer;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.CockpitComponentsUtils;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.validation.LocalizedQualifier;
import com.hybris.cockpitng.validation.ValidationHandler;
import com.hybris.cockpitng.validation.model.ValidationResult;
import com.hybris.cockpitng.validation.model.ValidationSeverity;
import com.hybris.cockpitng.widgets.configurableflow.validation.ConfigurableFlowValidationRenderer;
import com.hybris.cockpitng.widgets.configurableflow.validation.LocalizedValidationAwareCustomViewRenderer;
import com.hybris.cockpitng.widgets.configurableflow.validation.ValidationAwareCustomViewRenderer;


/**
 * Renders chosen attributes to be edit.
 * <ul>
 * <li>{@value PARAM_BULK_EDIT_FORM_MODEL_KEY} - path to bulk edit for {@link BulkEditForm} in widget model -
 * required</li>
 * <li>editor:qualifier - preferred editor for given qualifier</li>
 * <li>editor:qualifier:paramName - defines parameter which will be passed to an editor configured for given
 * qualifier</li>
 * </ul>
 */
public class BulkEditRenderer implements ValidationAwareCustomViewRenderer, LocalizedValidationAwareCustomViewRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(BulkEditRenderer.class);

	protected static final String PARAM_NAME_SEPARATOR = ":";
	protected static final String PARAM_EDITOR_PREFIX = "editor" + PARAM_NAME_SEPARATOR;
	protected static final String PARAM_BULK_EDIT_FORM_MODEL_KEY = "bulkEditFormModelKey";
	protected static final String PARAM_SHOW_VALIDATE_ALL_ATTRIBUTES_SWITCH = "showValidateAllAttributesSwitch";

	protected static final String SCLASS_ATTR = "bulkedit-attr";
	protected static final String SCLASS_SWITCH_CHECKBOX = "ye-switch-checkbox";
	protected static final String SCLASS_SWITCH_DELETE = SCLASS_SWITCH_CHECKBOX + "-delete";
	protected static final String SCLASS_VALIDATE_ALL_ATTRIBUTES = "bulkedit-validate-all-attributes";
	protected static final String SCLASS_ATTRIBUTE_LABEL_DESCRIPTION = "attribute-label-description";

	protected static final String LABEL_MISSING_FORM = "bulkedit.missing.form";
	protected static final String LABEL_MISSING_ATTRIBUTES = "bulkedit.missing.attributes";
	protected static final String LABEL_CLEAR_VALUE = "bulkedit.clear.value";
	protected static final String LABEL_MERGE_EXISTING = "bulkedit.merge.existing";
	protected static final String LABEL_VALIDATE_ALL_ATTRIBUTES = "bulkedit.validate.all.attributes.label";
	protected static final String LABEL_VALIDATE_ALL_ATTRIBUTES_POPUP = "bulkedit.validate.all.attributes.popup.message";


	private ConfigurableFlowValidationRenderer validationRenderer;
	private NotificationService notificationService;
	private BulkEditValidationHelper bulkEditValidationHelper;
	private BulkEditTemplateModelCreator bulkEditTemplateModelCreator;

	@Override
	public void render(final Component component, final ValidatableContainer validatableContainer, final ViewType viewType,
			final Map<String, String> params, final DataType dataType, final WidgetInstanceManager wim)
	{
		addBulkEditSclassToParent(component);

		final BulkEditForm form = getBulkEditForm(wim, params);
		if (form == null)
		{
			notificationService.notifyUser(BulkEditConstants.NOTIFICATION_SOURCE_BULK_EDIT,
					BulkEditConstants.NOTIFICATION_EVENT_TYPE_MISSING_FORM, NotificationEvent.Level.FAILURE);
			return;
		}
		
		if (form.getTemplateObject() == null)
		{
			bulkEditTemplateModelCreator.create(getTypeCode(wim)).ifPresent(form::setTemplateObject);
		}
		
		final AttributeChooserForm attributesForm = form.getAttributesForm();
		if (attributesForm == null || !attributesForm.hasSelectedAttributes())
		{
			notificationService.notifyUser(BulkEditConstants.NOTIFICATION_SOURCE_BULK_EDIT,
					BulkEditConstants.NOTIFICATION_EVENT_TYPE_MISSING_ATTRIBUTES, NotificationEvent.Level.FAILURE);
			return;
		}

		final ValidationHandler proxyValidationHandler = getBulkEditValidationHelper().createProxyValidationHandler(form);

		registerDataModelForVariants(wim, dataType);

		final Div attributesContainer = new Div();
		attributesContainer.setSclass(SCLASS_ATTR + "-cnt");
		attributesContainer.setParent(component);

		attributesForm.getSelectedAttributes().stream().sorted(Comparator.comparing(Attribute::getDisplayName))
				.forEach(selectedAttribute -> renderAttributeLine(attributesContainer, validatableContainer, proxyValidationHandler,
						dataType, selectedAttribute, wim, params, form));

		if (getShowValidateAllAttributesSwitchParameter(params))
		{
			renderValidateAllAttributesSwitch(component, params, form);
		}
	}

	protected String getTypeCode(final WidgetInstanceManager wim)
	{
		return wim.getModel().getValue("ctx.TYPE_CODE", String.class);
	}
	
	protected void addBulkEditSclassToParent(final Component parent)
	{
		final Optional<HtmlBasedComponent> wizard = CockpitComponentsUtils.findClosestComponent(parent, HtmlBasedComponent.class,
				"yw-wizard-content");
		wizard.ifPresent(w -> UITools.addSClass(w, "bulkedit-content"));
	}

	private boolean getShowValidateAllAttributesSwitchParameter(final Map<String, String> params)
	{
		final String param = params.getOrDefault(PARAM_SHOW_VALIDATE_ALL_ATTRIBUTES_SWITCH, Boolean.TRUE.toString());
		return Boolean.parseBoolean(param);
	}

	protected void renderValidateAllAttributesSwitch(final Component parent, final Map<String, String> params,
			final BulkEditForm form)
	{
		final Div container = new Div();
		container.setSclass(SCLASS_VALIDATE_ALL_ATTRIBUTES + "-cnt");
		container.setParent(parent);

		final Checkbox validateAllAttributesSwitch = new Checkbox(Labels.getLabel(LABEL_VALIDATE_ALL_ATTRIBUTES));
		validateAllAttributesSwitch
				.setSclass(String.format("%s %s", SCLASS_SWITCH_CHECKBOX, SCLASS_VALIDATE_ALL_ATTRIBUTES + "-switch"));
		validateAllAttributesSwitch.setParent(container);
		validateAllAttributesSwitch.setChecked(form.isValidateAllAttributes());
		validateAllAttributesSwitch.addEventListener(Events.ON_CHECK,
				(CheckEvent event) -> form.setValidateAllAttributes(event.isChecked()));

		final Span helpIcon = new Span();
		helpIcon
				.setSclass(String.format("%s %s", SCLASS_VALIDATE_ALL_ATTRIBUTES + "-help-icon", SCLASS_ATTRIBUTE_LABEL_DESCRIPTION));
		helpIcon.setParent(container);

		final Popup helpMessagePopup = new Popup();
		helpMessagePopup.setSclass(SCLASS_VALIDATE_ALL_ATTRIBUTES + "-popup");
		helpMessagePopup.setParent(parent);

		final Label popupText = new Label(Labels.getLabel(LABEL_VALIDATE_ALL_ATTRIBUTES_POPUP));
		popupText.setParent(helpMessagePopup);

		helpIcon.addEventListener(Events.ON_CLICK, event -> helpMessagePopup.open(helpIcon, "before_start"));
	}

	protected void renderAttributeLine(final Component parent, final ValidatableContainer validatableContainer,
			final ValidationHandler validationHandler, final DataType dataType, final Attribute selectedAttribute,
			final WidgetInstanceManager wim, final Map<String, String> params, final BulkEditForm form)
	{
		final Div attributesLine = new Div();
		attributesLine.setParent(parent);
		attributesLine.setSclass(SCLASS_ATTR);

		final Div labelColumn = new Div();
		labelColumn.setSclass(SCLASS_ATTR + "-name");
		labelColumn.appendChild(createAttributeLabel(selectedAttribute));

		final Div valueColumn = new Div();
		valueColumn.setSclass(SCLASS_ATTR + "-value yw-wizard-fullAttributeProperty");

		final Div featureColumn = new Div();
		featureColumn.setSclass(SCLASS_ATTR + "-clear-switch");

		attributesLine.appendChild(labelColumn);
		attributesLine.appendChild(valueColumn);
		attributesLine.appendChild(featureColumn);


		final Editor editor = createEditor(dataType, wim, selectedAttribute, params);
		valueColumn.appendChild(editor);
		enableValidationForEditor(editor, attributesLine, validatableContainer, validationHandler);
		final Optional<Checkbox> overrideCheckBox = createMergeCheckBox(dataType, selectedAttribute.getQualifier());
		overrideCheckBox.ifPresent(overrideSwitch -> {
			valueColumn.appendChild(overrideSwitch);
			overrideSwitch.setChecked(form.isQualifierToMerge(selectedAttribute.getQualifier()));
			overrideSwitch.addEventListener(Events.ON_CHECK, (CheckEvent event) -> {
				if (event.isChecked())
				{
					form.addQualifierToMerge(selectedAttribute.getQualifier());
				}
				else
				{
					form.removeQualifierToMerge(selectedAttribute.getQualifier());
				}
			});
		});
		createClearAttributeSwitch(dataType, selectedAttribute.getQualifier()).ifPresent(clearSwitch -> {
			featureColumn.appendChild(clearSwitch);
			final boolean clearAttributeInitialValue = form.isClearAttribute(selectedAttribute.getQualifier());
			clearSwitch.setChecked(clearAttributeInitialValue);
			editor.setReadOnly(clearAttributeInitialValue);
			UITools.addSClass(attributesLine, SCLASS_ATTR + "-cleared");
			clearSwitch.addEventListener(Events.ON_CHECK, (CheckEvent event) -> {
				if (event.isChecked())
				{
					form.addQualifierToClear(selectedAttribute.getQualifier());
					clearModelValue(wim, dataType, selectedAttribute, editor.getProperty());
				}
				else
				{
					form.removeQualifierToClear(selectedAttribute.getQualifier());
				}
				editor.editorValidationChanged();
				editor.setReadOnly(event.isChecked());
				UITools.modifySClass(attributesLine, SCLASS_ATTR + "-cleared", event.isChecked());
				overrideCheckBox.ifPresent(checkbox -> checkbox.setDisabled(event.isChecked()));
			});
		});

	}

	protected void registerDataModelForVariants(final WidgetInstanceManager widgetInstanceManager, final DataType dataType)
	{
		final Map variantAttributesMapModel = widgetInstanceManager.getModel()
				.getValue(BulkEditConstants.VARIANT_ATTRIBUTES_MAP_MODEL, Map.class);
		if (variantAttributesMapModel == null)
		{
			widgetInstanceManager.getModel().put(BulkEditConstants.VARIANT_ATTRIBUTES_MAP_MODEL, prepareNewVariantsModel(dataType));
		}
	}

	private static HashMap<Object, Object> prepareNewVariantsModel(final DataType dataType)
	{
		final HashMap<Object, Object> map = Maps.newHashMap();
		for (final String attribute : getVariantAttributes(dataType))
		{
			map.put(attribute, null);
		}
		return map;
	}

	private static Collection<String> getVariantAttributes(final DataType dataType)
	{
		return dataType.getAttributes().stream().filter(DataAttribute::isVariantAttribute).map(DataAttribute::getQualifier)
				.collect(Collectors.toList());
	}

	protected void clearModelValue(final WidgetInstanceManager wim, final DataType dataType, final Attribute attribute,
			final String fullAttributeProperty)
	{
		final DataAttribute dataAttribute = dataType.getAttribute(attribute.getQualifier());
		Object newValue = null;
		if (dataAttribute.isLocalized() && attribute.hasSubAttributes())
		{
			final Map<Locale, Object> nullValue = new HashMap<>();
			getSelectedLocalesForAttribute(attribute).forEach(locale -> nullValue.put(locale, null));
			newValue = nullValue;
		}
		wim.getModel().setValue(fullAttributeProperty, newValue);
	}

	protected void enableValidationForEditor(final Editor editor, final HtmlBasedComponent validationContainer,
			final ValidatableContainer validatableContainer, final ValidationHandler validationHandler)
	{
		editor.initValidation(validatableContainer, validationHandler);
		editor.addEventListener(EditorValidation.ON_VALIDATION_CHANGED, validationEvent -> {
			validationRenderer.cleanAllValidationCss(validationContainer);

			final ValidationResult validationResult = (ValidationResult) validationEvent.getData();
			final ValidationSeverity highestNotConfirmedSeverity = validationResult.getHighestNotConfirmedSeverity();
			UITools.addSClass(validationContainer, validationRenderer.getSeverityStyleClass(highestNotConfirmedSeverity));
		});
	}

	protected Component createAttributeLabel(final Attribute attr)
	{
		return new Label(attr.getDisplayName());
	}

	protected Optional<Checkbox> createClearAttributeSwitch(final DataType dataType, final String qualifier)
	{
		final DataAttribute attribute = dataType.getAttribute(qualifier);

		if (attribute != null && !attribute.isMandatory())
		{
			final Checkbox clearAttributeSwitch = new Checkbox(Labels.getLabel(LABEL_CLEAR_VALUE));
			clearAttributeSwitch.setSclass(String.format("%s %s", SCLASS_SWITCH_CHECKBOX, SCLASS_SWITCH_DELETE));
			return Optional.of(clearAttributeSwitch);
		}
		return Optional.empty();
	}

	protected Editor createEditor(final DataType genericType, final WidgetInstanceManager widgetInstanceManager,
			final Attribute attribute, final Map<String, String> params)
	{
		final EditorBuilder editorBuilder = createEditorBuilder(genericType, widgetInstanceManager, attribute, params)
				.configure(createItemKey(genericType, attribute, params), genericType.getAttribute(attribute.getQualifier()))
				.apply(editor -> editor.setEditorLabel(StringUtils.EMPTY))
				.addParameters(extractEditorParameters(params, attribute.getQualifier())).ifTrue(attribute.hasSubAttributes())
				.thenApply(editor -> {
					final Set<Locale> chosenLocales = getSelectedLocalesForAttribute(attribute);
					editor.setReadableLocales(chosenLocales);
					editor.setWritableLocales(chosenLocales);
				});
		getPreferredEditor(params, attribute.getQualifier()).ifPresent(editorBuilder::useEditor);

		return buildEditor(editorBuilder, genericType, widgetInstanceManager, attribute, params);
	}

	private String createItemKey(final DataType genericType, final Attribute attribute, final Map<String, String> params)
	{
		if (!isVariantAttribute(genericType, attribute))
		{
			final ObjectValuePath templateObjectPath = getTemplateObjectPath(params);
			return templateObjectPath.buildPath();
		}
		else
		{
			return BulkEditConstants.VARIANT_ATTRIBUTES_MAP_MODEL;
		}
	}

	private static boolean isVariantAttribute(final DataType genericType, final Attribute attribute)
	{
		return genericType.getAttribute(attribute.getQualifier()).isVariantAttribute();
	}

	protected EditorBuilder createEditorBuilder(final DataType genericType, final WidgetInstanceManager widgetInstanceManager,
			final Attribute attribute, final Map<String, String> params)
	{
		return new EditorBuilder(widgetInstanceManager);
	}

	protected Editor buildEditor(final EditorBuilder builder, final DataType genericType,
			final WidgetInstanceManager widgetInstanceManager, final Attribute attribute, final Map<String, String> params)
	{
		return builder.build();
	}

	protected Optional<Checkbox> createMergeCheckBox(final DataType dataType, final String qualifier)
	{
		final DataAttribute attribute = dataType.getAttribute(qualifier);

		if (attribute != null && isMergeable(attribute))
		{
			final Checkbox overrideExisting = new Checkbox(Labels.getLabel(LABEL_MERGE_EXISTING));
			overrideExisting.setSclass(SCLASS_SWITCH_CHECKBOX);
			return Optional.of(overrideExisting);
		}
		return Optional.empty();
	}

	protected boolean isMergeable(final DataAttribute attribute)
	{
		switch (attribute.getAttributeType())
		{
			case COLLECTION:
			case LIST:
			case SET:
			case MAP:
				return !attribute.isLocalized();
			default:
				return false;
		}
	}

	protected Map<String, Object> extractEditorParameters(final Map<String, String> params, final String qualifier)
	{
		final String prefix = PARAM_EDITOR_PREFIX + qualifier + PARAM_NAME_SEPARATOR;
		return params.entrySet().stream().filter(entry -> StringUtils.startsWith(entry.getKey(), prefix))
				.collect(Collectors.toMap(entry -> StringUtils.substringAfter(entry.getKey(), prefix), Map.Entry::getValue,
						(a, b) -> StringUtils.defaultIfEmpty((String) a, (String) b)));
	}

	protected Set<Locale> getSelectedLocalesForAttribute(final Attribute attribute)
	{
		return attribute.getSubAttributes().stream().map(Attribute::getIsoCode).filter(StringUtils::isNotBlank).sorted()
				.map(Locale::forLanguageTag).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	protected Optional<String> getPreferredEditor(final Map<String, String> params, final String qualifier)
	{
		final String preferredEditor = params.get(PARAM_EDITOR_PREFIX + qualifier);
		return StringUtils.isNotBlank(preferredEditor) ? Optional.of(preferredEditor) : Optional.empty();
	}

	protected BulkEditForm getBulkEditForm(final WidgetInstanceManager wim, final Map<String, String> params)
	{
		final String attributesFormModelKey = params.get(PARAM_BULK_EDIT_FORM_MODEL_KEY);
		return wim.getModel().getValue(attributesFormModelKey, BulkEditForm.class);
	}

	@Override
	public Set<String> getValidationProperties(final WidgetInstanceManager wim, final Map<String, String> params)
	{
		final BulkEditForm form = getBulkEditForm(wim, params);
		if (form.getAttributesForm() != null && form.getAttributesForm().getChosenAttributes() != null)
		{
			final ObjectValuePath templateItemPrefix = getTemplateObjectPath(params);
			return getBulkEditValidationHelper().getValidatableProperties(getBulkEditForm(wim, params)).stream()
					.map(ObjectValuePath::parse).map(path -> path.prepend(templateItemPrefix)).map(ObjectValuePath::buildPath)
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	@Override
	public Collection<LocalizedQualifier> getValidationPropertiesWithLocales(final WidgetInstanceManager wim,
			final Map<String, String> params)
	{
		final BulkEditForm form = getBulkEditForm(wim, params);
		if (form.getAttributesForm() != null && form.getAttributesForm().getChosenAttributes() != null)
		{
			final ObjectValuePath templateItemPrefix = getTemplateObjectPath(params);

			return getBulkEditValidationHelper().getValidatablePropertiesWithLocales(form).stream().map(localizedQualifier -> {
				final String name = ObjectValuePath.parse(localizedQualifier.getName()).prepend(templateItemPrefix).buildPath();
				return new LocalizedQualifier(name, localizedQualifier.getLocales());
			}).collect(Collectors.toSet());
		}

		return Collections.emptyList();
	}

	protected ObjectValuePath getTemplateObjectPath(final Map<String, String> params)
	{
		final String formPath = params.get(PARAM_BULK_EDIT_FORM_MODEL_KEY);
		if (StringUtils.isBlank(formPath))
		{
			LOG.warn("Missing template item param");
			return ObjectValuePath.empty();
		}
		return ObjectValuePath.parse(formPath).append("templateObject");
	}

	public ConfigurableFlowValidationRenderer getValidationRenderer()
	{
		return validationRenderer;
	}

	@Required
	public void setValidationRenderer(final ConfigurableFlowValidationRenderer validationRenderer)
	{
		this.validationRenderer = validationRenderer;
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

	public BulkEditValidationHelper getBulkEditValidationHelper()
	{
		return bulkEditValidationHelper;
	}

	@Required
	public void setBulkEditValidationHelper(final BulkEditValidationHelper bulkEditValidationHelper)
	{
		this.bulkEditValidationHelper = bulkEditValidationHelper;
	}

	public BulkEditTemplateModelCreator getBulkEditTemplateModelCreator()
	{
		return bulkEditTemplateModelCreator;
	}

	@Required
	public void setBulkEditTemplateModelCreator(final BulkEditTemplateModelCreator bulkEditTemplateModelCreator)
	{
		this.bulkEditTemplateModelCreator = bulkEditTemplateModelCreator;
	}
	
}

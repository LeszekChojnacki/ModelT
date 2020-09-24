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

/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package com.hybris.backoffice.renderer.attributeschooser;

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;

import com.google.common.collect.Sets;
import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.attributechooser.AttributeChooserRenderer;
import com.hybris.backoffice.attributechooser.AttributesChooserConfig;
import com.hybris.backoffice.excel.classification.ExcelClassificationService;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;


/**
 * Renders components that allow to pick a list of classification attributes to be exported to excel for given products.
 * <p/>
 * Displays a list of available classification attributes of given Products, and a second list of selected attributes.
 * Mandatory attributes are selected by default. Current implementation only allows to select classification attributes
 * of all selected products. Params:
 * <ul>
 * <li>{@value PARAM_ATTRIBUTES_FORM} - defines where in model attributes form is {@link AttributeChooserForm} -
 * required</li>
 * <li>{@value PARAM_ITEMS_TO_EXPORT} - defines spel to get items from model {@link AttributeChooserForm} -
 * required</li>
 * <li>{@value PARAM_INCLUDE_ALL_SUPPORTED} - defines if include all should be available</li>
 * <li>{@value PARAM_EMPTY_ATTRIBUTES_MESSAGE_KEY} - defines message key for label displayed in case there are no
 * attributes to select.</li>
 * <li>{@value PARAM_RETRIEVE_MODE} - defines in what way classification classes are retrieved. There are 3 modes:
 * <ul>
 * <li>{@value #RETRIEVE_MODE_ITEMS_INTERSECTION} - default value. In this mode displayed classification classes are
 * retrieved from given items and intersected - it means that only common classes are displayed.</li>
 * <li>{@value #RETRIEVE_MODE_ITEMS_UNION} - in this mode displayed classification classes are retrieved from given
 * items - all classification classes from given items are then displayed.</li>
 * <li>{@value #RETRIEVE_MODE_ALL} - in this mode all classification classes from the system are displayed - not only
 * from given items</li>
 * </ul>
 * </li>
 * </ul>
 * Available classification attributes can be filtered by {@link #setSupportedAttributesPredicate(Predicate)}
 */
public class ClassificationAttributesExportRenderer extends AbstractAttributesExportRenderer<ClassAttributeAssignmentModel>
{
	public static final String PARAM_ATTRIBUTES_FORM = "classificationAttributesFormModelKey";
	public static final String PARAM_ITEMS_TO_EXPORT = "itemsToExport";
	public static final String PARAM_EMPTY_ATTRIBUTES_MESSAGE_KEY = "emptyAttributesMessageKey";
	public static final String PARAM_RETRIEVE_MODE = "retrieveMode";

	public static final String RETRIEVE_MODE_ITEMS_INTERSECTION = "ITEMS_INTERSECTION";
	public static final String RETRIEVE_MODE_ITEMS_UNION = "ITEMS_UNION";
	public static final String RETRIEVE_MODE_ALL = "ALL";
	private final Collection<String> retrieveModes = Lists.newArrayList(RETRIEVE_MODE_ALL, RETRIEVE_MODE_ITEMS_INTERSECTION,
			RETRIEVE_MODE_ITEMS_UNION);

	private LabelService labelService;
	private ExcelClassificationService excelClassificationService;
	private Predicate<ClassAttributeAssignmentModel> supportedAttributesPredicate;
	private Predicate<ClassificationSystemModel> blacklistedClassificationPredicate;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> params,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		parent.addForward(AttributeChooserRenderer.EVENT_ATTRIBUTES_SELECTED, parent, Editor.ON_VALUE_CHANGED);

		final AttributeChooserForm attributesForm = getAttributesForm(wim, params);
		if (!attributesForm.hasPopulatedAttributes())
		{
			final List<ItemModel> items = getItems(wim, params);
			populateAttributesChooserForm(attributesForm, items, getRetrieveMode(params));
		}
		getAttributesChooserRenderer().render(parent, createAttributesChooserConfig(wim, params), attributesForm, dataType, wim);
	}

	protected void populateAttributesChooserForm(final AttributeChooserForm attributesForm, final List<ItemModel> items,
			final String retrieveMode)
	{
		final LanguagesCache languagesCache = new LanguagesCache(getSupportedLanguages(),
				cockpitLocaleService.getCurrentLocale().toLanguageTag());

		final Set<Attribute> mandatory = new HashSet<>();
		final Set<Attribute> available = new HashSet<>();

		filterByPermissionCheck(getCommonClassificationClasses(items, retrieveMode)).entrySet().stream()
				.filter(classificationVersion -> !blacklistedClassificationPredicate.test(classificationVersion.getKey().getCatalog()))
				.map(entry -> createClassificationClasses(entry.getKey(), entry.getValue(), languagesCache))
				.forEach(createSystemVersionNodes -> {
					if (createSystemVersionNodes.getAvailable().hasSubAttributes())
					{
						available.add(createSystemVersionNodes.getAvailable());
					}
					if (createSystemVersionNodes.getMandatory().hasSubAttributes())
					{
						mandatory.add(createSystemVersionNodes.getMandatory());
					}
				});

		attributesForm.setAvailableAttributes(available);
		attributesForm.setChosenAttributes(mandatory);
	}

	protected Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> filterByPermissionCheck(
			final Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> map)
	{
		final Function<Map.Entry<ClassificationSystemVersionModel, List<ClassificationClassModel>>, List<ClassificationClassModel>> mapper = entry -> entry
				.getValue().stream().filter(getPermissionFacade()::canReadInstance).collect(Collectors.toList());
		return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, mapper, (v1, v2) -> v1));
	}

	protected Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getCommonClassificationClasses(
			final Collection<ItemModel> items, final String retrieveMode)
	{
		switch (retrieveMode)
		{
			case RETRIEVE_MODE_ITEMS_INTERSECTION:
				return excelClassificationService.getItemsIntersectedClassificationClasses(items);
			case RETRIEVE_MODE_ITEMS_UNION:
				return excelClassificationService.getItemsAddedClassificationClasses(items);
			case RETRIEVE_MODE_ALL:
				return excelClassificationService.getAllClassificationClasses();
			default:
				throw new IllegalArgumentException(String.format("'%s' is not correct. Available %s are: %s", retrieveMode,
						PARAM_RETRIEVE_MODE, retrieveModes.stream().collect(Collectors.joining(", ", "[", "]"))));
		}
	}

	protected String getRetrieveMode(final Map<String, String> params)
	{
		if (params.containsKey(PARAM_RETRIEVE_MODE))
		{
			return params.get(PARAM_RETRIEVE_MODE);
		}
		return RETRIEVE_MODE_ITEMS_INTERSECTION;
	}

	private CreatedNodes createClassificationClasses(final ClassificationSystemVersionModel classificationSystemVersion,
			final List<ClassificationClassModel> classificationClasses, final LanguagesCache languagesCache)
	{
		final String classificationSystemVersionName = labelService.getObjectLabel(classificationSystemVersion);
		final Attribute availableSystemVersionNode = new Attribute(classificationSystemVersion.getPk().toString(),
				classificationSystemVersionName, false);

		final Attribute mandatorySystemVersionNode = new Attribute(classificationSystemVersion.getPk().toString(),
				classificationSystemVersionName, false);

		classificationClasses.stream()
				.map(classificationClass -> createClassificationAttributes(classificationClass, languagesCache))
				.forEach(createdNodes -> {
					if (createdNodes.getAvailable().hasSubAttributes())
					{
						availableSystemVersionNode.addSubAttribute(createdNodes.getAvailable());
					}

					if (createdNodes.getMandatory().hasSubAttributes())
					{
						mandatorySystemVersionNode.addSubAttribute(createdNodes.getMandatory());
					}
				});

		return new CreatedNodes(availableSystemVersionNode, mandatorySystemVersionNode);
	}

	private CreatedNodes createClassificationAttributes(final ClassificationClassModel classificationClass,
			final LanguagesCache languagesCache)
	{
		final String classificationClassName = labelService.getShortObjectLabel(classificationClass);
		final Attribute availableClassNode = new Attribute(classificationClass.getPk().toString(), classificationClassName, false);
		final Attribute mandatoryClassNode = new Attribute(classificationClass.getPk().toString(), classificationClassName, false);

		for (final ClassAttributeAssignmentModel classificationAttribute : classificationClass
				.getDeclaredClassificationAttributeAssignments())
		{
			if (!isSupported(classificationAttribute))
			{
				continue;
			}

			if (classificationAttribute.getMandatory())
			{
				final Attribute attribute;
				if (isLocalized(classificationAttribute))
				{
					attribute = createAttributeWithLocalizedChildren(classificationAttribute,
							Sets.newHashSet(languagesCache.getCurrentLanguage()), true);
					attribute.setMandatory(false);
					availableClassNode.addSubAttribute(
							createAttributeWithLocalizedChildren(classificationAttribute, languagesCache.getOtherLanguages(), false));
				}
				else
				{
					attribute = new Attribute(createAttributeQualifier(classificationAttribute),
							createAttributeName(classificationAttribute), true);
				}
				mandatoryClassNode.addSubAttribute(attribute);
			}
			else
			{
				availableClassNode.addSubAttribute(
						createAttributeWithLocalizedChildren(classificationAttribute, languagesCache.getSupportedLanguages(), false));
			}

		}
		return new CreatedNodes(availableClassNode, mandatoryClassNode);
	}

	private static class CreatedNodes
	{

		private final Attribute available;
		private final Attribute mandatory;

		public CreatedNodes(final Attribute available, final Attribute mandatory)
		{
			this.available = available;
			this.mandatory = mandatory;
		}

		public Attribute getAvailable()
		{
			return available;
		}

		public Attribute getMandatory()
		{
			return mandatory;
		}


	}

	private static class LanguagesCache
	{

		final Set<String> supportedLanguages;
		final String currentLanguage;
		final Set<String> otherLanguages;

		public LanguagesCache(final Set<String> supportedLanguages, final String currentLanguage)
		{
			this.supportedLanguages = supportedLanguages;
			this.currentLanguage = currentLanguage;
			this.otherLanguages = new HashSet<>(supportedLanguages);
			otherLanguages.remove(currentLanguage);
		}

		public Set<String> getSupportedLanguages()
		{
			return supportedLanguages;
		}

		public String getCurrentLanguage()
		{
			return currentLanguage;
		}

		public Set<String> getOtherLanguages()
		{
			return otherLanguages;
		}


	}

	protected boolean isSupported(final ClassAttributeAssignmentModel classificationAttribute)
	{
		return supportedAttributesPredicate == null || supportedAttributesPredicate.test(classificationAttribute);
	}

	protected AttributeChooserForm getAttributesForm(final WidgetInstanceManager wim, final Map<String, String> parameters)
	{
		final String excelFormModelKey = parameters.getOrDefault(PARAM_ATTRIBUTES_FORM, "attributesForm");
		return wim.getModel().getValue(excelFormModelKey, AttributeChooserForm.class);
	}

	protected List<ItemModel> getItems(final WidgetInstanceManager wim, final Map<String, String> parameters)
	{
		final String excelFormModelKey = parameters.getOrDefault(PARAM_ITEMS_TO_EXPORT, "itemsToEdit");
		return wim.getModel().getValue(excelFormModelKey, List.class);
	}

	@Override
	protected String createAttributeQualifier(final ClassAttributeAssignmentModel attrDesc)
	{
		return attrDesc.getPk().toString();
	}

	public Predicate<ClassAttributeAssignmentModel> getSupportedAttributesPredicate()
	{
		return supportedAttributesPredicate;
	}

	public void setSupportedAttributesPredicate(final Predicate<ClassAttributeAssignmentModel> supportedAttributePredicate)
	{
		this.supportedAttributesPredicate = supportedAttributePredicate;
	}

	@Override
	protected AttributesChooserConfig createAttributesChooserConfig(final WidgetInstanceManager wim,
			final Map<String, String> params)
	{
		final AttributesChooserConfig config = super.createAttributesChooserConfig(wim, params);
		config.setOpenFirstMultiChildNode(true);
		if (params.containsKey(PARAM_EMPTY_ATTRIBUTES_MESSAGE_KEY))
		{
			final String emptyAttributesMessage = Labels.getLabel(params.get(PARAM_EMPTY_ATTRIBUTES_MESSAGE_KEY));
			if (StringUtils.isNotBlank(emptyAttributesMessage))
			{
				config.setEmptyAttributesMessage(emptyAttributesMessage);
			}
		}
		return config;
	}

	@Override
	protected String createAttributeName(final ClassAttributeAssignmentModel attrDesc)
	{
		return labelService.getShortObjectLabel(attrDesc);
	}

	@Override
	protected boolean isLocalized(final ClassAttributeAssignmentModel classificationAttribute)
	{
		return classificationAttribute.getLocalized();
	}

	public ExcelClassificationService getExcelClassificationService()
	{
		return excelClassificationService;
	}

	@Required
	public void setExcelClassificationService(final ExcelClassificationService excelClassificationService)
	{
		this.excelClassificationService = excelClassificationService;
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

	public Predicate<ClassificationSystemModel> getBlacklistedClassificationPredicate()
	{
		return blacklistedClassificationPredicate;
	}

	@Required
	public void setBlacklistedClassificationPredicate(final Predicate<ClassificationSystemModel> blacklistedClassificationPredicate)
	{
		this.blacklistedClassificationPredicate = blacklistedClassificationPredicate;
	}
}

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
package com.hybris.backoffice.excel.exporting;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.variants.model.VariantProductModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.exporting.data.filter.ExcelExportAttributePredicate;
import com.hybris.backoffice.excel.exporting.data.filter.ExcelExportTypePredicate;
import com.hybris.backoffice.excel.exporting.data.filter.PermissionCrudAttributePredicate;
import com.hybris.backoffice.excel.exporting.data.filter.PermissionCrudTypePredicate;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;
import com.hybris.backoffice.variants.BackofficeVariantsService;


/**
 * Service for exporting data to excel workbook
 */
public class DefaultExcelExportService implements ExcelExportService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultExcelExportService.class);

	private DefaultExcelExportDivider divider;
	private ExcelExportDivider excelExportDivider;
	private String templatePath;
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	private ExcelTemplateService excelTemplateService;
	private TypeService typeService;
	private CommonI18NService commonI18NService;
	private PermissionCRUDService permissionCRUDService;
	private ModelService modelService;
	private ExcelWorkbookService excelWorkbookService;
	private ExcelSheetService excelSheetService;
	private ExcelCellService excelCellService;
	private ExcelHeaderService excelHeaderService;
	private I18NService i18NService;
	private SessionService sessionService;
	private UserService userService;
	private BackofficeVariantsService backofficeVariantsService;
	private Set<ExcelExportAttributePredicate> attributePredicates;
	private Set<ExcelExportTypePredicate> typePredicates;

	@Override
	public Workbook exportTemplate(final String typeCode)
	{
		final Map<String, Set<ItemModel>> itemsByType = new LinkedHashMap<>();

		final ComposedTypeModel type = getTypeService().getComposedTypeForCode(typeCode);
		if (BooleanUtils.isFalse(type.getAbstract()))
		{
			itemsByType.put(typeCode, Collections.emptySet());
		}
		final Collection<ComposedTypeModel> subTypes = type.getAllSubTypes();
		if (subTypes != null)
		{
			subTypes.stream()//
					.filter(subType -> BooleanUtils.isNotTrue(subType.getAbstract()))//
					.forEach(subtype -> itemsByType.put(subtype.getCode(), Collections.emptySet()));
		}
		return exportData(itemsByType, Collections.emptyList());
	}

	@Override
	public Workbook exportData(final List<ItemModel> selectedItems, final List<SelectedAttribute> selectedAttributes)
	{
		final Collection<ItemModel> refreshedItems = refreshSelectedItems(selectedItems);
		final Map<String, Set<ItemModel>> itemsByType = getExcelExportDivider().groupItemsByType(refreshedItems);
		return exportData(itemsByType, selectedAttributes);
	}

	protected List<ItemModel> refreshSelectedItems(final List<ItemModel> selectedItems)
	{
		final List<ItemModel> refreshedItems = new ArrayList<>();
		for (final ItemModel itemToRefresh : selectedItems)
		{
			try
			{
				getModelService().refresh(itemToRefresh);
				refreshedItems.add(itemToRefresh);
			}
			catch (final RuntimeException ex)
			{
				LOG.debug("Cannot refresh item", ex);
			}
		}
		return refreshedItems;
	}

	@Override
	public Workbook exportData(final String typeCode, final List<SelectedAttribute> selectedAttributes)
	{
		final Map<String, Set<ItemModel>> itemsByType = new HashMap<>();
		itemsByType.put(typeCode, Collections.emptySet());
		return exportData(itemsByType, selectedAttributes);
	}

	protected Workbook exportData(final Map<String, Set<ItemModel>> itemsByType, final List<SelectedAttribute> selectedAttributes)
	{
		final Map<String, Set<ItemModel>> itemsByTypeFiltered = applyTypePredicates(itemsByType);
		final Collection<SelectedAttribute> selectedAttributesFiltered = applyAttributePredicates(selectedAttributes);

		final Map<String, Set<SelectedAttribute>> attributesByType = getExcelExportDivider()
				.groupAttributesByType(itemsByTypeFiltered.keySet(), selectedAttributesFiltered);

		final Workbook workbook = getExcelWorkbookService().createWorkbook(loadExcelTemplate());

		attributesByType.forEach((typeCode, attributes) -> {
			final Sheet sheet = getExcelSheetService().createTypeSheet(workbook, typeCode);

			addHeader(sheet, attributes);
			addValues(itemsByTypeFiltered, typeCode, attributes, sheet);
		});
		signWorkbookFile(workbook);
		return workbook;
	}

	protected Map<String, Set<ItemModel>> applyTypePredicates(final Map<String, Set<ItemModel>> itemsByType)
	{
		final Predicate<Map.Entry<String, Set<ItemModel>>> applyTypePredicatesToItemByType = itemByType -> {
			final String typeCode = itemByType.getKey();
			final ComposedTypeModel type = typeService.getComposedTypeForCode(typeCode);
			return typePredicates.stream().allMatch(predicate -> predicate.test(type));
		};
		return itemsByType.entrySet().stream() //
				.filter(applyTypePredicatesToItemByType) //
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	protected List<SelectedAttribute> applyAttributePredicates(final List<SelectedAttribute> selectedAttributes)
	{
		final Predicate<SelectedAttribute> applyAttributePredicatesToSelectedAttribute = selectedAttribute -> attributePredicates
				.stream().allMatch(predicate -> predicate.test(selectedAttribute.getAttributeDescriptor()));
		return selectedAttributes.stream() //
				.filter(applyAttributePredicatesToSelectedAttribute) //
				.collect(Collectors.toList());
	}

	protected void signWorkbookFile(final Workbook workbook)
	{
		if (workbook instanceof XSSFWorkbook)
		{
			final POIXMLProperties workbookProperties = ((XSSFWorkbook) workbook).getProperties();
			workbookProperties.getCustomProperties().addProperty(ExcelTemplateConstants.CustomProperties.FILE_SIGNATURE,
					ExcelTemplateConstants.CustomProperties.FILE_SIGNATURE);
		}
	}


	/**
	 * @deprecated since 1811, attribute permission checking is now achieved by injecting into
	 *             {@link #setAttributePredicates(Set)} the {@link PermissionCrudAttributePredicate}
	 * @see PermissionCrudAttributePredicate
	 */
	@Deprecated
	protected List<SelectedAttribute> filterByPermissions(final List<SelectedAttribute> selectedAttributes)
	{
		final List<SelectedAttribute> filtered = selectedAttributes.stream().filter(attr -> getPermissionCRUDService()//
				.canReadAttribute(attr.getAttributeDescriptor()))//
				.collect(Collectors.toList());

		final Collection<SelectedAttribute> removed = CollectionUtils.removeAll(selectedAttributes, filtered);
		if (CollectionUtils.isNotEmpty(removed))
		{
			final List<String> removedAttributes = removed.stream().map(SelectedAttribute::getQualifier)
					.collect(Collectors.toList());
			LOG.warn("Insufficient permissions for attributes: {}", removedAttributes);
		}

		return filtered;
	}

	/**
	 * @deprecated since 1811, type permission checking is now achieved by injecting into
	 *             {@link #setAttributePredicates(Set)} the {@link PermissionCrudTypePredicate }
	 * @see PermissionCrudTypePredicate
	 */
	@Deprecated
	protected Map<String, Set<ItemModel>> filterByPermissions(final Map<String, Set<ItemModel>> itemsByType)
	{
		final Map<String, Set<ItemModel>> filtered = itemsByType.entrySet().stream()//
				.filter(mapEntry -> getPermissionCRUDService().canReadType(mapEntry.getKey()))//
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Collection<String> removed = CollectionUtils.removeAll(itemsByType.keySet(), filtered.keySet());
		if (CollectionUtils.isNotEmpty(removed))
		{
			LOG.warn("Insufficient permissions for types: {}", removed);
		}
		return filtered;
	}

	protected InputStream loadExcelTemplate()
	{
		return getClass().getResourceAsStream(getTemplatePath());
	}

	protected void addHeader(final Sheet sheet, final Set<SelectedAttribute> selectedAttributes)
	{
		getExcelHeaderService().insertAttributesHeader(sheet,
				selectedAttributes.stream()
						.map(attr -> new ExcelAttributeDescriptorAttribute(attr.getAttributeDescriptor(), attr.getIsoCode()))
						.collect(Collectors.toList()));
	}

	protected void addValues(final Map<String, Set<ItemModel>> itemsByType, final String type,
			final Set<SelectedAttribute> selectedAttributes, final Sheet sheet)
	{
		final Sheet pkSheet = getExcelSheetService().createOrGetUtilitySheet(sheet.getWorkbook(),
				ExcelTemplateConstants.UtilitySheet.PK.getSheetName());
		itemsByType.get(type).forEach(item -> {
			final Row row = getExcelSheetService().createEmptyRow(sheet);
			final AtomicInteger cellIndex = new AtomicInteger(0);
			insertPkRow(pkSheet, row, item);
			for (final SelectedAttribute selectedAttribute : selectedAttributes)
			{
				getExcelTranslatorRegistry().getTranslator(selectedAttribute.getAttributeDescriptor()).ifPresent(translator -> {
					translator.exportData(selectedAttribute.getAttributeDescriptor(), getItemAttribute(item, selectedAttribute))
							.ifPresent(
									excelValue -> getExcelCellService().insertAttributeValue(row.createCell(cellIndex.get()), excelValue));
					cellIndex.incrementAndGet();
				});
			}
		});
	}

	private void insertPkRow(final Sheet pkSheet, final Row row, final ItemModel item)
	{
		final Row emptyRow = getExcelSheetService().createEmptyRow(pkSheet);
		getExcelCellService().insertAttributeValue(emptyRow.createCell(ExcelTemplateConstants.PkColumns.PK),
				item.getPk().getLongValue());
		getExcelCellService().insertAttributeValue(emptyRow.createCell(ExcelTemplateConstants.PkColumns.SHEET_NAME),
				row.getSheet().getSheetName());
		getExcelCellService().insertAttributeValue(emptyRow.createCell(ExcelTemplateConstants.PkColumns.ROW_INDEX),
				row.getRowNum());
	}

	protected Object getItemAttribute(final ItemModel item, final SelectedAttribute selectedAttribute)
	{
		final String qualifier = selectedAttribute.getAttributeDescriptor().getQualifier();
		try
		{
			if (item instanceof VariantProductModel)
			{
				return getVariantAttribute((VariantProductModel) item, selectedAttribute,
						selectedAttribute.getAttributeDescriptor().getQualifier());
			}
			return getModelAttribute(item, selectedAttribute, selectedAttribute.getAttributeDescriptor().getQualifier());
		}
		catch (final AttributeNotSupportedException ex)
		{
			LOG.debug(String.format("Cannot get attribute [%s] value for type [%s]. Fallback with jalo.", qualifier,
					item.getItemtype()));
			return selectedAttribute.isLocalized()
					? item.getProperty(qualifier, getCommonI18NService().getLocaleForIsoCode(selectedAttribute.getIsoCode()))
					: item.getProperty(qualifier);
		}
	}

	private Object getModelAttribute(final ItemModel item, final SelectedAttribute selectedAttribute, final String qualifier)
	{
		if (selectedAttribute.isLocalized())
		{
			final Locale locale = getCommonI18NService().getLocaleForIsoCode(selectedAttribute.getIsoCode());
			return getModelService().getAttributeValue(item, qualifier, locale);
		}
		return getModelService().getAttributeValue(item, qualifier);
	}

	private Object getVariantAttribute(final VariantProductModel item, final SelectedAttribute selectedAttribute,
			final String qualifier)
	{
		if (selectedAttribute.isLocalized())
		{
			final Locale locale = getCommonI18NService().getLocaleForIsoCode(selectedAttribute.getIsoCode());
			return getBackofficeVariantsService().getLocalizedVariantAttributeValue(item, qualifier).get(locale);
		}
		return getBackofficeVariantsService().getVariantAttributeValue(item, qualifier);
	}

	/**
	 * @deprecated since 6.7 please use {@link #getExcelExportDivider()}
	 */
	@Deprecated
	public DefaultExcelExportDivider getDivider()
	{
		return divider;
	}

	/**
	 * @deprecated since 6.7 please use {@link #setExcelExportDivider(ExcelExportDivider)}
	 */
	@Deprecated
	public void setDivider(final DefaultExcelExportDivider divider)
	{
		this.divider = divider;
	}

	public ExcelExportDivider getExcelExportDivider()
	{
		return excelExportDivider;
	}

	@Required
	public void setExcelExportDivider(final ExcelExportDivider excelExportDivider)
	{
		this.excelExportDivider = excelExportDivider;
	}

	public String getTemplatePath()
	{
		return templatePath;
	}

	@Required
	public void setTemplatePath(final String templatePath)
	{
		this.templatePath = templatePath;
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
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelTemplateService getExcelTemplateService()
	{
		return excelTemplateService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Required
	@Deprecated
	public void setExcelTemplateService(final ExcelTemplateService excelTemplateService)
	{
		this.excelTemplateService = excelTemplateService;
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

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @deprecated since 1811 - not used anymore, the logic responsible for permission checking is now extracted to
	 *             {@link PermissionCrudTypePredicate} and {@link PermissionCrudAttributePredicate}
	 * @see PermissionCrudTypePredicate
	 * @see PermissionCrudAttributePredicate
	 */
	@Deprecated
	public PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	/**
	 * @deprecated since 1811 - not used anymore, the logic responsible for permission checking is now extracted to
	 *             {@link PermissionCrudTypePredicate} and {@link PermissionCrudAttributePredicate}
	 * @see PermissionCrudTypePredicate
	 * @see PermissionCrudAttributePredicate
	 */
	@Deprecated
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
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

	public ExcelSheetService getExcelSheetService()
	{
		return excelSheetService;
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}

	public ExcelWorkbookService getExcelWorkbookService()
	{
		return excelWorkbookService;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
	}

	public ExcelCellService getExcelCellService()
	{
		return excelCellService;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}

	public ExcelHeaderService getExcelHeaderService()
	{
		return excelHeaderService;
	}

	@Required
	public void setExcelHeaderService(final ExcelHeaderService excelHeaderService)
	{
		this.excelHeaderService = excelHeaderService;
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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

	public BackofficeVariantsService getBackofficeVariantsService()
	{
		return backofficeVariantsService;
	}

	@Required
	public void setBackofficeVariantsService(final BackofficeVariantsService backofficeVariantsService)
	{
		this.backofficeVariantsService = backofficeVariantsService;
	}

	public Set<ExcelExportAttributePredicate> getAttributePredicates()
	{
		return attributePredicates;
	}

	@Required
	public void setAttributePredicates(final Set<ExcelExportAttributePredicate> attributePredicates)
	{
		this.attributePredicates = attributePredicates;
	}

	public Set<ExcelExportTypePredicate> getTypePredicates()
	{
		return typePredicates;
	}

	@Required
	public void setTypePredicates(final Set<ExcelExportTypePredicate> typePredicates)
	{
		this.typePredicates = typePredicates;
	}
}

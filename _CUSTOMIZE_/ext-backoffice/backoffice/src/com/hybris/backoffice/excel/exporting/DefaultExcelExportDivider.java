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
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.template.mapper.ExcelMapper;


/**
 * Allows grouping items and attributes by their type
 */
public class DefaultExcelExportDivider implements ExcelExportDivider
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultExcelExportDivider.class);

	private ExcelMapper<ComposedTypeModel, AttributeDescriptorModel> mapper;
	private TypeService typeService;
	private ModelService modelService;
	private CommonI18NService commonI18NService;
	private PermissionCRUDService permissionCRUDService;

	public Map<String, Set<ItemModel>> groupItemsByType(final List<ItemModel> items)
	{
		return items.stream().collect(Collectors.groupingBy(modelService::getModelType, Collectors.toSet()));
	}

	public Map<String, Set<SelectedAttribute>> groupAttributesByType(final Set<String> typeCodes,
			final List<SelectedAttribute> selectedAttributes)
	{
		return typeCodes.stream()
				.collect(Collectors.toMap(Function.identity(), typeCode -> getAttributes(selectedAttributes, typeCode), (a, b) -> a));
	}

	protected Set<SelectedAttribute> getAttributes(final List<SelectedAttribute> selectedAttributes, final String typeCode)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(typeCode);

		final Set<String> selectedQualifiers = selectedAttributes.stream()
				.map(selectedAttribute -> selectedAttribute.getAttributeDescriptor().getQualifier()).collect(Collectors.toSet());

		final Set<SelectedAttribute> attributes = new LinkedHashSet<>();
		final Set<SelectedAttribute> required = getMissingRequiredAndUniqueAttributes(composedType, selectedQualifiers);
		attributes.addAll(filterByPermissions(required));
		attributes.addAll(selectedAttributes);
		return attributes;
	}

	protected List<SelectedAttribute> filterByPermissions(final Collection<SelectedAttribute> selectedAttributes)
	{
		final List<SelectedAttribute> filtered = selectedAttributes.stream()
				.filter(attr -> permissionCRUDService//
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

	protected Set<SelectedAttribute> getMissingRequiredAndUniqueAttributes(final ComposedTypeModel composedType,
			final Set<String> selectedQualifiers)
	{
		final Predicate<AttributeDescriptorModel> skipAlreadySelected = attribute -> !selectedQualifiers
				.contains(attribute.getQualifier());

		final String language = commonI18NService.getCurrentLanguage().getIsocode();
		final Function<AttributeDescriptorModel, String> langIsoCode = attributeDescriptor -> attributeDescriptor.getLocalized()
				? language : null;

		return mapper.apply(composedType).stream().filter(skipAlreadySelected)
				.map(attributeDescriptor -> new SelectedAttribute(langIsoCode.apply(attributeDescriptor), attributeDescriptor))
				.collect(Collectors.toSet());
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

	public PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	@Required
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

	public ExcelMapper<ComposedTypeModel, AttributeDescriptorModel> getMapper()
	{
		return mapper;
	}

	@Required
	public void setMapper(final ExcelMapper<ComposedTypeModel, AttributeDescriptorModel> mapper)
	{
		this.mapper = mapper;
	}
}

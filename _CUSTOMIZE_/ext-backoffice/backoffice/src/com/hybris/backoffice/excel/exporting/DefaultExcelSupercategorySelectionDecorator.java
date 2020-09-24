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

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.DescriptorModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;
import de.hybris.platform.servicelayer.type.TypeService;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ExcelExportParams;
import com.hybris.backoffice.excel.data.SelectedAttribute;


/**
 * {@link ExcelExportParamsDecorator} that appends 'supercategories' attribute selection.
 */
public class DefaultExcelSupercategorySelectionDecorator implements ExcelExportParamsDecorator
{
	private TypeService typeService;
	private PermissionCRUDService permissionCRUDService;
	private int order = 0;

	/**
	 * Adds <strong>supercategories</strong> attribute to {@link ExcelExportParams#getSelectedAttributes} if:
	 * <ul>
	 * <li>supercategory is not yet selected,</li>
	 * <li>and at least one classification attribute has been selected,</li>
	 * <li>and access to supercategory attribute is not forbidden.</li>
	 * </ul>
	 * Otherwise {@link ExcelExportParams} is returned without any modification.
	 *
	 * @param excelExportParams
	 *           that should be populated with supercategories attribute
	 * @return excelExportParams with selected supercategory attribute if the above conditions are met
	 */
	@Override
	public @Nonnull ExcelExportParams decorate(final @Nonnull ExcelExportParams excelExportParams)
	{
		if (hasNotSelectedSupercategoriesAttribute(excelExportParams)
				&& hasAtLeastOneClassificationAttributeSelected(excelExportParams) && canReadSupercategoriesAttribute())
		{
			excelExportParams.getSelectedAttributes().add(new SelectedAttribute(getProductSupercategoriesAttribute()));
		}
		return excelExportParams;
	}

	protected boolean hasNotSelectedSupercategoriesAttribute(final ExcelExportParams excelExportParams)
	{
		return excelExportParams.getSelectedAttributes() //
				.stream() //
				.map(SelectedAttribute::getAttributeDescriptor) //
				.map(DescriptorModel::getQualifier) //
				.noneMatch(ProductModel.SUPERCATEGORIES::equals);
	}

	protected boolean hasAtLeastOneClassificationAttributeSelected(final ExcelExportParams excelExportParams)
	{
		return excelExportParams.getAdditionalAttributes() //
				.stream() //
				.anyMatch(ExcelClassificationAttribute.class::isInstance);
	}

	protected boolean canReadSupercategoriesAttribute()
	{
		return permissionCRUDService.canReadAttribute(getProductSupercategoriesAttribute());
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	// optional
	public void setOrder(final int order)
	{
		this.order = order;
	}

	public ComposedTypeModel getProductType()
	{
		return typeService.getComposedTypeForCode(ProductModel._TYPECODE);
	}

	public AttributeDescriptorModel getProductSupercategoriesAttribute()
	{
		return typeService.getAttributeDescriptor(getProductType(), ProductModel.SUPERCATEGORIES);
	}
}

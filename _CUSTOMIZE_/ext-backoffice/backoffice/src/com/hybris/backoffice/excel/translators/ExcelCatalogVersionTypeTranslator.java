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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Default excel translator for catalog version.
 */
public class ExcelCatalogVersionTypeTranslator extends AbstractCatalogVersionAwareTranslator<CatalogVersionModel>
{

	private ExcelFilter<AttributeDescriptorModel> excelUniqueFilter;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;
	private TypeService typeService;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptorModel)
	{
		final TypeModel typeModel = typeService.getTypeForCode(CatalogVersionModel._TYPECODE);
		return typeService.isAssignableFrom(typeModel, attributeDescriptorModel.getAttributeType());
	}

	@Override
	public Optional<Object> exportData(final CatalogVersionModel objectToExport)
	{
		return Optional.ofNullable(exportCatalogVersionData(objectToExport));
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		return new ImpexValue(catalogVersionData(importParameters.getSingleValueParameters()),
				new ImpexHeaderValue.Builder(catalogVersionHeader(ProductModel._TYPECODE))
						.withUnique(excelUniqueFilter.test(attributeDescriptor))
						.withMandatory(getMandatoryFilter().test(attributeDescriptor)).withLang(importParameters.getIsoCode())
						.withQualifier(attributeDescriptor.getQualifier()).build());
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return referenceCatalogVersionFormat();
	}

	public ExcelFilter<AttributeDescriptorModel> getExcelUniqueFilter()
	{
		return excelUniqueFilter;
	}

	@Required
	public void setExcelUniqueFilter(final ExcelFilter<AttributeDescriptorModel> excelUniqueFilter)
	{
		this.excelUniqueFilter = excelUniqueFilter;
	}

	public ExcelFilter<AttributeDescriptorModel> getMandatoryFilter()
	{
		return mandatoryFilter;
	}

	@Required
	public void setMandatoryFilter(final ExcelFilter<AttributeDescriptorModel> mandatoryFilter)
	{
		this.mandatoryFilter = mandatoryFilter;
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

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
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Default translator for base product of variant product.
 */
public class ExcelBaseProductTypeTranslator extends AbstractCatalogVersionAwareTranslator<ProductModel>
{

	private ExcelFilter<AttributeDescriptorModel> excelUniqueFilter;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;
	private static final String PATTERN = "%s:%s";

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor instanceof RelationDescriptorModel
				&& attributeDescriptor.getAttributeType() instanceof ComposedTypeModel && VariantProductModel._PRODUCT2VARIANTRELATION
						.equals(((RelationDescriptorModel) attributeDescriptor).getRelationType().getCode());
	}

	@Override
	public Optional<Object> exportData(final ProductModel objectToExport)
	{
		if (objectToExport != null && objectToExport.getCatalogVersion() != null)
		{
			final CatalogVersionModel catalogVersion = objectToExport.getCatalogVersion();
			return Optional.ofNullable(String.format(PATTERN, objectToExport.getCode(), exportCatalogVersionData(catalogVersion)));
		}
		return Optional.empty();
	}

	/**
	 * Returns reference format as "baseProductCode:version:catalog".
	 *
	 * @param attributeDescriptor
	 *           {@link AttributeDescriptorModel}
	 * @return Format how a reference should be presented
	 */
	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return String.format(PATTERN, VariantProductModel.BASEPRODUCT, referenceCatalogVersionFormat());
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final String catalogVersion = catalogVersionData(importParameters.getSingleValueParameters());
		final String baseProduct = catalogVersion != null ? String.format(PATTERN,
				importParameters.getSingleValueParameters().get(VariantProductModel.BASEPRODUCT), catalogVersion) : null;
		return new ImpexValue(baseProduct,
				new ImpexHeaderValue.Builder(String.format("%s(%s, %s)", VariantProductModel.BASEPRODUCT, ProductModel.CODE,
						catalogVersionHeader(ProductModel._TYPECODE))).withUnique(excelUniqueFilter.test(attributeDescriptor))
								.withMandatory(getMandatoryFilter().test(attributeDescriptor)).withLang(importParameters.getIsoCode())
								.withQualifier(attributeDescriptor.getQualifier()).build());
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
}

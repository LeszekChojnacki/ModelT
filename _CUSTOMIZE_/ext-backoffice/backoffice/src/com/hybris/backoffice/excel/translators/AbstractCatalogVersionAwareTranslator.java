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

import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract class for translators of types which are catalog version aware.
 * 
 * @param <T>
 *           - class which can be handled by translator. The class should be catalog version aware.
 */
public abstract class AbstractCatalogVersionAwareTranslator<T> extends AbstractExcelValueTranslator<T>
{

	private CatalogTypeService catalogTypeService;
	private static final String PATTERN = "%s:%s";

	/**
	 * Transforms catalog version model into String value in format catalog:version
	 * 
	 * @param objectToExport
	 *           - catalog version model which should be transformed
	 * @return string (catalog:version) represents given catalog version
	 */
	public String exportCatalogVersionData(final CatalogVersionModel objectToExport)
	{
		if (objectToExport != null && objectToExport.getCatalog() != null && objectToExport.getCatalog().getId() != null
				&& objectToExport.getVersion() != null)
		{
			return String.format(PATTERN, objectToExport.getCatalog().getId(), objectToExport.getVersion());
		}
		return ":";
	}

	/**
	 * Prepares formatted value (catalog:version) based on params. Method assumes that params contain values for keys:
	 * "catalog" and "version"
	 *
	 * @param params
	 *           maps which should contain values for keys: "catalog" and "version"
	 * @return value in format: "catalog:version"
	 */
	protected String catalogVersionData(final Map<String, String> params)
	{
		final String version = params.get(CatalogVersionModel.VERSION);
		final String catalog = params.get(CatalogVersionModel.CATALOG);
		return StringUtils.isNotBlank(version) && StringUtils.isNotBlank(catalog) ? String.format(PATTERN, version, catalog) : null;
	}

	/**
	 * Returns header value for catalog version property. This method uses
	 * {@link CatalogTypeService#getCatalogVersionContainerAttribute(String)} in order to obtain catalog version
	 * qualifier of type which is catalog version aware.
	 * 
	 * @param typeCode
	 *           of type which is catalog version aware
	 * @return value in format catalogVersionQualifier(version, catalog(id))
	 */
	protected String catalogVersionHeader(final String typeCode)
	{
		final String catalogVersionQualifier = getCatalogTypeService().getCatalogVersionContainerAttribute(typeCode);
		return String.format("%s(%s,%s(%s))", catalogVersionQualifier, CatalogVersionModel.VERSION, CatalogVersionModel.CATALOG,
				CatalogModel.ID);
	}

	/**
	 * Returns reference format for catalog version. Default implementation returns "catalog:version"
	 * 
	 * @return reference format for catalog version.
	 */
	public String referenceCatalogVersionFormat()
	{
		return String.format(PATTERN, CatalogVersionModel.CATALOG, CatalogVersionModel.VERSION);
	}

	public CatalogTypeService getCatalogTypeService()
	{
		return catalogTypeService;
	}

	@Required
	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}

}

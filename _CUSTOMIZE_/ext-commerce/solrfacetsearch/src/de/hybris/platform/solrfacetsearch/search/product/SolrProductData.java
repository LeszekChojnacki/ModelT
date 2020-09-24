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
package de.hybris.platform.solrfacetsearch.search.product;

import java.util.Collection;


public class SolrProductData
{

	private String code;
	private String name;
	private String description;
	private String catalogVersion;
	private String catalog;
	private Long pk;
	private Collection<String> categories;
	private Double price;
	private String ean;


	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *           the code to set
	 */
	public void setCode(final String code)
	{
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *           the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *           the description to set
	 */
	public void setDescription(final String description)
	{
		this.description = description;
	}

	/**
	 * @return the catalogVersion
	 */
	public String getCatalogVersion()
	{
		return catalogVersion;
	}

	/**
	 * @param catalogVersion
	 *           the catalogVersion to set
	 */
	public void setCatalogVersion(final String catalogVersion)
	{
		this.catalogVersion = catalogVersion;
	}

	/**
	 * @return the catalog
	 */
	public String getCatalog()
	{
		return catalog;
	}

	/**
	 * @param catalog
	 *           the catalog to set
	 */
	public void setCatalog(final String catalog)
	{
		this.catalog = catalog;
	}

	/**
	 * @return the pk
	 */
	public Long getPk()
	{
		return pk;
	}

	/**
	 * @param pk
	 *           the pk to set
	 */
	public void setPk(final Long pk)
	{
		this.pk = pk;
	}

	/**
	 * @return the categories
	 */
	public Collection<String> getCategories()
	{
		return categories;
	}

	/**
	 * @param categories
	 *           the categories to set
	 */
	public void setCategories(final Collection<String> categories)
	{
		this.categories = categories;
	}

	/**
	 * @return the price
	 */
	public Double getPrice()
	{
		return price;
	}

	/**
	 * @param price
	 *           the price to set
	 */
	public void setPrice(final Double price)
	{
		this.price = price;
	}

	/**
	 * @return the ean
	 */
	public String getEan()
	{
		return ean;
	}

	/**
	 * @param ean
	 *           the ean to set
	 */
	public void setEan(final String ean)
	{
		this.ean = ean;
	}



}

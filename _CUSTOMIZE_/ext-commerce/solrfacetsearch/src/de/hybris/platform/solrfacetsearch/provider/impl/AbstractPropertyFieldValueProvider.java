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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.internal.i18n.LocalizationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.RangeNameProvider;

import java.util.List;


/**
 * Abstract class for all {@link FieldValueProvider} implementations
 */
public abstract class AbstractPropertyFieldValueProvider
{
	protected I18NService i18nService;
	protected ModelService modelService;
	protected LocalizationService localeService;
	protected RangeNameProvider rangeNameProvider;

	/**
	 * If the property is ranged {@link RangeNameProvider#isRanged(IndexedProperty)}}, the method return range list
	 * basing on the assigned ranges and the given property value. If the property is not multiValue
	 * {@link IndexedProperty#isMultiValue()} only first matching range will be returned
	 *
	 * @deprecated Since 5.2, replaced by {@link RangeNameProvider#getRangeNameList(IndexedProperty, Object)}
	 *
	 */
	@Deprecated
	public List<String> getRangeNameList(final IndexedProperty property, final Object value) throws FieldValueProviderException
	{
		return rangeNameProvider.getRangeNameList(property, value);
	}

	/**
	 * Method returns collection of range name list that results from evaluation of ranged properties. For numerical
	 * types it allows open upper-limit range If the property is not multiValue {@link IndexedProperty#isMultiValue()}
	 * only first matching range will be returned
	 *
	 * @deprecated Since 5.2, replaced by {@link RangeNameProvider#getRangeNameList(IndexedProperty, Object, String)}
	 *
	 */
	@Deprecated
	public List<String> getRangeNameList(final IndexedProperty property, final Object value, final String qualifier)
			throws FieldValueProviderException
	{
		return rangeNameProvider.getRangeNameList(property, value, qualifier);
	}

	/**
	 * @param i18nService
	 *           the i18nService to set
	 */
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}


	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


	/**
	 * @param localeService
	 *           the localeService to set
	 */
	public void setLocaleService(final LocalizationService localeService)
	{
		this.localeService = localeService;
	}

	/**
	 * @param rangeNameProvider
	 *           the rangeNameProvider to set
	 */
	public void setRangeNameProvider(final RangeNameProvider rangeNameProvider)
	{
		this.rangeNameProvider = rangeNameProvider;
	}
}

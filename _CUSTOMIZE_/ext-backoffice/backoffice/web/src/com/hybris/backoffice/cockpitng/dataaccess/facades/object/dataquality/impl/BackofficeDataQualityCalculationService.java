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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.dataquality.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.validation.coverage.CoverageCalculationService;
import de.hybris.platform.validation.coverage.CoverageInfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dataquality.DataQualityCalculationService;
import com.hybris.cockpitng.dataquality.model.DataQuality;
import com.hybris.cockpitng.dataquality.model.DataQualityProperty;


/**
 * Default Backoffice implementation of coverage calculation service. This implementation of the
 * {@link DataQualityCalculationService} uses
 * {@link de.hybris.platform.validation.coverage.strategies.CoverageCalculationStrategyRegistry} to lookup matching
 * strategy and redirect the calculation to it.
 */
public class BackofficeDataQualityCalculationService implements DataQualityCalculationService
{

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeDataQualityCalculationService.class);

	private CoverageCalculationService coverageCalculationService;
	private TypeFacade typeFacade;


	@Override
	public Optional<DataQuality> calculate(final Object object, final String domainId)
	{
		if (getCoverageCalculationService() != null && object instanceof ItemModel)
		{
			return calculate(object, ((ItemModel) object).getItemtype(), domainId);
		}

		return Optional.empty();
	}

	@Override
	public Optional<DataQuality> calculate(final Object object, final String templateCode, final String domainId)
	{
		if (object instanceof ItemModel)
		{
			final CoverageInfo coverageInfo = getCoverageCalculationService().calculate((ItemModel) object, templateCode, domainId);
			return convertToDataQuality(coverageInfo);
		}

		LOG.error("object {} is not an instance of the ItemModel", object);
		return Optional.empty();
	}

	protected Optional<DataQuality> convertToDataQuality(final CoverageInfo coverageInfo)
	{
		if (coverageInfo == null)
		{
			return Optional.empty();
		}
		final DataQuality dataQuality = new DataQuality();
		dataQuality.setDataQualityIndex(coverageInfo.getCoverageIndex());
		dataQuality.setDescription(coverageInfo.getCoverageDescription());
		dataQuality.setDataQualityProperties(convertToCoverageProperties(coverageInfo.getPropertyInfoMessages()));
		return Optional.of(dataQuality);
	}

	protected List<DataQualityProperty> convertToCoverageProperties(
			final List<CoverageInfo.CoveragePropertyInfoMessage> propertyInfoMessages)
	{
		return propertyInfoMessages.stream().filter(Objects::nonNull)
				.map(property -> new DataQualityProperty(getPropertyQualifier(property), property.getMessage()))
				.collect(Collectors.toList());
	}

	protected String getPropertyQualifier(final CoverageInfo.CoveragePropertyInfoMessage property)
	{
		final ObjectValuePath propertyPath = ObjectValuePath.parse(property.getPropertyQualifier());
		final ObjectValuePath root = propertyPath.getRoot();
		try
		{
			getTypeFacade().load(root.toString());
			final String relativePath = propertyPath.getRelative(root).toString();
			final boolean isLocalized = propertyPath.getLocale() != null;
			return isLocalized ? String.format("%s[%s]", relativePath, propertyPath.getLocale()) : relativePath;
		}
		catch (final TypeNotFoundException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Type not found", e);
			}
			return propertyPath.toString();
		}
	}

	@Required
	public void setCoverageCalculationService(final CoverageCalculationService coverageCalculationService)
	{
		this.coverageCalculationService = coverageCalculationService;
	}

	protected CoverageCalculationService getCoverageCalculationService()
	{
		return coverageCalculationService;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

}

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

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.QualifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 * Qualifier provider for currencies.
 *
 * <p>
 * The available qualifiers will be created based on the configured currencies for the index configuration (see
 * {@link IndexConfig#getCurrencies()}).
 *
 * <p>
 * It supports the following types:
 * <ul>
 * <li>{@link CurrencyModel}
 * </p>
 *
 */
public class CurrencyQualifierProvider implements QualifierProvider
{
	private CommonI18NService commonI18NService;

	private final Set<Class<?>> supportedTypes;

	public CurrencyQualifierProvider()
	{
		supportedTypes = Collections.<Class<?>> singleton(CurrencyModel.class);
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Override
	public Set<Class<?>> getSupportedTypes()
	{
		return supportedTypes;
	}

	@Override
	public Collection<Qualifier> getAvailableQualifiers(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		Objects.requireNonNull(facetSearchConfig, "facetSearchConfig is null");
		Objects.requireNonNull(indexedType, "indexedType is null");

		final List<Qualifier> qualifiers = new ArrayList<Qualifier>();

		for (final CurrencyModel currency : facetSearchConfig.getIndexConfig().getCurrencies())
		{
			final Qualifier qualifier = new CurrencyQualifier(currency);
			qualifiers.add(qualifier);
		}

		return Collections.unmodifiableList(qualifiers);
	}

	@Override
	public boolean canApply(final IndexedProperty indexedProperty)
	{
		Objects.requireNonNull(indexedProperty, "indexedProperty is null");

		return indexedProperty.isCurrency();
	}

	@Override
	public void applyQualifier(final Qualifier qualifier)
	{
		Objects.requireNonNull(qualifier, "qualifier is null");

		if (!(qualifier instanceof CurrencyQualifier))
		{
			throw new IllegalArgumentException("provided qualifier is not of expected type");
		}

		commonI18NService.setCurrentCurrency(((CurrencyQualifier) qualifier).getCurrency());
	}

	@Override
	public Qualifier getCurrentQualifier()
	{
		final CurrencyModel currency = commonI18NService.getCurrentCurrency();
		if (currency == null)
		{
			return null;
		}

		return new CurrencyQualifier(currency);
	}

	protected static class CurrencyQualifier implements Qualifier
	{
		private final CurrencyModel currency;

		public CurrencyQualifier(final CurrencyModel currency)
		{
			Objects.requireNonNull(currency, "currency is null");

			this.currency = currency;
		}

		public CurrencyModel getCurrency()
		{
			return currency;
		}

		@Override
		public <U> U getValueForType(final Class<U> type)
		{
			Objects.requireNonNull(type, "type is null");

			if (Objects.equals(type, CurrencyModel.class))
			{
				return (U) currency;
			}

			throw new IllegalArgumentException("type not supported");
		}

		@Override
		public String toFieldQualifier()
		{
			return currency.getIsocode();
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj == null || this.getClass() != obj.getClass())
			{
				return false;
			}

			final CurrencyQualifier that = (CurrencyQualifier) obj;
			return new EqualsBuilder()
					.append(this.currency, that.currency)
					.isEquals();
		}

		@Override
		public int hashCode()
		{
			return currency.hashCode();
		}
	}
}

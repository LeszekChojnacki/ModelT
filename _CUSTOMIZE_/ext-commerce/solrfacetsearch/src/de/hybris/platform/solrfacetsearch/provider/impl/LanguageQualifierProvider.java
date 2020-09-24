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

import de.hybris.platform.core.model.c2l.LanguageModel;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Required;


/**
 * Qualifier provider for languages.
 *
 * <p>
 * The available qualifiers will be created based on the configured languages for the index configuration (see
 * {@link IndexConfig#getLanguages()}).
 *
 * <p>
 * It supports the following types:
 * <ul>
 * <li>{@link LanguageModel}
 * <li>{@link Locale}
 * </p>
 *
 */
public class LanguageQualifierProvider implements QualifierProvider
{
	private CommonI18NService commonI18NService;

	private final Set<Class<?>> supportedTypes;

	public LanguageQualifierProvider()
	{
		final Set<Class<?>> types = new HashSet<Class<?>>();
		types.add(LanguageModel.class);
		types.add(Locale.class);
		supportedTypes = Collections.unmodifiableSet(types);
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

		for (final LanguageModel language : facetSearchConfig.getIndexConfig().getLanguages())
		{
			final Locale locale = commonI18NService.getLocaleForLanguage(language);
			final Qualifier qualifier = new LanguageQualifier(language, locale);
			qualifiers.add(qualifier);
		}

		return Collections.unmodifiableList(qualifiers);
	}

	@Override
	public boolean canApply(final IndexedProperty indexedProperty)
	{
		Objects.requireNonNull(indexedProperty, "indexedProperty is null");

		return indexedProperty.isLocalized();
	}

	@Override
	public void applyQualifier(final Qualifier qualifier)
	{
		Objects.requireNonNull(qualifier, "qualifier is null");

		if (!(qualifier instanceof LanguageQualifier))
		{
			throw new IllegalArgumentException("provided qualifier is not of expected type");
		}

		commonI18NService.setCurrentLanguage(((LanguageQualifier) qualifier).getLanguage());
	}

	@Override
	public Qualifier getCurrentQualifier()
	{
		final LanguageModel language = commonI18NService.getCurrentLanguage();
		if (language == null)
		{
			return null;
		}

		final Locale locale = commonI18NService.getLocaleForLanguage(language);
		return new LanguageQualifier(language, locale);
	}

	protected static class LanguageQualifier implements Qualifier
	{
		private final LanguageModel language;
		private final Locale locale;

		public LanguageQualifier(final LanguageModel language, final Locale locale)
		{
			Objects.requireNonNull(language, "language is null");
			Objects.requireNonNull(locale, "locale is null");

			this.language = language;
			this.locale = locale;
		}

		public LanguageModel getLanguage()
		{
			return language;
		}

		public Locale getLocale()
		{
			return locale;
		}

		@Override
		public <U> U getValueForType(final Class<U> type)
		{
			Objects.requireNonNull(type, "type is null");

			if (Objects.equals(type, LanguageModel.class))
			{
				return (U) language;
			}
			else if (Objects.equals(type, Locale.class))
			{
				return (U) locale;
			}

			throw new IllegalArgumentException("type not supported");
		}

		@Override
		public String toFieldQualifier()
		{
			return language.getIsocode();
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

			final LanguageQualifier that = (LanguageQualifier) obj;
			return new EqualsBuilder()
					.append(this.language, that.language)
					.append(this.locale, that.locale)
					.isEquals();
		}

		@Override
		public int hashCode()
		{
			return language.hashCode() + locale.hashCode();
		}
	}
}

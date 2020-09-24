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
package com.hybris.backoffice.excel.data;

import java.io.Serializable;


/**
 * Represents single header value. Header value consists of attribute name, indicator whether attribute is unique and
 * information about language for localized values.
 *
 * @see Builder Builder to create new instance
 */
public class ImpexHeaderValue implements Serializable
{

	private final String name;
	private String qualifier;
	private final boolean unique;
	private boolean mandatory;
	private final String lang;
	private final String dateFormat;
	private final String translator;

	private ImpexHeaderValue(final String name, final String qualifier, final boolean unique, final boolean mandatory,
			final String lang, final String dateFormat, final String translator)
	{
		this.name = name;
		this.qualifier = qualifier;
		this.unique = unique;
		this.lang = lang;
		this.dateFormat = dateFormat;
		this.translator = translator;
		this.mandatory = mandatory;
	}

	/**
	 * @deprecated since 1808, use {@link Builder} instead
	 */
	@Deprecated
	public ImpexHeaderValue(final String name, final boolean unique, final String lang, final String dateFormat,
			final String translator)
	{
		this.name = name;
		this.unique = unique;
		this.lang = lang;
		this.dateFormat = dateFormat;
		this.translator = translator;
	}

	/**
	 * @deprecated since 1808, use {@link Builder} instead
	 */
	@Deprecated
	public ImpexHeaderValue(final String name, final boolean unique, final String lang, final String dateFormat)
	{
		this(name, unique, lang, dateFormat, null);
	}

	/**
	 * @deprecated since 1808, use {@link Builder} instead
	 */
	@Deprecated
	public ImpexHeaderValue(final String name, final boolean unique, final String lang)
	{
		this(name, unique, lang, null, null);
	}

	/**
	 * @deprecated since 1808, use {@link Builder} instead
	 */
	@Deprecated
	public ImpexHeaderValue(final String name, final boolean unique)
	{
		this(name, unique, null, null, null);
	}

	/**
	 * @return name of header attribute.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return qualifier of header attribute.
	 */
	public String getQualifier()
	{
		return qualifier;
	}

	/**
	 * @return whether attribute is unique.
	 */
	public boolean isUnique()
	{
		return unique;
	}

	/**
	 * @return whether attribute is mandatory.
	 */
	public boolean isMandatory()
	{
		return mandatory;
	}

	/**
	 * @return information about language for localized attributes.
	 */
	public String getLang()
	{
		return lang;
	}

	/**
	 * @return format for date
	 */
	public String getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * @return translator name
	 */
	public String getTranslator()
	{
		return translator;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final ImpexHeaderValue that = (ImpexHeaderValue) o;

		if (unique != that.unique)
		{
			return false;
		}
		if (mandatory != that.mandatory)
		{
			return false;
		}
		if (name != null ? !name.equals(that.name) : that.name != null)
		{
			return false;
		}
		if (qualifier != null ? !qualifier.equals(that.qualifier) : that.qualifier != null)
		{
			return false;
		}
		if (lang != null ? !lang.equals(that.lang) : that.lang != null)
		{
			return false;
		}
		if (dateFormat != null ? !dateFormat.equals(that.dateFormat) : that.dateFormat != null)
		{
			return false;
		}
		return translator != null ? translator.equals(that.translator) : that.translator == null;
	}

	@Override
	public int hashCode()
	{
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
		result = 31 * result + (unique ? 1 : 0);
		result = 31 * result + (mandatory ? 1 : 0);
		result = 31 * result + (lang != null ? lang.hashCode() : 0);
		result = 31 * result + (dateFormat != null ? dateFormat.hashCode() : 0);
		result = 31 * result + (translator != null ? translator.hashCode() : 0);
		return result;
	}

	public static class Builder
	{
		private final String name;
		private String qualifier;
		private boolean unique;
		private boolean mandatory;
		private String lang;
		private String dateFormat;
		private String translator;

		public Builder(final String name)
		{
			this.name = name;
		}

		public Builder withQualifier(final String qualifier)
		{
			this.qualifier = qualifier;
			return this;
		}

		public Builder withUnique(final boolean unique)
		{
			this.unique = unique;
			return this;
		}

		public Builder withMandatory(final boolean mandatory)
		{
			this.mandatory = mandatory;
			return this;
		}

		public Builder withLang(final String lang)
		{
			this.lang = lang;
			return this;
		}

		public Builder withDateFormat(final String dateFormat)
		{
			this.dateFormat = dateFormat;
			return this;
		}

		public Builder withTranslator(final String translator)
		{
			this.translator = translator;
			return this;
		}

		public ImpexHeaderValue build()
		{
			return new ImpexHeaderValue(name, qualifier, unique, mandatory, lang, dateFormat, translator);
		}
	}
}

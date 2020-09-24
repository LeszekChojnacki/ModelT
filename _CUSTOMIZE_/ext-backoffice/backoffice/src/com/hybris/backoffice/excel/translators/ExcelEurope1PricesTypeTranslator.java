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

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.europe1.enums.PriceRowChannel;
import de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.impex.constants.ImpExConstants;
import de.hybris.platform.util.StandardDateRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Joiner;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * Price translator which allows to import product prices. It utilises impex {@link Europe1PricesTranslator} and
 * {@link de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator.Europe1PriceRowTranslator} to import prices.
 * <b>Format:</b> price currency:'N'|'G':user|userPriceGroup:quantity unit:[dateFrom to dateTo]:channel <b>where:</b>
 * <ul>
 * <li>price currency: defines price and currency which are mandatory fields</li>
 * <li>N|G: N stands for {@link Europe1PricesTranslator#NETTO} and G for {@link Europe1PricesTranslator#GROSS} which is
 * taken as default when not provided.</li>
 * <li>user|userPriceGroup: User or userPriceGroup for which the price is created</li>
 * <li>quantity unit: if not defined then default quantity is 1 and unit is taken from the product but when user or
 * userPriceGroup is given this field is mandatory</li>
 * <li>[dateFrom to dateTo]: defines price active range. The date time should be in format
 * {@link ExcelDateUtils#getDateTimeFormat()}. Date is exported in {@link ExcelDateUtils#getExportTimeZone()} time zone.
 * During import dates are converted from exported time zone to system time zone. By default export/import is done in
 * <b>UTC</b></li>
 * <li>channel: defines channel in which price is valid</li>
 * </ul>
 * Examples:
 * <ul>
 * <li>10 EUR::axel:2 pieces:[10.11.2017 12:00:00 to 15:12:2017 12:00:00]:mobile - defines price with value 10 EUR Gross
 * for user axel with min quantity 2 pieces, active from 10.11.2017 12:00:00 to 15:12:2017 12:00:00 in mobile
 * channel</li>
 * <li>20 EUR:N::3 pieces - defines price with value 20 EUR Netto for 3 pieces</li>
 * <li>15 EUR - defines price 15 EUR for 1 unit which will be taken from {@link ProductModel#getUnit()}</li>
 * </ul>
 * Under the hood Above examples are translated into format accepted by
 * {@link de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator.Europe1PriceRowTranslator}
 * <ul>
 * <li>10 EUR::axel:2 pieces:[10.11.2017 12:00:00 to 15:12:2017 12:00:00]:mobile -> axel 2 pieces = 10 EUR [10.11.2017
 * 12:00:00,15:12:2017 12:00:00] mobile</li>
 * <li>20 EUR:N::3 pieces ->2 pieces = 20 EUR N</li>
 * <li>15 EUR -> 15 EUR</li>
 * </ul>
 */
public class ExcelEurope1PricesTypeTranslator extends AbstractExcelValueTranslator<Collection<PriceRowModel>>
{
	/**
	 * @deprecated since 1811, not used anymore. The attribute qualifier ({@link AttributeDescriptorModel#getQualifier()})
	 *             will be used as impex header value name ({@link ImpexHeaderValue#name})
	 */
	@Deprecated
	public static final String EUROPE1_PRICE_HEADER = "europe1prices";
	/**
	 * price currency:N|G:user|userPriceGroup:quantity unit:dateFrom to dateTo:channel
	 */
	private static final String PATTERN = "%s %s:%c:%s:%s %s:%s:%s";
	public static final String PRICE_CURRENCY = "price currency";
	public static final String NET_GROSS = "N|G";
	public static final String USER_OR_UPG = "user|userPriceGroup";
	public static final String QUANTITY_UNIT = "minQuantity unit";
	public static final String CHANNEL = "channel";
	private ExcelDateUtils excelDateUtils;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor.getAttributeType() instanceof CollectionTypeModel && PriceRowModel._TYPECODE
				.equals(((CollectionTypeModel) attributeDescriptor.getAttributeType()).getElementType().getCode());
	}

	@Override
	public Optional<Object> exportData(final Collection<PriceRowModel> objectToExport)
	{
		return CollectionUtils.emptyIfNull(objectToExport).stream()//
				.map(this::exportPriceRow)//
				.reduce(Joiner.on(',')::join)//
				.map(Object.class::cast);
	}

	protected String exportPriceRow(final PriceRowModel priceRow)
	{
		final char netGross = BooleanUtils.isTrue(priceRow.getNet()) ? Europe1PricesTranslator.NETTO
				: Europe1PricesTranslator.GROSS;
		final String userOrUserPriceGroup = priceRow.getUg() != null ? priceRow.getUg().getCode()
				: (getValueOrEmpty(priceRow.getUser(), PrincipalModel::getUid));
		final String channel = getValueOrEmpty(priceRow.getChannel(), PriceRowChannel::getCode);
		final String unit = getValueOrEmpty(priceRow.getUnit(), UnitModel::getCode);

		return String.format(PATTERN, priceRow.getPrice(), priceRow.getCurrency().getIsocode(), netGross, userOrUserPriceGroup,
				priceRow.getMinqtd(), unit, getDateRange(priceRow.getDateRange()), channel);
	}

	protected String getDateRange(final StandardDateRange range)
	{
		if (range != null && range.getStart() != null && range.getEnd() != null)
		{
			return excelDateUtils.exportDateRange(range.getStart(), range.getEnd());
		}
		return StringUtils.EMPTY;
	}

	protected <T> String getValueOrEmpty(final T reference, final Function<T, String> valueSupplier)
	{
		return reference != null ? valueSupplier.apply(reference) : StringUtils.EMPTY;
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return String.format("%s:%s:%s:%s:%s:%s", PRICE_CURRENCY, NET_GROSS, USER_OR_UPG, QUANTITY_UNIT,
				excelDateUtils.getDateRangePattern(), CHANNEL);
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final List<String> formattedPrices = new ArrayList<>();
		for (final Map<String, String> params : importParameters.getMultiValueParameters())
		{
			formattedPrices.add(buildSinglePriceImpexValue(params));
		}
		return new ImpexValue(String.join(", ", formattedPrices),
				new ImpexHeaderValue.Builder(attributeDescriptor.getQualifier()).withDateFormat(excelDateUtils.getDateTimeFormat())
						.withTranslator(Europe1PricesTranslator.class.getName()).withQualifier(attributeDescriptor.getQualifier())
						.build());
	}

	protected String buildSinglePriceImpexValue(final Map<String, String> params)
	{
		final StringBuilder sb = new StringBuilder();
		appendIfPresent(sb, params.get(USER_OR_UPG));
		appendIfPresent(sb, params.get(QUANTITY_UNIT));
		if (sb.length() > 0)
		{
			sb.append(" =");
		}
		appendIfPresent(sb, params.get(PRICE_CURRENCY));
		appendIfPresent(sb, params.get(NET_GROSS));
		appendIfPresent(sb, getImpexDateRange(params.get(excelDateUtils.getDateRangeParamKey())));
		appendIfPresent(sb, params.get(CHANNEL));

		return sb.toString();
	}

	protected void appendIfPresent(final StringBuilder sb, final String value)
	{
		if (StringUtils.isNotEmpty(value))
		{
			if (sb.length() > 0)
			{
				sb.append(" ");
			}
			sb.append(value);
		}
	}

	protected String getImpexDateRange(final String dateRange)
	{
		if (StringUtils.isNotEmpty(dateRange))
		{
			final Pair<String, String> range = excelDateUtils.extractDateRange(dateRange);
			if (range != null)
			{
				return String.format("[%s%s%s]", excelDateUtils.importDate(range.getLeft()),
						ImpExConstants.Syntax.DATERANGE_DELIMITER, excelDateUtils.importDate(range.getRight()));
			}
		}
		return null;
	}

	public ExcelDateUtils getExcelDateUtils()
	{
		return excelDateUtils;
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}
}

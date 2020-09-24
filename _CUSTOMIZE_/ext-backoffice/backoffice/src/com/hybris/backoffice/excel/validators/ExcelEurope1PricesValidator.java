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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.europe1.enums.PriceRowChannel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.daos.CurrencyDao;
import de.hybris.platform.servicelayer.user.UserService;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.ExcelEurope1PricesTypeTranslator;
import com.hybris.backoffice.excel.util.ExcelDateUtils;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Validates prices for {@link ExcelEurope1PricesTypeTranslator}.
 * 
 * <pre>
 *    <b>Format:</b>price currency:'N'|'G':user|userPriceGroup:quantity unit:[dateFrom to dateTo]:channel
 * </pre>
 * 
 * Validator checks if:
 * <ul>
 * <li>price is defined and given currency exists</li>
 * <li>Netto | Gross are in correct format</li>
 * <li>user or userPriceGroup exists</li>
 * <li>unit exists and if user or userPriceGroup is provided this field is mandatory</li>
 * <li>dateFrom and dateTo are in correct format {@link ExcelDateUtils#getDateTimeFormat()} and dateFrom is not after
 * dateTo</li>
 * <li>defined channel exists</li>
 * </ul>
 */
public class ExcelEurope1PricesValidator implements ExcelValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelEurope1PricesValidator.class);
	protected static final String UNIT_KEY = "Unit";
	protected static final String CURRENCY_KEY = "Currency";
	public static final Pattern PATTERN_PRICE_CURRENCY = Pattern.compile("(\\d+(\\.?\\d+)?)\\s*([a-zA-z]+)");
	public static final Pattern PATTERN_QUANTITY_UNIT = Pattern.compile("(\\d+)\\s*(\\w+)");
	public static final Pattern PATTERN_DATE_RANGE = Pattern.compile("(.+)\\s*to\\s*(.+)");
	public static final String VALIDATION_CURRENCY_DOESNT_EXIST = "excel.import.validation.price.currency.doesnt.exist";
	public static final String VALIDATION_INCORRECT_PRICE_CURRENCY = "excel.import.validation.price.currency.incorrect";
	public static final String VALIDATION_EMPTY_PRICE_CURRENCY = "excel.import.validation.priceandcurrency.empty";
	public static final String VALIDATION_INCORRECT_PRICE_VALUE = "excel.import.validation.price.value.incorrect";
	public static final String VALIDATION_INVALID_NET_GROSS = "excel.import.validation.price.netgross.incorrect";
	public static final String VALIDATION_INCORRECT_QUANTITY_UNIT = "excel.import.validation.price.quantityunit.incorrect";
	public static final String VALIDATION_INCORRECT_QUANTITY = "excel.import.validation.price.quantity.incorrect";
	public static final String VALIDATION_INCORRECT_UNIT = "excel.import.validation.price.unit.incorrect";
	public static final String VALIDATION_INCORRECT_QUANTITY_LOWE_THAN_ONE = "excel.import.validation.price.quantity.lowerthanone";
	public static final String VALIDATION_INCORRECT_USER_OR_USER_PRICE_GROUP = "excel.import.validation.price.user.incorrect";
	public static final String VALIDATION_QUANTITY_UNIT_CANNOT_BE_EMPTY_WHEN_USER_DEFINED = "excel.import.validation.price.quantityunit.missing.user.defined";
	public static final String VALIDATION_NO_SUCH_CHANNEL = "excel.import.validation.price.channel.does.not.exist";
	public static final String VALIDATION_INCORRECT_DATE_RANGE = "excel.import.validation.price.date.incorrect.format";
	public static final String VALIDATION_START_DATE_AFTER_END_DATE = "excel.import.validation.price.date.start.after.end";

	private CurrencyDao currencyDao;
	private UnitService unitService;
	private EnumerationService enumerationService;
	private UserService userService;
	private ExcelDateUtils excelDateUtils;


	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> ctx)
	{
		if (!ctx.containsKey(CURRENCY_KEY))
		{
			populateContext(ctx);
		}

		final List<ValidationMessage> validationMessages = new ArrayList<>();

		for (final Map<String, String> parameters : importParameters.getMultiValueParameters())
		{
			validationMessages.addAll(validateSingleValue(ctx, parameters));
		}
		return validationMessages.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(validationMessages);
	}

	protected List<ValidationMessage> validateSingleValue(final Map<String, Object> ctx, final Map<String, String> parameters)
	{
		final List<ValidationMessage> validations = new ArrayList<>();
		validatePrice(ctx, parameters.get(ExcelEurope1PricesTypeTranslator.PRICE_CURRENCY)).ifPresent(validations::addAll);
		validateNetGross(parameters.get(ExcelEurope1PricesTypeTranslator.NET_GROSS)).ifPresent(validations::add);
		validateQuantityUnit(ctx, parameters.get(ExcelEurope1PricesTypeTranslator.QUANTITY_UNIT),
				parameters.get(ExcelEurope1PricesTypeTranslator.USER_OR_UPG)).ifPresent(validations::addAll);
		validateUserPriceGroup(parameters.get(ExcelEurope1PricesTypeTranslator.USER_OR_UPG)).ifPresent(validations::add);
		validateDateRange(parameters.get(excelDateUtils.getDateRangeParamKey())).ifPresent(validations::add);
		validateChannel(parameters.get(ExcelEurope1PricesTypeTranslator.CHANNEL)).ifPresent(validations::add);
		return validations;
	}

	protected Optional<List<ValidationMessage>> validatePrice(final Map<String, Object> ctx, final String value)
	{
		if (StringUtils.isEmpty(value))
		{
			return Optional.of(Lists.newArrayList(new ValidationMessage(VALIDATION_EMPTY_PRICE_CURRENCY, value)));
		}

		final Matcher matcher = PATTERN_PRICE_CURRENCY.matcher(value);

		if (matcher.matches())
		{
			final List<ValidationMessage> validation = new ArrayList<>();
			validatePriceValue(matcher.group(1)).ifPresent(validation::add);
			validateCurrency(ctx, matcher.group(3)).ifPresent(validation::add);
			return validation.isEmpty() ? Optional.empty() : Optional.of(validation);
		}
		return Optional.of(Lists.newArrayList(new ValidationMessage(VALIDATION_INCORRECT_PRICE_CURRENCY, value)));
	}

	protected Optional<ValidationMessage> validatePriceValue(final String price)
	{
		if (StringUtils.isBlank(price) || !NumberUtils.isCreatable(price))
		{
			return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_PRICE_VALUE, price));
		}
		return Optional.empty();
	}

	protected Optional<ValidationMessage> validateCurrency(final Map<String, Object> ctx, final String currency)
	{
		if (!containsCurrency(ctx, currency))
		{
			return Optional.of(new ValidationMessage(VALIDATION_CURRENCY_DOESNT_EXIST, currency));
		}
		return Optional.empty();
	}

	protected Optional<ValidationMessage> validateNetGross(final String netGross)
	{
		if (StringUtils.isNotEmpty(netGross) && (netGross.length() != 1
				|| !(netGross.charAt(0) == Europe1PricesTranslator.GROSS || netGross.charAt(0) == Europe1PricesTranslator.NETTO)))
		{
			return Optional.of(new ValidationMessage(VALIDATION_INVALID_NET_GROSS, netGross, Europe1PricesTranslator.NETTO,
					Europe1PricesTranslator.GROSS));
		}
		return Optional.empty();
	}

	protected Optional<ValidationMessage> validateUserPriceGroup(final String groupOrUser)
	{
		if (StringUtils.isNotEmpty(groupOrUser))
		{
			try
			{
				enumerationService.getEnumerationValue(UserPriceGroup.class, groupOrUser);

			}
			catch (final UnknownIdentifierException uie)
			{
				try
				{
					userService.getUserForUID(groupOrUser);
				}
				catch (final UnknownIdentifierException e)
				{
					return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_USER_OR_USER_PRICE_GROUP, groupOrUser));
				}
			}
		}
		return Optional.empty();
	}

	protected Optional<ValidationMessage> validateDateRange(final String dateRange)
	{
		if (StringUtils.isEmpty(dateRange))
		{
			return Optional.empty();
		}

		final Matcher matcher = PATTERN_DATE_RANGE.matcher(dateRange);

		if (matcher.matches())
		{
			final Date from = parseDate(matcher.group(1));
			final Date to = parseDate(matcher.group(2));

			if (from != null && to != null)
			{
				if (from.after(to))
				{
					return Optional
							.of(new ValidationMessage(VALIDATION_START_DATE_AFTER_END_DATE, matcher.group(1), matcher.group(2)));
				}
				return Optional.empty();
			}
		}
		return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_DATE_RANGE, dateRange));
	}

	protected Date parseDate(final String date)
	{
		try
		{
			return excelDateUtils.convertToImportedDate(date);
		}
		catch (final DateTimeParseException e)
		{
			LOG.debug("Wrong date format " + date, e);
			return null;
		}
	}

	protected Optional<ValidationMessage> validateChannel(final String channel)
	{
		if (StringUtils.isNotEmpty(channel))
		{
			try
			{
				enumerationService.getEnumerationValue(PriceRowChannel.class, channel);
			}
			catch (final UnknownIdentifierException nie)
			{
				return Optional.of(new ValidationMessage(VALIDATION_NO_SUCH_CHANNEL, channel));
			}
		}
		return Optional.empty();
	}

	protected Optional<List<ValidationMessage>> validateQuantityUnit(final Map<String, Object> ctx, final String quantityUnit,
			final String groupOrUser)
	{
		if (StringUtils.isEmpty(quantityUnit))
		{
			if (StringUtils.isNotEmpty(groupOrUser))
			{

				return Optional.of(Lists
						.newArrayList(new ValidationMessage(VALIDATION_QUANTITY_UNIT_CANNOT_BE_EMPTY_WHEN_USER_DEFINED, groupOrUser)));
			}
			else
			{
				return Optional.empty();
			}
		}

		final Matcher matcher = PATTERN_QUANTITY_UNIT.matcher(quantityUnit);

		if (matcher.matches())
		{
			final List<ValidationMessage> validation = new ArrayList<>();
			validateQuantity(matcher.group(1)).ifPresent(validation::add);
			validateUnit(ctx, matcher.group(2)).ifPresent(validation::add);
			return validation.isEmpty() ? Optional.empty() : Optional.of(validation);
		}
		return Optional.of(Lists.newArrayList(new ValidationMessage(VALIDATION_INCORRECT_QUANTITY_UNIT, quantityUnit)));
	}

	protected Optional<ValidationMessage> validateQuantity(final String quantity)
	{
		try
		{
			if (Integer.parseInt(quantity) < 1)
			{
				return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_QUANTITY_LOWE_THAN_ONE, quantity));
			}
		}
		catch (final NumberFormatException nfe)
		{
			return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_QUANTITY, quantity));
		}
		return Optional.empty();
	}

	protected Optional<ValidationMessage> validateUnit(final Map<String, Object> ctx, final String unit)
	{
		if (!containsUnit(ctx, unit))
		{
			return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_UNIT, unit));
		}
		return Optional.empty();
	}

	protected boolean containsCurrency(final Map<String, Object> ctx, final String currency)
	{
		return ((Set) ctx.get(CURRENCY_KEY)).contains(currency);
	}

	protected boolean containsUnit(final Map<String, Object> ctx, final String unit)
	{
		return ((Set) ctx.get(UNIT_KEY)).contains(unit);
	}

	protected void populateContext(final Map<String, Object> ctx)
	{
		final Set<String> units = unitService.getAllUnits().stream().map(UnitModel::getCode).collect(Collectors.toSet());
		ctx.put(UNIT_KEY, units);

		final Set<String> currencies = currencyDao.findCurrencies().stream().map(CurrencyModel::getIsocode)
				.collect(Collectors.toSet());
		ctx.put(CURRENCY_KEY, currencies);
	}

	protected boolean checkIfCurrencyExist(final Map<String, Object> ctx, final String currency)
	{
		final Set<String> currencies = ctx.get(CURRENCY_KEY) instanceof Set ? (Set) ctx.get(CURRENCY_KEY) : new HashSet<>();
		return currencies.contains(currency);
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank() && attributeDescriptor.getAttributeType() instanceof CollectionTypeModel
				&& PriceRowModel._TYPECODE
						.equals(((CollectionTypeModel) attributeDescriptor.getAttributeType()).getElementType().getCode());
	}

	public CurrencyDao getCurrencyDao()
	{
		return currencyDao;
	}

	@Required
	public void setCurrencyDao(final CurrencyDao currencyDao)
	{
		this.currencyDao = currencyDao;
	}

	public UnitService getUnitService()
	{
		return unitService;
	}

	@Required
	public void setUnitService(final UnitService unitService)
	{
		this.unitService = unitService;
	}

	public EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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

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
package com.hybris.backoffice.excel.importing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexRow;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.data.ExcelImportResult;
import com.hybris.backoffice.excel.importing.parser.ExcelParserException;
import com.hybris.backoffice.excel.importing.parser.ParsedValues;
import com.hybris.backoffice.excel.importing.parser.ParserRegistry;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslator;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslatorRegistry;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.util.ExcelValidationResultUtil;


/**
 * Abstract class for import workbook decorator. This class provides utility methods for finding appropriate attributes,
 * importing parameters and merging impexes.
 */
public abstract class AbstractExcelImportWorkbookDecorator implements ExcelImportWorkbookValidationAwareDecorator
{

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExcelImportWorkbookDecorator.class);

	private ExcelHeaderService excelHeaderService;
	private ExcelAttributeTranslatorRegistry excelAttributeTranslatorRegistry;
	private ParserRegistry parserRegistry;
	private List<ExcelAttributeValidator<? extends ExcelAttribute>> validators;
	private ExcelSheetService excelSheetService;
	private ExcelCellService excelCellService;

	/**
	 * {@inheritDoc}
	 * 
	 * @param workbook
	 * @return
	 */
	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		final List<ExcelValidationResult> results = new ArrayList<>();
		final DecoratorConsumer consumer = (excelAttribute, row, columnIndex, typeCode) -> {
			final ImportParameters importParameters = getImportParameters(typeCode, excelAttribute, getCell(row, columnIndex));
			final Map<String, Object> validationContext = new HashMap<>();
			for (final ExcelAttributeValidator validator : validators)
			{
				if (validator.canHandle(excelAttribute, importParameters))
				{
					final ExcelValidationResult validationResult = validator.validate(excelAttribute, importParameters,
							validationContext);
					if (validationResult != null && validationResult.hasErrors())
					{
						ExcelValidationResultUtil.insertHeaderIfNeeded(validationResult, row.getRowNum() + 1, typeCode,
								excelAttribute.getName());
						results.add(validationResult);
					}
				}
			}
		};
		consumeWorkbook(workbook, consumer);
		return results;
	}

	@Override
	public void decorate(@Nonnull final ExcelImportResult excelImportResult)
	{
		final DecoratorConsumer consumer = (excelAttribute, row, columnIndex, typeCode) -> {
			final int rowIndex = row.getRowNum();
			final ImpexRow impexRow = excelImportResult.getImpex().findUpdates(typeCode).getRow(rowIndex);
			final ImportParameters importParameters = getImportParameters(typeCode, excelAttribute, getCell(row, columnIndex));
			final ExcelImportContext excelImportContext = new ExcelImportContext();
			excelImportContext.setImpexRow(impexRow);
			final Impex subImpex = convertToImpex(excelAttribute, importParameters, excelImportContext);
			excelImportResult.getImpex().mergeImpex(subImpex, importParameters.getTypeCode(), rowIndex);
		};
		consumeWorkbook(excelImportResult.getWorkbook(), consumer);
	}

	private void consumeWorkbook(final Workbook workbook, final DecoratorConsumer decoratorConsumer)
	{
		for (final Sheet sheet : getExcelSheetService().getSheets(workbook))
		{
			consumeSheet(decoratorConsumer, sheet);
		}
	}

	private void consumeSheet(final DecoratorConsumer decoratorConsumer, final Sheet sheet)
	{
		for (final ExcelAttribute excelAttribute : getExcelAttributes(sheet))
		{
			consumeAttribute(decoratorConsumer, sheet, excelAttribute);
		}
	}

	private void consumeAttribute(final DecoratorConsumer decoratorConsumer, final Sheet sheet,
			final ExcelAttribute excelAttribute)
	{
		final Optional<Integer> foundColumn = findColumnIndex(sheet.getRow(ExcelTemplateConstants.HEADER_ROW_INDEX),
				excelAttribute.getName());
		foundColumn.ifPresent(columnIndex -> {
			for (int rowIndex = ExcelTemplateConstants.FIRST_DATA_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++)
			{
				final Row row = sheet.getRow(rowIndex);
				if (hasContent(row))
				{
					final String typeCode = getExcelSheetService().findTypeCodeForSheetName(sheet.getWorkbook(), sheet.getSheetName());
					decoratorConsumer.consume(excelAttribute, row, columnIndex, typeCode);
				}
			}
		});
	}

	@FunctionalInterface
	private interface DecoratorConsumer
	{
		void consume(ExcelAttribute attribute, Row row, Integer columnIndex, String typeCode);
	}

	private static Cell getCell(final Row row, final int columnIndex)
	{
		final Cell cell = row.getCell(columnIndex);
		if (cell != null)
		{
			return cell;
		}
		return row.createCell(columnIndex);
	}

	/**
	 * Finds appropriate attributes for given decorator implementation.
	 * 
	 * @param sheet
	 * @return collection of excel's attributes
	 */
	protected abstract Collection<ExcelAttribute> getExcelAttributes(final Sheet sheet);

	/**
	 * Converts given excel attribute and import parameters into impex object.
	 * 
	 * @param excelAttribute
	 * @param importParameters
	 * @param excelImportContext
	 * @return Impex object for given cell
	 */
	protected Impex convertToImpex(final ExcelAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		return getExcelAttributeTranslatorRegistry().findTranslator(excelAttribute) //
				.map(translator -> translator.importData(excelAttribute, importParameters, excelImportContext)) //
				.orElse(new Impex());
	}

	protected Optional<Integer> findColumnIndex(final Row headerRow, final String content)
	{
		for (int columnIndex = headerRow.getFirstCellNum(); columnIndex <= headerRow.getLastCellNum(); columnIndex++)
		{
			final String headerWithoutMarkers = getExcelHeaderService()
					.getHeaderValueWithoutSpecialMarks(getExcelCellService().getCellValue(headerRow.getCell(columnIndex)));
			if (headerWithoutMarkers.equals(content))
			{
				return Optional.of(columnIndex);
			}
		}
		return Optional.empty();
	}

	private boolean hasContent(final Row row)
	{
		for (int columnIndex = row.getFirstCellNum(); columnIndex <= row.getLastCellNum(); columnIndex++)
		{
			if (StringUtils.isNotBlank(getExcelCellService().getCellValue(row.getCell(columnIndex))))
			{
				return true;
			}
		}
		return false;
	}

	private ImportParameters getImportParameters(final String typeCode, final ExcelAttribute excelAttribute, final Cell cell)
	{
		final Optional<ExcelAttributeTranslator<ExcelAttribute>> translator = getExcelAttributeTranslatorRegistry()
				.findTranslator(excelAttribute);
		final int columnIndex = cell.getColumnIndex();
		final String values = getExcelCellService().getCellValue(cell);
		final String referenceFormat = translator.map(t -> t.referenceFormat(excelAttribute)).orElse(StringUtils.EMPTY);
		try
		{
			final String defaultValues = getExcelCellService()
					.getCellValue(cell.getSheet().getRow(ExcelTemplateConstants.DEFAULT_VALUES_ROW_INDEX).getCell(columnIndex));
			final ParsedValues parsedValues = getParserRegistry().getParser(referenceFormat).parseValue(referenceFormat,
					defaultValues, values);
			return new ImportParameters(typeCode, excelAttribute.getIsoCode(), parsedValues.getCellValue(), null,
					parsedValues.getParameters());
		}
		catch (final ExcelParserException e)
		{
			LOGGER.debug(String.format("%s is in incorrect format. Correct format is: %s", e.getCellValue(), e.getExpectedFormat()),
					e);
			return new ImportParameters(typeCode, excelAttribute.getIsoCode(), e.getCellValue(), null, e.getExpectedFormat());
		}
	}

	@Override
	public int getOrder()
	{
		return 0;
	}

	public ParserRegistry getParserRegistry()
	{
		return parserRegistry;
	}

	@Required
	public void setParserRegistry(final ParserRegistry parserRegistry)
	{
		this.parserRegistry = parserRegistry;
	}

	public ExcelAttributeTranslatorRegistry getExcelAttributeTranslatorRegistry()
	{
		return excelAttributeTranslatorRegistry;
	}

	@Required
	public void setExcelAttributeTranslatorRegistry(final ExcelAttributeTranslatorRegistry excelAttributeTranslatorRegistry)
	{
		this.excelAttributeTranslatorRegistry = excelAttributeTranslatorRegistry;
	}

	@Required
	public void setValidators(final List<ExcelAttributeValidator<? extends ExcelAttribute>> validators)
	{
		this.validators = validators;
	}

	public ExcelSheetService getExcelSheetService()
	{
		return excelSheetService;
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}

	public ExcelHeaderService getExcelHeaderService()
	{
		return excelHeaderService;
	}

	@Required
	public void setExcelHeaderService(final ExcelHeaderService excelHeaderService)
	{
		this.excelHeaderService = excelHeaderService;
	}

	public ExcelCellService getExcelCellService()
	{
		return excelCellService;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}
}

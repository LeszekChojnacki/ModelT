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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.ExcelColumn;
import com.hybris.backoffice.excel.data.ExcelWorkbook;
import com.hybris.backoffice.excel.data.ExcelWorksheet;
import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.importing.parser.DefaultValues;
import com.hybris.backoffice.excel.importing.parser.ExcelParserException;
import com.hybris.backoffice.excel.importing.parser.ParsedValues;
import com.hybris.backoffice.excel.importing.parser.ParserRegistry;
import com.hybris.backoffice.excel.template.DisplayNameAttributeNameFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;
import com.hybris.backoffice.excel.translators.ExcelValueTranslator;
import com.hybris.backoffice.excel.validators.ExcelValidator;
import com.hybris.backoffice.excel.validators.WorkbookValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;
import com.hybris.backoffice.excel.validators.engine.ExcelValidationEngineAwareValidator;


/**
 * Service responsible for generating {@link Impex} based on excel file represented by {@link Workbook} object. In order
 * to generating impex script, we can pass the cellValidation of {#importData} to {@link ImpexConverter#convert(Impex)}.
 */
public class DefaultExcelImportService implements ExcelImportService
{

	/**
	 * @deprecated since 1808 - Use {@link ImportParameters#MULTIVALUE_SEPARATOR}
	 */
	@Deprecated
	public static final String MULTIVALUE_SEPARATOR = ",";
	public static final int FIRST_DATA_ROW_INDEX = 3;
	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	private ExcelTemplateService excelTemplateService;
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	private List<WorkbookValidator> workbookValidators;
	private ExcelValidationEngineAwareValidator excelValidationEngineAwareValidator;
	private ParserRegistry parserRegistry;
	private ExcelWorkbookService excelWorkbookService;
	private ExcelSheetService excelSheetService;
	private ExcelCellService excelCellService;
	private ExcelHeaderService excelHeaderService;
	private DisplayNameAttributeNameFormatter displayNameAttributeNameFormatter;

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelImportService.class);

	/**
	 * Transforms excel workbook into {@link Impex} object. It takes into account all sheets included in excel file.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return generated {@link Impex} object which represents data from the workbook
	 */
	@Override
	public Impex convertToImpex(final Workbook workbook)
	{
		final Sheet typeSystemSheet = getExcelWorkbookService().getMetaInformationSheet(workbook);
		final Impex workbookImpex = new Impex();
		for (final Sheet typeSheet : getExcelSheetService().getSheets(workbook))
		{
			final Impex sheetImpex = generateImpexForSheet(typeSystemSheet, typeSheet);
			workbookImpex.mergeImpex(sheetImpex);
		}

		return workbookImpex;
	}

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook, final Set<String> mediaContentEntries)
	{
		final List<ExcelValidationResult> validationWorkbookResults = new ArrayList<>();

		validateExcelFileOrigin(workbook).ifPresent(validationWorkbookResults::add);

		if (validationWorkbookResults.isEmpty())
		{
			validationWorkbookResults.addAll(validateWorkbook(workbook));
		}

		if (validationWorkbookResults.isEmpty())
		{
			final ExcelWorkbook excelWorkbook = populate(workbook);
			final Map<String, Object> context = new HashMap<>();
			if (mediaContentEntries != null)
			{
				context.put(ExcelValidator.CTX_MEDIA_CONTENT_ENTRIES, mediaContentEntries);
			}
			context.put(ExcelWorkbook.class.getCanonicalName(), excelWorkbook);
			excelWorkbook.forEachWorksheet(excelWorksheet -> {
				final List<ExcelValidationResult> validationWorksheetResults = validate(excelWorksheet, context);
				validationWorkbookResults.addAll(validationWorksheetResults);
			});
		}

		Collections.sort(validationWorkbookResults);
		return validationWorkbookResults;
	}

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		return validate(workbook, Collections.emptySet());
	}

	protected Optional<ExcelValidationResult> validateExcelFileOrigin(final Workbook workbook)
	{
		if (workbook instanceof XSSFWorkbook)
		{
			final POIXMLProperties properties = ((XSSFWorkbook) workbook).getProperties();
			if (!properties.getCustomProperties().contains(ExcelTemplateConstants.CustomProperties.FILE_SIGNATURE))
			{
				final ExcelValidationResult validationResult = new ExcelValidationResult(
						new ValidationMessage("excel.import.validation.incorrect.file.description"));
				validationResult.setHeader(new ValidationMessage("excel.import.validation.incorrect.file.header"));
				validationResult.setWorkbookValidationResult(true);
				return Optional.of(validationResult);
			}
		}
		return Optional.empty();
	}

	protected List<ExcelValidationResult> validateWorkbook(final Workbook workbook)
	{
		final Set<ExcelValidationResult> allUniqueValidationResults = new LinkedHashSet<>();
		for (final WorkbookValidator workbookValidator : getWorkbookValidators())
		{
			final List<ExcelValidationResult> validationResults = workbookValidator.validate(workbook);
			if (!validationResults.isEmpty())
			{
				allUniqueValidationResults.addAll(validationResults.stream() //
						.filter(result -> CollectionUtils.isNotEmpty(result.getValidationErrors())) //
						.peek(result -> result.setWorkbookValidationResult(true)) //
						.collect(Collectors.toSet()));
			}
		}
		return new LinkedList<>(allUniqueValidationResults);
	}

	protected ExcelWorkbook populate(final Workbook workbook)
	{
		final Sheet typeSystemSheet = getExcelWorkbookService().getMetaInformationSheet(workbook);
		final ExcelWorkbook excelWorkbook = new ExcelWorkbook();

		for (final Sheet typeSheet : getExcelSheetService().getSheets(workbook))
		{
			final ExcelWorksheet excelWorksheet = populate(typeSystemSheet, typeSheet);
			excelWorkbook.add(excelWorksheet);
		}

		return excelWorkbook;
	}

	protected ExcelWorksheet populate(final Sheet typeSystemSheet, final Sheet typeSheet)
	{
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(typeSheet.getWorkbook(), typeSheet.getSheetName());
		final ExcelWorksheet excelWorksheet = new ExcelWorksheet(typeCode);
		final Impex mainImpex = new Impex();
		final List<String> entriesRef = generateDocumentRefs(typeSheet.getLastRowNum() - FIRST_DATA_ROW_INDEX);
		final Collection<SelectedAttribute> selectedAttributes = getExcelHeaderService().getHeaders(typeSystemSheet, typeSheet);
		insertDocumentReferences(mainImpex, typeCode, entriesRef);
		for (final SelectedAttribute selectedAttribute : selectedAttributes)
		{
			final int columnIndex = getExcelSheetService().findColumnIndex(typeSystemSheet, typeSheet,
					new ExcelAttributeDescriptorAttribute(selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()));
			if (columnIndex != -1)
			{
				populateRows(typeSheet, excelWorksheet, entriesRef, selectedAttribute, columnIndex);
			}
		}

		return excelWorksheet;
	}

	protected void populateRows(final Sheet typeSheet, final ExcelWorksheet excelWorksheet, final List<String> entriesRef,
			final SelectedAttribute selectedAttribute, final int columnIndex)
	{
		final ExcelColumn excelColumn = new ExcelColumn(selectedAttribute, columnIndex);
		for (int rowIndex = FIRST_DATA_ROW_INDEX; rowIndex <= typeSheet.getLastRowNum(); rowIndex++)
		{
			if (hasRowCorrectData(typeSheet.getRow(rowIndex)))
			{
				final String cellValue = getExcelCellService().getCellValue(typeSheet.getRow(rowIndex).getCell(columnIndex));
				final ImportParameters importParameters = findImportParameters(selectedAttribute, cellValue,
						excelWorksheet.getSheetName(), entriesRef.get(rowIndex - FIRST_DATA_ROW_INDEX));

				excelWorksheet.add(rowIndex + 1, excelColumn, importParameters);
			}
		}
	}

	protected boolean hasRowCorrectData(final Row row)
	{
		if (row == null)
		{
			return false;
		}
		return findFirstNotBlankCell(row);
	}

	protected boolean findFirstNotBlankCell(final Row row)
	{
		for (int i = 0; i < row.getLastCellNum(); i++)
		{
			if (StringUtils.isNotBlank(getExcelCellService().getCellValue(row.getCell(i))))
			{
				return true;
			}
		}
		return false;
	}

	protected List<ExcelValidationResult> validate(final ExcelWorksheet excelWorksheet, final Map<String, Object> context)
	{
		final Map<Integer, ExcelValidationResult> rowsValidation = new HashMap<>();
		excelWorksheet.forEachColumn(excelColumn -> {

			final SelectedAttribute attribute = excelColumn.getSelectedAttribute();
			final String attributeDisplayName = getAttributeDisplayName(attribute);
			excelWorksheet.forEachRow(excelColumn, (rowIndex, importParameters) -> {
				final CellValidationMetaData metaData = new CellValidationMetaData(rowIndex, importParameters, attribute,
						attributeDisplayName, context);
				validateUsingValidationEngine(attribute, importParameters, metaData)
						.ifPresent(validationEngineResult -> mergeWithRowValidation(rowsValidation, rowIndex, validationEngineResult));

				excelTranslatorRegistry.getTranslator(attribute.getAttributeDescriptor())
						.ifPresent(translator -> validateCell(translator, metaData)
								.ifPresent(result -> mergeWithRowValidation(rowsValidation, rowIndex, result)));
			});
		});

		return new ArrayList<>(rowsValidation.values());
	}

	private Optional<ExcelValidationResult> validateUsingValidationEngine(final SelectedAttribute selectedAttribute,
			final ImportParameters importParameters, final CellValidationMetaData metaData)
	{
		final ExcelValidationResult singleResult = excelValidationEngineAwareValidator
				.validate(new ExcelAttributeDescriptorAttribute(selectedAttribute.getAttributeDescriptor()), importParameters);
		if (singleResult.hasErrors())
		{
			insertValidationHeaders(metaData, singleResult);
			return Optional.of(singleResult);
		}
		return Optional.empty();
	}

	protected void mergeWithRowValidation(final Map<Integer, ExcelValidationResult> rowsValidation, final Integer rowIndex,
			final ExcelValidationResult cellValidation)
	{
		final ExcelValidationResult excelValidationResult = rowsValidation.get(rowIndex);
		if (excelValidationResult != null)
		{
			cellValidation.getValidationErrors().forEach(excelValidationResult::addValidationError);
		}
		else
		{
			rowsValidation.put(rowIndex, cellValidation);
		}
	}

	protected String getAttributeDisplayName(final SelectedAttribute attribute)
	{
		return getDisplayNameAttributeNameFormatter().format(DefaultExcelAttributeContext
				.ofExcelAttribute(new ExcelAttributeDescriptorAttribute(attribute.getAttributeDescriptor(), attribute.getIsoCode())));
	}

	protected Optional<ExcelValidationResult> validateCell(final ExcelValueTranslator translator,
			final CellValidationMetaData metaData)
	{
		final ExcelValidationResult singleResult = translator.validate(metaData.getImportParameters(),
				metaData.getSelectedAttribute().getAttributeDescriptor(), metaData.getContext());

		if (singleResult.hasErrors())
		{
			return Optional.of(insertValidationHeaders(metaData, singleResult));
		}
		return Optional.empty();
	}

	private ExcelValidationResult insertValidationHeaders(final CellValidationMetaData metaData,
			final ExcelValidationResult singleResult)
	{
		singleResult.getValidationErrors().forEach(validationError -> populateHeaderMetadata(validationError, metaData));
		singleResult.setHeader(prepareValidationHeader(metaData));
		return singleResult;
	}

	protected ValidationMessage prepareValidationHeader(final CellValidationMetaData metaData)
	{
		final ValidationMessage header = new ValidationMessage("excel.import.validation.header.title",
				metaData.getImportParameters().getTypeCode(), metaData.getRowIndex());
		populateHeaderMetadata(header, metaData);
		return header;
	}

	protected void populateHeaderMetadata(final ValidationMessage header, final CellValidationMetaData metaData)
	{
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY, metaData.getRowIndex());
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY,
				metaData.getImportParameters().getTypeCode());
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.SELECTED_ATTRIBUTE_KEY,
				metaData.getSelectedAttribute());
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.SELECTED_ATTRIBUTE_DISPLAYED_NAME_KEY,
				metaData.getSelectedAttributeDisplayedName());

	}

	/**
	 * Generates impex for given sheet
	 *
	 * @param typeSystemSheet
	 *           - sheet contains metainformation about type codes
	 * @param typeSheet
	 *           - sheet contains data for given type code
	 * @return {@link Impex} generated impex object
	 */
	protected Impex generateImpexForSheet(final Sheet typeSystemSheet, final Sheet typeSheet)
	{
		final Impex mainImpex = new Impex();
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(typeSheet.getWorkbook(), typeSheet.getSheetName());
		final List<String> entriesRef = generateDocumentRefs(typeSheet.getLastRowNum() - FIRST_DATA_ROW_INDEX);
		final Collection<SelectedAttribute> selectedAttributes = getExcelHeaderService().getHeaders(typeSystemSheet, typeSheet);
		insertDocumentReferences(mainImpex, typeCode, entriesRef);
		for (final SelectedAttribute selectedAttribute : selectedAttributes)
		{
			final int columnIndex = excelSheetService.findColumnIndex(typeSystemSheet, typeSheet,
					new ExcelAttributeDescriptorAttribute(selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()));
			if (columnIndex < 0)
			{
				continue;
			}

			for (int rowIndex = FIRST_DATA_ROW_INDEX; rowIndex <= typeSheet.getLastRowNum(); rowIndex++)
			{
				if (!hasRowCorrectData(typeSheet.getRow(rowIndex)))
				{
					mainImpex.findUpdates(typeCode).getImpexTable().rowKeySet().remove(rowIndex);
					continue;
				}

				final String cellValue = getExcelCellService().getCellValue(typeSheet.getRow(rowIndex).getCell(columnIndex));
				final ImportParameters importParameters = findImportParameters(selectedAttribute, cellValue, typeCode,
						entriesRef.get(rowIndex - FIRST_DATA_ROW_INDEX));
				final Optional<ExcelValueTranslator<Object>> translator = excelTranslatorRegistry
						.getTranslator(selectedAttribute.getAttributeDescriptor());
				if (translator.isPresent())
				{
					final Impex impex = translator.get().importData(selectedAttribute.getAttributeDescriptor(), importParameters);
					mainImpex.mergeImpex(impex, typeCode, rowIndex);
				}
			}
		}
		return mainImpex;
	}

	/**
	 * In order to simplify cross-reference imports, impex allows to use document reference. This method inserts references
	 * for each row.
	 *
	 * @param mainImpex
	 *           {@link Impex} - represents impex for currently processing workbook
	 * @param typeCode
	 *           - type code of currently processing sheet
	 * @param entriesRef
	 *           - list of generated document references
	 */
	protected void insertDocumentReferences(final Impex mainImpex, final String typeCode, final List<String> entriesRef)
	{
		final ImpexForType impexForType = mainImpex.findUpdates(typeCode);
		final ImpexHeaderValue referenceHeader = new ImpexHeaderValue.Builder(Impex.EXCEL_IMPORT_DOCUMENT_REF_HEADER_NAME).build();
		for (int rowIndex = 0; rowIndex < entriesRef.size(); rowIndex++)
		{
			impexForType.putValue(FIRST_DATA_ROW_INDEX + rowIndex, referenceHeader, entriesRef.get(rowIndex));
		}
	}

	/**
	 * In order to simplify cross-reference imports, impex allows to use document reference. The method generates list of
	 * random UUIDs for each row of imported excel.
	 *
	 * @param rowsCount
	 *           - indicates how many document references should be generated.
	 * @return list of UUIDs
	 */
	protected List<String> generateDocumentRefs(final Integer rowsCount)
	{
		final List<String> refs = new ArrayList<>();
		for (int i = 0; i <= rowsCount; i++)
		{
			refs.add(UUID.randomUUID().toString());
		}
		return refs;
	}

	/**
	 * Parses cell's value taking into account reference pattern and default values (which are located in the second and
	 * third row). Merges default values if needed.
	 *
	 * @param selectedAttribute
	 *           {@link SelectedAttribute} - Information about selected attribute: its isoCode (for localized values),
	 *           referencePattern for references and attributeDescriptor
	 * @param cellValue
	 *           - Original cell's value represented as String
	 * @param typeCode
	 *           - Type code of current sheet
	 * @return {@link ImportParameters} Converted import parameters, merged with default values
	 */
	protected ImportParameters findImportParameters(final SelectedAttribute selectedAttribute, final String cellValue,
			final String typeCode, final String entryRef)
	{
		Preconditions.checkNotNull(cellValue, "Cell's value cannot be null");
		final String referenceFormat = selectedAttribute.getReferenceFormat();
		try
		{
			final DefaultValues defaultValues = getParserRegistry().getParser(referenceFormat).parseDefaultValues(referenceFormat,
					selectedAttribute.getDefaultValues());
			final ParsedValues parsedValues = getParserRegistry().getParser(referenceFormat).parseValue(cellValue, defaultValues);
			return new ImportParameters(typeCode, selectedAttribute.getIsoCode(), parsedValues.getCellValue(), entryRef,
					parsedValues.getParameters());
		}
		catch (final ExcelParserException e)
		{
			LOGGER.debug(String.format("%s is in incorrect format. Correct format is: %s", e.getCellValue(), e.getExpectedFormat()),
					e);
			return new ImportParameters(typeCode, selectedAttribute.getIsoCode(), e.getCellValue(), entryRef, e.getExpectedFormat());
		}
	}

	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	public ExcelTemplateService getExcelTemplateService()
	{
		return excelTemplateService;
	}

	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	@Required
	public void setExcelTemplateService(final ExcelTemplateService excelTemplateService)
	{
		this.excelTemplateService = excelTemplateService;
	}

	public ExcelTranslatorRegistry getExcelTranslatorRegistry()
	{
		return excelTranslatorRegistry;
	}

	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}

	public List<WorkbookValidator> getWorkbookValidators()
	{
		return workbookValidators;
	}

	@Required
	public void setWorkbookValidators(final List<WorkbookValidator> workbookValidators)
	{
		this.workbookValidators = workbookValidators;
	}

	public ExcelValidationEngineAwareValidator getExcelValidationEngineAwareValidator()
	{
		return excelValidationEngineAwareValidator;
	}

	@Required
	public void setExcelValidationEngineAwareValidator(
			final ExcelValidationEngineAwareValidator excelValidationEngineAwareValidator)
	{
		this.excelValidationEngineAwareValidator = excelValidationEngineAwareValidator;
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

	public ExcelWorkbookService getExcelWorkbookService()
	{
		return excelWorkbookService;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
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

	public ExcelCellService getExcelCellService()
	{
		return excelCellService;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
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

	public DisplayNameAttributeNameFormatter getDisplayNameAttributeNameFormatter()
	{
		return displayNameAttributeNameFormatter;
	}

	@Required
	public void setDisplayNameAttributeNameFormatter(final DisplayNameAttributeNameFormatter displayNameAttributeNameFormatter)
	{
		this.displayNameAttributeNameFormatter = displayNameAttributeNameFormatter;
	}

	/**
	 * POJO which contains cell validation meta data
	 */
	protected static class CellValidationMetaData
	{

		private final Integer rowIndex;
		private final ImportParameters importParameters;
		private final SelectedAttribute selectedAttribute;
		private final String selectedAttributeDisplayedName;
		private final Map<String, Object> context;

		public CellValidationMetaData(final Integer rowIndex, final ImportParameters importParameters,
				final SelectedAttribute selectedAttribute, final String selectedAttributeDisplayedName,
				final Map<String, Object> context)
		{
			this.rowIndex = rowIndex;
			this.importParameters = importParameters;
			this.selectedAttribute = selectedAttribute;
			this.selectedAttributeDisplayedName = selectedAttributeDisplayedName;
			this.context = context;
		}

		public Integer getRowIndex()
		{
			return rowIndex;
		}

		public ImportParameters getImportParameters()
		{
			return importParameters;
		}

		public SelectedAttribute getSelectedAttribute()
		{
			return selectedAttribute;
		}

		public String getSelectedAttributeDisplayedName()
		{
			return selectedAttributeDisplayedName;
		}

		public Map<String, Object> getContext()
		{
			return context;
		}
	}
}

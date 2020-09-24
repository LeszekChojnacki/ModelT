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
package com.hybris.backoffice.excel.template;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;


public class ExcelTemplateConstants
{
	public static final int HEADER_ROW_INDEX = 0;
	public static final int REFERENCE_PATTERN_ROW_INDEX = 1;
	public static final int DEFAULT_VALUES_ROW_INDEX = 2;
	public static final int FIRST_DATA_ROW = 3;

	public static final int TYPE_SYSTEM_FIRST_ROW_INDEX = 1;

	public static final String MULTI_VALUE_DELIMITER = ",";
	public static final String REFERENCE_PATTERN_SEPARATOR = ":";

	private static final String TYPE_SYSTEM_SHEET_NAME = "TypeSystem";
	private static final String TYPE_TEMPLATE_SHEET_NAME = "TypeTemplate";
	/**
	 * @deprecated since 1808, use {@link ExcelTemplateConstants.UtilitySheet#TYPE_SYSTEM} instead
	 */
	@Deprecated
	public static final String TYPE_SYSTEM = TYPE_SYSTEM_SHEET_NAME;
	/**
	 * @deprecated since 1808, use {@link ExcelTemplateConstants.UtilitySheet#TYPE_TEMPLATE} instead
	 */
	@Deprecated
	public static final String TYPE_TEMPLATE = TYPE_TEMPLATE_SHEET_NAME;
	/**
	 * @deprecated since 1808, use {@link ExcelTemplateConstants.UtilitySheet} instead
	 */
	@Deprecated
	protected static final Set<String> UTILITY_SHEETS = new HashSet<>(
			Arrays.asList(UtilitySheet.TYPE_SYSTEM.getSheetName(), UtilitySheet.TYPE_TEMPLATE.getSheetName()));

	private ExcelTemplateConstants()
	{
		// blocks the possibility of create a new instance
	}

	public enum Header
	{
		DISPLAY_NAME(0), //
		REFERENCE_PATTERN(1), //
		DEFAULT_VALUE(2);

		private final int index;

		Header(final int index)
		{
			this.index = index;
		}

		public int getIndex()
		{
			return index;
		}
	}

	public enum UtilitySheet
	{
		CLASSIFICATION_TYPE_SYSTEM("ClassificationTypeSystem"), //
		HEADER_PROMPT("HeaderPrompt"), //
		PK("_PK"), //,
		TYPE_TEMPLATE(TYPE_TEMPLATE_SHEET_NAME), //
		TYPE_SYSTEM(TYPE_SYSTEM_SHEET_NAME);

		private final String sheetName;

		UtilitySheet(final String sheetName)
		{
			this.sheetName = sheetName;
		}

		public String getSheetName()
		{
			return sheetName;
		}

		public static boolean isUtilitySheet(final Collection<UtilitySheet> utilitySheets, final String sheetName)
		{
			return utilitySheets.stream() //
					.map(UtilitySheet::getSheetName) //
					.anyMatch(utilitySheetName -> StringUtils.equals(utilitySheetName, sheetName));
		}
	}

	/**
	 * @deprecated since 1808, use {@link ExcelTemplateConstants.UtilitySheet#isUtilitySheet(Collection, String)} instead
	 */
	@Deprecated
	public static boolean isUtilitySheet(final String sheetName)
	{
		return UTILITY_SHEETS.contains(sheetName);
	}

	/**
	 * @deprecated since 1808, use {@link SpecialMark} instead
	 */
	@Deprecated
	public static class Mark
	{
		public static final String MANDATORY = "*";
		public static final String UNIQUE = "^";

		private Mark()
		{
			// blocks the possibility of create a new instance
		}
	}

	public enum SpecialMark
	{
		MANDATORY('*'), //
		MULTIVALUE('+'), //
		READONLY('='), //
		UNIQUE('^');

		private final char mark;

		SpecialMark(final char mark)
		{
			this.mark = mark;
		}

		public char getMark()
		{
			return mark;
		}

		public static String getMergedMarks()
		{
			return Stream.of(ExcelTemplateConstants.SpecialMark.values()) //
					.map(ExcelTemplateConstants.SpecialMark::getMark) //
					.map(String::valueOf) //
					.collect(Collectors.joining());
		}
	}

	public enum ClassificationTypeSystemColumns
	{
		FULL_NAME(0), //
		CLASSIFICATION_SYSTEM(1), //
		CLASSIFICATION_VERSION(2), //
		CLASSIFICATION_CLASS(3), //
		CLASSIFICATION_ATTRIBUTE(4), //
		ATTRIBUTE_LOCALIZED(5), //
		ATTRIBUTE_LOC_LANG(6), //
		MANDATORY(7);

		private final int index;

		ClassificationTypeSystemColumns(final int index)
		{
			this.index = index;
		}

		public int getIndex()
		{
			return index;
		}
	}

	/**
	 * @deprecated since 1808, use {@link TypeSystem} instead
	 */
	@Deprecated
	public static class TypeSystemColumns
	{
		public static final int TYPE_CODE = 0;
		public static final int TYPE_NAME = 1;
		public static final int ATTR_QUALIFIER = 2;
		public static final int ATTR_NAME = 3;
		public static final int ATTR_OPTIONAL = 4;
		public static final int ATTR_TYPE_CODE = 5;
		public static final int ATTR_TYPE_ITEMTYPE = 6;
		public static final int ATTR_LOCALIZED = 7;
		public static final int ATTR_LOC_LANG = 8;
		public static final int ATTR_DISPLAYED_NAME = 9;
		public static final int ATTR_UNIQUE = 10;
		public static final int REFERENCE_FORMAT = 11;

		private TypeSystemColumns()
		{
			// blocks the possibility of create a new instance
		}
	}

	public enum TypeSystem
	{
		TYPE_CODE(0), //
		TYPE_NAME(1), //
		ATTR_QUALIFIER(2), //
		ATTR_NAME(3), //
		ATTR_OPTIONAL(4), //
		ATTR_TYPE_CODE(5), //
		ATTR_TYPE_ITEMTYPE(6), //
		ATTR_LOCALIZED(7), //
		ATTR_LOC_LANG(8), //
		ATTR_DISPLAYED_NAME(9), //
		ATTR_UNIQUE(10), //
		REFERENCE_FORMAT(11);

		private final int index;

		TypeSystem(final int index)
		{
			this.index = index;
		}

		public int getIndex()
		{
			return index;
		}
	}

	public enum HeaderPrompt
	{
		HEADER_TYPE_CODE(0), //
		HEADER_ATTR_DISPLAYED_NAME(1), //
		HEADER_REFERENCE_FORMAT(2);

		private final int index;

		HeaderPrompt(final int index)
		{
			this.index = index;
		}

		public int getIndex()
		{
			return index;
		}
	}

	public static class PkColumns
	{
		public static final int PK = 0;
		public static final int SHEET_NAME = 1;
		public static final int ROW_INDEX = 2;

		private PkColumns()
		{
			// blocks the possibility of create a new instance
		}
	}

	public static class ValidationMessageMetadata
	{
		public static final String ROW_INDEX_KEY = "rowIndex";
		public static final String SELECTED_ATTRIBUTE_KEY = "selectedAttribute";
		public static final String SELECTED_ATTRIBUTE_DISPLAYED_NAME_KEY = "selectedAttributeDisplayedName";
		public static final String SHEET_NAME_KEY = "sheetName";

		private ValidationMessageMetadata()
		{
			// blocks the possibility of create a new instance
		}
	}

	public static class CustomProperties
	{
		public static final String FILE_SIGNATURE = "SAP Hybris vendor v2";

		private CustomProperties()
		{
			// blocks the possibility of create a new instance
		}
	}
}

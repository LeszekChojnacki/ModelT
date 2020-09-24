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
package com.hybris.backoffice.excel.template.populator;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * Default implementation of {@link ExcelAttributeContext} which can be used excel population process.
 * 
 * @see com.hybris.backoffice.excel.template.populator.ExcelCellPopulator
 * @param <ATTRIBUTE>
 *           mandatory attribute of context
 */
public class DefaultExcelAttributeContext<ATTRIBUTE extends ExcelAttribute> implements ExcelAttributeContext<ATTRIBUTE>
{
	private final Map<String, Object> map;
	public static final String EXCEL_ATTRIBUTE = "excelAttribute";

	private DefaultExcelAttributeContext(final Map<String, Object> map)
	{
		this.map = map;
	}

	public static <T extends ExcelAttribute> ExcelAttributeContext<T> ofExcelAttribute(final T excelAttribute)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put(EXCEL_ATTRIBUTE, excelAttribute);
		return new DefaultExcelAttributeContext<>(map);
	}

	public static <T extends ExcelAttribute> ExcelAttributeContext<T> ofMap(final T excelAttribute, final Map<String, Object> map)
	{
		if (!map.containsKey(EXCEL_ATTRIBUTE))
		{
			final Map<String, Object> copiedMap = ImmutableMap.<String, Object> builder().putAll(map)
					.put(EXCEL_ATTRIBUTE, excelAttribute).build();
			return new DefaultExcelAttributeContext<>(copiedMap);
		}
		return new DefaultExcelAttributeContext<>(ImmutableMap.copyOf(map));
	}

	@Override
	public <TYPE> TYPE getAttribute(final @Nonnull String name, final @Nonnull Class<TYPE> type)
	{
		return type.cast(map.get(name));
	}

	@Override
	public ATTRIBUTE getExcelAttribute(final @Nonnull Class<ATTRIBUTE> type)
	{
		return getAttribute(EXCEL_ATTRIBUTE, type);
	}
}

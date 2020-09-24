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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Table;


/**
 * Represents impex for all typeCodes. In order to generate impex script based on this object please see
 * {@link com.hybris.backoffice.excel.importing.ImpexConverter}.
 */
public class Impex implements Serializable
{

	public static final String EXCEL_IMPORT_DOCUMENT_REF_HEADER_NAME = "&ExcelImportRef";

	/**
	 * List of impexes for particular type codes.
	 */
	private final List<ImpexForType> impexes = new ArrayList<>();

	/**
	 * Finds {@link ImpexForType} for given type code. If not found then a new instance of {@link ImpexForType} will be
	 * created, automatically added to list of impexes and returned.
	 * 
	 * @param typeCode
	 * @return
	 */
	public ImpexForType findUpdates(final String typeCode)
	{
		return impexes.stream() //
				.filter(impex -> typeCode.equals(impex.getTypeCode())) //
				.findFirst() //
				.orElseGet(() -> createNewImpex(typeCode));
	}

	/**
	 * Creates a new Impex type for given type code.
	 * 
	 * @param typeCode
	 * @return empty {@link ImpexForType} for given type code
	 */
	protected ImpexForType createNewImpex(final String typeCode)
	{
		final ImpexForType newlyCreatedImpex = new ImpexForType(typeCode);
		this.impexes.add(newlyCreatedImpex);
		return newlyCreatedImpex;
	}

	public List<ImpexForType> getImpexes()
	{
		return impexes.stream().sorted((a1, a2) -> usesDocumentRef(a1) ? 1 : -1).collect(Collectors.toList());
	}

	protected boolean usesDocumentRef(final ImpexForType impexForType)
	{
		return impexForType.getImpexTable().columnKeySet().stream()
				.anyMatch(header -> header.getName().contains(String.format("(%s)", EXCEL_IMPORT_DOCUMENT_REF_HEADER_NAME)));
	}

	/**
	 * Merges subImpex to current main impex.
	 *
	 * @param subImpex
	 *           {@link Impex} contains impex's values for current sheet's cell
	 * @param typeCode
	 *           for current excel's sheet. It's required in order to recognize whether {@link ImpexForType} contains single
	 *           value for current typeCode or dependent impex for another type.
	 * @param rowIndex
	 *           of currently processing row
	 */
	public void mergeImpex(final Impex subImpex, final String typeCode, final Integer rowIndex)
	{
		if (subImpex != null)
		{
			for (final ImpexForType impexForType : subImpex.getImpexes())
			{
				final ImpexForType mainImpexForType = findUpdates(impexForType.getTypeCode());
				final int lastRowIndex = mainImpexForType.getImpexTable().rowKeySet().size();
				for (final Table.Cell<Integer, ImpexHeaderValue, Object> cell : impexForType.getImpexTable().cellSet())
				{
					final Integer index = typeCode.equals(impexForType.getTypeCode()) ? rowIndex : (cell.getRowKey() + lastRowIndex);
					mainImpexForType.putValue(index, cell.getColumnKey(), cell.getValue());
				}
			}
		}
	}

	/**
	 * Merges subImpex into mainImpex. All rows from subImpex are rewritten to mainImpex
	 *
	 * @param subImpex
	 *           {@link Impex} contains changes which should be merged to mainImpex
	 */
	public void mergeImpex(final Impex subImpex)
	{
		if (subImpex != null)
		{
			for (final ImpexForType impexForType : subImpex.getImpexes())
			{
				final ImpexForType mainImpexForType = findUpdates(impexForType.getTypeCode());
				final int latestRowIndex = mainImpexForType.getImpexTable().rowKeySet().size();
				for (final Table.Cell<Integer, ImpexHeaderValue, Object> cell : impexForType.getImpexTable().cellSet())
				{
					mainImpexForType.putValue(latestRowIndex + cell.getRowKey(), cell.getColumnKey(), cell.getValue());
				}
			}
		}
	}

}

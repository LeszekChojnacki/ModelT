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
package com.hybris.backoffice.excel.jobs;

/**
 * Represents file with its content, name and content type.
 */
public class FileContent
{
	private final byte[] data;
	private final String contentType;
	private final String name;

	public FileContent(final byte[] data, final String contentType, final String name)
	{
		this.data = data;
		this.contentType = contentType;
		this.name = name;
	}

	public byte[] getData()
	{
		return data;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getName()
	{
		return name;
	}
}

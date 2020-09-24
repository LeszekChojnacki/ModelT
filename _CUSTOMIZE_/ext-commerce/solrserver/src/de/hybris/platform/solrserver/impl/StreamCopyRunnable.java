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
package de.hybris.platform.solrserver.impl;

import de.hybris.platform.solrserver.SolrServerRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * {@link Runnable} that copies data between streams.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class StreamCopyRunnable implements Runnable
{
	private final InputStream inputStream;
	private final OutputStream outputStream;

	/**
	 * Initializes a new {@link StreamCopyRunnable}.
	 *
	 * @param inputStream
	 *           - the input stream
	 * @param outputStream
	 *           - the output stream
	 *
	 */
	public StreamCopyRunnable(final InputStream inputStream, final OutputStream outputStream)
	{
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void run()
	{
		try
		{
			int chr;
			while ((chr = inputStream.read()) != -1)
			{
				outputStream.write(chr);
			}
		}
		catch (final IOException e)
		{
			throw new SolrServerRuntimeException(e);
		}
	}
}
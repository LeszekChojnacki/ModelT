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
package de.hybris.platform.solrfacetsearch.indexer.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.ExporterException;
import de.hybris.platform.solrfacetsearch.indexer.spi.Exporter;
import de.hybris.platform.solrfacetsearch.indexer.xml.add.Add;
import de.hybris.platform.solrfacetsearch.indexer.xml.add.DocType;
import de.hybris.platform.solrfacetsearch.indexer.xml.add.FieldType;
import de.hybris.platform.solrfacetsearch.indexer.xml.add.ObjectFactory;
import de.hybris.platform.solrfacetsearch.indexer.xml.delete.Delete;
import de.hybris.platform.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


/**
 * Implementation of {@link Exporter} for XML.
 */
public class XMLExporter implements Exporter
{

	protected enum ExportMode
	{
		UPDATE, DELETE
	}

	private static final String TEMP_ENCODING = "UTF-8";

	public static final String SOLR_JAXB_ADD_CONTEXT = "de.hybris.platform.solrfacetsearch.indexer.xml.add";
	public static final String SOLR_JAXB_DELETE_CONTEXT = "de.hybris.platform.solrfacetsearch.indexer.xml.delete";
	public static final String EXPORT_SUB_DIR = "solrExport";
	private static final String UPDATE_BASE_FILE_NAME = "update";
	private static final String DELETE_BASE_FILE_NAME = "delete";

	private static final Logger LOG = Logger.getLogger(XMLExporter.class.getName());

	private final ObjectFactory addObjectFactory;
	private final de.hybris.platform.solrfacetsearch.indexer.xml.delete.ObjectFactory delObjectFactory;

	/**
	 * Default constructor.
	 */
	public XMLExporter()
	{
		addObjectFactory = new ObjectFactory();
		delObjectFactory = new de.hybris.platform.solrfacetsearch.indexer.xml.delete.ObjectFactory();
	}

	@Override
	public void exportToDeleteFromIndex(final Collection<String> pksForDelete, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws ExporterException
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		final String exportDirPath = getExportDirPath(indexConfig);
		verifyCreateFolder(exportDirPath);

		final Delete toMarshallDoc = prepareDeleteXMLDoc(pksForDelete);
		writeToXMLFile(exportDirPath, toMarshallDoc, indexConfig, SOLR_JAXB_DELETE_CONTEXT, indexedType.getUniqueIndexedTypeCode(),
				ExportMode.DELETE);
	}


	@Override
	public void exportToUpdateIndex(final Collection<SolrInputDocument> solrDocuments, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws ExporterException
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		doExport(solrDocuments, indexConfig, indexedType, ExportMode.UPDATE);
	}

	protected void writeToXMLFile(final String exportDirPath, final Object jaxbDocument, final IndexConfig indexConfig,
			final String jaxbContext, final String typeName, final ExportMode exportMode) throws ExporterException
	{
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;

		try
		{
			final File exportFile = prepareExportFile(typeName, exportMode, exportDirPath);
			LOG.info("Writing export result to: " + exportFile.getPath());
			fos = new FileOutputStream(exportFile);
			osw = new OutputStreamWriter(fos, getEncoding(indexConfig));
			final JAXBContext context = JAXBContext.newInstance(jaxbContext);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(jaxbDocument, osw);
		}
		catch (final FileNotFoundException e)
		{
			LOG.error("File for export can not be created. File path = " + exportDirPath);
			throw new ExporterException("File for export can not be created. File path = " + exportDirPath, e);
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("Problem with encoding during JAXB marshalling. " + e);
			throw new ExporterException("Uncorrect encoding for marshaller.", e);
		}
		catch (final JAXBException e)
		{
			LOG.error("JAXB exception occured during marshall.");
			throw new ExporterException("JAXB exception occured during marshall.", e);
		}
		finally
		{
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(osw);
		}

	}

	protected Delete prepareDeleteXMLDoc(final Collection<String> pksForDelete)
	{
		final Delete deleteRoot = delObjectFactory.createDelete();
		final List<String> ids = deleteRoot.getId();
		for (final String pk : pksForDelete)
		{
			if (StringUtils.isNotEmpty(pk))
			{
				ids.add(pk);
			}
		}
		return deleteRoot;
	}

	protected Add prepareXMLDoc(final Collection<SolrInputDocument> solrDocuments)
	{
		final Add addRoot = addObjectFactory.createAdd();
		final List<DocType> docs = addRoot.getDoc();
		for (final SolrInputDocument solrInputDocument : solrDocuments)
		{
			final DocType exDoc = addObjectFactory.createDocType();
			final List<FieldType> fields = exDoc.getField();
			for (final SolrInputField solrInputField : solrInputDocument)
			{
				if (solrInputField != null)
				{
					final FieldType exField = addObjectFactory.createFieldType();
					exField.setName(solrInputField.getName());
					exField.setValue(solrInputField.getValue() == null ? "" : solrInputField.getValue().toString());
					fields.add(exField);
				}
			}
			docs.add(exDoc);
		}
		return addRoot;
	}

	protected String getExportDirPath(final IndexConfig indexConfig) throws ExporterException
	{
		String confExportDirPath = indexConfig.getExportPath();
		if (StringUtils.isEmpty(confExportDirPath))
		{
			LOG.info("Export path was not set in configuration. Indexer try to get path from");
			final String platformTempDir = Utilities.getPlatformConfig().getSystemConfig().getTempDir().getPath();
			if (StringUtils.isEmpty(platformTempDir))
			{
				LOG.error("Export path was not specified neither in configuration nor in HYBRIS_TEMP_DIR");
				throw new ExporterException("Unspecified export path.");
			}

			confExportDirPath = platformTempDir + File.separator + EXPORT_SUB_DIR;
		}
		LOG.info("Exporter dir path: " + confExportDirPath);
		return confExportDirPath;
	}

	protected String getEncoding(final IndexConfig indexConfig)
	{
		return TEMP_ENCODING;
	}

	protected File prepareExportFile(final String typeName, final ExportMode exportMode, final String dirPath)
			throws ExporterException
	{
		final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault());
		final String fileDateStr = dateFormat.format(new Date());

		final StringBuilder filePrefix = new StringBuilder();
		if (exportMode == ExportMode.UPDATE)
		{
			filePrefix.append(UPDATE_BASE_FILE_NAME);
		}
		else if (exportMode == ExportMode.DELETE)
		{
			filePrefix.append(DELETE_BASE_FILE_NAME);
		}
		else
		{
			throw new IllegalArgumentException("Invalid export mode " + exportMode);
		}

		filePrefix.append('_').append(typeName).append('_').append(fileDateStr).append('_');

		try
		{
			return File.createTempFile(filePrefix.toString(), ".xml", new File(dirPath));
		}
		catch (final IOException e)
		{
			LOG.error("Can not create unique file for indexer. ");
			throw new ExporterException(e);
		}
	}

	protected void verifyCreateFolder(final String path) throws ExporterException
	{

		try
		{
			if (path != null)
			{
				final File folder = new File(path);
				if (!(folder.exists() && folder.isDirectory() && folder.canWrite()) && !folder.mkdirs())
				{
					throw new IOException("Failed to create Directory: " + folder.getPath());
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Problem accessing/creating the folder: \"" + path + "\"");
			throw new ExporterException("Uncorrect destination folder for indexer files. " + path, e);
		}

	}

	protected void doExport(final Collection<SolrInputDocument> solrDocuments, final IndexConfig indexConfig,
			final IndexedType indexedType, final ExportMode exportMode) throws ExporterException
	{
		final String exportDirPath = getExportDirPath(indexConfig);
		verifyCreateFolder(exportDirPath);
		LOG.debug("Writing export result to directory " + exportDirPath);
		final Add toMarchalDoc = prepareXMLDoc(solrDocuments);
		writeToXMLFile(exportDirPath, toMarchalDoc, indexConfig, SOLR_JAXB_ADD_CONTEXT, indexedType.getUniqueIndexedTypeCode(),
				exportMode);
	}

}

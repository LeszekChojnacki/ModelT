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
package com.hybris.backoffice;

import de.hybris.platform.util.Config;
import de.hybris.platform.util.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.cockpitng.core.CockpitApplicationException;
import com.hybris.cockpitng.core.modules.LibraryFetcher;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.modules.ModulesEnumeration;
import com.hybris.cockpitng.modules.server.ws.jaxb.CockpitModuleInfo;


/**
 * A tooling class capable of finding and reading module's libraries and holding information about them.
 */
public class BackofficeModulesManager
{

	private static final Logger LOGGER = LoggerFactory.getLogger(BackofficeModulesManager.class);

	private static final String DIR_WIDGET_JAR_LIB = "widgetlib";
	private static final String DIR_DEPLOYED = "deployed";
	private static final String DIR_COCKPIT = ".cockpit";

	private static final String FILE_LIBRARY_INFO = "library.info";

	private static final String CONFIG_KEY_ROOT_DIR = "backoffice.library.home";

	private static final String CONSTANT_DATA_HOME = "${data.home}";
	private static final String CONSTANT_USER_HOME = "${user.home}";
	private static final String CONSTANT_TEMP_DIR = "${java.io.tmpdir}";

	private static final String DEFAULT_ROOT_DIR = CONSTANT_DATA_HOME;

	private static final String URI_SCHEME_MANUAL = "manual";

	private final Map<String, ModuleInfo> moduleInfos;

	private final Map<String, URI> moduleSources;

	private final Map<File, String> libsModules;

	private final LibraryFetcher libraryFetcher;

	private final ModulesEnumeration modules;

	public BackofficeModulesManager(final LibraryFetcher libraryFetcher, final ModulesEnumeration modules)
	{
		this.libraryFetcher = libraryFetcher;
		this.modules = modules;
		this.moduleInfos = new LinkedHashMap<>();
		this.libsModules = new HashMap<>();
		this.moduleSources = new HashMap<>();
	}

	/**
	 * Reloads all informations about registered backoffice modules.
	 * <P>
	 * All currently loaded modules are detached, newly initialized, modules libraries are being recreated and modules
	 * reattached.
	 * </P>
	 *
	 * @throws CockpitApplicationException
	 *            throws if modules could not be reattached or libraries could not be recreated
	 */
	public void refreshAndFetch() throws CockpitApplicationException
	{
		final File libdir = getModuleJarsRootDir();

		final Properties libsURLs = loadModuleLibsURLS(libdir);

		final List<File> libsToRemove = libsURLs.stringPropertyNames().stream().map(File::new)
				.filter(lib -> qualifiesForRemoval(lib, libsURLs)).collect(Collectors.toList());
		for (final File lib : libsToRemove)
		{
			try
			{
				removeModuleJarImmediately(getModuleName(lib).orElse(null), lib, libsURLs);
			}
			catch (final IOException ex)
			{
				throw new CockpitApplicationException(ex);
			}
		}

		libsModules.clear();
		moduleInfos.clear();
		moduleSources.clear();
		modules.reset();
		registerExistingModules(libsURLs);
		fetchModules(libdir, libsURLs);
	}

	/**
	 * Checks if specified module is registered.
	 *
	 * @param moduleName
	 *           name of module
	 * @return <code>true</code> if module with specified name is already registered
	 */
	public boolean isModuleRegistered(final String moduleName)
	{
		return moduleInfos.containsKey(moduleName);
	}

	/**
	 * Provides a list of attached backoffice modules. Returned list contains both fetched modules and manually registered.
	 *
	 * @return list of modules names
	 * @see #registerNewModuleJar(String)
	 */
	public List<String> getModules()
	{
		return Collections.unmodifiableList(new ArrayList<>(moduleInfos.keySet()));
	}

	/**
	 * Provides a name of module that origines from provided URI
	 *
	 * @param moduleURI
	 *           module origin
	 * @return name of module originated from specified URI
	 */
	public Optional<String> getModuleName(final URI moduleURI)
	{
		return moduleSources.entrySet().stream().filter(source -> Objects.equals(moduleURI, source.getValue())).findFirst()
				.map(Map.Entry::getKey);
	}

	/**
	 * Gets specified module's library.
	 *
	 * @param moduleName
	 *           name of module
	 * @return library of a module
	 */
	public Optional<File> getModuleLib(final String moduleName)
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();
		final Properties moduleLibsURLS = loadModuleLibsURLS(moduleJarsRootDir);
		return getModulePath(moduleName, moduleLibsURLS, File::new);
	}

	/**
	 * Gets the origin URI of specified module
	 *
	 * @param moduleName
	 *           name of module
	 * @return origin URI of module
	 */
	public Optional<URI> getModuleSource(final String moduleName)
	{
		return Optional.ofNullable(moduleSources.get(moduleName));
	}

	/**
	 * Gets the origin URI of module that is stored in specified library
	 *
	 * @param moduleLib
	 *           module's library
	 * @return origin URI of module
	 */
	public Optional<URI> getModuleSource(final File moduleLib)
	{
		return getModuleName(moduleLib).map(this::getModuleSource).filter(Optional::isPresent).map(Optional::get);
	}

	/**
	 * Gets information about a specified module.
	 * <P>
	 * Please bear in mind that manager cannot provide module information about manually registerd modules. In this case
	 * {@link Optional#empty()} is returned.
	 * </P>
	 *
	 * @param moduleName
	 *           name of module
	 * @return information about module
	 * @see #registerNewModuleJar(String)
	 */
	public Optional<ModuleInfo> getModuleInfo(final String moduleName)
	{
		return Optional.ofNullable(moduleInfos.get(moduleName));
	}

	/**
	 * Gets information about a specified module that is stored in specified library
	 *
	 * @param moduleLibFile
	 *           module's library
	 * @return information about module
	 */
	public Optional<ModuleInfo> getModuleInfo(final File moduleLibFile)
	{
		return Optional.ofNullable(libsModules.get(moduleLibFile)).map(moduleInfos::get);
	}

	/**
	 * Gets name of module that is stored in specified library
	 *
	 * @param moduleLibFile
	 *           module's library
	 * @return module name
	 */
	public Optional<String> getModuleName(final File moduleLibFile)
	{
		return Optional.ofNullable(libsModules.get(moduleLibFile));
	}

	/**
	 * Gets a root directory for data files
	 *
	 * @return data root directory
	 */
	public File getDataRootDir()
	{
		final String configuredRoot = Config.getString(CONFIG_KEY_ROOT_DIR, DEFAULT_ROOT_DIR);
		return getRootDir(configuredRoot, getDirProcessors());
	}

	/**
	 * Gets a root directory, where module's libraries are kept.
	 *
	 * @return module's libraries directory
	 */
	public File getModuleJarsRootDir()
	{
		final File dataRootDir = getDataRootDir();
		final File widgetJarLibDir = new File(dataRootDir, DIR_WIDGET_JAR_LIB);
		return new File(widgetJarLibDir, DIR_DEPLOYED);
	}

	/**
	 * Creates and attaches new module library
	 *
	 * @param moduleName
	 *           name of module
	 * @return module's library
	 * @throws IOException
	 *            thrown is a library could not be created (i.e. file already exists)
	 * @throws CockpitApplicationException
	 *            thrown if a library for this module is already attached
	 */
	public File registerNewModuleJar(final String moduleName) throws IOException, CockpitApplicationException
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();
		final String moduleLibFileName = getModuleLibFileName(moduleName);
		final File moduleLibFile = new File(moduleJarsRootDir, moduleLibFileName);
		if (moduleLibFile.exists() && !FileUtils.deleteQuietly(moduleLibFile))
		{
			throw new IOException("File already exists: " + moduleLibFile.getAbsolutePath());
		}
		else
		{
			final URI registeredModuleUrl = getManuallyRegisteredModuleUrl(moduleName);
			final Properties moduleLibsURLs = loadModuleLibsURLS(moduleJarsRootDir);
			registerNewModuleJarImmediately(moduleName, moduleLibFile, registeredModuleUrl, moduleLibsURLs);
			return moduleLibFile;
		}
	}

	protected void registerNewModuleJarImmediately(final String moduleName, final File moduleLibFile, final URI source,
			final Properties moduleLibsURLs) throws CockpitApplicationException
	{
		if (moduleLibsURLs.stringPropertyNames().contains(moduleLibFile.getAbsolutePath())
				&& !StringUtils.equals(moduleLibsURLs.getProperty(moduleLibFile.getAbsolutePath()), source.toString()))
		{
			throw new CockpitApplicationException("A library for module is already registered: " + moduleLibFile.getAbsolutePath());
		}

		moduleLibsURLs.put(moduleLibFile.getAbsolutePath(), source.toString());
		final File moduleJarsRootDir = getModuleJarsRootDir();
		storeModuleLibsURLS(moduleLibsURLs, moduleJarsRootDir);
		moduleInfos.put(moduleName, null);
		libsModules.put(moduleLibFile, moduleName);
		moduleSources.put(moduleName, source);
	}

	/**
	 * Detaches previously attached module
	 *
	 * @param moduleName
	 *           module name
	 * @throws IOException
	 *            thrown if library could not be deleted
	 * @throws CockpitApplicationException
	 *            thrown if module cannot be detached (i.e. is a built in module)
	 */
	public void unregisterModuleJar(final String moduleName) throws IOException, CockpitApplicationException
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();
		final String moduleLibFileName = getModuleLibFileName(moduleName);
		final File moduleLibFile = new File(moduleJarsRootDir, moduleLibFileName);
		unregisterModuleJar(moduleName, moduleLibFile);
	}

	protected void unregisterModuleJar(final String moduleName, final File moduleLibFile)
			throws IOException, CockpitApplicationException
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();

		final Properties moduleLibsURLs = loadModuleLibsURLS(moduleJarsRootDir);
		if (qualifiesForUnregistering(moduleLibFile, moduleLibsURLs))
		{
			removeModuleJarImmediately(moduleName, moduleLibFile, moduleLibsURLs);
		}
		else
		{
			throw new CockpitApplicationException("Module library does not qualify for removal: " + moduleLibFile.getAbsolutePath());
		}
	}

	protected void removeModuleJarImmediately(final String moduleName, final File moduleLibFile, final Properties moduleLibsURLs)
			throws IOException
	{
		if (moduleLibFile.exists() && !FileUtils.deleteQuietly(moduleLibFile))
		{
			throw new IOException("Unable to remove a module library: " + moduleLibFile.getAbsolutePath());
		}

		final File moduleJarsRootDir = getModuleJarsRootDir();
		moduleLibsURLs.remove(moduleLibFile.getAbsolutePath());
		storeModuleLibsURLS(moduleLibsURLs, moduleJarsRootDir);
		moduleInfos.remove(moduleName);
		libsModules.remove(moduleLibFile);
		moduleSources.remove(moduleName);
	}

	protected boolean qualifiesForUnregistering(final File moduleLibFile, final Properties moduleLibsURLs)
	{
		return !moduleLibsURLs.containsKey(moduleLibFile.getAbsolutePath())
				|| URI_SCHEME_MANUAL.equals(URI.create(moduleLibsURLs.getProperty(moduleLibFile.getAbsolutePath())).getScheme());
	}

	protected boolean qualifiesForRemoval(final File moduleLibFile, final Properties moduleLibsURLs)
	{
		return !qualifiesForUnregistering(moduleLibFile, moduleLibsURLs);
	}

	protected <R> Optional<R> getModulePath(final String moduleName, final Properties moduleLibsURLS,
			final Function<String, R> pathConverter)
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();
		final String filename = getModuleLibFileName(moduleName);
		final File archiveFile = new File(moduleJarsRootDir, filename);
		return getModulePath(archiveFile, moduleLibsURLS, pathConverter);
	}

	protected <R> Optional<R> getModulePath(final File moduleLib, final Properties moduleLibsURLS,
			final Function<String, R> pathConverter)
	{
		if (moduleLibsURLS.containsKey(moduleLib.getAbsolutePath()))
		{
			return Optional.ofNullable(pathConverter.apply(moduleLib.getAbsolutePath()));
		}
		else
		{
			return Optional.empty();
		}
	}

	protected LibraryFetcher getLibraryFetcher()
	{
		return libraryFetcher;
	}

	protected ModulesEnumeration getModulesEnumeration()
	{
		return modules;
	}

	protected Function<String, String>[] getDirProcessors()
	{
		return new Function[]
		{ dir -> ((String) dir).replace(CONSTANT_USER_HOME, FileUtils.getUserDirectoryPath()),
				dir -> ((String) dir).replace(CONSTANT_TEMP_DIR, FileUtils.getTempDirectoryPath()),
				dir -> ((String) dir).replace(CONSTANT_DATA_HOME, Utilities.getPlatformConfig().getSystemConfig().getDataDir()
						+ File.separator + BackofficeConstants.EXTENSIONNAME) };
	}

	protected File getRootDir(final String configuredRootDir, final Function<String, String>... processors)
	{
		String root = FileUtils.getUserDirectoryPath().concat(IOUtils.DIR_SEPARATOR_UNIX + DIR_COCKPIT);

		if (configuredRootDir != null)
		{
			root = processDir(configuredRootDir, processors);
		}

		if (root != null)
		{
			root = FilenameUtils.normalize(root);
		}

		if (root == null)
		{
			final String errorMessage = "root directory path is null";
			LOGGER.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		final File rootDir = new File(root);
		final boolean dirCreated = rootDir.mkdirs();
		if (dirCreated && LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Directory created: " + rootDir.getAbsolutePath());
		}
		return rootDir;
	}

	protected String processDir(final String dir, final Function<String, String>... processors)
	{
		final String ret = Stream.of(processors).reduce(dir, (before, processor) -> processor.apply(before),
				(before, after) -> after);
		if (ret.contains("${"))
		{
			LOGGER.error("Could not resolve variable in path {}", ret);
		}
		return ret;
	}

	/**
	 * Loads information about libraries mapping: library -> module URL
	 *
	 * @param libRoot
	 *           root directory for local modules libraries
	 * @return map &lt;<code>library path</code> ; <code>module URL</code>&gt;
	 */
	protected Properties loadModuleLibsURLS(final File libRoot)
	{
		final File libpropfile = new File(libRoot, FILE_LIBRARY_INFO);
		final Properties props = new Properties();

		try (final FileInputStream inputStream = new FileInputStream(libpropfile))
		{
			props.load(inputStream);
		}
		catch (final IOException e)
		{
			LOGGER.debug("Could not open file", e);

			return props;
		}

		return props;
	}

	protected void registerExistingModules(final Properties libsURLs)
	{
		for (final String filename : libsURLs.stringPropertyNames())
		{
			final URI moduleURI = URI.create(libsURLs.getProperty(filename));
			final String moduleName = moduleURI.getHost();
			final File moduleLibFile = new File(filename);
			if (isModuleFetched(moduleLibFile))
			{
				LOGGER.info("[{}] Registering manually fetched backoffice module", moduleName);
				try
				{
					registerNewModuleJarImmediately(moduleName, moduleLibFile, moduleURI, libsURLs);
				}
				catch (final CockpitApplicationException e)
				{
					LOGGER.error("Could not register library for module {}: {}", moduleName, e.getLocalizedMessage());
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("Exception occurred while registering library", e);
					}
				}
			}
		}
	}

	protected boolean isModuleFetched(final File moduleLibFile)
	{
		return moduleLibFile.exists();
	}

	/**
	 * @deprecated since 1808, introduce only to ease upgrade process - remove all occurrences when removed
	 */
	@Deprecated
	protected void assureCorrectExistingModulesStructure()
	{
		final File moduleJarsRootDir = getModuleJarsRootDir();
		final Properties libsURLs = loadModuleLibsURLS(moduleJarsRootDir);
		libsURLs.stringPropertyNames().stream().map(File::new).filter(lib -> !qualifiesForRemoval(lib, libsURLs))
				.forEach(moduleLibFile -> {
					final URI moduleURI = URI.create(libsURLs.getProperty(moduleLibFile.getAbsolutePath()));
					final String moduleName = moduleURI.getHost();
					Optional<File> newModuleLib = Optional.empty();
					try (final JarFile jarFile = new JarFile(moduleLibFile))
					{
						if (jarFile.stream().filter(((Predicate<JarEntry>) JarEntry::isDirectory).negate())
								.anyMatch(entry -> !entry.getName().contains(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))))
						{
							LOGGER.warn("Restructuring {} file - moving root level files", moduleLibFile);

							final String[] moduleNameParts = moduleName.split("\\.");
							final String widgetName = moduleNameParts[moduleNameParts.length - 1];
							final File tempFile = File.createTempFile(moduleLibFile.getName(), UUID.randomUUID().toString());
							try (final JarOutputStream temp = new JarOutputStream(new FileOutputStream(tempFile)))
							{
								jarFile.stream().forEach(entry -> {
									if (!entry.isDirectory() && !entry.getName().contains(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX)))
									{
										LOGGER.warn("Moving '{}' to '{}/{}/{}'", entry.getName(), "cockpitng/widgets", widgetName,
												entry.getName());

										final JarEntry newEntry = new JarEntry("cockpitng/widgets/" + widgetName + "/" + entry.getName());
										newEntry.setTime(entry.getTime());
										newEntry.setComment(entry.getComment());
										try
										{
											temp.putNextEntry(newEntry);
											IOUtils.copy(jarFile.getInputStream(entry), temp);
										}
										catch (final IOException e)
										{
											LOGGER.error(e.getLocalizedMessage(), e);
										}
									}
									else
									{
										try
										{
											temp.putNextEntry(entry);
											IOUtils.copy(jarFile.getInputStream(entry), temp);
										}
										catch (final IOException e)
										{
											LOGGER.error(e.getLocalizedMessage(), e);
										}
									}
								});
							}

							newModuleLib = Optional.of(tempFile);
						}
					}
					catch (final IOException e)
					{
						LOGGER.error(e.getLocalizedMessage(), e);
					}

					if (newModuleLib.isPresent())
					{
						FileUtils.deleteQuietly(moduleLibFile);
						try
						{
							FileUtils.moveFile(newModuleLib.get(), moduleLibFile);
						}
						catch (final IOException e)
						{
							LOGGER.error(e.getLocalizedMessage(), e);
						}
					}
				});
	}

	protected void fetchModules(final File libdir, final Properties libsURLs)
	{
		final List<CockpitModuleInfo> modulesToFetch = EnumerationUtils.toList(modules);
		for (final CockpitModuleInfo moduleInfo : modulesToFetch)
		{
			final String moduleName = moduleInfo.getId();
			final String filename = getModuleLibFileName(moduleName);
			final File archiveFile = new File(libdir, filename);

			if (archiveFile.exists() && !FileUtils.deleteQuietly(archiveFile))
			{
				LOGGER.warn("Unable to clean and refresh library for backoffice module: {}", moduleName);
			}

			if (!archiveFile.exists() && getLibraryFetcher().canFetchLibrary(moduleInfo))
			{
				LOGGER.info("[{}] Registering and fetching full backoffice module", moduleName);
				try
				{
					final URI fetchedModuleUrl = URI.create(moduleInfo.getLocationUrl());
					getLibraryFetcher().fetchLibrary(moduleInfo, archiveFile);
					registerNewModuleJarImmediately(moduleName, archiveFile, fetchedModuleUrl, libsURLs);
					registerNewModuleImmediately(moduleInfo, fetchedModuleUrl);
				}
				catch (final CockpitApplicationException e)
				{
					LOGGER.error("Could not fetch or register library for module {}: {}", moduleName, e.getLocalizedMessage());
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("Exception occurred while fetching or registering library", e);
					}
				}
			}
			else
			{
				LOGGER.info("[{}] Registering backoffice configuration-only module", moduleName);
				registerNewModuleImmediately(moduleInfo, URI.create(moduleInfo.getLocationUrl()));
			}
		}

		storeModuleLibsURLS(libsURLs, libdir);
	}

	protected String getModuleLibFileName(final String moduleName)
	{
		return StringUtils.replace(moduleName, ".", "_", -1) + ".jar";
	}

	protected void storeModuleLibsURLS(final Properties props, final File libRoot)
	{
		final File libpropfile = new File(libRoot, FILE_LIBRARY_INFO);

		try (final OutputStream ostream = new FileOutputStream(libpropfile))
		{
			props.store(ostream, "This files was generated. Do not change anything since it will be overwritten.");
		}
		catch (final IOException e)
		{
			LOGGER.warn("Could not store modules information: {}", e.getLocalizedMessage());
			LOGGER.debug(e.getLocalizedMessage(), e);
		}
	}

	protected void registerNewModuleImmediately(final ModuleInfo moduleInfo, final URI source)
	{
		moduleInfos.put(moduleInfo.getId(), moduleInfo);
		moduleSources.put(moduleInfo.getId(), source);
	}

	protected URI getManuallyRegisteredModuleUrl(final String moduleName)
	{
		try
		{
			return new URI(URI_SCHEME_MANUAL, moduleName, null, null);
		}
		catch (final URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}

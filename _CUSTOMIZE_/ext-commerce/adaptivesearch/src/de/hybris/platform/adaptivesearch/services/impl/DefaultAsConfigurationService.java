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
package de.hybris.platform.adaptivesearch.services.impl;

import de.hybris.platform.adaptivesearch.daos.AsConfigurationDao;
import de.hybris.platform.adaptivesearch.data.AsRankChange;
import de.hybris.platform.adaptivesearch.data.AsRankChangeType;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.services.AsConfigurationService;
import de.hybris.platform.adaptivesearch.strategies.AsCloneStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsValidationStrategy;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsConfigurationService}.
 */
public class DefaultAsConfigurationService implements AsConfigurationService
{
	protected static final String TYPE_PARAM = "type";
	protected static final String PARENT_CONFIGURATION_PARAM = "parentConfiguration";
	protected static final String SOURCE_ATTRIBUTE_PARAM = "sourceAttribute";
	protected static final String TARGET_ATTRIBUTE_PARAM = "targetAttribute";
	protected static final String UID_PARAM = "uid";
	protected static final String UIDS_PARAM = "uids";

	private TypeService typeService;
	private ModelService modelService;
	private AsConfigurationDao asConfigurationDao;
	private AsCloneStrategy asCloneStrategy;
	private AsValidationStrategy asValidationStrategy;

	@Override
	public <T extends AbstractAsConfigurationModel> Optional<T> getConfigurationForUid(final Class<T> type,
			final CatalogVersionModel catalogVersion, final String uid)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(TYPE_PARAM, type);
		ServicesUtil.validateParameterNotNullStandardMessage(UID_PARAM, uid);

		return asConfigurationDao.findConfigurationByUid(type, catalogVersion, uid);
	}

	@Override
	public void refreshConfiguration(final AbstractAsConfigurationModel configuration)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("configuration", configuration);

		modelService.refresh(configuration);
	}

	@Override
	public <T extends AbstractAsConfigurationModel> T createConfiguration(final Class<T> type)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(TYPE_PARAM, type);

		return modelService.create(type);
	}

	@Override
	public <T extends AbstractAsConfigurationModel> T cloneConfiguration(final T configuration)
	{
		return asCloneStrategy.clone(configuration);
	}

	@Override
	public void saveConfiguration(final AbstractAsConfigurationModel configuration)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("configuration", configuration);

		modelService.save(configuration);
	}

	@Override
	public void removeConfiguration(final AbstractAsConfigurationModel configuration)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("configuration", configuration);

		modelService.remove(configuration);
	}

	@Override
	public boolean isValid(final AbstractAsConfigurationModel configuration)
	{
		return asValidationStrategy.isValid(configuration);
	}

	@Override
	public boolean moveConfiguration(final AbstractAsConfigurationModel parentConfiguration, final String sourceAttribute,
			final String targetAttribute, final String uid)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PARENT_CONFIGURATION_PARAM, parentConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(SOURCE_ATTRIBUTE_PARAM, sourceAttribute);
		ServicesUtil.validateParameterNotNullStandardMessage(TARGET_ATTRIBUTE_PARAM, targetAttribute);
		ServicesUtil.validateParameterNotNullStandardMessage(UID_PARAM, uid);

		findAndCheckConfigurationType(parentConfiguration, sourceAttribute);
		final Class<? extends AbstractAsConfigurationModel> targetType = findAndCheckConfigurationType(parentConfiguration,
				targetAttribute);

		final Optional<AbstractAsConfigurationModel> configurationOptional = findConfiguration(parentConfiguration, sourceAttribute,
				uid);
		if (!configurationOptional.isPresent())
		{
			return false;
		}

		final AbstractAsConfigurationModel configuration = configurationOptional.get();

		final AbstractAsConfigurationModel newConfiguration = modelService.clone(configuration, targetType);

		modelService.remove(configuration);
		modelService.save(newConfiguration);
		modelService.refresh(parentConfiguration);

		return true;
	}



	@Override
	public List<AsRankChange> rankBeforeConfiguration(final AbstractAsConfigurationModel parentConfiguration,
			final String attribute, final String rankBeforeUid, final String... uids)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PARENT_CONFIGURATION_PARAM, parentConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(SOURCE_ATTRIBUTE_PARAM, attribute);
		ServicesUtil.validateParameterNotNullStandardMessage(UIDS_PARAM, uids);

		if (ArrayUtils.isEmpty(uids))
		{
			return Collections.emptyList();
		}

		findAndCheckConfigurationType(parentConfiguration, attribute);
		final List<AbstractAsConfigurationModel> oldConfigurations = parentConfiguration.getProperty(attribute);

		final ConfigurationInfo<AbstractAsConfigurationModel> rankBeforeConfigurationInfo = buildConfigurationInfo(
				oldConfigurations, rankBeforeUid);

		if (StringUtils.isNotBlank(rankBeforeUid) && !rankBeforeConfigurationInfo.isPresent())
		{
			// we cannot move the configurations because we don't know the new rank
			return Arrays.stream(uids).map(this::createNoOperationRankChange).collect(Collectors.toList());
		}

		final List<ConfigurationInfo<AbstractAsConfigurationModel>> configurationInfos = buildConfigurationInfos(oldConfigurations,
				uids);

		final int index = calculateRankBeforeIndex(oldConfigurations, rankBeforeConfigurationInfo, configurationInfos);
		int changeIndex = index;

		final List<AsRankChange> rankChanges = new ArrayList<>();
		final List<AbstractAsConfigurationModel> configurations = new ArrayList<>();

		for (final ConfigurationInfo<AbstractAsConfigurationModel> configurationInfo : configurationInfos)
		{
			if (configurationInfo.getConfiguration() != null)
			{
				final int oldRank = configurationInfo.getRank();
				final int newRank = changeIndex;
				rankChanges.add(createMoveRankChange(configurationInfo.getUid(), oldRank, newRank));

				configurations.add(configurationInfo.getConfiguration());

				changeIndex++;
			}
			else
			{
				rankChanges.add(createNoOperationRankChange(configurationInfo.getUid()));
			}
		}

		rerankConfigurations(parentConfiguration, attribute, index, configurations);

		return rankChanges;
	}

	protected int calculateRankBeforeIndex(final List<AbstractAsConfigurationModel> oldConfigurations,
			final ConfigurationInfo<AbstractAsConfigurationModel> rankBeforeConfigurationInfo,
			final List<ConfigurationInfo<AbstractAsConfigurationModel>> configurationInfos)
	{
		int newRank;

		if (rankBeforeConfigurationInfo.isPresent())
		{
			newRank = rankBeforeConfigurationInfo.getRank();
		}
		else
		{
			newRank = oldConfigurations.isEmpty() ? 0 : oldConfigurations.size();
		}

		for (final ConfigurationInfo<AbstractAsConfigurationModel> configurationInfo : configurationInfos)
		{
			if (configurationInfo.isPresent() && configurationInfo.getRank() < newRank)
			{
				newRank--;
			}
		}

		return newRank;
	}

	@Override
	public List<AsRankChange> rankAfterConfiguration(final AbstractAsConfigurationModel parentConfiguration,
			final String attribute, final String rankAfterUid, final String... uids)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PARENT_CONFIGURATION_PARAM, parentConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(SOURCE_ATTRIBUTE_PARAM, attribute);
		ServicesUtil.validateParameterNotNullStandardMessage(UIDS_PARAM, uids);

		if (ArrayUtils.isEmpty(uids))
		{
			return Collections.emptyList();
		}

		findAndCheckConfigurationType(parentConfiguration, attribute);
		final List<AbstractAsConfigurationModel> oldConfigurations = parentConfiguration.getProperty(attribute);

		final ConfigurationInfo<AbstractAsConfigurationModel> rankAfterConfigurationInfo = buildConfigurationInfo(oldConfigurations,
				rankAfterUid);

		if (StringUtils.isNotBlank(rankAfterUid) && !rankAfterConfigurationInfo.isPresent())
		{
			// we cannot move the configurations because we don't know the new rank
			return Arrays.stream(uids).map(this::createNoOperationRankChange).collect(Collectors.toList());
		}

		final List<ConfigurationInfo<AbstractAsConfigurationModel>> configurationInfos = buildConfigurationInfos(oldConfigurations,
				uids);

		final int index = calculateRankAfterIndex(rankAfterConfigurationInfo, configurationInfos);
		int changeIndex = index;

		final List<AsRankChange> rankChanges = new ArrayList<>();
		final List<AbstractAsConfigurationModel> configurations = new ArrayList<>();

		for (final ConfigurationInfo<AbstractAsConfigurationModel> configurationInfo : configurationInfos)
		{
			if (configurationInfo.getConfiguration() != null)
			{
				final int oldRank = configurationInfo.getRank();
				final int newRank = changeIndex;
				rankChanges.add(createMoveRankChange(configurationInfo.getUid(), oldRank, newRank));

				configurations.add(configurationInfo.getConfiguration());

				changeIndex++;
			}
			else
			{
				rankChanges.add(createNoOperationRankChange(configurationInfo.getUid()));
			}
		}

		rerankConfigurations(parentConfiguration, attribute, index, configurations);

		return rankChanges;
	}

	protected int calculateRankAfterIndex(final ConfigurationInfo<AbstractAsConfigurationModel> rankBeforeConfigurationInfo,
			final List<ConfigurationInfo<AbstractAsConfigurationModel>> configurationInfos)
	{
		int newRank;

		if (rankBeforeConfigurationInfo.isPresent())
		{
			newRank = rankBeforeConfigurationInfo.getRank() + 1;
		}
		else
		{
			newRank = 0;
		}

		for (final ConfigurationInfo<AbstractAsConfigurationModel> configurationInfo : configurationInfos)
		{
			if (configurationInfo.isPresent() && configurationInfo.getRank() < newRank)
			{
				newRank--;
			}
		}

		return newRank;
	}

	@Override
	public AsRankChange rerankConfiguration(final AbstractAsConfigurationModel parentConfiguration, final String attribute,
			final String uid, final int change)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(PARENT_CONFIGURATION_PARAM, parentConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(SOURCE_ATTRIBUTE_PARAM, attribute);
		ServicesUtil.validateParameterNotNullStandardMessage(UID_PARAM, uid);

		findAndCheckConfigurationType(parentConfiguration, attribute);
		final List<AbstractAsConfigurationModel> configurations = parentConfiguration.getProperty(attribute);

		if (change == 0 || CollectionUtils.isEmpty(configurations))
		{
			return createNoOperationRankChange(uid);
		}

		final ConfigurationInfo<AbstractAsConfigurationModel> configurationInfo = buildConfigurationInfo(configurations, uid);

		if (!configurationInfo.isPresent())
		{
			return createNoOperationRankChange(uid);
		}

		final int oldRank = configurationInfo.getRank();
		final int newRank = oldRank + change;

		if (newRank < 0 || newRank >= configurations.size())
		{
			return createNoOperationRankChange(uid);
		}

		rerankConfigurations(parentConfiguration, attribute, newRank,
				Collections.singletonList(configurationInfo.getConfiguration()));

		return createMoveRankChange(configurationInfo.getUid(), oldRank, newRank);
	}

	protected <T extends AbstractAsConfigurationModel> Class<T> findAndCheckConfigurationType(
			final AbstractAsConfigurationModel parentConfiguration, final String attribute)
	{
		final ComposedTypeModel parentType = typeService.getComposedTypeForClass(parentConfiguration.getClass());
		try
		{
			final AttributeDescriptorModel attributeDescriptor = typeService.getAttributeDescriptor(parentType, attribute);

			if (!(attributeDescriptor.getAttributeType() instanceof CollectionTypeModel))
			{
				throw new AttributeNotSupportedException("cannot perform operation on attribute" + attribute, attribute);
			}

			final CollectionTypeModel attributeType = (CollectionTypeModel) attributeDescriptor.getAttributeType();
			final Class<?> configurationType = typeService.getModelClass(attributeType.getElementType().getCode());

			if (!AbstractAsConfigurationModel.class.isAssignableFrom(configurationType))
			{
				throw new AttributeNotSupportedException("cannot perform operation on attribute" + attribute, attribute);
			}

			return (Class<T>) configurationType;
		}
		catch (final UnknownIdentifierException e)
		{
			throw new AttributeNotSupportedException(e.getMessage(), e, attribute);
		}
	}

	protected <T extends AbstractAsConfigurationModel> Optional<T> findConfiguration(
			final AbstractAsConfigurationModel parentConfiguration, final String attribute, final String uid)
	{
		if (StringUtils.isBlank(uid))
		{
			return Optional.empty();
		}

		final List<T> configurations = parentConfiguration.getProperty(attribute);

		for (final T configuration : configurations)
		{
			if (configurationnMatches(configuration, uid))
			{
				return Optional.of(configuration);
			}
		}

		return Optional.empty();
	}

	protected boolean configurationnMatches(final AbstractAsConfigurationModel configuration, final String uid)
	{
		return StringUtils.isNotBlank(uid) && Objects.equals(configuration.getUid(), uid);
	}

	protected <T extends AbstractAsConfigurationModel> ConfigurationInfo<T> buildConfigurationInfo(final List<T> configurations,
			final String uid)
	{
		if (StringUtils.isBlank(uid))
		{
			return new ConfigurationInfo<>(uid);
		}

		int index = 0;

		for (final T configuration : configurations)
		{
			if (configurationnMatches(configuration, uid))
			{
				return new ConfigurationInfo(uid, configuration, index);
			}

			index++;
		}

		return new ConfigurationInfo<>(uid);
	}

	protected <T extends AbstractAsConfigurationModel> List<ConfigurationInfo<T>> buildConfigurationInfos(
			final List<T> configurations, final String... uids)
	{
		return Arrays.stream(uids).map(uid -> buildConfigurationInfo(configurations, uid)).collect(Collectors.toList());
	}

	protected <T extends AbstractAsConfigurationModel> void rerankConfigurations(
			final AbstractAsConfigurationModel parentConfiguration, final String attribute, final int newIndex,
			final List<T> configurations)
	{
		if (CollectionUtils.isNotEmpty(configurations))
		{
			final List<T> oldConfigurations = parentConfiguration.getProperty(attribute);

			final List<T> newConfigurations = new ArrayList(oldConfigurations);
			newConfigurations.removeAll(configurations);
			newConfigurations.addAll(newIndex, configurations);

			parentConfiguration.setProperty(attribute, newConfigurations);

			modelService.save(parentConfiguration);
		}
	}

	protected AsRankChange createNoOperationRankChange(final String uid)
	{
		final AsRankChange rankChange = new AsRankChange();
		rankChange.setType(AsRankChangeType.NO_OPERATION);
		rankChange.setUid(uid);
		return rankChange;
	}

	protected AsRankChange createMoveRankChange(final String uid, final int oldRank, final int newRank)
	{
		final AsRankChange rankChange = new AsRankChange();
		rankChange.setType(AsRankChangeType.MOVE);
		rankChange.setUid(uid);
		rankChange.setOldRank(Integer.valueOf(oldRank));
		rankChange.setNewRank(Integer.valueOf(newRank));
		return rankChange;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public AsConfigurationDao getAsConfigurationDao()
	{
		return asConfigurationDao;
	}

	@Required
	public void setAsConfigurationDao(final AsConfigurationDao asConfigurationDao)
	{
		this.asConfigurationDao = asConfigurationDao;
	}

	public AsCloneStrategy getAsCloneStrategy()
	{
		return asCloneStrategy;
	}

	@Required
	public void setAsCloneStrategy(final AsCloneStrategy asCloneStrategy)
	{
		this.asCloneStrategy = asCloneStrategy;
	}

	public AsValidationStrategy getAsValidationStrategy()
	{
		return asValidationStrategy;
	}

	@Required
	public void setAsValidationStrategy(final AsValidationStrategy asValidationStrategy)
	{
		this.asValidationStrategy = asValidationStrategy;
	}

	protected static class ConfigurationInfo<T extends AbstractAsConfigurationModel>
	{
		private final String uid;
		private final T configuration;
		private final int rank;

		public ConfigurationInfo(final String uid)
		{
			this.uid = uid;
			configuration = null;
			rank = -1;
		}

		public ConfigurationInfo(final String uid, final T configuration, final int rank)
		{
			this.uid = uid;
			this.configuration = configuration;
			this.rank = rank;
		}

		public String getUid()
		{
			return uid;
		}

		public T getConfiguration()
		{
			return configuration;
		}

		public int getRank()
		{
			return rank;
		}

		public boolean isPresent()
		{
			return configuration != null;
		}
	}

}

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
package com.hybris.backoffice.cockpitng.dataaccess.facades.type;

import de.hybris.platform.cache.AbstractCacheUnit;
import de.hybris.platform.cache.Cache;
import de.hybris.platform.cache.InvalidationListener;
import de.hybris.platform.cache.InvalidationManager;
import de.hybris.platform.cache.InvalidationTarget;
import de.hybris.platform.cache.InvalidationTopic;
import de.hybris.platform.cache.RemoteInvalidationSource;
import de.hybris.platform.core.Constants;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.enums.TypeOfCollectionEnum;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.MapTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.core.model.type.RelationMetaTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewAttributeDescriptorModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.product.VariantsService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.ViewResultItem;
import de.hybris.platform.util.typesystem.PlatformStringUtils;
import de.hybris.platform.variants.model.VariantAttributeDescriptorModel;
import de.hybris.platform.variants.model.VariantTypeModel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.expression.spel.SpelEvaluationException;

import com.google.common.collect.Lists;
import com.hybris.backoffice.cockpitng.dataaccess.facades.common.PlatformFacadeStrategyHandleCache;
import com.hybris.backoffice.cockpitng.dataaccess.facades.type.expression.AttributeExpressionResolver;
import com.hybris.backoffice.cockpitng.dataaccess.facades.type.expression.AttributeExpressionResolverFactory;
import com.hybris.cockpitng.core.util.ClassLoaderUtils;
import com.hybris.cockpitng.core.util.Resettable;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.CollectionDataType;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.MapDataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;


public class DefaultPlatformTypeFacadeStrategy implements TypeFacadeStrategy, Resettable
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPlatformTypeFacadeStrategy.class);
	private final ReadWriteLock cacheLock = new ReentrantReadWriteLock(false);
	private final Map<String, DataType> typeCache = new HashMap<>();
	private final Map<String, DataType> lightTypeCache = new HashMap<>();
	private final Map<String, List<DataAttribute>> attributeCache = new HashMap<>();
	private ModelService modelService;
	private TypeService typeService;
	private VariantsService variantsService;
	private I18NService i18nService;
	private PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache;
	private final InvalidationListener invalidationListener = new InvalidationListener()
	{

		public final String COMPOSEDTYPE_TC = PlatformStringUtils.valueOf(Constants.TC.ComposedType);
		public final String ATOMICTYPE_TC = PlatformStringUtils.valueOf(Constants.TC.AtomicType);
		public final String COLLECTIONTYPE_TC = PlatformStringUtils.valueOf(Constants.TC.CollectionType);
		public final String MAPTYPE_TC = PlatformStringUtils.valueOf(Constants.TC.MapType);
		public final String ATTRIBUTE_DESCRIPTOR = PlatformStringUtils.valueOf(Constants.TC.AttributeDescriptor);

		@Override
		public void keyInvalidated(final Object[] key, final int invalidationType, final InvalidationTarget target,
				final RemoteInvalidationSource remoteSrc)
		{
			if (isRealCacheInvalidation(target) && isTypeSystemChanged(key) && isInvalidate(invalidationType))
			{
				LOG.debug("Platform type system cache invalidation");
				reset();
			}
		}

		/**
		 * If the invalidation happens in a rolling transaction it may be still rolled back. In this case cache invalidation may
		 * happen too early.
		 */
		private boolean isRealCacheInvalidation(final InvalidationTarget target)
		{
			return target instanceof Cache;
		}

		private boolean isTypeSystemChanged(final Object[] key)
		{
			final Object deploymentTypeCode = key[2];

			return Stream.of(COMPOSEDTYPE_TC, ATOMICTYPE_TC, COLLECTIONTYPE_TC, MAPTYPE_TC, ATTRIBUTE_DESCRIPTOR)
					.anyMatch(deploymentTypeCode::equals);
		}

		private boolean isInvalidate(final int invalidationType)
		{
			return AbstractCacheUnit.INVALIDATIONTYPE_REMOVED == invalidationType
					|| AbstractCacheUnit.INVALIDATIONTYPE_MODIFIED == invalidationType;
		}
	};
	private List<String> dynamicTypesBlacklist;
	private Map<String, Collection<String>> typeAttributesBlackList;
	private AttributeExpressionResolverFactory resolverFactory;

	@PostConstruct
	public void init()
	{
		final InvalidationTopic topic = InvalidationManager.getInstance().getInvalidationTopic(new String[]
		{ Cache.CACHEKEY_HJMP, Cache.CACHEKEY_ENTITY });
		topic.addInvalidationListener(invalidationListener);
	}

	@Override
	public DataType load(final String code) throws TypeNotFoundException
	{
		Validate.notNull("The given code is null!", code);
		return load(code, null);
	}

	@Override
	public DataType load(final String code, final Context ctx) throws TypeNotFoundException
	{
		Validate.notNull("The given code is null!", code);
		return loadAndCache(code, ctx, false);
	}

	/**
	 * @param code
	 *           type code
	 * @param context
	 *           generic context
	 * @param lightWeight
	 *           if true only the type's definition is loaded, no reference types etc must be loaded at this point
	 * @return The (possibly) cached type.
	 * @throws TypeNotFoundException
	 *            throw when the given type cannot be found
	 */
	protected DataType loadAndCache(final String code, final Context context, final boolean lightWeight)
			throws TypeNotFoundException
	{
		DataType dataType;
		Lock readLock = null;
		try
		{
			readLock = cacheLock.readLock();
			readLock.lock();
			dataType = lightWeight ? lightTypeCache.get(code) : typeCache.get(code);
		}
		finally
		{
			if (readLock != null)
			{
				readLock.unlock();
			}
		}
		if (dataType == null)
		{
			try
			{
				boolean staticType = true;
				final TypeModel typeForCode = typeService.getTypeForCode(code);
				if (CollectionUtils.isNotEmpty(getDynamicTypesBlacklist()))
				{
					for (final String type : dynamicTypesBlacklist)
					{
						if (typeService.isAssignableFrom(type, code)
								|| (ViewTypeModel._TYPECODE.equals(type) && typeService.getTypeForCode(code) instanceof ViewTypeModel))
						{
							staticType = false;
							break;
						}
					}
				}
				Lock lock = null;
				if (staticType)
				{
					lock = cacheLock.writeLock();
					lock.lock();
				}
				try
				{
					dataType = convertType(typeForCode, lightWeight, context);
					if (dataType == null)
					{
						throw new TypeNotFoundException(code);
					}
					if (staticType)
					{
						if (lightWeight)
						{
							lightTypeCache.put(code, dataType);
						}
						else
						{
							typeCache.put(code, dataType);
						}
					}
				}
				finally
				{
					if (lock != null)
					{
						lock.unlock();
					}
				}
			}
			catch (final UnknownIdentifierException e)
			{
				throw new TypeNotFoundException(code, e);
			}
		}
		return dataType;

	}

	public DataType convertType(final TypeModel platformType, final boolean lightWeight)
	{
		return convertType(platformType, lightWeight, null);
	}

	public DataType convertType(final TypeModel platformType, final boolean lightWeight, final Context context)
	{

		final DataType.Builder typeBuilder;

		if (platformType instanceof AtomicTypeModel)
		{
			typeBuilder = createTypeBuilder(platformType.getCode());
			typeBuilder.type(DataType.Type.ATOMIC);
			typeBuilder.clazz(((AtomicTypeModel) platformType).getJavaClass());
		}
		else if (platformType instanceof CollectionTypeModel)
		{
			typeBuilder = createCollectionTypeBuilder(platformType.getCode(),
					((CollectionTypeModel) platformType).getTypeOfCollection());
			final TypeModel valueType = ((CollectionTypeModel) platformType).getElementType();
			((CollectionDataType.CollectionBuilder) typeBuilder).valueType(convertType(valueType, lightWeight));
		}
		else if (platformType instanceof MapTypeModel)
		{
			typeBuilder = createMapTypeBuilder(platformType.getCode());
			final TypeModel keyType = ((MapTypeModel) platformType).getArgumentType();
			final TypeModel valueType = ((MapTypeModel) platformType).getReturntype();
			((MapDataType.MapBuilder) typeBuilder).keyType(convertType(keyType, lightWeight));
			((MapDataType.MapBuilder) typeBuilder).valueType(convertType(valueType, lightWeight));
		}
		else if (platformType instanceof ViewTypeModel)
		{
			typeBuilder = createTypeBuilder(platformType.getCode());
			typeBuilder.supertype(Object.class.getName());

			final ViewTypeModel viewType = (ViewTypeModel) platformType;
			final Collection<ViewAttributeDescriptorModel> attributes = Lists.newArrayList(viewType.getParams());
			attributes.addAll(viewType.getColumns());
			for (final ViewAttributeDescriptorModel att : attributes)
			{
				try
				{
					typeBuilder.attribute(convertAttribute(att));
				}
				catch (final TypeNotFoundException e)
				{
					LOG.error("Could not convert attribute " + att, e);
				}
			}
		}
		else if (platformType instanceof ComposedTypeModel)
		{
			typeBuilder = createTypeBuilder(platformType.getCode());
			typeBuilder.clazz(typeService.getModelClass((ComposedTypeModel) platformType));

			final Collection<AttributeDescriptorModel> variantAttributeDescriptors = new HashSet<>();
			if (platformType instanceof VariantTypeModel)
			{
				final VariantTypeModel variantType = getVariantsService().getVariantTypeForCode(platformType.getCode());
				variantAttributeDescriptors.addAll(getVariantsService().getVariantAttributesForVariantType(variantType));
			}
			else if (platformType instanceof EnumerationMetaTypeModel)
			{
				typeBuilder.type(DataType.Type.ENUM);
			}
			else
			{
				typeBuilder.type(DataType.Type.COMPOUND);
			}
			typeBuilder.abstractType(BooleanUtils.isTrue(((ComposedTypeModel) platformType).getAbstract()));
			final ComposedTypeModel superType = ((ComposedTypeModel) platformType).getSuperType();
			if (superType != null)
			{
				typeBuilder.supertype(superType.getCode());
			}
			final Collection<ComposedTypeModel> allSuperTypes = ((ComposedTypeModel) platformType).getAllSuperTypes();
			if (CollectionUtils.isNotEmpty(allSuperTypes))
			{
				final List<String> superTypesCodes = allSuperTypes.stream().map(TypeModel::getCode).collect(Collectors.toList());
				superTypesCodes.add(Object.class.getCanonicalName());
				typeBuilder.allSuperTypes(superTypesCodes);
			}
			for (final ComposedTypeModel subType : ((ComposedTypeModel) platformType).getSubtypes())
			{
				typeBuilder.subtype(subType.getCode());
			}

			if (!lightWeight)
			{
				final Set<DataAttribute> attributes = convertAndCacheAttributes(platformType, variantAttributeDescriptors);
				attributes.forEach(typeBuilder::attribute);
			}
			typeBuilder.singleton(Boolean.TRUE.equals(((ComposedTypeModel) platformType).getSingleton()));
		}
		else
		{
			typeBuilder = createTypeBuilder(platformType.getCode());
		}

		final Set<Locale> supportedLocales = i18nService.getSupportedLocales();
		for (final Locale locale : supportedLocales)
		{
			typeBuilder.label(locale, platformType.getName(locale));
		}
		typeBuilder.searchable(isSearchable(platformType));

		return typeBuilder.build();
	}

	protected DataType.Builder createTypeBuilder(final String typeCode)
	{
		return new DataType.Builder(typeCode);
	}

	/**
	 * @deprecated since 2005, please use {@link #createCollectionTypeBuilder(String, TypeOfCollectionEnum)}
	 */
	@Deprecated
	protected CollectionDataType.Builder createCollectionTypeBuilder(final String typeCode)
	{
		return createCollectionTypeBuilder(typeCode, TypeOfCollectionEnum.COLLECTION);
	}

	protected DataType.Builder createCollectionTypeBuilder(final String typeCode, final TypeOfCollectionEnum typeOfCollection)
	{
		final DataType.Type type = convertPlatformCollectionTypeToDatatypeType(typeOfCollection);
		return new CollectionDataType.CollectionBuilder(typeCode, type);
	}

	protected DataType.Type convertPlatformCollectionTypeToDatatypeType(final TypeOfCollectionEnum typeOfCollection)
	{
		final DataType.Type type;
		switch (typeOfCollection)
		{
			case LIST:
				type = DataType.Type.LIST;
				break;
			case SET:
				type = DataType.Type.SET;
				break;
			case COLLECTION:
				type = DataType.Type.COLLECTION;
				break;
			default:
				throw new IllegalArgumentException("Only LIST, SET or COLLECTION types are allowed");
		}
		return type;
	}

	protected MapDataType.MapBuilder createMapTypeBuilder(final String typeCode)
	{
		return new MapDataType.MapBuilder(typeCode);
	}

	private Set<DataAttribute> convertAndCacheAttributes(@NotNull final TypeModel platformType,
			@NotNull final Collection<AttributeDescriptorModel> variantAttributes)
	{
		final Set<String> hierarchy = getTypeHierarchy((ComposedTypeModel) platformType);
		final Set<String> blacklistedAttributes = getBlacklistedAttributes(hierarchy);

		final Set<DataAttribute> allAttributes = new LinkedHashSet<>(
				convertAttributes(hierarchy, variantAttributes, blacklistedAttributes));
		final ComposedTypeModel type = (ComposedTypeModel) platformType;

		if (attributeCache.containsKey(type.getCode()))
		{
			attributeCache.get(type.getCode()).stream()
					.filter(attribute -> !blacklistedAttributes.contains(attribute.getQualifier())).forEach(allAttributes::add);
		}
		else
		{
			final Set<AttributeDescriptorModel> attributeDescriptors = typeService.getAttributeDescriptorsForType(type);
			final List<DataAttribute> convertedAttributes = convertAttributes(hierarchy, attributeDescriptors,
					blacklistedAttributes);
			attributeCache.put(type.getCode(), convertedAttributes);
			allAttributes.addAll(convertedAttributes);
		}
		return allAttributes;
	}

	protected Set<String> getTypeHierarchy(final ComposedTypeModel typeModel)
	{
		final Set<String> hierarchy = new LinkedHashSet<>();
		ComposedTypeModel type = typeModel;
		while (type != null)
		{
			final String typeCode = type.getCode();
			hierarchy.add(typeCode);
			type = type.getSuperType();
		}
		return hierarchy;
	}

	private List<DataAttribute> convertAttributes(@NotNull final Set<String> typeCodeHierarchy,
			@NotNull final Collection<AttributeDescriptorModel> attributeDescriptors, @NotNull final Collection<String> blackList)
	{
		return attributeDescriptors.stream()//
				.filter(attr -> !blackList.contains(attr.getQualifier()))//
				.filter(attr -> !BooleanUtils.isTrue(attr.getHiddenForUI()))//
				.map(attr -> {
					try
					{
						return convertAttribute(attr);
					}
					catch (final TypeNotFoundException e)
					{
						LOG.error("Could not convert attribute " + attr, e);
						return null;
					}
				})//
				.filter(Objects::nonNull)//
				.collect(Collectors.toList());

	}

	protected Set<String> getBlacklistedAttributes(final Set<String> typeCodeHierarchy)
	{
		final String typeCode = typeCodeHierarchy.iterator().next();
		return new HashSet<>(getTypeAttributesBlackList().getOrDefault(typeCode, Collections.emptyList()));
	}

	private DataAttribute convertAttribute(final AttributeDescriptorModel attributeDescriptorModel) throws TypeNotFoundException
	{
		TypeModel attributeValueType = attributeDescriptorModel.getAttributeType();
		final boolean localized = Boolean.TRUE.equals(attributeDescriptorModel.getLocalized());
		final DataAttribute.Builder attrBuilder;
		boolean attributeTypeOfSet = false;

		if (attributeValueType instanceof MapTypeModel && localized)
		{
			attributeValueType = ((MapTypeModel) attributeValueType).getReturntype();
		}

		attrBuilder = new DataAttribute.Builder(attributeDescriptorModel.getQualifier());

		if (attributeValueType instanceof MapTypeModel)
		{
			final TypeModel argumentType = ((MapTypeModel) attributeValueType).getArgumentType();
			final TypeModel returnType = ((MapTypeModel) attributeValueType).getReturntype();

			attrBuilder.valueType(new DataAttribute.MapType(loadAndCache(argumentType.getCode(), null, true),
					loadAndCache(returnType.getCode(), null, true)));
		}
		else
		{
			if (attributeValueType instanceof CollectionTypeModel)
			{
				final TypeOfCollectionEnum typeOfCollection = ((CollectionTypeModel) attributeValueType).getTypeOfCollection();
				if (TypeOfCollectionEnum.SET.equals(typeOfCollection))
				{
					attributeTypeOfSet = true;
				}
				else if (!TypeOfCollectionEnum.COLLECTION.equals(typeOfCollection)
						&& !TypeOfCollectionEnum.LIST.equals(typeOfCollection))
				{
					throw new TypeNotFoundException(attributeValueType.getCode());
				}
			}
		}

		final DataType type = loadAndCache(attributeValueType.getCode(), null, true);
		attrBuilder.valueType(type);

		final boolean viewTypeAttribute = attributeDescriptorModel.getEnclosingType() instanceof ViewTypeModel;
		attrBuilder.mandatory(Boolean.FALSE.equals(attributeDescriptorModel.getOptional()) || viewTypeAttribute)
				.unique(Boolean.TRUE.equals(attributeDescriptorModel.getUnique()));
		attrBuilder.writable(BooleanUtils.isNotTrue(attributeDescriptorModel.getReadOnlyForUI())
				&& Boolean.TRUE.equals(attributeDescriptorModel.getWritable()));
		attrBuilder.localized(localized);
		attrBuilder.writableOnCreation(Boolean.TRUE.equals(attributeDescriptorModel.getInitial()));

		attrBuilder.searchable(isSearchable(attributeDescriptorModel)
				|| (viewTypeAttribute && BooleanUtils.isTrue(((ViewAttributeDescriptorModel) attributeDescriptorModel).getParam())));
		attrBuilder.writeThrough(isJaloProperty(attributeDescriptorModel) || isRuntimeVariantAttribute(attributeDescriptorModel));
		attrBuilder.ordered(isOrdered(attributeDescriptorModel) && (!attributeTypeOfSet));
		attrBuilder.primitive(attributeDescriptorModel.getPrimitive().booleanValue());
		attrBuilder.variantAttribute(isVariantAttribute(attributeDescriptorModel));
		final Set<Locale> supportedLocales = i18nService.getSupportedLocales();
		for (final Locale locale : supportedLocales)
		{
			attrBuilder.label(locale, attributeDescriptorModel.getName(locale));
			attrBuilder.description(locale, attributeDescriptorModel.getDescription(locale));
		}

		if (attributeDescriptorModel.getSelectionOf() != null)
		{
			attrBuilder.selectionOf(attributeDescriptorModel.getSelectionOf().getQualifier());
		}

		attrBuilder.defaultValue(attributeDescriptorModel.getDefaultValue());
		handleSpecialPlatformAttributes(attrBuilder, attributeDescriptorModel);
		return attrBuilder.build();
	}

	private void handleSpecialPlatformAttributes(final DataAttribute.Builder builder,
			final AttributeDescriptorModel attributeDescriptorModel) throws TypeNotFoundException
	{
		builder.partOf(attributeDescriptorModel.getPartOf().booleanValue());
		if (ItemModel.ITEMTYPE.equalsIgnoreCase(attributeDescriptorModel.getQualifier()))
		{
			builder.valueType(loadAndCache("java.lang.String", null, true));
			builder.writable(false);
		}

	}

	private boolean isSearchable(final TypeModel platformType)
	{
		boolean searchable = (platformType instanceof ComposedTypeModel)
				&& !BooleanUtils.toBoolean(((ComposedTypeModel) platformType).getJaloonly())
				|| (platformType instanceof ViewTypeModel);
		if (platformType instanceof RelationMetaTypeModel)
		{
			searchable = searchable && !BooleanUtils.toBoolean(((RelationMetaTypeModel) platformType).getAbstract());
		}
		return searchable;
	}

	private boolean isSearchable(final AttributeDescriptorModel attributeDescriptorModel)
	{
		final int modifier = primitiveInt(attributeDescriptorModel.getModifiers());
		final boolean fsSearchable = ((modifier & AttributeDescriptor.SEARCH_FLAG) == AttributeDescriptor.SEARCH_FLAG);
		final boolean isNotJalo = !isJaloProperty(attributeDescriptorModel);
		boolean isSupportedCollectionType = true;
		if (attributeDescriptorModel instanceof RelationDescriptorModel)
		{
			final RelationDescriptorModel rdm = (RelationDescriptorModel) attributeDescriptorModel;
			isSupportedCollectionType = BooleanUtils.toBoolean(rdm.getSearch());
		}
		else if (attributeDescriptorModel.getAttributeType() instanceof CollectionTypeModel
				|| (attributeDescriptorModel.getAttributeType() instanceof MapTypeModel
						&& !attributeDescriptorModel.getLocalized().booleanValue()))
		{
			isSupportedCollectionType = false;
		}
		return fsSearchable && isNotJalo && isSupportedCollectionType;
	}

	protected boolean isOrdered(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return isOrderedRelation(attributeDescriptorModel) || isOrderedCollection(attributeDescriptorModel.getAttributeType());
	}

	private boolean isOrderedRelation(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return attributeDescriptorModel instanceof RelationDescriptorModel
				&& BooleanUtils.isTrue(((RelationDescriptorModel) attributeDescriptorModel).getOrdered());
	}

	private boolean isOrderedCollection(final TypeModel attributeType)
	{
		return attributeType instanceof CollectionTypeModel
				&& TypeOfCollectionEnum.LIST.equals(((CollectionTypeModel) attributeType).getTypeOfCollection());
	}

	private int primitiveInt(final Integer value)
	{
		return value == null ? 0 : value.intValue();
	}

	private boolean isJaloProperty(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return !(attributeDescriptorModel instanceof RelationDescriptorModel)
				&& !BooleanUtils.toBoolean(attributeDescriptorModel.getProperty())
				&& (attributeDescriptorModel.getDatabaseColumn() == null || attributeDescriptorModel.getPersistenceClass() == null);
	}

	private boolean isVariantAttribute(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return attributeDescriptorModel instanceof VariantAttributeDescriptorModel;
	}

	private boolean isRuntimeVariantAttribute(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return isVariantAttribute(attributeDescriptorModel) && attributeDescriptorModel.getDatabaseColumn() == null;
	}

	@Override
	public boolean canHandle(final String typeCode)
	{
		if (typeCode == null)
		{
			return false;
		}
		Lock lock = null;
		try
		{
			lock = cacheLock.readLock();
			lock.lock();
			return platformFacadeStrategyHandleCache.canHandle(typeCode) || isViewResult(typeCode);
		}
		finally
		{
			if (lock != null)
			{
				lock.unlock();
			}
		}
	}

	private boolean isViewResult(final String typeCode)
	{
		final ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader(getClass());
		try
		{
			return ViewResultItem.class.isAssignableFrom(classLoader.loadClass(typeCode));
		}
		catch (final ClassNotFoundException e)
		{
			LOG.debug(String.format("Type code '%s' is not a class name", typeCode), e);
		}
		return false;
	}

	@Override
	public String getType(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		if (object instanceof ItemModel)
		{
			return getModelService().getModelType(object);
		}
		else if (object instanceof HybrisEnumValue)
		{
			return ((HybrisEnumValue) object).getType();
		}
		else if (object instanceof ViewResultItem)
		{
			return ((ViewResultItem) object).getComposedType().getCode();
		}
		return object.getClass().getName();
	}

	@Override
	public DataAttribute getAttribute(final Object object, final String attributeQualifier)
	{
		DataAttribute result = null;
		final String type = getType(object);
		try
		{
			final DataType dataType = load(type);
			result = dataType.getAttribute(attributeQualifier);
		}
		catch (final TypeNotFoundException ex)
		{
			LOG.error(ex.getMessage(), ex);
		}

		if (result == null)
		{
			result = evaluateAttributeExpression(object, attributeQualifier);
		}

		return result;
	}

	private DataAttribute evaluateAttributeExpression(final Object object, final String expression)
	{
		DataAttribute result = null;
		final AttributeExpressionResolver resolver = getResolverFactory().createResolver();
		try
		{
			final Object value = resolver.getValue(object, expression);
			if (resolver.getItem() != null && ObjectUtils.equals(value, resolver.getValue()))
			{
				final String type = getType(resolver.getItem());
				final DataType dataType = load(type);
				result = dataType.getAttribute(resolver.getAttribute());
			}
		}
		catch (final TypeNotFoundException | SpelEvaluationException ex)
		{
			LOG.warn("Evaluation failed", ex);
		}
		return result;
	}

	@Override
	public String getAttributeDescription(final String type, final String qualifier)
	{
		return getAttributeDescription(type, qualifier, null);
	}

	@Override
	public String getAttributeDescription(final String type, final String qualifier, final Locale locale)
	{
		final AttributeDescriptorModel attributeDescriptor;
		try
		{
			attributeDescriptor = typeService.getAttributeDescriptor(type, qualifier);
		}
		catch (final UnknownIdentifierException ex)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.warn(String.format("Attribute [%s] not found on type [%s]", qualifier, type), ex);
			}
			else
			{
				LOG.warn(String.format("Attribute [%s] not found on type [%s]", qualifier, type));
			}
			return null;
		}

		if (locale == null)
		{
			return attributeDescriptor.getDescription();
		}
		return attributeDescriptor.getDescription(locale);
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

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

	@Required
	public void setPlatformFacadeStrategyHandleCache(final PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache)
	{
		this.platformFacadeStrategyHandleCache = platformFacadeStrategyHandleCache;
	}

	@Override
	public void reset()
	{
		Lock lock = null;
		try
		{
			lock = cacheLock.writeLock();
			lock.lock();
			if (typeCache.isEmpty())
			{
				LOG.debug("Resetting platform types cache [empty cache]");
			}
			else
			{
				LOG.info("Resetting platform types cache");
			}
			lightTypeCache.clear();
			typeCache.clear();
			platformFacadeStrategyHandleCache.invalidate();
			attributeCache.clear();
		}
		finally
		{
			if (lock != null)
			{
				lock.unlock();
			}
		}
	}

	/**
	 * @return a collection ('black list') of type names that are NOT going to be cached since are considered to be dynamic
	 *         (may change in the runtime)
	 */
	public List<String> getDynamicTypesBlacklist()
	{
		return dynamicTypesBlacklist;
	}

	public void setDynamicTypesBlacklist(final List<String> dynamicTypesBlacklist)
	{
		this.dynamicTypesBlacklist = dynamicTypesBlacklist;
	}

	public Map<String, Collection<String>> getTypeAttributesBlackList()
	{
		if (typeAttributesBlackList == null)
		{
			typeAttributesBlackList = Collections.emptyMap();
		}
		return typeAttributesBlackList;
	}

	public void setTypeAttributesBlackList(final Map<String, Collection<String>> typeAttributesBlackList)
	{
		this.typeAttributesBlackList = typeAttributesBlackList;
	}


	protected AttributeExpressionResolverFactory getResolverFactory()
	{
		return resolverFactory;
	}

	@Required
	public void setResolverFactory(final AttributeExpressionResolverFactory resolverFactory)
	{
		this.resolverFactory = resolverFactory;
	}

	public VariantsService getVariantsService()
	{
		return variantsService;
	}

	@Required
	public void setVariantsService(final VariantsService variantsService)
	{
		this.variantsService = variantsService;
	}
}

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
package de.hybris.platform.couponservices.interceptor;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_PREFIX_REGEX_DEFAULT_VALUE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_GENERATION_PREFIX_REGEX_PROPERTY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Equivalence;


/**
 * validation interceptor for Multi-code coupon
 */
public class MultiCodeCouponValidateInterceptor implements ValidateInterceptor<MultiCodeCouponModel>, InitializingBean
{

	private ConfigurationService configurationService;
	private Pattern couponIdPattern;

	@Override
	public void onValidate(@Nonnull final MultiCodeCouponModel coupon, final InterceptorContext ctx) throws InterceptorException
	{
		checkArgument(nonNull(coupon), "Coupon model cannot be NULL here");
		checkArgument(nonNull(coupon.getCouponId()), "CouponId must be specified");

		final String couponId = coupon.getCouponId();
		final Matcher matcher = couponIdPattern.matcher(couponId);
		if (!matcher.matches())
		{
			throw new CouponInterceptorException("CouponId pattern should satisfy pattern convention: " + couponIdPattern.pattern());
		}

		if (!ctx.isNew(coupon))
		{
			if (checkActiveCoupon(coupon, ctx))
			{
				if (ctx.isModified(coupon, MultiCodeCouponModel.COUPONID))
				{
					throw new CouponInterceptorException(
							"CouponId cannot be modified if coupon is active or at least one batch of codes has been generated");
				}
				if (isCodeGenerationConfigurationChanged(coupon, ctx))
				{
					throw new CouponInterceptorException(
							"Code Generation Configuration cannot be modified if multi-code coupon is active or at least one batch of codes has been generated");
				}
			}
			if (ctx.isModified(coupon, MultiCodeCouponModel.GENERATEDCODES))
			{
				checkRemovalOfGeneratedCodes(coupon, ctx);
			}
			if (ctx.isModified(coupon, MultiCodeCouponModel.COUPONCODENUMBER))
			{
				checkSeedNumberIsNotDecremented(coupon, ctx);
			}
		}
	}

	/**
	 * @throws CouponInterceptorException
	 *            throws exception if trying to remove generated codes from multi-code coupon
	 */
	protected void checkRemovalOfGeneratedCodes(final MultiCodeCouponModel coupon, final InterceptorContext ctx)
			throws CouponInterceptorException
	{
		final Collection<MediaModel> generatedCodes = coupon.getGeneratedCodes();
		final Collection<MediaModel> originalGeneratedCodes = getOriginal(coupon, ctx, MultiCodeCouponModel.GENERATEDCODES);
		if (isNotEmpty(originalGeneratedCodes))
		{
			final Equivalence<MediaModel> eq = new MediaModelEquivalence();
			final Set<Equivalence.Wrapper<MediaModel>> originalGeneratedCodesSet = getEquivalenceWrappedSet(originalGeneratedCodes,
					eq);
			final Set<Equivalence.Wrapper<MediaModel>> generatedCodesSet = getEquivalenceWrappedSet(generatedCodes, eq);
			if (!generatedCodesSet.containsAll(originalGeneratedCodesSet))
			{
				throw new CouponInterceptorException("Generated codes cannot be removed from multi-code coupon");
			}
		}
	}

	protected void checkSeedNumberIsNotDecremented(final MultiCodeCouponModel coupon, final InterceptorContext ctx)
			throws CouponInterceptorException
	{
		final Long couponCodeNumber = getOriginal(coupon, ctx, MultiCodeCouponModel.COUPONCODENUMBER);

		if (nonNull(couponCodeNumber)
				&& (isNull(coupon.getCouponCodeNumber()) || coupon.getCouponCodeNumber().longValue() < couponCodeNumber.longValue()))
		{
			throw new CouponInterceptorException("Coupon code seed number cannot be decremented");
		}
	}

	@Override
	public void afterPropertiesSet()
	{
		final String couponIdRegexp = getConfigurationService().getConfiguration()
				.getString(COUPON_CODE_GENERATION_PREFIX_REGEX_PROPERTY, COUPON_CODE_GENERATION_PREFIX_REGEX_DEFAULT_VALUE);
		couponIdPattern = compile(couponIdRegexp);
	}

	protected Set<Equivalence.Wrapper<MediaModel>> getEquivalenceWrappedSet(final Collection<MediaModel> seedCollection,
			final Equivalence<MediaModel> eq)
	{
		final Set<Equivalence.Wrapper<MediaModel>> equivalenceWrappedSet = new HashSet<>();

		if (isNotEmpty(seedCollection))
		{
			seedCollection.forEach(s -> equivalenceWrappedSet.add(eq.wrap(s)));
		}

		return equivalenceWrappedSet;
	}

	protected boolean checkActiveCoupon(final MultiCodeCouponModel coupon, final InterceptorContext ctx)
	{
		return CollectionUtils.isEmpty(coupon.getGeneratedCodes())
				? isTrue(coupon.getActive()) && isTrue(getOriginal(coupon, ctx, MultiCodeCouponModel.ACTIVE)) : true;
	}

	protected boolean isCodeGenerationConfigurationChanged(final MultiCodeCouponModel model, final InterceptorContext ctx)
	{
		final CodeGenerationConfigurationModel originalConfiguration = getOriginal(model, ctx, MultiCodeCouponModel.CODEGENERATIONCONFIGURATION);
		return !model.getCodeGenerationConfiguration().getName()
				.equals(originalConfiguration.getName());
	}

	protected <T extends Object> T getOriginal(final MultiCodeCouponModel coupon, final InterceptorContext ctx,
			final String attributeQualifier)
	{
		if (ctx.isModified(coupon, attributeQualifier))
		{
			final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(coupon);
			return modelContext.getOriginalValue(attributeQualifier);
		}
		final ModelService modelService = requireNonNull(ctx.getModelService());
		return modelService.getAttributeValue(coupon, attributeQualifier);
	}

	private static class MediaModelEquivalence extends Equivalence<MediaModel>
	{

		@Override
		protected boolean doEquivalent(final MediaModel mediaModel1, final MediaModel mediaModel2) // NOSONAR
		{
			return nonNull(mediaModel1) && nonNull(mediaModel2) && mediaModel1.getCode().equals(mediaModel2.getCode());
		}

		@Override
		protected int doHash(final MediaModel mediaModel)
		{
			return mediaModel.getCode().hashCode();
		}

	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}


}

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
package de.hybris.platform.couponwebservices.config;

import de.hybris.platform.couponwebservices.constants.CouponwebservicesConstants;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ClientCredentialsGrant;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2
@Component
public class SwaggerConfig
{

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Bean
	public Docket apiDocumentation()
	{
		return new Docket(DocumentationType.SWAGGER_2)//
				.apiInfo(apiInfo())//
				.select()//
				.paths(PathSelectors.any())//
				.build()//
				.securitySchemes(Arrays.asList(clientCredentialFlow(), passwordFlow()))//
				.securityContexts(Arrays.asList(oauthSecurityContext()));//
	}

	protected ApiInfo apiInfo()
	{
		return new ApiInfoBuilder()//
				.title(getPropertyValue(CouponwebservicesConstants.DOCUMENTATION_TITLE_PROPERTY))//
				.description(getPropertyValue(CouponwebservicesConstants.DOCUMENTATION_DESC_PROPERTY))//
				.termsOfServiceUrl(getPropertyValue(CouponwebservicesConstants.TERMS_OF_SERVICE_URL_PROPERTY))//
				.license(getPropertyValue(CouponwebservicesConstants.LICENSE_PROPERTY))//
				.licenseUrl(getPropertyValue(CouponwebservicesConstants.LICENSE_URL_PROPERTY))//
				.version(CouponwebservicesConstants.API_VERSION)//
				.build();
	}

	protected OAuth passwordFlow()
	{
		final AuthorizationScope authorizationScope = new AuthorizationScope(
				getPropertyValue(CouponwebservicesConstants.AUTHORIZATION_SCOPE_PROPERTY), StringUtils.EMPTY);
		final ResourceOwnerPasswordCredentialsGrant resourceOwnerPasswordCredentialsGrant = new ResourceOwnerPasswordCredentialsGrant(
				CouponwebservicesConstants.AUTHORIZATION_URL);
		return new OAuth(CouponwebservicesConstants.PASSWORD_AUTHORIZATION_NAME, Arrays.asList(authorizationScope),
				Arrays.asList(resourceOwnerPasswordCredentialsGrant));
	}

	protected OAuth clientCredentialFlow()
	{
		final AuthorizationScope authorizationScope = new AuthorizationScope(
				getPropertyValue(CouponwebservicesConstants.AUTHORIZATION_SCOPE_PROPERTY), StringUtils.EMPTY);
		final ClientCredentialsGrant clientCredentialsGrant = new ClientCredentialsGrant(
				CouponwebservicesConstants.AUTHORIZATION_URL);
		return new OAuth(CouponwebservicesConstants.CLIENT_CREDENTIAL_AUTHORIZATION_NAME, Arrays.asList(authorizationScope),
				Arrays.asList(clientCredentialsGrant));
	}

	protected String getPropertyValue(final String propertyName)
	{
		return configurationService.getConfiguration().getString(propertyName);
	}

	protected SecurityContext oauthSecurityContext()
	{
		return SecurityContext.builder().securityReferences(oauthSecurityReferences()).forPaths(PathSelectors.any()).build();
	}

	protected List<SecurityReference> oauthSecurityReferences()
	{
		final AuthorizationScope[] authorizationScopes = {};
		return Arrays.asList(new SecurityReference(CouponwebservicesConstants.PASSWORD_AUTHORIZATION_NAME, authorizationScopes),
				new SecurityReference(CouponwebservicesConstants.CLIENT_CREDENTIAL_AUTHORIZATION_NAME, authorizationScopes));
	}

}

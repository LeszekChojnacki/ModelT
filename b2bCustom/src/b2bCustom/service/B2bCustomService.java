/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package b2bCustom.service;

public interface B2bCustomService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}

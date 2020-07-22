/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package b2bCustom.setup;

import static b2bCustom.constants.B2bCustomConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import b2bCustom.constants.B2bCustomConstants;
import b2bCustom.service.B2bCustomService;


@SystemSetup(extension = B2bCustomConstants.EXTENSIONNAME)
public class B2bCustomSystemSetup
{
	private final B2bCustomService b2bCustomService;

	public B2bCustomSystemSetup(final B2bCustomService b2bCustomService)
	{
		this.b2bCustomService = b2bCustomService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		b2bCustomService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return B2bCustomSystemSetup.class.getResourceAsStream("/b2bCustom/sap-hybris-platform.png");
	}
}

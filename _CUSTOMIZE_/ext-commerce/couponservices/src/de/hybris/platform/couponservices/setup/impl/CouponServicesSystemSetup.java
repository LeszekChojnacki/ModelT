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
package de.hybris.platform.couponservices.setup.impl;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.couponservices.constants.CouponServicesConstants;
import de.hybris.platform.ruleengine.setup.AbstractRuleEngineSystemSetup;


@SystemSetup(extension = CouponServicesConstants.EXTENSIONNAME)
public class CouponServicesSystemSetup extends AbstractRuleEngineSystemSetup
{

	@SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.ALL)
	public void createEssentialData(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		importImpexFile("/couponservices/import/essentialdata-definitions.impex", true, false);
		importImpexFile("/couponservices/import/essentialdata-mediafolder.impex", true, false);
		importImpexFile("/couponservices/import/essentialdata-users.impex", true, false);
		importImpexFile("/couponservices/import/essentialdata-validation.impex", true, false);
	}

	@SystemSetup(type = SystemSetup.Type.PROJECT, process = SystemSetup.Process.ALL)
	public void createProjectData(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		importImpexFile("/couponservices/import/projectdata-templates.impex", true, false);
	}

}

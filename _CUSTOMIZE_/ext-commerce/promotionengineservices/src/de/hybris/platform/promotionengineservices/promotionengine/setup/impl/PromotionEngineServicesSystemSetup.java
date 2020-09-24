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
package de.hybris.platform.promotionengineservices.promotionengine.setup.impl;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.promotionengineservices.constants.PromotionEngineServicesConstants;
import de.hybris.platform.ruleengine.setup.AbstractRuleEngineSystemSetup;


@SystemSetup(extension = PromotionEngineServicesConstants.EXTENSIONNAME)
public class PromotionEngineServicesSystemSetup extends AbstractRuleEngineSystemSetup
{

	@SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.ALL)
	public void createEssentialData(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		importImpexFile("/promotionengineservices/import/essentialdata-cronjobs.impex", true, false);
		importImpexFile("/promotionengineservices/import/essentialdata-definitions.impex", true, false);
		importImpexFile("/promotionengineservices/import/essentialdata-users.impex", true, false);
		importImpexFile("/promotionengineservices/import/essentialdata-validation.impex", true, false);
	}

	@SystemSetup(type = SystemSetup.Type.PROJECT, process = SystemSetup.Process.ALL)
	public void createProjectData(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		importImpexFile("/promotionengineservices/import/projectdata-module.impex", true, false);
		importImpexFile("/promotionengineservices/import/projectdata-templates.impex", true, false);
	}

}

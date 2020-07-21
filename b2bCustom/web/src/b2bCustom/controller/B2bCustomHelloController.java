/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package b2bCustom.controller;

import static b2bCustom.constants.B2bCustomConstants.PLATFORM_LOGO_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import b2bCustom.service.B2bCustomService;


@Controller
public class B2bCustomHelloController
{
	@Autowired
	private B2bCustomService b2bCustomService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String printWelcome(final ModelMap model)
	{
		model.addAttribute("logoUrl", b2bCustomService.getHybrisLogoUrl(PLATFORM_LOGO_CODE));
		return "welcome";
	}
}

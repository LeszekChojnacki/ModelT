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
package de.hybris.platform.ruleengine.kie.api.builder.impl;

import org.appformer.maven.support.PomModel;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.KieScannerFactoryService;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.kiescanner.KieScannerEventListener;
import org.kie.api.internal.utils.KieService;
import org.kie.api.runtime.KieContainer;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;


/**
 * Implementation of {@link KieScannerFactoryService} to provide dummy implementation of {@link KieScanner}
 * that is exact replica of {@link org.drools.compiler.kie.builder.impl.KieRepositoryImpl.DummyKieScanner}, the only difference
 * being no logger invocations, that were introduced as part of code change
 * https://github.com/kiegroup/drools/commit/40f0c0c7cb546b10fc505dea23d7f18779424e3d
 * This service is registered through configuration declaration in META-INF/kie.conf, supported by
 * {@link org.kie.api.internal.utils.ServiceRegistryImpl} and {@link org.kie api.internal.utils.ServiceDiscoveryImpl}
 */
public class DummyKieScannerFactoryService implements KieScannerFactoryService, KieService
{
	@Override
	public KieScanner newKieScanner()
	{
		return new DummyKieScanner();
	}

	private static class DummyKieScanner implements InternalKieScanner
	{
		private DummyKieScanner()
		{
		}

		@Override
		public void start(long pollingInterval)
		{
			// empty
		}

		@Override
		public void stop()
		{
			// empty
		}

		@Override
		public void shutdown()
		{
			// empty
		}

		@Override
		public void scanNow()
		{
			// empty
		}

		@Override
		public void addListener(final KieScannerEventListener kieScannerEventListener)
		{
			// empty
		}

		@Override
		public void removeListener(final KieScannerEventListener kieScannerEventListener)
		{
			// empty
		}

		@Override
		public Collection<KieScannerEventListener> getListeners()
		{
			return Collections.emptyList(); //NOSONAR
		}

		@Override
		public void setKieContainer(final KieContainer kieContainer)
		{
			// empty
		}

		@Override
		public KieModule loadArtifact(final ReleaseId releaseId)
		{
			return null;
		}

		@Override
		public KieModule loadArtifact(final ReleaseId releaseId, final InputStream pomXML)
		{
			return null;
		}

		@Override
		public KieModule loadArtifact(final ReleaseId releaseId, final PomModel pomModel)
		{
			return null;
		}

		@Override
		public String getArtifactVersion(final ReleaseId releaseId)
		{
			return null;
		}

		@Override
		public ReleaseId getScannerReleaseId()
		{
			return null;
		}

		@Override
		public ReleaseId getCurrentReleaseId()
		{
			return null;
		}

		@Override
		public Status getStatus()
		{
			return Status.STOPPED;
		}

		@Override
		public long getPollingInterval()
		{
			return 0L;
		}
	}
}

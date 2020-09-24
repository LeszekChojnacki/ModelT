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
package de.hybris.platform.ruleengine.init.impl;

import de.hybris.platform.ruleengine.init.ConcurrentMapFactory;
import de.hybris.platform.ruleengine.init.RuleEngineContainerRegistry;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default implementation of {@link RuleEngineContainerRegistry} interface, based on Drools
 */
public class DefaultRuleEngineContainerRegistry implements RuleEngineContainerRegistry<ReleaseId, KieContainer>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineContainerRegistry.class);

	private Map<ReleaseId, KieContainer> kieContainerMap;
	private ConcurrentMapFactory concurrentMapFactory;

	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	private boolean keepOnlyOneContainerVersion = true;

	/**
	 * {@inheritDoc}. <b>Note:</b> Depending on the flag set via {@link #setKeepOnlyOneContainerVersion(boolean)} (OOTB
	 * this is set via the property <code>ruleengine.containerregistry.keep.only.one.module.version</code>) this call
	 * will also remove all other KieContainer instances of the same groupId and artifactId (essentially all previously
	 * deployed versions of the same rule module).
	 */
	@Override
	public void setActiveContainer(final ReleaseId releaseId, final KieContainer rulesContainer)
	{
		kieContainerMap.put(releaseId, rulesContainer);
		if (isKeepOnlyOneContainerVersion())
		{
			removeAllPreviousVersions(releaseId);
		}
	}

	@Override
	public KieContainer getActiveContainer(final ReleaseId releaseId)
	{
		return kieContainerMap.get(releaseId);
	}

	@Override
	public Optional<ReleaseId> lookupForDeployedRelease(final String... releaseTokens)
	{
		Preconditions.checkArgument(Objects.nonNull(releaseTokens), "Lookup release tokens should be provided");
		if(releaseTokens.length == 2)
		{
			return kieContainerMap.keySet().stream()
					.filter(rid -> rid.getGroupId().equals(releaseTokens[0]) && rid.getArtifactId()
							.equals(releaseTokens[1])).findFirst();
		}
		return Optional.empty();
	}

	@Override
	public KieContainer removeActiveContainer(final ReleaseId releaseHolder)
	{
		return kieContainerMap.remove(releaseHolder);
	}

	@Override
	public void lockReadingRegistry()
	{
		readLock.lock();
	}

	@Override
	public void unlockReadingRegistry()
	{
		readLock.unlock();
	}

	@Override
	public void lockWritingRegistry()
	{
		writeLock.lock();
	}

	@Override
	public void unlockWritingRegistry()
	{
		writeLock.unlock();
	}

	@Override
	public boolean isLockedForReading()
	{
		return readWriteLock.getReadLockCount() > 0;
	}

	@Override
	public boolean isLockedForWriting()
	{
		return readWriteLock.isWriteLocked();
	}

	@PostConstruct
	public void setup()
	{
		kieContainerMap = getConcurrentMapFactory().createNew();
	}
	
	protected void removeAllPreviousVersions(final ReleaseId newReleaseId)
	{
		// remove kieContainer reference for all other versions
		final Iterator<ReleaseId> it = kieContainerMap.keySet().iterator();
		while (it.hasNext())
		{
			final ReleaseId oldReleaseId = it.next();
			if (isPreviousReleaseId(newReleaseId, oldReleaseId))
			{
				LOGGER.info("Removing old Kie module [{}] from container registry", oldReleaseId);
				it.remove();
			}
		}
	}

	protected boolean isPreviousReleaseId(final ReleaseId newReleaseId, final ReleaseId oldReleaseId)
	{
		// it is a previous release id if groupId and artifactId match but version differs
		return newReleaseId.getArtifactId().equals(oldReleaseId.getArtifactId())
				&& newReleaseId.getGroupId().equals(oldReleaseId.getGroupId())
				&& !newReleaseId.getVersion().equals(oldReleaseId.getVersion());
	}

	protected ConcurrentMapFactory getConcurrentMapFactory()
	{
		return concurrentMapFactory;
	}

	@Required
	public void setConcurrentMapFactory(final ConcurrentMapFactory concurrentMapFactory)
	{
		this.concurrentMapFactory = concurrentMapFactory;
	}

	protected ReadWriteLock getReadWriteLock()
	{
		return readWriteLock;
	}

	protected Lock getReadLock()
	{
		return readLock;
	}

	protected Lock getWriteLock()
	{
		return writeLock;
	}
	
	protected boolean isKeepOnlyOneContainerVersion()
	{
		return keepOnlyOneContainerVersion;
	}

	public void setKeepOnlyOneContainerVersion(final boolean keepOnlyOneContainerVersion)
	{
		this.keepOnlyOneContainerVersion = keepOnlyOneContainerVersion;
	}

}

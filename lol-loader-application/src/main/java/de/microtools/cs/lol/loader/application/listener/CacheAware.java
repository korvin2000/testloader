/*
 * @File: CacheAware.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 *
 *
 */
package de.microtools.cs.lol.loader.application.listener;

import org.springframework.batch.core.StepExecution;

public interface CacheAware {
   void invalidate(StepExecution stepExecution);
}

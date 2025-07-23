/*
 * @File: FTPDownloaderTask.java
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
package de.microtools.cs.lol.loader.application.tasklet;

import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.listener.CacheAware;
import de.microtools.cs.lol.loader.integration.LoginServiceWrapper;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * {@link Tasklet} executes a login in to crefo system and saves the security token in to job execution context.
 *
 * @author KostikX
 */
@Setter
public class CrefoLoginTasklet implements Tasklet, ApplicationContextAware, InitializingBean {

   private LoginServiceWrapper loginServiceWrapper;
   private ApplicationContext applicationContext;

   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      try {
            StepExecution stepExecution =
               chunkContext
               .getStepContext()
               .getStepExecution();
            doPreLogin(stepExecution);
            loginServiceWrapper.login();
         } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }

   /**
    * Executes the following pre login steps:
    * <ul>
    * <li> Notifies the invalidation of {@link CacheAware caches}</li>
    * </ul>
    * @throws Exception
    */
   private void doPreLogin(final StepExecution stepExecution) throws Exception {
      String msg;
      try {
            Map<String, CacheAware> caches = applicationContext.getBeansOfType(CacheAware.class);
            // trigger cache invalidation
            if (MapUtils.isNotEmpty(caches)) {
               IterableUtils
               .forEach(caches.values(),
                     new Closure<CacheAware>() {
                        @Override
                        public void execute(CacheAware cache) {
                           cache.invalidate(stepExecution);
                        }
                     });
            }
      } catch (Exception e) {
         msg = String.format("Failed to pre login due to %s.",
               StringUtils.join(
                     new String[]{
                        ExceptionUtils.getRootCauseMessage(e),
                        StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                     ". StackTrace: "));
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg)
         );
         throw e;
      }
   }

   @Override
   public void afterPropertiesSet() {
      Assert.notNull(loginServiceWrapper, "loginServiceWrapper must not be null.");
   }
}

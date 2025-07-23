/*
 * @File: Lol2DownloadDecider.java
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
package de.microtools.cs.lol.loader.application.decider;

import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.tasklet.CleanupTasklet;
import de.microtools.cs.lol.loader.application.tasklet.TableSwitchTasklet;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This {@link JobExecutionDecider} is responsible for deciding whether the upcoming step {@link TableSwitchTasklet#postImport switch table} shall be executed.
 * <ul>
 * <li>import mode:</li>
 * For this purpose check it whether all of incoming files have been successfully imported or not.
 * If all of incoming files have been failed to be imported (due to a technical error), so this decider will fail the job,
 * otherwise will notify the upcoming step {@link TableSwitchTasklet#postImport switch table} to switch the import tables and saves
 * the successfully imported files in a list, which will be deleted in upcoming step {@link CleanupTasklet}.
 *
 * The not imported files will be left in work folder to be imported with the next try.</br>
 * <li>not import mode (check business contacts):</li>
 * No more checks are necessary, as if the follow has reached this decider, so all thing must be ok and we can switch the tables
 * <ul>
 * @author KostikX
 */
public class TableSwitchDecider implements JobExecutionDecider {

   @Autowired
   private JobRepository jobRepository;

   @Override
   public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
      FlowExecutionStatus result = null;
      try {
         // no pre switch checks necessary by chechBusinessContact
         if (! LolUtils.isImportMode(stepExecution)) {
            // make post import switch running after this step
            BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.POST_IMPORT_SWITCH, Boolean.TRUE);
            result = FlowExecutionStatus.COMPLETED;
         } else {
            Collection<Resource> cleanupResources = getCleanupResources(stepExecution);
            if (CollectionUtils.isNotEmpty(cleanupResources)) {
               // save cleanup files for next step
               BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.CLEANUP_FILES, cleanupResources);
               // make post import switch running after this step
               BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.POST_IMPORT_SWITCH, Boolean.TRUE);
               result = FlowExecutionStatus.COMPLETED;
            } else {
               result = FlowExecutionStatus.FAILED;
            }
         }
         return result;
      } catch (Exception e) {
         // handled before
         throw e;
      } finally {
         // explicit update of step execution context to get the new step execution context infos on focus of previous step updated
         jobRepository.updateExecutionContext(stepExecution);
         // explicit update of job execution context as no job execution context update will be performed by framework while FlowExecutionStatus.FAILED
         if (FlowExecutionStatus.FAILED.equals(result)) {
            jobRepository.updateExecutionContext(jobExecution);
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected List<Resource> getCleanupResources (StepExecution stepExecution) {
      try {
         List<Resource> incomingResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES);
         if (CollectionUtils.isEmpty(incomingResources)) {
            String msg = "TableSwitchDecider: No incoming files could be resolved.";
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
                  throw new UnexpectedJobExecutionException(msg);
         }
         // are there any failed files?
         List<Resource> failedResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.FAILED_FILES);
         List<Resource> cleanupResources = incomingResources;
         if (CollectionUtils.isNotEmpty(failedResources)) {
            cleanupResources = new ArrayList<>(CollectionUtils.subtract(incomingResources, failedResources));
            // nothing left to cleanup, so we must fail the job
            if(CollectionUtils.isEmpty(cleanupResources)) {
               BatchExecutionUtils.addStepExecutionInfo(
                     stepExecution,
                     BatchExecutionInfo
                        .of(Level.ERROR, Category.TECHNICAL)
                        .message("All of incoming files have failed to be imported. Job will fail."));
                // set step/job to failed
                stepExecution.setExitStatus(ExitStatus.FAILED);
                return Collections.<Resource>emptyList();
            }
         }
         return cleanupResources;
      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
   }
}

/*
 * @File: FileArchiverTasklet.java
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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * {@link Tasklet} which deletes the successfully imported files from work folder.
 *
 * @author KostikX
 */
@Setter
public class CleanupTasklet implements Tasklet {

    private Resource workDir;

   @SuppressWarnings("unchecked")
   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      try {
         StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
         List<Resource> cleanupResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.CLEANUP_FILES);
         if (CollectionUtils.isEmpty(cleanupResources)) {
            String msg = "CleanupTasklet: No cleanup files could be resolved form job execution context.";
            BatchExecutionUtils.addStepExecutionInfo(
                  chunkContext
                     .getStepContext()
                     .getStepExecution(),
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
                  throw new UnexpectedJobExecutionException(msg);
         }
         // move successfully imported resources from local work directory to an archive folder
         LolUtils.moveFilesToArchive(stepExecution, cleanupResources, workDir.getFile().getAbsolutePath());
         // exists any failed files? log them
         List<Resource> failedResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.FAILED_FILES);
         if (CollectionUtils.isNotEmpty(failedResources)) {
           String msg =
                 String.format(
                       "Following resource(s) have been failed to be imported and will be left in work directoty: %s",
                        FluentIterable
                           .from(failedResources)
                           .transform(new Function<Resource, String>() {
                              @Override
                              public String apply(Resource file) {
                                 return file != null ? file.getFilename() : "n/a";
                              }
                           })
                           .toString()
                     );
            BatchExecutionUtils.addStepExecutionInfo(
                  chunkContext
                     .getStepContext()
                     .getStepExecution(),
                  BatchExecutionInfo
                     .of()
                     .message(msg)
            );
         }
      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }
}

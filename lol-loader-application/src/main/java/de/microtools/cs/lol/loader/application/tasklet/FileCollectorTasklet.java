/*
 * @File: FileCollectorTasklet.java
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
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolRuecklieferungErwDatum;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hsqldb.lib.FileUtil;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReaderException;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Tasklet} which executes for following tasks
 * <ul>
 * <li>Ensures that the configured work directory exists. Otherwise creates it.</li>
 * <li>Determines the processing files via pattern matching</li>
 * <li>Put the import resources and import date in to {@link ExecutionContext job execution context} while execution schema validation</li>
 * </ul>
 *
 * @author KostikX
 */
@Setter
public class FileCollectorTasklet implements Tasklet, InitializingBean {

   private static final Logger logger = LoggerFactory.getLogger(FileCollectorTasklet.class);
   private Resource workDir;
   private String fileRegEx;
   private Integer maxCollectCount;

   private static final FileSystem defaultFileSystem = FileSystems.getDefault();
   private StaxEventItemReader<LolRuecklieferungErwDatum> reportDateAndValidatorReader;

   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      try {
         StepExecution stepExecution =
               chunkContext
               .getStepContext()
               .getStepExecution();
         doPreCollect(stepExecution);
         if(doCollect(stepExecution)) {
            doPostCollect(stepExecution);
         }
      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }

   /**
    * Sends import messages for all files
    *
    * @throws Exception
    */
   private boolean doCollect(final StepExecution stepExecution) throws Exception {
      String msg;
      final String pattern = getFileRegEx(stepExecution);
      try {
         List<Resource> resources = new ArrayList<>();
         // filter the entry files which match the fileRegExp

         String outDirPath = StringUtils.appendIfMissing(workDir.getURL().getPath(), FileSystems.getDefault().getSeparator() + LolUtils.getQualifier(stepExecution));
         try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(defaultFileSystem.getPath(outDirPath),
               new DirectoryStream.Filter<Path>() {
                  @Override
                  public boolean accept(Path path) {
                     return path != null &&
                              Files.isRegularFile(path) &&
                              path.getFileName() != null &&
                              path.getFileName().toString().matches(pattern);
                  }
               })) {
            int maxCollect = getMaxCollectCount();
            int counter = 0;
            for (Path path : dirStream) {
               if (counter++ < maxCollect) {
                  resources.add(new FileSystemResource(new File(outDirPath, path.getFileName().toString())));
                  if (logger.isDebugEnabled()) {
                     logger.debug(".doCollect: file {0} will be queued for importing.", path.getFileName().toString());
                  }
               }
            }
         } catch (Exception e) {
            msg = String.format("Failed to select / evaluate files with pattern %s due to %s.",
                  pattern,
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

         // is there any file to import?
         if(! checkResourcesExistForImport(stepExecution, resources)) {
            return false;
         }

         // validate resources via xsd and set the report date (Stichtag)
         List<Resource> validResources = new ArrayList<>(resources);
         validateAndSetReportDate(stepExecution, validResources);
         // delete invalid files from work folder
         LolUtils.deleteResources(stepExecution, CollectionUtils.subtract(resources, validResources));

         // is there still any valid file to import?
         if(! checkResourcesExistForImport(stepExecution, validResources)) {
            return false;
         }

         // set import date for all importing files in to job execution context
         BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.IMPORT_DATE, new Date());
         // set list of valid incoming files in to job execution context
         BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES, validResources);
         // Setting import mode to true controls delete by filename mechanisms as pot import switching
         BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.IMPORT_MODE, Boolean.TRUE);

         msg = String.format("Determined files to import (count: %d): %s",
                  validResources.size(),
                  FluentIterable
                     .from(validResources)
                     .transform(new Function<Resource, String>() {
                        @Override
                        public String apply(Resource file) {
                           return file != null ? file.getFilename() : "n/a";
                        }
                     })
                     .toString()
               );
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message(msg)
         );
         return true;
      } catch (Exception e) {
         msg = String.format("Failed to collect files with pattern %s due to %s.",
               pattern,
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

   /**
    * Validates the resources via xsd and set the report date (Stichtag) for each file in to the job execution context.
    * @param stepExecution The StepExecution
    * @param resources The resources, to be validated. If a resource is not valid, so it will be removed from list.
    */
   private void validateAndSetReportDate(final StepExecution stepExecution, List<Resource> resources) throws Exception {
      // execute the scheme validation and read the report date from each file
      for (Iterator<Resource> it = resources.iterator(); it.hasNext();) {
         Resource resource = it.next();
         try {
            logger.info("validating {0}", resource.getFile().getName());
            reportDateAndValidatorReader.setResource(resource);
            reportDateAndValidatorReader.open(stepExecution.getExecutionContext());
            LolRuecklieferungErwDatum erwDatum = reportDateAndValidatorReader.read();
            if(erwDatum != null) {
               Date reportAsDate = erwDatum.getDatum() != null ? erwDatum.getDatum().toGregorianCalendar().getTime() : null;
               // set the report date for resource in to job execution context
               BatchExecutionUtils.putToJobExecutionContext(
                  stepExecution,
                  LolParameters.REPORT_DATE + resource.getFilename(),
                  reportAsDate);
                  if (logger.isDebugEnabled()) {
                     logger.debug(".validateAndSetReportDate: set report date {0} for file {1} in to job execution context",
                             reportAsDate, resource.getFile().getName());
                  }
            }
            logger.info("{0} validated.", resource.getFile().getName());
         } catch (XmlMappingException | DataAccessException | ItemReaderException e) {
            // resource is invalid, create a business error to trigger the sending of business error mail to lol team
            String msg = String.format("Schema validation of file %s failed or could not be parsed and will not be imported. Error: %s",
                  resource.getFilename(),
                  ExceptionUtils.getRootCauseMessage(e));
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.BUSINESS)
                     .message(msg)
                     .source(resource.getFilename())
            );
            // remove resource from import list
            it.remove();
         } finally {
            reportDateAndValidatorReader.close();
         }
      }
   }

   /**
    * Executes the following pre collect steps:
    * <ul>
    * <li>Check if {@link #workDir} exist and creates it id it dosn't exist.</li>
    * </ul>
    *
    * @throws Exception
    */
   private void doPreCollect(StepExecution stepExecution) throws Exception {

      String outDirPath = StringUtils.appendIfMissing(workDir.getURL().getPath(), FileSystems.getDefault().getSeparator() + LolUtils.getQualifier(stepExecution));

      String msg;
      final String pattern = getFileRegEx(stepExecution);
      try {
         File checkDir = new File(new UrlResource("file:" + outDirPath).getURL().getPath());
         if(! (checkDir.exists() || checkDir.mkdirs())) {
            msg = String.format("Could not create directory %s.", pattern);
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg)
            );
            throw new IllegalStateException(msg);
         }
      } catch (Exception e) {
         msg = String.format("Failed to collect files with pattern %s due to %s.",
               pattern,
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

   protected void doPostCollect(StepExecution stepExecution) throws Exception {
      // template
   }

   @Override
   public void afterPropertiesSet() throws IOException {
      Assert.notNull(fileRegEx, "fileRegEx must not be null.");
      Assert.notNull(workDir, "collectDir must not be null.");
   }

   protected boolean checkResourcesExistForImport(StepExecution stepExecution, List<Resource> resources) throws Exception {

      if (resources.isEmpty()) {
        String msg = String.format("No valid files resolved with pattern %s in directory %s.",
              getFileRegEx(stepExecution),
              workDir.getURL().toString() + FileSystems.getDefault().getSeparator() + LolUtils.getQualifier(stepExecution));
         BatchExecutionUtils.addStepExecutionInfo(stepExecution, BatchExecutionInfo.of().message(msg));

         // set the exit status to stopped if nothing collected -> job completed
         stepExecution.setExitStatus(ExitStatus.STOPPED);
         return false;
      }
      return true;
   }

   /**
    * Gets the job parameter {@link LolParameters#BATCH_FILE_REGEX} or {@link #fileRegEx} if no job parameter exists.
    * @param stepExecution The {@link StepExecution}
    * @return The regular expression to be applied while collecting files from local server
    */
   public String getFileRegEx(StepExecution stepExecution) {
      Assert.notNull(stepExecution, "stepExecution must not be null.");
      String jobParameterRegEx = BatchExecutionUtils.getJobParameter(stepExecution, LolParameters.BATCH_FILE_REGEX, true);
      return StringUtils.defaultIfBlank(jobParameterRegEx, fileRegEx);
   }

   /**
    * Gets the value of {@link #maxCollectCount}. As fallback / upper limit {@link Integer#MAX_VALUE} will be returned.
    * @return maxCollectCount or Integer#MAX_VALUE otherwise
    */
   public Integer getMaxCollectCount() {
      return ObjectUtils.defaultIfNull(maxCollectCount, Integer.MAX_VALUE);
   }

}

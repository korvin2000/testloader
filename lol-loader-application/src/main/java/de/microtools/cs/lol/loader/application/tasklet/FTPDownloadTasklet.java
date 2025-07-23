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
import de.microtools.cs.lol.loader.application.ftp.FileTransferClient;
import de.microtools.cs.lol.loader.application.ftp.FileTransferConfig;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.conf.PlaceholderProperties;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link Tasklet} which processes the ftp download
 *
 * @author KostikX
 */
@Setter
public class FTPDownloadTasklet implements Tasklet, ApplicationContextAware, InitializingBean {

   private FileTransferConfig fileTransferConfig;
   private String deliveryDirectoryPrefix;
   @Setter(AccessLevel.NONE)
   private String deliveryDirectory;
   private String fileRegEx;
   private Resource localDirectory;
   private Boolean deleteDownloaded;
   private ApplicationContext applicationContext;
   private Integer maxDownloadCount;

   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      try {
         StepExecution stepExecution =
               chunkContext
               .getStepContext()
               .getStepExecution();

         // 1. resolve schema specified ftp directoty
         deliveryDirectory = resolveDeliveryDirectory(stepExecution);

         // 2. execute download
         doDownload(stepExecution);

      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }

   /**
    * Executed the download from ftp server
    *
    * @throws Exception
    */
   protected void doDownload(StepExecution stepExecution) throws Exception {
      // FTP-Download
      // determine the delivery path on ftp server
      String ftpPath = StringUtils.appendIfMissing(deliveryDirectory, "/");
      String msg;

      // determine / create local output file
      String outDirPath = StringUtils.appendIfMissing(localDirectory.getURL().getPath(), FileSystems.getDefault().getSeparator() + LolUtils.getQualifier(stepExecution));
      File outDir = new File(outDirPath);
      if(! (outDir.exists() || outDir.mkdirs())) {
         msg = String.format("%s could not be created", outDir.getAbsolutePath());
         // add step error for tracing
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg)
         );
         throw new IllegalStateException(msg);
      }
      List<File> incomingFiles = new ArrayList<File>();
      FileTransferClient fileTransferClient = new FileTransferClient(fileTransferConfig); // Facade switch between FTP or SFTP
      String pattern = getFileRegEx(stepExecution);
      try {
         // logHostInfo
         logHostInfo(stepExecution, fileTransferConfig, ftpPath);
         // Get the file list
         List<String> files = fileTransferClient.listNames(ftpPath);
         // Nothing to if no file exist on ftp server
         if (CollectionUtils.isEmpty(files)) {
            msg = "No files available on ftp server to download.";
            // add step error for tracing
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of()
                     .message(msg)
            );
         } else {
            int maxDownload = getMaxDownloadCount();
            String filename = null;
            // iterate over incoming files
            for (int i = 0; i < files.size() && (incomingFiles.size() < maxDownload); i++) {
               try {
                  filename = files.get(i).replace(ftpPath, "");
                  // validate against regexp
                  if (filename.matches(pattern)) {
                     msg = String.format("Downloading file: %s", filename);
                     BatchExecutionUtils.addStepExecutionInfo(
                           stepExecution,
                           BatchExecutionInfo
                              .of()
                              .message(msg)
                     );
                     File outFile = new File(outDir, filename);
                     // delete the target if exists
                     if (outFile.exists()) {
                        msg = String.format("Overwriting file: %s", outFile.getAbsolutePath());
                        BatchExecutionUtils.addStepExecutionInfo(
                              stepExecution,
                              BatchExecutionInfo
                                 .of()
                                 .message(msg)
                        );
                        if(! outFile.delete()) {
                           msg = String.format("Failed to delete existing file %s. %s will not be transfered from ftp server.",
                                 outFile.getAbsolutePath(),
                                 filename);
                           BatchExecutionUtils.addStepExecutionInfo(
                                 stepExecution,
                                 BatchExecutionInfo
                                    .of(Level.ERROR, Category.TECHNICAL)
                                    .message(msg)
                           );
                           continue;
                        }
                     }
                     // don't transfer if target cannot be created
                     if(! outFile.createNewFile()) {
                        msg = String.format("Failed to create file %s. %s will not be transfered from ftp server.",
                              outFile.getAbsolutePath(),
                              filename);
                        BatchExecutionUtils.addStepExecutionInfo(
                              stepExecution,
                              BatchExecutionInfo
                                 .of(Level.ERROR, Category.TECHNICAL)
                                 .message(msg)
                        );
                        continue;
                     }
                     // open the output stream to write
                     try (OutputStream outputStream = Files.newOutputStream(outFile.toPath())) {
                        fileTransferClient.downloadFile(ftpPath + filename, outputStream);
                        // remove the file from ftp server if configured
                        if (BooleanUtils.isTrue(deleteDownloaded)) {
                           fileTransferClient.delete(ftpPath + filename);
                           msg = String.format("Deleted file %s on FTP Server.", ftpPath + filename);
                           BatchExecutionUtils.addStepExecutionInfo(
                                 stepExecution,
                                 BatchExecutionInfo
                                    .of()
                                    .message(msg)
                           );
                        }
                        incomingFiles.add(outFile);
                     } catch (Exception e) {
                        // go on with the next file if any exists
                        // respectively step off
                       msg = String.format("Failed to save file %s to %s due to %s, continue with next file if exists...",
                              filename,
                              outFile.getAbsolutePath(),
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
                        continue;
                     }
                  } else {
                     msg = String.format("File %s doesn't match the pattern %s and will not be downloaded.",
                           filename,
                           pattern);
                     BatchExecutionUtils.addStepExecutionInfo(
                           stepExecution,
                           BatchExecutionInfo
                              .of()
                              .message(msg)
                     );
                  }
               } catch (Exception e) {
                  // go on with the next file if any exists
                  // respectively step off
                  msg = String.format("Failed to process file %s due to %s, continue with next if exists...",
                        filename,
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
                  continue;
               }
            }
         }
      } catch (Exception e) {
         msg = String.format("Failed to execute ftp transfer due to: %s",
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
      } finally {
         if (CollectionUtils.isEmpty(incomingFiles)) {
            // set the exit status to stopped if nothing downloaded -> job completed
            stepExecution.setExitStatus(ExitStatus.STOPPED);
         } else {
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of()
                     .message(String.format("%d files have been successfully downloaded.", incomingFiles.size()))
            );
         }

         fileTransferClient.close();
      }
   }

   /**
    * Logs the connecting ftp server information
    */
   protected void logHostInfo(StepExecution stepExecution, FileTransferConfig fileTransferConfig, String ftpPath) {
      String msg = String.format("Downloading from %s.", fileTransferConfig.getHost() + ":" + fileTransferConfig.getPort() + "/" + ftpPath);
      BatchExecutionUtils.addStepExecutionInfo(
            stepExecution,
            BatchExecutionInfo
               .of()
               .message(msg)
      );
   }


   private String resolveDeliveryDirectory(StepExecution stepExecution) {
      String msg;
      try {
         String qualifier = StringUtils.lowerCase(LolUtils.getQualifier(stepExecution), Locale.GERMAN);
         PlaceholderProperties props = applicationContext.getBean("placeholderProperties", PlaceholderProperties.class);
         String deliveryDirectory = props.getString(deliveryDirectoryPrefix + qualifier);
         if (StringUtils.isBlank(deliveryDirectory)) {
            msg = String.format("Could not resolve deliveryDirectory for %s.",
                  deliveryDirectoryPrefix + qualifier);
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
            throw new UnexpectedJobExecutionException(msg);
         }
         return deliveryDirectory;
      }  catch (Exception e) {
         if (! (e instanceof UnexpectedJobExecutionException)) {
            msg = String.format("Failed to resolve deliveryDirectory due to %s.",
                  StringUtils.join(
                        new String[]{
                           ExceptionUtils.getRootCauseMessage(e),
                           StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                        ". StackTrace: "));
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg));
         }
         throw e;
      }
   }

   @Override
   public void afterPropertiesSet() {
      Assert.notNull(fileTransferConfig, "fileTransferConfig must not be null.");
      Assert.notNull(deliveryDirectoryPrefix, "deliveryDirectoryPrefix must not be null.");
      Assert.notNull(fileRegEx, "fileRegEx must not be null.");
      Assert.notNull(localDirectory, "localDirectory must not be null.");
      Assert.notNull(deleteDownloaded, "deleteDownloaded must not be null.");
      Assert.notNull(applicationContext, "applicationContext must not be null.");
   }

   /**
    * Gets the job parameter {@link LolParameters#FTP_FILE_REGEX} or {@link #fileRegEx} if no job parameter exists.
    * @param stepExecution The {@link StepExecution}
    * @return The regular expression to be applied while getting files from ftp server
    */
   public String getFileRegEx(StepExecution stepExecution) {
      Assert.notNull(stepExecution, "stepExecution must not be null.");
      String jobParameterRegEx = BatchExecutionUtils.getJobParameter(stepExecution, LolParameters.FTP_FILE_REGEX, true);
      return StringUtils.defaultIfBlank(jobParameterRegEx, fileRegEx);
   }

   /**
    * Gets the value of {@link #maxDownloadCount}. Ass fallback / upper limit {@link Integer#MAX_VALUE} will be returned.
    * @return
    */
   public Integer getMaxDownloadCount() {
      return ObjectUtils.defaultIfNull(maxDownloadCount, Integer.MAX_VALUE);
   }


}

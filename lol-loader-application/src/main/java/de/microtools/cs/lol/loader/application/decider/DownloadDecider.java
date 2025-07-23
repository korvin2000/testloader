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
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * This {@link JobExecutionDecider} is responsible for deciding whether the lol files shall be downloaded fromm ftp server.
 * The Decision will be done via evaluation of {@link JobParameter job parameter}: {@link LolParameters#DOWNLOAD download}.
 *
 * @author KostikX
 */
public class DownloadDecider implements JobExecutionDecider {

   @Override
   public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
      JobParameters jobParameters = jobExecution.getJobParameters();
      String isDownload = jobParameters.getString(LolParameters.DOWNLOAD, "true");
      if (StringUtils.equalsIgnoreCase(isDownload, "true")) {
         return FlowExecutionStatus.COMPLETED;
      } else {
         return FlowExecutionStatus.FAILED;
      }
   }
}

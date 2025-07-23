/*
 * @File: BatchMailBuilder.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: #1 $Date: $
 *
 *
 */
package de.microtools.cs.lol.loader.application.logger;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.api.util.ConvertUtils;
import de.microtools.n5.infrastructure.batching.application.spring.logger.BatchMailBuilder;
import de.microtools.n5.infrastructure.batching.application.spring.logger.BatchSummaryBo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.messaging.Message;

import java.util.Date;

/**
 * This class is responsible for mapping of job execution information in to a
 * {@link Message message} with attachments as job log file which can be sent as Mail.
 */
@Setter
public class LolBatchMailBuilder extends BatchMailBuilder<BatchSummaryBo> {

   private static final Logger logger = LoggerFactory.getLogger(LolBatchMailBuilder.class);

   @Override
   public String getBatchSummarySql(JobExecution jobExecution) {
      // no load summary for lolCheckBusinessContact
      if (! LolUtils.isImportMode(jobExecution)) {
         if (logger.isDebugEnabled()) {
            logger.debug(".getLoadSummaries: no import mode.");
         }
         return null;
      }
      else if (StringUtils.isNotBlank(batchSummarySql)) {
         Date importDate = (Date) BatchExecutionUtils.getFromJobExecutionContext(jobExecution, LolParameters.IMPORT_DATE);
         if (importDate != null) {
            String importDateFormatted = ConvertUtils.DATE_TIME_FORMAT_SHORT.format(importDate);
            String summarySql = String.format(batchSummarySql, "TO_TIMESTAMP('"+ importDateFormatted + "','YYYY-MM-DD HH24:MI:SS.FF')");
            logger.debug(".getBatchSummarySql: summary sql is {0} ", summarySql);
            return summarySql;
         }
      }
      return super.getBatchSummarySql(jobExecution);
   }

}

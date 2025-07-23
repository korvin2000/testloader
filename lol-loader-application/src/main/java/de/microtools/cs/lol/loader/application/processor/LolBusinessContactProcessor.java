/*
 * @File: LolBusinessContactProcessor.java
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
package de.microtools.cs.lol.loader.application.processor;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.integration.BusinessContactServiceWrapper;
import lombok.Setter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicInteger;
@Setter
public class LolBusinessContactProcessor implements ItemProcessor<LolImportData, LolImportData>, InitializingBean  {

   private static final Logger logger = LoggerFactory.getLogger(LolBusinessContactProcessor.class);
   private StepExecution stepExecution;
   private BusinessContactServiceWrapper businessContactServiceWrapper;
   private AtomicInteger counter = new AtomicInteger(0);

   @Override
   public LolImportData process(LolImportData importData) throws Exception {
      try {
         return businessContactServiceWrapper.checkedBusinessContact(importData) ? importData : null;
      } finally {
         int currentCount = counter.addAndGet(1);
         if (currentCount % 10000 == 0) {
            logger.info(".process: {0} lol entries processed.", String.valueOf(currentCount));
         }
      }
   }

   @Override
   public void afterPropertiesSet() throws Exception {
      Assert.notNull(stepExecution, "stepExecution must not be null.");
      Assert.notNull(stepExecution.getJobExecution(), "JobExecution must not be null.");
      Assert.notNull(stepExecution.getJobExecution().getExecutionContext(), "JobExecutionContext must not be null.");
      Assert.notNull(businessContactServiceWrapper, "businessContactService must not be null.");
   }

}

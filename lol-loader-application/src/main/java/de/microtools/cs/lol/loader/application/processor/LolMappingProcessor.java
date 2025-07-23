/*
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
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.BetragType;
import de.microtools.cs.lol.loader.application.domain.LolDebitorTypeErw;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.integration.MappingServiceWrapper;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
@Setter
public class LolMappingProcessor implements ItemProcessor<LolDebitorTypeErw, LolImportData>, InitializingBean  {

   private static final Logger logger = LoggerFactory.getLogger(LolMappingProcessor.class);
   private StepExecution stepExecution;
   private MappingServiceWrapper mappingServiceWrapper;
   private AtomicInteger counter = new AtomicInteger(0);

   @Override
   public LolImportData process(LolDebitorTypeErw lolDebitor) throws Exception {
      try {
         // 1. map common data
         LolImportData data = map(lolDebitor);
         // 2. set client mapping id
         setClientMapping(data);
         return data;
      } finally {
         int currentCount = counter.addAndGet(1);
         if (currentCount % 10000 == 0) {
            logger.info(".process: {0} lol entries processed.", String.valueOf(currentCount));
         }
      }
   }

   private LolImportData map(LolDebitorTypeErw lolDebitor) {
      LolImportData importData = new LolImportData(getImportDate());
      BeanUtils.copyProperties(lolDebitor, importData);
      // set bobiknummer
      importData.setBobiknummer(lolDebitor.getBobiknummer() != null ? lolDebitor.getBobiknummer() : null);
      // set name1-name3
      importData.setName1(StringUtils.trimToEmpty(lolDebitor.getName() != null ? lolDebitor.getName().getName1() : null));
      importData.setName2(StringUtils.trimToEmpty(lolDebitor.getName() != null ? lolDebitor.getName().getName2() : null));
      importData.setName3(StringUtils.trimToEmpty(lolDebitor.getName() != null ? lolDebitor.getName().getName3() : null));
      // set Betrag*
      importData.setBetragBranche(getBetragValue(lolDebitor.getBetragBranche()));
      importData.setBetragBrancheW(getBetragWaehrung(lolDebitor.getBetragBranche()));
      importData.setBetragLieferant2(getBetragValue(lolDebitor.getBetragLieferant2()));
      importData.setBetragLieferant2W(getBetragWaehrung(lolDebitor.getBetragLieferant2()));
      importData.setBetragLieferant4(getBetragValue(lolDebitor.getBetragLieferant4()));
      importData.setBetragLieferant4W(getBetragWaehrung(lolDebitor.getBetragLieferant4()));
      importData.setBetragLieferant8(getBetragValue(lolDebitor.getBetragLieferant8()));
      importData.setBetragLieferant8W(getBetragWaehrung(lolDebitor.getBetragLieferant8()));
      importData.setBetragLieferant12(getBetragValue(lolDebitor.getBetragLieferant12()));
      importData.setBetragLieferant12W(getBetragWaehrung(lolDebitor.getBetragLieferant12()));
      importData.setBetragPoolOhneLieferant2(getBetragValue(lolDebitor.getBetragPoolOhneLieferant2()));
      importData.setBetragPoolOhneLieferant2W(getBetragWaehrung(lolDebitor.getBetragPoolOhneLieferant2()));
      importData.setBetragPoolOhneLieferant4(getBetragValue(lolDebitor.getBetragPoolOhneLieferant4()));
      importData.setBetragPoolOhneLieferant4W(getBetragWaehrung(lolDebitor.getBetragPoolOhneLieferant4()));
      importData.setBetragPoolOhneLieferant8(getBetragValue(lolDebitor.getBetragPoolOhneLieferant8()));
      importData.setBetragPoolOhneLieferant8W(getBetragWaehrung(lolDebitor.getBetragPoolOhneLieferant8()));
      importData.setBetragPoolOhneLieferant12(getBetragValue(lolDebitor.getBetragPoolOhneLieferant12()));
      importData.setBetragPoolOhneLieferant12W(getBetragWaehrung(lolDebitor.getBetragPoolOhneLieferant12()));
      importData.setBetragsvolumenOp(getBetragValue(lolDebitor.getBetragsvolumenOP()));
      importData.setBetragsvolumenOpW(getBetragWaehrung(lolDebitor.getBetragsvolumenOP()));
      // set Resource and Filename
      importData.setResource(lolDebitor.getResource());
      importData.setFilename(lolDebitor.getResource().getFilename());
      // set Stichtag
      importData.setStichtag(getReportDate(lolDebitor));
      return importData;
   }

   private static BigDecimal getBetragValue(BetragType betragType) {
      return betragType != null ? betragType.getValue() : null;
   }

   private static String getBetragWaehrung(BetragType betragType) {
      return betragType != null && betragType.getWaehrung() != null ? betragType.getWaehrung().value() : null;
   }

   private Date getImportDate() {
      ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
      return (Date) jobExecutionContext.get(LolParameters.IMPORT_DATE);
   }

   private Date getReportDate(LolDebitorTypeErw lolDebitor) {
      return (Date) (lolDebitor != null && lolDebitor.getResource() != null ?
               BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.REPORT_DATE+lolDebitor.getResource().getFilename())
               : null);
   }

   @Override
   public void afterPropertiesSet() throws Exception {
      Assert.notNull(stepExecution, "stepExecution must not be null.");
      Assert.notNull(stepExecution.getJobExecution(), "JobExecution must not be null.");
      Assert.notNull(stepExecution.getJobExecution().getExecutionContext(), "JobExecutionContext must not be null.");
      Assert.notNull(mappingServiceWrapper, "mappingServiceWrapper must not be null.");
   }

   protected void setClientMapping(LolImportData data) {
     try {
        // set client mapping id
        data.setMappingId(mappingServiceWrapper.getMappingId(data));
     } catch (Exception e) {
        // only log or not import?
        String msg = String.format("Failed to get client mapping id due to  to %s.",
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
     }
   }
}

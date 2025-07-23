/*
 * @File: LolImportDataValidator.java
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
package de.microtools.cs.lol.loader.application.validator;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for technical aspects
 *
 * @author KostikX
 *
 */
@Setter
public class LolImportDataValidator extends SpringValidator<LolImportData> {

   private static final Logger logger = LoggerFactory.getLogger(LolImportDataValidator.class);
   private StepExecution stepExecution;

   @Override
   public void validate(LolImportData item) throws ValidationException {
      try {
         super.validate(item);
      } catch (ValidationException e) {
         if (logger.isDebugEnabled()) {
            logger.error(e.getLocalizedMessage());
         }
         addFailedResource(item, e);
         throw e;
      }
   }

   @Override
   public void afterPropertiesSet() throws Exception {
      super.afterPropertiesSet();
      Assert.notNull(stepExecution, "stepExecution must not be null.");
   }

   @SuppressWarnings("unchecked")
   private void addFailedResource(LolImportData item, ValidationException e) {

      List<Resource> failedResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.FAILED_FILES);
      if(failedResources == null) {
         failedResources = new ArrayList<>();
         BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.FAILED_FILES, failedResources);
      }
      Resource resource = item.getResource();
      if (! failedResources.contains(resource)) {
         failedResources.add(resource);
         String msg = String.format("Resource %s will be left in work directory due to antecedent technical validation erros (sample: %s).",
               resource.getFilename(),
               e.getLocalizedMessage());
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.INFO, Category.TECHNICAL)
                  .message(msg));
      }
   }
}

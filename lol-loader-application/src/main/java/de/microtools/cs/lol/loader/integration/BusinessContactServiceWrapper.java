/*
 * @File: BusinessContactServiceWrapper.java
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
package de.microtools.cs.lol.loader.integration;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.listener.CacheAware;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.cs.lol.mapping.model.CsClient;
import de.microtools.n5.core.businesscontact.domain.BusinessContact;
import de.microtools.n5.core.party.api.BusinessContactAPI;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("businessContactService")
@Setter
public class BusinessContactServiceWrapper implements InitializingBean, CacheAware {

   private static final Logger logger = LoggerFactory.getLogger(BusinessContactServiceWrapper.class);
   private BusinessContactAPI businessContactService;
   private StepExecution stepExecution;
   @Autowired
   private MappingServiceWrapper mappingServiceWrapper;
   @Autowired
   private LoginServiceWrapper loginServiceWrapper;
   private static final Cache<String, Boolean> crefoBusinessContactCache =
         CacheBuilder
            .newBuilder()
            .maximumSize(1000000)
            .build();

   private final static String cacheKeyDelimiter = "#";
   private final static CopyOnWriteArrayList<Long> loggedFailingClients = new CopyOnWriteArrayList<>();
   private final static CopyOnWriteArrayList<Long> loggedFailingMappings = new CopyOnWriteArrayList<>();

   /**
    * Calls the business contact service to get or create businessContact
    * @param data a {@link LolImportData lol data entry}
    * @return {@code true} if a business contact for given {@link LolImportData} exists or could be created, otherwise {@code false}
    */
   public boolean checkedBusinessContact(final LolImportData data) {
      final String[] finalCrefoNr = new String[1];
      final Long[] finalClientId = new Long[1];
      final String[] finalCacheKey = new String[1];
      final String[] finalClientLoginAsToken = new String[1];
      String clientloginAs = null;

      try {
         if (data == null || data.getMappingId() == null || StringUtils.isBlank(finalCrefoNr[0] = data.getBobiknummer())) {
            String msg = String.format("import data or mappingId or crefoNr for %s is null. No business contact will be checked.", data);
            throw new UnexpectedJobExecutionException(msg);
         }
         CsClient csClient = mappingServiceWrapper.getCsClient(data.getMappingId());
         if (csClient == null
               || csClient.getClientId() == null
               || (finalClientId[0] = csClient.getClientId()).equals(MappingServiceWrapper.notMappedCsClient.getClientId())
               || StringUtils.isBlank(clientloginAs = csClient.getImportUserName())) {
            // csClient not mapped yet completely, nothing to do
            if (loggedFailingMappings.addIfAbsent(data.getMappingId())) {
               String msg =
                     String.format("No completely mapped client(%s) available for mappingId %d (as sample entry). No business contact for this mapping/client will be checked.",
                     csClient != null ? csClient : "n/a",
                     data.getMappingId());
               BatchExecutionUtils.addStepExecutionInfo(
                     stepExecution,
                     BatchExecutionInfo
                        .of(Level.WARN, Category.BUSINESS)
                        .message(msg));
            } else if(logger.isDebugEnabled()) {
               // duplicate code in favor of performance
               logger.warn("No completely mapped client({0}) available for mappingId {1} (as sample entry). No business contact for this mapping/client will be checked.",
                     csClient != null ? csClient : "n/a",
                     String.valueOf(data.getMappingId()));
            }
            return false;
         } else if (StringUtils.isBlank(finalClientLoginAsToken[0] = loginServiceWrapper.login(clientloginAs))) {
            // login as for client user has been failed before. error is handled before. nothing to do for all data of this client
            return false;
         }
         // build cache key as crefoNr+clientId
         finalCacheKey[0] =
               Joiner
                  .on(cacheKeyDelimiter)
                  .join(
                        finalCrefoNr[0],
                        String.valueOf(csClient.getClientId()));
         return
               crefoBusinessContactCache.get(
                     finalCacheKey[0],
                     new Callable<Boolean>() {
                           @Override
                           public Boolean call() throws Exception {
                              try {
                                 String[] keyTokens = finalCacheKey[0].split(cacheKeyDelimiter);
                                 if (keyTokens.length != 2) {
                                    String msg = String.format("incorrect cache key %s detected while checking business contact.", finalCacheKey[0]);
                                    throw new UnexpectedJobExecutionException(msg);
                                 }
                                 String crefoNr = keyTokens[0];
                                 String clientId = keyTokens[1];
                                 // set login as token
                                 businessContactService.setAccessToken(finalClientLoginAsToken[0]);
                                 boolean businessContactExists = businessContactService.isBusinessContactExists(crefoNr);
                                 if (logger.isDebugEnabled()) {
                                    logger.debug("businessContactService.isBusinessContactExists({0}) delivered: {1}", crefoNr, businessContactExists);
                                 }
                                 if (businessContactExists) {
                                    incrementExistingBusinessContacts(stepExecution.getJobExecution());
                                 } else {
                                    BusinessContact businessContact = LolUtils.toBusinessContact(data);
                                    boolean businessContactSaved = businessContactService.saveBusinessContact(businessContact);
                                    if (logger.isDebugEnabled()) {
                                       logger.debug("businessContactService.saveBusinessContact({0}) delivered: {1}", businessContact, businessContactSaved);
                                    }
                                    if (businessContactSaved) {
                                       incrementCreatedBusinessContacts(stepExecution.getJobExecution());
                                    } else {
                                       String msg = String.format("business contact creation failed for CrefoNr %s and clientId %s.", crefoNr, clientId);
                                       throw new UnexpectedJobExecutionException(msg);
                                    }
                                 }
                                 return true;
                              } catch (Exception e) {
                                 logBusinessContactError(finalCrefoNr[0], finalClientId[0], stepExecution, e, false);
                                 return false;
                              }
                           }
                        });
      } catch (Exception e) {
         logBusinessContactError(finalCrefoNr[0], finalClientId[0], stepExecution, e, !(e instanceof UnexpectedJobExecutionException));
         // set to false avoiding multiple error message for same crefonr.
         if (finalCacheKey[0] != null &&
               crefoBusinessContactCache.getIfPresent(finalCacheKey[0]) == null) {
            crefoBusinessContactCache.put(finalCacheKey[0], false);
         }
         return false;
      }
   }

   private static void logBusinessContactError(String crefoNr, Long clientId, StepExecution stepExecution, Exception e, boolean stackTrace) {
      Long errorClientId = (Long) ObjectUtils.defaultIfNull(clientId, -1L);
      String msg = "n/a";
      if (loggedFailingClients.addIfAbsent(errorClientId)) {
         msg =
               String.format("Failed to check business contact for CrefoNr %s and clientId  %d (as sample entry) due to %s",
                  StringUtils.defaultIfBlank(crefoNr, "n/a"),
                  errorClientId,
                  stackTrace ?
                  StringUtils.join(
                        new String[]{
                           ExceptionUtils.getRootCauseMessage(e),
                           StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                        ". StackTrace: ")
                  : ExceptionUtils.getRootCauseMessage(e));
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg));
         logger.error(msg);
      } else if (logger.isDebugEnabled()) {
         // duplicate code in favor of performance
         msg =
               String.format("Failed to check business contact for CrefoNr %s and clientId  %d (as sample entry) due to %s",
                  StringUtils.defaultIfBlank(crefoNr, "n/a"),
                  errorClientId,
                  stackTrace ?
                  StringUtils.join(
                        new String[]{
                           ExceptionUtils.getRootCauseMessage(e),
                           StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, LolParameters.STACKTRACE_LENGTH)},
                        ". StackTrace: ")
                  : ExceptionUtils.getRootCauseMessage(e));
         logger.error(msg);
      }
   }

   @Override
   public void afterPropertiesSet() throws Exception {
     Assert.notNull(businessContactService, "businessContactService must not be null.");
   }

   @Override
   public void invalidate(StepExecution stepExecution) {
      if (stepExecution != null) {
         // relevant for business contact check
         if (! LolUtils.isImportMode(stepExecution.getJobExecution())) {
            crefoBusinessContactCache.invalidateAll();
            loggedFailingClients.clear();
            loggedFailingMappings.clear();
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of()
                     .message("BusinessContactServiceWrapper caches successfully invalidated."));
         }
      } else {
         crefoBusinessContactCache.invalidateAll();
         loggedFailingClients.clear();
         loggedFailingMappings.clear();
         logger.warn(".invalidate: received mode independent cache invalidation of BusinessContactServiceWrapper.");
      }
   }

   @BeforeStep
   public void beforeStep(StepExecution stepExecution) {
      try {
         this.stepExecution = stepExecution;
         businessContactService.setAccessToken(LolUtils.getSecurityToken(stepExecution));
         businessContactService.setQualifier(LolUtils.getQualifier(stepExecution));
         Assert.isTrue(businessContactService.ping(), "businessContactService is not accessible.");
         // set wrapper step execution to current one
         loginServiceWrapper.setStepExecution(stepExecution);
      } catch (Exception e) {
         if (!(e instanceof UnexpectedJobExecutionException)) {
            String msg = String.format("Failed to prepare step due to %s",
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
         throw e; // fail the job
      }
   }

   @AfterStep
   public void afterStep(StepExecution stepExecution) {
      // set the check results in to job execution context
      Long existingBCs =
            BatchExecutionUtils
               .getFromJobExecutionContext(
                     stepExecution.getJobExecution(),
                     LolParameters.CHECKED_BUSINESS_CONTACTS_EXIST,
                     Long.class);
      Long createdBCs =
            BatchExecutionUtils
               .getFromJobExecutionContext(
                     stepExecution.getJobExecution(),
                     LolParameters.CHECKED_BUSINESS_CONTACTS_CREATED,
                     Long.class);
      Long notCheckedBCs =
            BatchExecutionUtils
               .getFromJobExecutionContext(
                     stepExecution.getJobExecution(),
                     LolParameters.NOT_CHECKED_BUSINESS_CONTACTS,
                     Long.class);
      BatchExecutionUtils.addStepExecutionInfo(
            stepExecution,
            BatchExecutionInfo
               .of()
               .message(
                     String.format(
                           "%d business contacts created and %d already exist (total count of not checked business contacts was: %d).",
                           createdBCs,
                           existingBCs,
                           notCheckedBCs))
      );
   }

   protected static void incrementExistingBusinessContacts(JobExecution jobExecution) {
      Long count =
            BatchExecutionUtils
               .getFromJobExecutionContext(
                     jobExecution,
                     LolParameters.CHECKED_BUSINESS_CONTACTS_EXIST,
                     Long.class,
                     true);
      BatchExecutionUtils
         .putToJobExecutionContext(
               jobExecution,
               LolParameters.CHECKED_BUSINESS_CONTACTS_EXIST,
               ++count);
   }

   protected static void incrementCreatedBusinessContacts(JobExecution jobExecution) {
      Long count =
            BatchExecutionUtils
               .getFromJobExecutionContext(
                     jobExecution,
                     LolParameters.CHECKED_BUSINESS_CONTACTS_CREATED,
                     Long.class,
                     true);
      BatchExecutionUtils
         .putToJobExecutionContext(
               jobExecution,
               LolParameters.CHECKED_BUSINESS_CONTACTS_CREATED,
               ++count);
   }
}

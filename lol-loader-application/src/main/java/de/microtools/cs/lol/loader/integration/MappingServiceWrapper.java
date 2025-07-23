/*
 * @File: MappingServiceWrapper.java
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.listener.CacheAware;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.cs.lol.mapping.api.LolMappingAPI;
import de.microtools.cs.lol.mapping.model.CompositeKey;
import de.microtools.cs.lol.mapping.model.CsClient;
import de.microtools.cs.lol.mapping.model.DoubleKeyEntry;
import de.microtools.cs.lol.mapping.model.MappingEntry;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("mappingService")
public class MappingServiceWrapper implements InitializingBean, CacheAware {

   private static final Logger logger = LoggerFactory.getLogger(MappingServiceWrapper.class);
   @Setter
   private LolMappingAPI mappingService;
   private List<String> relevantKeys;
   private StepExecution stepExecution;
   private final static CopyOnWriteArrayList<CompositeKey> loggedFailingMappingKeys = new CopyOnWriteArrayList<CompositeKey>();
   private final static CopyOnWriteArrayList<Long> loggedFailingMappingIds = new CopyOnWriteArrayList<Long>();
   static final NotMappedCsClient notMappedCsClient = new NotMappedCsClient();

   private static final Cache<CompositeKey, Long> mappingIdCache =
         CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .build();

   private static final Cache<Long, CsClient> csClientCache =
         CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .build();

   @Override
   public void afterPropertiesSet() throws Exception {
      Assert.notNull(mappingService, "mappingService must not be null.");
   }

   public Long getMappingId(LolImportData data) {
      if (relevantKeys == null) {
         // logging handled in beforeStep, import file will be failed in LolImportDataValidator
         return notMappedCsClient.getClientId();
      }
      CompositeKey key = null;
      try {
         Map<String, String> keyValues = new HashMap<>();
         for (String keyPart : relevantKeys) {
            Field field = FieldUtils.getField(LolImportData.class, keyPart, true);
            if (field == null) {
               logger.error(".getClientMap: No Field for {0} could be resolved in class {1} to get value for.", new Object[]{keyPart, LolImportData.class.getSimpleName()});
               continue;
            }
            keyValues.put(keyPart, (String) field.get(data));
         }
         key = new CompositeKey(keyValues);
         final CompositeKey[] finalKey = new CompositeKey[]{key};
         return
               mappingIdCache.get(
                     key,
                     new Callable<Long>() {
                        @Override
                        public Long call() throws Exception {
                           try {
                              final Long[] finalResult = new Long[]{null};
                              // execute the get it can deliver nothing if the mapping doesn't exist yet
                              finalResult[0] = executeGetMapping(finalKey[0], stepExecution, false);
                              // have we got any mappingId?
                              if (finalResult[0] == null) {
                                 // mapping not exists yet (RTException from mapping service as 404), try to put a new one
                                 finalResult[0] = executePutMapping(finalKey[0], stepExecution, false);
                                 // do have we put anyone?
                                 if (finalResult[0] == null) {
                                    // try at least 3 times more getting if putting has failed, due to concurrent / unique key aspects
                                    int tryCount = 0;
                                    while (finalResult[0] == null && tryCount++ < 3) {
                                       // execute the get closure and increment the counter
                                       finalResult[0] = executeGetMapping(finalKey[0], stepExecution, tryCount == 3);
                                       if (finalResult[0] == null && tryCount < 3) {
                                          try {
                                             // try again after 1 second
                                             Thread.sleep(1000);
                                          } catch (Exception ignore) {
                                             // ignore
                                          }
                                       }
                                    }
                                    if (finalResult[0] == null) {
                                       String msg = String.format("No uid received for %s from mapping service.", finalKey[0]);
                                       throw new UnexpectedJobExecutionException(msg);
                                    }
                                 }
                              }
                              return finalResult[0];
                           } catch (Exception e) {
                              logMappingError(finalKey[0], stepExecution, e, false);
                              return notMappedCsClient.getClientId();
                           }
                  }});
      } catch (Exception e) {
         logMappingError(key, stepExecution, e, true);
         // set to -1 avoiding multiple error message for same key
         if (key != null && mappingIdCache.getIfPresent(key) == null) {
            mappingIdCache.put(key, notMappedCsClient.getClientId());
         }
         return notMappedCsClient.getClientId();
      }
   }

   public CsClient getCsClient(final Long mappingId) {
      try {
         return
               csClientCache.get(
                     mappingId,
                     new Callable<CsClient>() {
                        @Override
                        public CsClient call() throws Exception {
                           try {
                              List<MappingEntry> mappingsById = mappingService.getClientMappings(Collections.singletonList(mappingId));
                              if (logger.isDebugEnabled()) {
                                 logger.debug("mappingService.getClientMappings({0}) delivered: {1}", mappingId, IterableUtils.toString(mappingsById));
                              }
                              if (CollectionUtils.isEmpty(mappingsById)) {
                                 String msg = String.format("Could not resolve any mapping entries for mapping id %d from mapping service.", mappingId);
                                 throw new UnexpectedJobExecutionException(msg);
                              } else if(mappingsById.size() > 1) {
                                 String msg = String.format("Resolved %d mapping entries keys for mapping id %d from mapping service.", mappingsById.size(), mappingId);
                                 throw new UnexpectedJobExecutionException(msg);
                              }
                              CsClient mappedClient = mappingsById.get(0).getClient();
                              if (mappedClient == null) {
                                 // dummy not mapped CsClient
                                 return notMappedCsClient;
                              } else if (mappedClient.getClientId() == null) {
                                 String msg = String.format("No Client id returned for mapping id %d and client %s from mapping service.",
                                       mappingId,
                                       StringUtils.defaultIfBlank(mappedClient.getName(), "n/a"));
                                 throw new UnexpectedJobExecutionException(msg);
                              }
                              return mappedClient;
                           } catch (Exception e) {
                              logCsClientError(mappingId, stepExecution, e, false);
                              return notMappedCsClient;
                           }
                        }
                     });
      } catch (Exception e) {
         logCsClientError(mappingId, stepExecution, e, true);
         // set to notMappedCsClient avoiding multiple error message for same mapping id
         if (csClientCache.getIfPresent(mappingId) == null) {
            csClientCache.put(mappingId, notMappedCsClient);
         }
         return notMappedCsClient;
      }
   }

   private static void logCsClientError(Long mappingId, StepExecution stepExecution, Exception e, boolean stackTrace) {
      Long errorMappingId = (Long) ObjectUtils.defaultIfNull(mappingId, -1L);
      String msg = "n/a";
      if(loggedFailingMappingIds.addIfAbsent(errorMappingId)) {
          msg =
            String.format("Could not resolve cs client id for mappingId %d (as sample entry), due to %s",
               mappingId,
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
               String.format("Could not resolve cs client id for mappingId %d (as sample entry), due to %s",
                  mappingId,
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

   private static void logMappingError(CompositeKey key, StepExecution stepExecution, Exception e, boolean stackTrace) {
      CompositeKey errorKey = (CompositeKey) ObjectUtils.defaultIfNull(key, new CompositeKey(ImmutableMap.of("n/a", "n/a")));
      String msg = "n/a";
      if(loggedFailingMappingKeys.addIfAbsent(errorKey)) {
         msg =
               String.format("Could not resolve client map for %s (as sample entry), due to %s",
               errorKey,
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
               String.format("Could not resolve client map for %s (as sample entry), due to %s",
               errorKey,
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

   /**
    * Executes a {@link LolMappingAPI#getClientMappings(int, int, String, Map)} with the given {@link CompositeKey}
    *
    * @param key The composite key
    *
    * @return The mapping id or {@code null} if no mapping returned
    * @throws UnexpectedJobExecutionException while unexpected returned values from service
    */
   private Long executeGetMapping(CompositeKey key, StepExecution stepExecution, boolean logError) {
      Assert.notNull(key);
      Assert.notNull(stepExecution);
      Assert.notNull(mappingService);

      Long result = null;
       // query mapping
      List<MappingEntry> mappings = null;
      try {
         mappings = mappingService.getClientMappings(0, 1, null, key.getKeys());
         if (logger.isDebugEnabled()) {
            logger.debug("mappingService.getClientMappings(0, 0, null, {0}) delivered: {1}", key, IterableUtils.toString(mappings));
         }
      } catch (Exception e) {
        if (logError) {
           logMappingError(key, stepExecution, e, true);
        } else{
           logger.warn("mappingService.getClientMappings(0, 0, null, {0}) failed: {1}", key, ExceptionUtils.getRootCauseMessage(e));
        }
        return null;
      }
      if (CollectionUtils.size(mappings) > 1) {
         // more than one mappings resolved, error fall
         String msg = String.format("Resolved %d mapping entries for %s from mapping service.", mappings.size(), key);
         throw new UnexpectedJobExecutionException(msg);
      } else if (CollectionUtils.size(mappings) == 1) {
         // key exists, so return it
         MappingEntry mapEntry = mappings.get(0);
         result = mapEntry.getUid();
         if (result == null) {
            String msg = String.format("Null uid in resolved mapping entry %s for %s from mapping service.", mapEntry, key);
            throw new UnexpectedJobExecutionException(msg);
         }
      }
      return result;
   }


   /**
    * Executes a {@link LolMappingAPI#putUIDs(List)} with the given {@link CompositeKey}
    *
    * @param key The composite key
    *
    * @return The mapping id or {@code null} if no mapping returned
    * @throws UnexpectedJobExecutionException while unexpected returned values from service
    */
   private Long executePutMapping(CompositeKey key, StepExecution stepExecution, boolean logError) {
      Assert.notNull(key);
      Assert.notNull(stepExecution);
      Assert.notNull(mappingService);
      Long result = null;
      List<DoubleKeyEntry> mappingDoubleKeys = null;
      try {
         mappingDoubleKeys = mappingService.putUIDs(Collections.singletonList(key));
         if (logger.isDebugEnabled()) {
            logger.debug("mappingService.putUIDs({0}) delivered: {1}", key, IterableUtils.toString(mappingDoubleKeys));
         }
      } catch (Exception e) {
        if (logError) {
           logMappingError(key, stepExecution, e, true);
        } else{
           logger.warn("mappingService.putUIDs({0}) failed: {1}", key, ExceptionUtils.getRootCauseMessage(e));
        }
        return null;
      }
      if(CollectionUtils.size(mappingDoubleKeys) > 1) {
         String msg = String.format("Resolved %d mapping double keys for %s from mapping service.", mappingDoubleKeys.size(), key);
         throw new UnexpectedJobExecutionException(msg);
      }
      else if(CollectionUtils.size(mappingDoubleKeys) == 1) {
         DoubleKeyEntry mappingKeyEntry = mappingDoubleKeys.get(0);
         result = mappingKeyEntry.getUid();
         if (result == null) {
            String msg = String.format("Null uid in resolved mapping double key %s for %s from mapping service.", mappingKeyEntry, key);
            throw new UnexpectedJobExecutionException(msg);
         }
      }
      return result;
   }

   @Override
   public void invalidate(StepExecution stepExecution) {
      if (stepExecution != null) {
         if (LolUtils.isImportMode(stepExecution.getJobExecution())) {
            // relevant for import mode
            relevantKeys = null;
            mappingIdCache.invalidateAll();
            loggedFailingMappingKeys.clear();
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of()
                     .message("MappingServiceWrapper caches successfully invalidated for import."));
         } else {
            // relevant for check business contact mode
            csClientCache.invalidateAll();
            loggedFailingMappingIds.clear();
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of()
                     .message("MappingServiceWrapper caches successfully invalidated for check business conatcts."));
         }
      } else {
         relevantKeys = null;
         mappingIdCache.invalidateAll();
         csClientCache.invalidateAll();
         loggedFailingMappingKeys.clear();
         loggedFailingMappingIds.clear();
         logger.warn(".invalidate: received mode independent cache invalidation of MappingServiceWrapper.");
      }
   }

   @BeforeStep
   public void beforeStep(StepExecution stepExecution) {
      try {
         this.stepExecution = stepExecution;
         // second setting of the same token (lol.import.user.{env}), just for safety reasons
         mappingService.setAccessToken(LolUtils.getSecurityToken(stepExecution));
         relevantKeys = mappingService.getRelevantKeys();
         if (logger.isDebugEnabled()) {
            logger.debug("mappingService.getRelevantKeys() delivered: {0}", IterableUtils.toString(relevantKeys));
         }
         Assert.notEmpty(relevantKeys, "Could not resolve any relevant keys from mapping service");
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

   /**
    * Fetches all of tenant(mandant) associated client mapping from {@link LolMappingAPI}.</br>
    * REMARK:</br>
    * Only the mappings will be given back and put to {@link #csClientCache} either, which have import user name.
    *
    * @param stepExecution The step execution (caller scope)
    * @return The list of Mapping ids
    */
   public List<Long> fetchClientMappings(final StepExecution stepExecution) {
      Assert.notNull(stepExecution);
      Assert.notNull(mappingService);
      // initial setting of token as is calls by CheckBusinessContactDeciderBean
      mappingService.setAccessToken(LolUtils.getSecurityToken(stepExecution));
      final List<Long> mappingIds = new ArrayList<>();
      try {
         List<MappingEntry> clientMappings = mappingService.getClientMappings(0, Integer.MAX_VALUE, null, null);
         if (logger.isDebugEnabled()) {
            logger.debug("mappingService.fetchClientMappings(0, Integer.MAX_VALUE, null, null) delivered: {1}", IterableUtils.toString(clientMappings));
         }
         // there exist mappings
         if (clientMappings != null) {
            IterableUtils
            .forEach(clientMappings,
               new Closure<MappingEntry>() {
                  @Override
                  public void execute(MappingEntry mappingEntry) {
                     if (mappingEntry != null && mappingEntry.getUid() != null) {
                        CsClient client = mappingEntry.getClient();
                        String msg;
                        if (client != null && client.getClientId() != null) {
                           // give back only mappings with import user name
                           if(StringUtils.isNotEmpty(client.getImportUserName())) {
                              csClientCache.put(mappingEntry.getUid(), client);
                              mappingIds.add(mappingEntry.getUid());
                           } else {
                              msg = String.format("Mapping %d has no client import user name (%s). Please check mapping service.", mappingEntry.getUid(), client);
                              BatchExecutionUtils.addStepExecutionInfo(
                                    stepExecution,
                                    BatchExecutionInfo
                                       .of(Level.WARN, Category.BUSINESS)
                                       .message(msg));
                              // not in step logger scope
                              logger.warn(msg);
                           }
                        } else {
                           msg = String.format("Mapping %d has no client mapping. Please check mapping service.", mappingEntry.getUid());
                           BatchExecutionUtils.addStepExecutionInfo(
                                 stepExecution,
                                 BatchExecutionInfo
                                    .of(Level.WARN, Category.BUSINESS)
                                    .message(msg));
                           // not in step logger scope
                           logger.warn(msg);
                        }
                     }
                  }
            });
         }
         return mappingIds;
      } catch (Exception e) {
         String msg =
               String.format("Could not fetch all client mappings form mapping service, due to %s",
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
         logger.error(msg);
         throw e; // fail the job
      }
   }

   @Getter
   static class NotMappedCsClient extends CsClient {
      private static final long serialVersionUID = -1582352069227840193L;
      private static final Long invalidCsClientId = -1L;

      public NotMappedCsClient() {
        super(invalidCsClientId, "n/a", null);
      }
   }
}

/*
 * @File: LolUtil.java
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
package de.microtools.cs.lol.loader.application.util;

import com.google.common.collect.Lists;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.n5.core.businesscontact.domain.Address;
import de.microtools.n5.core.businesscontact.domain.BusinessContact;
import de.microtools.n5.core.businesscontact.domain.Identifier;
import de.microtools.n5.core.businesscontact.domain.Identifier.IdentifierClass;
import de.microtools.n5.core.businesscontact.domain.Name;
import de.microtools.n5.core.businesscontact.domain.NamePart.FieldType;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Builder;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.functors.NotNullPredicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LolUtils {

   private static final Logger logger = LoggerFactory.getLogger(LolUtils.class);

   public static void moveFilesToArchive(final StepExecution stepExecution, Collection<Resource> resources, String workDir) {

      if (CollectionUtils.isEmpty(resources)) return;

      String archiveFolderPath = StringUtils.appendIfMissing(workDir, FileSystems.getDefault().getSeparator() + "archive" + FileSystems.getDefault().getSeparator() + LolUtils.getQualifier(stepExecution) + FileSystems.getDefault().getSeparator());
      File archiveFolder = new File(archiveFolderPath);

      if (!archiveFolder.exists()) {
         archiveFolder.mkdirs();
      }

      resources.forEach(resource -> {

         if(resource == null) return;
         if(!resource.exists()) return;

         try {
            File potentialExistingFile = new File(archiveFolderPath + resource.getFile().getName());

            if (potentialExistingFile.exists()) {
               FileUtils.deleteQuietly(potentialExistingFile);
            }

            FileUtils.moveFile(resource.getFile(), potentialExistingFile);
            boolean deleted = resource.getFile().delete();
            if (!deleted) {
               logger.warn("Unable to delete file " + resource.getFile().getAbsolutePath());
            }
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      });
   }

   public static void deleteResources(final StepExecution stepExecution, Collection<Resource> resources) {
      if (CollectionUtils.isNotEmpty(resources)) {
         IterableUtils
            .forEach(resources, new Closure<Resource>() {
               @Override
               public void execute(Resource resource) {
                  String msg;
                  try {
                     if(resource != null && resource.exists()) {
                        if (! resource.getFile().delete()) {
                           msg = String.format("Failed to delete resource %s.", resource.getFile().getAbsolutePath());
                           BatchExecutionUtils.addStepExecutionInfo(
                                 stepExecution,
                                 BatchExecutionInfo
                                    .of(Level.ERROR, Category.TECHNICAL)
                                    .message(msg));
                           throw new UnexpectedJobExecutionException(msg);
                        } else {
                           msg = String.format("Resource %s successfully deleted.", resource.getFile().getAbsolutePath());
                           BatchExecutionUtils.addStepExecutionInfo(
                                 stepExecution,
                                 BatchExecutionInfo
                                    .of()
                                    .message(msg));
                        }
                     }
                  } catch (Exception e) {
                     if (e instanceof UnexpectedJobExecutionException) {
                        throw (UnexpectedJobExecutionException)e;
                     }
                     msg = String.format("Failed to delete resource %s due to %s",
                           resource.getDescription(),
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
                     throw new UnexpectedJobExecutionException(msg, e);
                  }
               }
         });
      }
   }

   /**
    * Executes the following steps:
    * <ul>
    * <li>Checks if qualifier(schema) exists and gives it back</li>
    * </ul>
    *
    * @throws UnexpectedJobExecutionException if check fails
    */
   public static String getQualifier(StepExecution stepExecution) throws UnexpectedJobExecutionException {
      String qualifier = BatchExecutionUtils.getEnviromentQualifier(stepExecution);
      if (StringUtils.isBlank(qualifier)) {
         String msg = "No qualifier has been given as job parameter. Please check job parameter.";
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg));
         throw new UnexpectedJobExecutionException(msg);
      }
      return qualifier;
   }

   /**
    * Executes the following steps:
    * <ul>
    * <li>Checks if security(access-) token exists and gives it back</li>
    * </ul>
    *
    * @throws UnexpectedJobExecutionException if check fails
    */
   public static String getSecurityToken(StepExecution stepExecution) throws UnexpectedJobExecutionException {
      String securityToken = BatchExecutionUtils.getEnviromentSecurityToken(stepExecution);
      if (StringUtils.isBlank(securityToken)) {
         String msg = "No security token has been found in job execution contxt. Please check login task.";
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg));
         throw new UnexpectedJobExecutionException(msg);
      }
      return securityToken;
   }

   /**
    * Returns <code>true</code> id the current job has an execution entry with key LolParameters.IMPORT_MODE and value <code>true</code>, otherwise <code>false</code>.
    */
   public static boolean isImportMode(StepExecution stepExecution) {
      return (boolean) ObjectUtils.defaultIfNull(
            BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.IMPORT_MODE),
            Boolean.FALSE);
   }

   /**
    * Returns <code>true</code> id the current job has an execution entry with key LolParameters.IMPORT_MODE and value <code>true</code>, otherwise <code>false</code>.
    */
   public static boolean isImportMode(JobExecution jobExecution) {
      return jobExecution != null &&
            (boolean) ObjectUtils.defaultIfNull(
                           BatchExecutionUtils.getFromJobExecutionContext(jobExecution, LolParameters.IMPORT_MODE),
                           Boolean.FALSE);
   }

   /**
    * Creates a {@link BusinessContact} object from given {@link LolImportData}
    * @param lolImportData lol import data entry
    * @return created {@link BusinessContact}
    */
   public static BusinessContact toBusinessContact(LolImportData lolImportData) {
      if (lolImportData == null) {
         return null;
      }
      BusinessContact bc = new BusinessContact();
      bc.setIdentifiers(Collections.singletonList(new Identifier(lolImportData.getBobiknummer(), IdentifierClass.CrefoNo)));
      bc.setName(toName(lolImportData));
      // due to NPE
      //bc.addAddress(toAddress(lolImportData));
      bc.getAddresses().add(toAddress(lolImportData));
      return bc;
   }

   /**
    * Creates a {@link Name} object from given {@link LolImportData}
    * @param lolImportData lol import data entry
    * @return created {@link Name}
    */
   public static Name toName(LolImportData lolImportData) {
      if (lolImportData == null) {
         return null;
      }
      final Name result = new Name();
      final int[] i = {1};
      IterableUtils.forEach(
            IterableUtils
            .filteredIterable(
                  Lists.newArrayList(
                     StringUtils.trimToNull(lolImportData.getName1()),
                     StringUtils.trimToNull(lolImportData.getName2()),
                     StringUtils.trimToNull(lolImportData.getName3()),
                     StringUtils.trimToNull(lolImportData.getName4())),
                  NotNullPredicate.notNullPredicate()),
             new Closure<String>() {
               @Override
               public void execute(String namePart) {
                  result.addNamePart(FieldType.Name, namePart, i[0]++);
               }
            });
      return result;
   }

   /**
    * Creates a {@link Address} object from given {@link LolImportData}
    * @param lolImportData lol import data entry
    * @return created {@link Address}
    */
   public static Address toAddress(LolImportData lolImportData) {
      if (lolImportData == null) {
         return null;
      }
      Address address = new Address();
      address.setZip(lolImportData.getPlz());
      address.setCity(lolImportData.getOrt());
      address.setCountryCode(lolImportData.getLand());
      return address;
   }

   @Builder
   public static class SelectInGroup<T> {
      public String getSelectInClause(List<T> inValues) {
         Assert.notEmpty(inValues, "inValues may not be empty.");
         CollectionUtils.filter(inValues, NotNullPredicate.notNullPredicate());
         final String dummyGroup = "1";
         // format (1,COLUMN) IN (%s) properly [(1,inValue1), (1,inValue2), (1,inValue3), ...] to annul oracle 1000 entry limitation
         final StringBuilder inGroupBuilder = new StringBuilder();
         for (int i = 0; i < inValues.size(); i++) {
            T inValue = inValues.get(i);
            inGroupBuilder
               .append("(")
               .append(dummyGroup)
               .append(",")
               .append(String.valueOf(inValue))
               .append(")")
               .append(i < inValues.size()-1 ? "," : "");
         }
         return inGroupBuilder.toString();
      }
   }

}

/*
 * @File: TableSwitchTasklet.java
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
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * {@link Tasklet} which executes for following tasks
 * <ul>
 * <li> pre import
 * <ul>
 * <li>Resets the active / import tables selections</li>
 * <li>Determines the active / import tables</li>
 * <li>Initializes the import table</li>
 * </ul>
 * </li>
 * <li> post import
 * <ul>
 * <li>Sets the import table as active</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author KostikX
 *
 * Das NOSONAR ist dem Umstand geschuldet, dass man in einer H2 Tabelle nicht alles testen kann
 */
//NOSONAR
@Setter
public class TableSwitchTasklet implements Tasklet, InitializingBean {

   private static final Logger logger = LoggerFactory.getLogger(TableSwitchTasklet.class);
   private String lookupSql;
   private String truncateTableSql;
   private String deleteTableSql;
   private String copyTableSql;
   private String switchSql;
   private String importTable_1;
   private String importTable_2;
   private String tableSynonym;
   private String importTable;
   private String activeTable;
   private String importView_1;
   private String importView_2;
   private String viewSynonym;
   private String importView;
   private String activeView;
   private String schema;
   private JdbcTemplate jdbcTemplate;

   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
      Object postSwitch = BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.POST_IMPORT_SWITCH);

      RepeatStatus repeatStatus = null;

      if ((Boolean) ObjectUtils.defaultIfNull(postSwitch, Boolean.FALSE)) {
         repeatStatus = postImport(contribution, stepExecution);
      } else {
         repeatStatus = preImport(contribution, stepExecution);
      }

      return repeatStatus;
   }

   protected RepeatStatus postImport(StepContribution contribution, StepExecution stepExecution) throws Exception {

      logger.info("START postImport");
      try {
         if (logger.isDebugEnabled()) {
            logger.debug(".postImport: executing post import table and view switching.");
         }
         // 1. reset tables and views
         logger.info("resetTablesAndViews");
         resetTablesAndViews();
         // 2. determine tables
         logger.info("determineTablesAndViews");
         determineTablesAndViews(stepExecution, false);
         // 3. switch synonyms
         // 3.1 switch table synonym
         logger.info("switchTableSynonym");
         switchTableSynonym(stepExecution);
         // 3.2 switch view
         logger.info("switchViewSynonym");
         switchViewSynonym(stepExecution);
         logger.info(".postImport: set active table to {0}.", importTable);
      } catch (Exception e) {
         if (! (e instanceof UnexpectedJobExecutionException)) {
            String msg = String.format("Post importing failed due to %s.",
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
         throw e; // will fail the job
      }
      logger.info("END postImport");
      return RepeatStatus.FINISHED;
   }

   protected void switchTableSynonym(StepExecution stepExecution) {
      logger.info("START switchTableSynonym");
      String sql = getSwitchTableSql();
      if (logger.isDebugEnabled()) {
         logger.debug(".switchTableSynonym: switching synonym {0} to table {1} with sql {2}.", new Object[]{tableSynonym, importTable, sql});
      }
      jdbcTemplate.update(sql);
      jdbcTemplate.execute("COMMIT");
      logger.info("END switchTableSynonym");
      BatchExecutionUtils.addStepExecutionInfo(
            stepExecution,
            BatchExecutionInfo
               .of()
               .message(String.format("%s set as active table.", importTable))
      );
   }

   protected void switchViewSynonym(StepExecution stepExecution) {
      String sql = getSwitchViewSql();
      if (logger.isDebugEnabled()) {
         logger.debug(".switchViewSynonym: switching synonym {0} to view {1} with sql {2}.", new Object[]{viewSynonym, importView, sql});
      }
      jdbcTemplate.update(sql);
      jdbcTemplate.execute("COMMIT");
      BatchExecutionUtils.addStepExecutionInfo(
            stepExecution,
            BatchExecutionInfo
               .of()
               .message(String.format("%s set as active view.", importView))
      );
   }

   protected RepeatStatus preImport(StepContribution contribution, StepExecution stepExecution) throws Exception {
      try {
         if (logger.isDebugEnabled()) {
            logger.debug(".preImport: executing pre import table preparing.");
         }
         // 1. reset tables
         logger.info("resetTablesAndViews");
         resetTablesAndViews();
         // 2. determine tables and views
         logger.info("determineTablesAndViews");
         determineTablesAndViews(stepExecution, true);
         // 3 init import table
         logger.info("initImportTable");
         initImportTable(stepExecution);
      } catch (Exception e) {
         // UnexpectedJobExecutionException handled before
         if (! (e instanceof UnexpectedJobExecutionException)) {
            String msg = String.format("Pre importing failed due to %s.",
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
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }

   protected void initImportTable(StepExecution stepExecution) {
      try {
         // 1. truncate import table
         truncateImportTable();

         // 2. copy data from active table to import table
         copyToImportTable();

         // 3.delete all by file name
         deleteByFilename(stepExecution);
      } catch (Exception e) {
         String msg = String.format("Failed due to %s.",
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

   protected void deleteByFilename(StepExecution stepExecution) {
      logger.info("START deleteByFilename");
      // delete sql is not set for lolCheckBusinessContact
      if(StringUtils.isNotBlank(deleteTableSql)) {
         String sql = getDeleteTableSql(stepExecution);
         if (logger.isDebugEnabled()) {
            logger.debug(".deleteByFilename: delete imported data by file name from import table {0} with sql {1}.", new Object[]{importTable, sql});
         }
         jdbcTemplate.update(sql);
         jdbcTemplate.execute("COMMIT");
      } else if (logger.isDebugEnabled()) {
         logger.debug(".deleteByFilename: not configured to delete entries by file name");
      }
      logger.info("END deleteByFilename");
   }

   protected void copyToImportTable() {
      logger.info("START copyToImportTable");
      String sql = getCopyTableSql();
      if (logger.isDebugEnabled()) {
         logger.debug(".copyToImportTable: coping active table {0} data in to import table {1} with sql {2}.", new Object[]{activeTable, importTable, sql});
      }
      jdbcTemplate.update(sql);
      jdbcTemplate.execute("COMMIT");
      logger.info("END copyToImportTable");
   }

   protected void truncateImportTable() {
      String sql = getTruncateTableSql();
      if (logger.isDebugEnabled()) {
         logger.debug(".truncateImportTable: truncating import table {0} with sql {1}.", new Object[]{importTable, sql});
      }
      jdbcTemplate.update(sql);
      jdbcTemplate.execute("COMMIT");
   }

   protected void resetTablesAndViews() {
      importTable = activeTable = null;
      importView = activeView = null;
   }

   /**
    * Determines the import and active tables and views
    * @param stepExecution The step execution
    * @param preImport Defines whether the resolving is execution in before or after database import
    * @throws Exception
    */
   protected void determineTablesAndViews(final StepExecution stepExecution, boolean preImport) throws Exception {
      determine(stepExecution, preImport, true);
      determine(stepExecution, preImport, false);
   }

   /**
    * Determines the import and active tables or views
    * @param stepExecution The step execution
    * @param preImport Defines whether the resolving is execution in before or after database import
    * @param tableMode Defines whether the import/active tables shall be determined or import/active views
    * @throws Exception
    */
   protected void determine(final StepExecution stepExecution, boolean preImport, boolean tableMode) throws Exception {
      String msg;
      if (preImport) {
         // preImport mode
         List<String> foundEntries = jdbcTemplate.query(getLookupSql(), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                  //select TABLE_NAME, TABLE_OWNER, OWNER
                  String tableName = rs.getString("TABLE_NAME");
                  String tableOwner = rs.getString("TABLE_OWNER");
                  String synOwner = rs.getString("OWNER");
                  if (!StringUtils.equalsIgnoreCase(schema, tableOwner) &&
                        !StringUtils.equalsIgnoreCase(schema, synOwner)) {
                     String msg = String.format("Table owner %s or synonym owner %s is not identical to schema name %s.",
                           tableOwner,
                           synOwner,
                           schema);
                     BatchExecutionUtils.addStepExecutionInfo(
                           stepExecution,
                           BatchExecutionInfo
                              .of(Level.ERROR, Category.TECHNICAL)
                              .message(msg)
                     );
                     throw new UnexpectedJobExecutionException(msg);
                  }
                  return tableName;
            }}, tableMode ? tableSynonym : viewSynonym);

         if (CollectionUtils.isEmpty(foundEntries) || foundEntries.size() > 1) {
            msg = String.format("No or several active tables/views found for synonym %s.", tableMode ? tableSynonym : viewSynonym);
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg)
            );
            throw new UnexpectedJobExecutionException(msg);
         }

         if (tableMode) {
            activeTable = foundEntries.get(0);
            importTable = activeTable.equalsIgnoreCase(importTable_1) ? importTable_2 : activeTable.equalsIgnoreCase(importTable_2) ? importTable_1 : null;
            msg = String.format("%s as active table and %s as import table determined.", activeTable, importTable);
            // write the current import table in to job context
            BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.IMPORT_TABLE, importTable);
         } else {
            activeView = foundEntries.get(0);
            importView = activeView.equalsIgnoreCase(importView_1) ? importView_2 : activeView.equalsIgnoreCase(importView_2) ? importView_1 : null;
            msg = String.format("%s as active view and %s as import view determined.", activeView, importView);
            // write the current import view in to job context
            BatchExecutionUtils.putToJobExecutionContext(stepExecution, LolParameters.IMPORT_VIEW, importView);
         }
         // handleNotResolved
         handleNotResolved(stepExecution, tableMode);
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message(msg)
         );
      } else {
         //postImport mode
         if (tableMode) {
            importTable = (String) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.IMPORT_TABLE);
            activeTable = importTable != null ?
                           (importTable.equalsIgnoreCase(importTable_1) ? importTable_2 : importTable.equalsIgnoreCase(importTable_2) ? importTable_1 : null)
                           : null;
            msg = String.format("%s as active table and %s as import table determined.",
                  StringUtils.defaultIfBlank(activeTable, "n/a"),
                  StringUtils.defaultIfBlank(importTable, "n/a"));
         } else {
            importView = (String) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.IMPORT_VIEW);
            activeView = importView != null ?
                           (importView.equalsIgnoreCase(importView_1) ? importView_2 : importView.equalsIgnoreCase(importView_2) ? importView_1 : null)
                           : null;
            msg = String.format("%s as active view and %s as import view determined.",
                  StringUtils.defaultIfBlank(activeView, "n/a"),
                        StringUtils.defaultIfBlank(importView, "n/a"));
         }
         // handleNotResolved
         handleNotResolved(stepExecution, tableMode);
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message(msg)
         );
      }
   }

   /**
    * Creates {@link BatchExecutionInfo} and throws an {@link UnexpectedJobExecutionException},
    * if import table or import view is not resolved.
    * @param stepExecution The step execution
    * @param tableMode Defines whether the import table shall be checked or import view
    */
   protected void handleNotResolved(final StepExecution stepExecution, boolean tableMode) {
      String msg;
      // error handling not resolved
      if ((tableMode && StringUtils.isEmpty(importTable)) ||
            (!tableMode && StringUtils.isEmpty(importView))) {
         msg = String.format(".handleNotResolved: activeTable/View %s doesn't match to neither %s nor %s.",
               tableMode ? activeTable : activeView,
               tableMode ? importTable_1 : importView_1,
               tableMode ? importTable_2 : importView_2);
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg)
         );
         throw new UnexpectedJobExecutionException(msg);
      }
   }

   @Override
   public void afterPropertiesSet() {
      Assert.notNull(lookupSql, "lookupSql must not be null.");
      Assert.notNull(truncateTableSql, "truncateSql must not be null.");
      Assert.notNull(deleteTableSql, "deleteSql must not be null.");
      Assert.notNull(copyTableSql, "copySql must not be null.");
      Assert.notNull(switchSql, "switchSql must not be null.");
      Assert.notNull(importTable_1, "importTable_1 must not be null.");
      Assert.notNull(importTable_2, "importTable_2 must not be null.");
      Assert.notNull(tableSynonym, "tableSynonym must not be null.");
      Assert.notNull(schema, "schema must not be null.");
   }

   protected String getLookupSql() {
      return StringUtils.trimToEmpty(lookupSql);
   }

   protected String getTruncateTableSql() {
      return String.format(truncateTableSql, schema+"."+importTable).trim();
   }

   protected String getCopyTableSql() {
      return String.format(
            copyTableSql,
            schema+"."+importTable,
            schema+"."+importTable,
            schema+"."+activeTable,
            schema+"."+activeTable)
            .trim();
   }

   @SuppressWarnings("unchecked")
   protected String getDeleteTableSql(StepExecution stepExecution) {
      try {
         Object incomingFiles = BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES);
         List<Resource> files = (List<Resource>) (incomingFiles != null
               ? incomingFiles
               : null);
         if (files == null) {
            String msg = String.format(".getDeleteSQL: No incoming files could be resolved from execution context.");
            BatchExecutionUtils.addStepExecutionInfo(
                  stepExecution,
                  BatchExecutionInfo
                     .of(Level.ERROR, Category.TECHNICAL)
                     .message(msg)
            );
            throw new UnexpectedJobExecutionException(msg);
         }

         List<String> fileNames =
               FluentIterable
                  .from(files)
                  .transform(new Function<Resource, String>() {
                        @Override
                        public String apply(Resource resource) {
                           return resource != null
                                 ? "'" + resource.getFilename() + "'"
                                 : null;
                        }
                     })
                  .toList();

         // build select in clause
         String selectInCaluse =
               LolUtils.SelectInGroup
                  .<String>builder()
                  .build()
                  .getSelectInClause(fileNames);

         return String.format(deleteTableSql, schema + "." + importTable, selectInCaluse).trim();
      } catch (Exception e) {
         if (e instanceof UnexpectedJobExecutionException) {
            throw (UnexpectedJobExecutionException)e;
         }
         String msg = String.format("Failed due to %s.",
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

   protected String getSwitchTableSql() {
      return String.format(switchSql, schema + "." + tableSynonym, schema + "." + importTable).trim();
   }

   protected String getSwitchViewSql() {
      return String.format(switchSql, schema + "." + viewSynonym, schema + "." + importView).trim();
   }


}

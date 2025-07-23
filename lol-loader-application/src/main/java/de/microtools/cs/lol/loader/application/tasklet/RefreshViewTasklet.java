package de.microtools.cs.lol.loader.application.tasklet;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.util.Assert;

@Setter
public class RefreshViewTasklet implements Tasklet, InitializingBean {

   private static final Logger logger = LoggerFactory.getLogger(RefreshViewTasklet.class);
   private JdbcTemplate jdbcTemplate;
   @Value("${lol.batch.jdbc.schema}")
   private String schema;
   private String refreshSql;

   @Override
   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
      try {
         StepExecution stepExecution =
               chunkContext
               .getStepContext()
               .getStepExecution();
         refreshView(stepExecution);
      } catch (Exception e) {
         // handled before
         throw e; // will fail the job
      }
      return RepeatStatus.FINISHED;
   }

   protected void refreshView(StepExecution stepExecution) {
      try {
         String importView = (String)BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.IMPORT_VIEW);
         if (StringUtils.isBlank(importView)) {
            throw new UnexpectedJobExecutionException("Could not resolve import view to refresh.");
         }
         String refreshCall = getRefreshSql(importView);
         String msg = String.format("executing %s", refreshCall);
         logger.info(msg);
         // create and execute StoredProcedure
         StoredProcedure procedure = new GenericStoredProcedure();
         procedure.setJdbcTemplate(jdbcTemplate);
         procedure.setSqlReadyForUse(true);
         procedure.setFunction(false);
         procedure.setSql(refreshCall);
         procedure.execute();

         BatchExecutionUtils.addStepExecutionInfo(
                 stepExecution,
                 BatchExecutionInfo
                         .of()
                         .message("View " + importView + " successfully refreshed via " + msg)
         );
      } catch (Exception e) {
         String msg = String.format("Failed to refresh view due to %s.",
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

   @Override
   public void afterPropertiesSet() {
      Assert.notNull(jdbcTemplate, "jdbcTemplate must not be null.");
      Assert.notNull(refreshSql, "refreshSql must not be null.");
   }

   protected String getRefreshSql(String importView) {
      return String.format(refreshSql, schema + "." + importView).trim();
   }
}

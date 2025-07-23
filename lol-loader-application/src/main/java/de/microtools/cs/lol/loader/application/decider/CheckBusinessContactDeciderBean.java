package de.microtools.cs.lol.loader.application.decider;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.cs.lol.loader.integration.MappingServiceWrapper;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Setter
public class CheckBusinessContactDeciderBean implements JobExecutionDecider, InitializingBean {

   private static final Logger logger = LoggerFactory.getLogger(CheckBusinessContactDeciderBean.class);
   private MappingServiceWrapper mappingServiceWrapper;
   private JdbcTemplate jdbcTemplate;
   private String countSql;
   @Autowired
   private JobRepository jobRepository;

   @Override
   public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
      FlowExecutionStatus result = null;
      try {
         String msg = "n/a";
         List<Long> mappinIds = mappingServiceWrapper.fetchClientMappings(stepExecution);
         // set the mappings ids ti job execution, they will be used to select lol entries to check business contact
         BatchExecutionUtils
         .putToJobExecutionContext(
               stepExecution,
               LolParameters.MAPPING_IDS,
               ObjectUtils.defaultIfNull(
                     mappinIds,
                     Collections.<Long>emptyList()));
         if(CollectionUtils.isEmpty(mappinIds)) {
            msg = String.format("No complete client mappings exists for tenant %s. Nothing to do.", LolUtils.getQualifier(stepExecution));
            result = FlowExecutionStatus.FAILED;
         } else {
            List<NotCheckedCount> notCheckedEntries = jdbcTemplate.query(getCountSql(mappinIds), new RowMapper<NotCheckedCount>() {
               @Override
               public NotCheckedCount mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return NotCheckedCount.of(rs.getLong("COUNT_ALL"), rs.getLong("COUNT_BC"));
               }});

            if (CollectionUtils.isEmpty(notCheckedEntries) || notCheckedEntries.size() > 1 || notCheckedEntries.get(0) == null) {
               msg = String.format("No or several count values found.");
               BatchExecutionUtils.addStepExecutionInfo(
                     stepExecution,
                     BatchExecutionInfo
                        .of(Level.ERROR, Category.TECHNICAL)
                        .message(msg)
               );
               throw new UnexpectedJobExecutionException(msg);
            }
            NotCheckedCount notCheckedCount = notCheckedEntries.get(0);
            Long notCheckedLolEntries = notCheckedCount.getNotCheckedLolEntries();
            Long notCheckedBusinessContacts = notCheckedCount.getNotCheckedBusinessContacts();
            // set the count of not checked business contacts to job execution context
            setBusinessContactCountsToExecutionContext(stepExecution, notCheckedBusinessContacts);
            if (notCheckedLolEntries.equals(0L)) {
               msg = String.format("No unchecked lol entries found to process. Nothing to do.");
               result = FlowExecutionStatus.FAILED;
            } else {
               msg = String.format(
                     "%d lol entries (%d business contacts) have not been checked yet and will be processed now.",
                     notCheckedLolEntries,
                     notCheckedBusinessContacts);
               result = FlowExecutionStatus.COMPLETED;
            }
         }
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of()
                  .message(msg));
         // not registered by step listener
         logger.info(".decide: " + msg);
         return result;
      } catch (Exception e) {
         boolean stackTrace = !(e instanceof UnexpectedJobExecutionException);
         String msg = String.format("Failed to decide to check business contacts due to %s",
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
         throw e; // will fail the job
      } finally {
         // explicit update of step execution context to get the new step execution context infos on focus of previous step updated
         jobRepository.updateExecutionContext(stepExecution);
         // explicit update of job execution context as no job execution context update will be performed by framework while FlowExecutionStatus.FAILED
         if (FlowExecutionStatus.FAILED.equals(result)) {
            jobRepository.updateExecutionContext(jobExecution);
         }
      }
   }

   public String getCountSql(List<Long> mappingIds) {
      Assert.notEmpty(mappingIds, "no mapping id has been provided.");
      // format (1,MAPPINGID) IN (%s) properly [(1,id1), (1,id2), (1,id3), ...] to annul oracle 1000 entry limitation
      // build select in clause
      String selectInCaluse =
            LolUtils.SelectInGroup
               .<Long>builder()
               .build()
               .getSelectInClause(mappingIds);
      return StringUtils.trimToEmpty(String.format(countSql, selectInCaluse));
   }

   @Override
   public void afterPropertiesSet() {
      Assert.notNull(jdbcTemplate, "jdbcTemplate must not be null.");
      Assert.notNull(countSql, "countSql must not be null.");
   }

   protected static void setBusinessContactCountsToExecutionContext(StepExecution stepExecution, Long notCheckedBusinessContacts) {
      Assert.notNull(notCheckedBusinessContacts);
      BatchExecutionUtils.putToJobExecutionContext(
            stepExecution.getJobExecution(),
            LolParameters.NOT_CHECKED_BUSINESS_CONTACTS,
            notCheckedBusinessContacts);
      BatchExecutionUtils.putToJobExecutionContext(
            stepExecution.getJobExecution(),
            LolParameters.CHECKED_BUSINESS_CONTACTS_EXIST,
            0L);
      BatchExecutionUtils.putToJobExecutionContext(
            stepExecution.getJobExecution(),
            LolParameters.CHECKED_BUSINESS_CONTACTS_CREATED,
            0L);
   }

   @RequiredArgsConstructor(staticName = "of")
   @Getter
   private static class NotCheckedCount {
      @NonNull
      private Long notCheckedLolEntries;
      @NonNull
      private Long notCheckedBusinessContacts;
   }

}

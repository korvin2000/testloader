package de.microtools.cs.lol.loader.application.reader;

import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Category;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfoProvider.Level;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.util.Assert;

import java.util.List;

@Setter
public class LolDatabaseReader extends JdbcCursorItemReader<LolImportData> {

   private StepExecution stepExecution;

   @Override
   public void afterPropertiesSet() throws Exception {
      super.afterPropertiesSet();
      Assert.notNull(stepExecution, "The SQL query must be provided");
   }


   @SuppressWarnings("unchecked")
   @Override
   public void setSql(String sql) {
      List<Long> mappingIds = (List<Long>)BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.MAPPING_IDS);
      if(CollectionUtils.isEmpty(mappingIds)) {
         String msg = "No mapping id have been resolved to select lol entried for business contact check. Job will fail.";
         BatchExecutionUtils.addStepExecutionInfo(
               stepExecution,
               BatchExecutionInfo
                  .of(Level.ERROR, Category.TECHNICAL)
                  .message(msg));
          // set step/job to failed
          throw new UnexpectedJobExecutionException(msg);
      }

      // build select in clause
      String selectInCaluse =
            LolUtils.SelectInGroup
               .<Long>builder()
               .build()
               .getSelectInClause(mappingIds);

      // set the substituted sql to parent
      super.setSql(StringUtils.trimToEmpty(String.format(sql, selectInCaluse)));
   }

}

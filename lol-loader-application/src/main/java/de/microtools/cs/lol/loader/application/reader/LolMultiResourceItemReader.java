package de.microtools.cs.lol.loader.application.reader;

import de.microtools.n5.infrastructure.batching.application.spring.reader.MultiResourceItemReader;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionInfo;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import lombok.Setter;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
@Setter
public class LolMultiResourceItemReader<T> extends MultiResourceItemReader<T> implements InitializingBean {

   private StepExecution stepExecution;

   @Override
   public void afterPropertiesSet() throws Exception {
      Assert.notNull(stepExecution, "stepExecution must not be null.");
   }

   @Override
   protected void logCurrentResource() {
      BatchExecutionUtils.addStepExecutionInfo(
            stepExecution,
            BatchExecutionInfo
               .of()
               .message(String.format("Start reading file %s", getCurrentResource().getFilename()))
         );
   }

}

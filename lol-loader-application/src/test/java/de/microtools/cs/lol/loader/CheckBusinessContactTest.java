package de.microtools.cs.lol.loader;

import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.processor.LolBusinessContactProcessor;
import de.microtools.cs.lol.loader.integration.BusinessContactServiceWrapper;
import de.microtools.cs.lol.loader.integration.MappingServiceWrapper;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @File: CheckBusinessContactTest.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 */
@ActiveProfiles({"test", "sync"})
public class CheckBusinessContactTest extends LolImportCommonTest {

   @Test
   // sync test as async transactions is not supported by hsql
   public void testJobRunningCheck() throws Exception {
      // execute lolImport
      executeLolImport();
      // execute lolCheckBusinessContact
      JobExecution jobExecution = checkBusinessContactJobLauncher.launchJob(qualifierParamCsMulticlient);
      // started? Synchronized task executor
      assertThat("checkBusinesscontact is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
   }

   @Test
   public void testDefaultImportMode() throws Exception {
      // execute lolImport
      executeLolImport();

      // all entries are not checked after import?
      assertThat("Count of not checked lol entries is not " + testEntryCount, getImportTableEntryCountNotCheckedBC(), is(testEntryCount));

      // execute lolCheckBusinessContact
      JobExecution jobExecution = checkBusinessContactJobLauncher.launchJob(qualifierParamCsMulticlient);
      // completed? Synchronized task executor
      assertThat("job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of checked lol entries is not " + testEntryCount, getImportTableEntryCountCheckedBC(), is(testEntryCount));

      // get the import mode
      Boolean importMode = (Boolean) jobExecution.getExecutionContext().get(LolParameters.IMPORT_MODE);
      assertThat("Found importMode in job execution context.", importMode, is(nullValue()));
   }


   @Test
   public void testProcessedMappingFail() throws Exception {
      MappingServiceWrapper mappingServiceWrapper = context.getBean(MappingServiceWrapper.class);
      mappingServiceWrapper.setMappingService(exceptionThorwingMappingApi);
      mappingServiceWrapper.beforeStep(mockedCheckBusinessContactStepExecution);

      BusinessContactServiceWrapper businessContactServiceWrapper = context.getBean(BusinessContactServiceWrapper.class);
      businessContactServiceWrapper.setMappingServiceWrapper(mappingServiceWrapper);
      businessContactServiceWrapper.beforeStep(mockedCheckBusinessContactStepExecution);

      LolBusinessContactProcessor processor = new LolBusinessContactProcessor();
      processor.setBusinessContactServiceWrapper(businessContactServiceWrapper);
      processor.setStepExecution(mockedCheckBusinessContactStepExecution);
      LolImportData lolImportData = processor.process(mockedLolImportData);
      assertThat("lolImportData is not null", lolImportData, nullValue());
   }

   @Test
   public void testProcessedMappingEmpty() throws Exception {
      MappingServiceWrapper mappingServiceWrapper = context.getBean(MappingServiceWrapper.class);
      mappingServiceWrapper.setMappingService(emptyReturningMappingApi);
      mappingServiceWrapper.beforeStep(mockedCheckBusinessContactStepExecution);

      BusinessContactServiceWrapper businessContactServiceWrapper = context.getBean(BusinessContactServiceWrapper.class);
      businessContactServiceWrapper.setMappingServiceWrapper(mappingServiceWrapper);
      businessContactServiceWrapper.beforeStep(mockedCheckBusinessContactStepExecution);

      LolBusinessContactProcessor processor = new LolBusinessContactProcessor();
      processor.setBusinessContactServiceWrapper(businessContactServiceWrapper);
      processor.setStepExecution(mockedCheckBusinessContactStepExecution);
      LolImportData lolImportData = processor.process(mockedLolImportData);
      assertThat("lolImportData is not null", lolImportData, nullValue());
   }

   @Override
   protected JobLauncherTestUtils getJobLauncher() {
      return checkBusinessContactJobLauncher;
   }

   @Override
   protected String getLoginCrefoStepName() {
      return "loginBobiksystemCheck";
   }

}

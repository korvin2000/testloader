package de.microtools.cs.lol.loader;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.processor.LolMappingProcessor;
import de.microtools.cs.lol.loader.integration.LoginServiceWrapper;
import de.microtools.cs.lol.loader.integration.MappingServiceWrapper;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.jdbc.JdbcTestUtils;


/**
 * @File: Lol2ImportJobTest.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.

 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 */
public class LolImportTest extends LolImportCommonTest {

   @Test
   public void testLolImportWithoutDataInsideFtp() throws Exception {
      // empty the FTP Folder
      for (Object o : fileSystem.listFiles(FAKE_FOLDER)) {
         FileEntry fileEntry = (FileEntry) o;
         fileSystem.delete(fileEntry.getPath());
      }

      Assert.assertEquals("We expect an empty FTP folder in this special case!", 0, fileSystem.listFiles(FAKE_FOLDER).size());

      // copy test data directly into the "ftp-in" folder (which normally is being used by the ftp-Downloader tasklet)
      FileUtils.copyFile(new File("src/test/resources/data/123456789001_100_1100_150828.xml"),
              new File("target/ftp-in/cs_axelclient/123456789001_100_1100_150828.xml"));

      // execute Job without all the additional asserts inside "executeLolImport();"
      JobExecution jobExecution =
              lolImportJobLauncher.launchJob(
                      new JobParametersBuilder(qualifierParamCsMulticlient)
                              .addString("uuid", UUID.randomUUID().toString())
                              .toJobParameters());

      Assert.assertTrue("Job execution should not stop at the download step", jobExecution.getStepExecutions().size() >= 3);
      File archiveFolder = new File(workDir.replace("file:","") + "/archive/cs_axelclient/");
      Assert.assertTrue("we expect files inside the archive folder", archiveFolder.listFiles().length > 0);
   }

   @Test
   public void testLolImportForDifferentSchemas() throws Exception {
      JobExecution jobExecution =
              lolImportJobLauncher.launchJob(
                      new JobParametersBuilder(qualifierParamCsGwgAbruf) // <-- not multiclient, but gwgAbruf!
                              .addString("uuid", UUID.randomUUID().toString())
                              .toJobParameters());
      Assert.assertTrue("Job execution should not stop at the download step", jobExecution.getStepExecutions().size() > 2);

      File archiveFolder = new File(workDir.replace("file:","") + "/archive/cs_gogoabruf/");
      Assert.assertTrue("we expect files inside the archive folder", archiveFolder.listFiles().length > 0);
   }

   /*
   Use this test only for mass and stress tests
    */
   //@Test
   public void testLolImportLotsOfData() throws Exception {
      // Initially, copy your favorite testdata into the ftp-in/cs_axelclient folder and then start the test
      // cp /vagrant/LOL/*.xml /home/vagrant/Git/lol-loader/lol-loader-application/target/ftp-in/cs_axelclient/
      File archiveFolder = new File(workDir.replace("file:","") + "/archive/cs_axelclient/");

      if (archiveFolder.exists()) {

         for (File file : archiveFolder.listFiles()) {

            if (file.isFile() && file.getName().endsWith("xml")) {
               FileUtils.copyFile(file, new File(workDir.replace("file:", "") + "/cs_axelclient/" + file.getName()));
            }
         }
      }

      // use this for generating your own test data
      //GenerateTestData.generate();

      JobExecution jobExecution =
              lolImportJobLauncher.launchJob(
                      new JobParametersBuilder(qualifierParamCsMulticlient)
                              .addString("uuid", UUID.randomUUID().toString())
                              .toJobParameters());
      Assert.assertTrue("Job execution should not stop at the download step", jobExecution.getStepExecutions().size() > 2);

      System.out.println(archiveFolder.getAbsolutePath());
      Assert.assertTrue("we expect files inside the archive folder", archiveFolder.listFiles().length > 0);

      for (StepExecution step : jobExecution.getStepExecutions()) {
         Assert.assertTrue("BatchStatus " + step.getStepName() + " is not completed, but " + step.getStatus().name(), step.getStatus().equals(BatchStatus.COMPLETED));
      }
   }

   @Test
   public void testLolImportReportDate() throws Exception {
      // execute lolImport
      executeLolImport();

      // Test the imported filename + report date
      for (TestSource testSource : testFiles) {
         int countByFileAndReportDate = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, schema +"."+ importTable_1,
               "filename ='" + testSource.getResource().getFilename() + "' AND " +
               "stichtag = TIMESTAMP ( '" + dateFormatter.print(testSource.getReportDate(), Locale.US) + "', '" +
               timeFormatter.print(testSource.getReportDate(), Locale.GERMAN) + "' )");
         assertThat("Count of imported lol entries by FileAndReportDate is not " + testSource.getCount(), countByFileAndReportDate, is(testSource.getCount()));
      }
   }

   @Test
   public void testLolImportDate() throws Exception {
      // execute lolImport
      JobExecution jobExecution = executeLolImport();

      // get the step executions
      Date importDate = (Date) jobExecution.getExecutionContext().get(LolParameters.IMPORT_DATE);
      assertThat("Could not find importDate in job execution context.", importDate, not(nullValue()));

      // Test the imported entries by importDate
      int countByImportDate = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, schema +"."+ importTable_1,
            "importdatum = TO_TIMESTAMP ( '" + dateFormatter.print(importDate, Locale.GERMAN) + " " +
            timeFormatter.print(importDate, Locale.GERMAN) + "', 'YYYY-MM-DD HH:MI:SS.FF' )");
      assertThat("Count of imported lol entries by import date is not " + testEntryCount, countByImportDate, is(testEntryCount));
   }

   @Test
   public void testLolImportMode() throws Exception {
      // execute lolImport
      JobExecution jobExecution = executeLolImport();

      // get the import mode
      Boolean importMode = (Boolean) jobExecution.getExecutionContext().get(LolParameters.IMPORT_MODE);
      assertThat("Could not find importMode in job execution context.", importMode, not(nullValue()));
      assertThat("ImportMode is not true", importMode, is(true));
   }

   @Test
   public void testLolImportJobDownloadDefault() throws Exception {
      // execute lolImport
      executeLolImport();

      deleteImportTable();

      // explicit download parameter
      JobExecution jobExecution =
            lolImportJobLauncher.launchJob(
                  new JobParametersBuilder(qualifierParamCsMulticlient)
                  .addString(LolParameters.DOWNLOAD, "true")
                  .toJobParameters());
      // completed? Synchronized task executor
      assertThat("job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of imported lol entries is not " + testEntryCount, getImportTableEntryCount(), is(testEntryCount));
   }

   @Test
   public void testLolImportJobDownload() throws Exception {
      // table will be recreated, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));
      // execute the job
      // explicit download parameter
      JobExecution jobExecution = lolImportJobLauncher.launchJob(
            new JobParametersBuilder(qualifierParamCsMulticlient)
            .addString(LolParameters.DOWNLOAD, "true")
            .toJobParameters());
      // completed? Synchronized task executor
      assertThat("job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of imported lol entries is not " + testEntryCount, getImportTableEntryCount(), is(testEntryCount));
   }

   @Test
   public void testLolImportJobNoDownload() throws Exception {
      // table will be recreated, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));

      // 1. explicit no download parameter
      JobExecution jobExecution = lolImportJobLauncher.launchJob(
            new JobParametersBuilder(qualifierParamCsMulticlient)
            .addString(LolParameters.DOWNLOAD, "false")
            .toJobParameters());
      // completed? Synchronized task executor
      assertThat("job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of imported lol entries is not ", getImportTableEntryCount(), is(0));

      // get the step executions
      Long jobExecutionId = jobExecution.getId();
      List<StepExecution> stepExecutions = new ArrayList<>(jobService.getStepExecutions(jobExecutionId));
      // expected 5 steps
      assertThat("stepExecutions are not >=5", stepExecutions.size(), greaterThanOrEqualTo(5));
      // filter collectFiles step
      CollectionUtils.filter(stepExecutions, new Predicate() {
         @Override
         public boolean evaluate(Object object) {
            StepExecution stepExecution = object != null ?  (StepExecution) object : null;
            return stepExecution != null && "collectFiles".equals(stepExecution.getStepName());
         }
      });
      // exists step execution collectFiles?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      // is it failed
      assertThat("collectFiles didn't stopped.", stepExecutions.get(0).getExitStatus(), is(ExitStatus.STOPPED));

      // 2. download from ftp and execute it a gain
      jobExecution = lolImportJobLauncher.launchStep("download", qualifierParamCsMulticlient);
      assertThat("Step download is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // 2.1 execute the job without download
      jobExecution = lolImportJobLauncher.launchJob(
            new JobParametersBuilder(qualifierParamCsMulticlient)
               .addString(LolParameters.DOWNLOAD, "true")
               .addString("uuid", UUID.randomUUID().toString())
               .toJobParameters());

      // completed? Synchronized task executor
      assertThat("job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of imported lol entries is not " + testEntryCount, getImportTableEntryCount(), is(testEntryCount));

      // check the step executions again
      jobExecutionId = jobExecution.getId();
      stepExecutions = new ArrayList<>(jobService.getStepExecutions(jobExecutionId));
      // expected 5 steps
      assertThat("stepExecutions are not >=5", stepExecutions.size(), greaterThanOrEqualTo(5));
      // filter collectFiles step
      CollectionUtils.filter(stepExecutions, new Predicate() {
         @Override
         public boolean evaluate(Object object) {
            StepExecution stepExecution = object != null ?  (StepExecution) object : null;
            return stepExecution != null && "collectFiles".equals(stepExecution.getStepName());
         }
      });
      // exists step execution collectFiles?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      // is it failed
      assertThat("collectFiles didn't completed", stepExecutions.get(0).getExitStatus(), is(ExitStatus.COMPLETED));
   }

   @Test
   public void testCheckJobRunningStep() throws Exception {
      // execute lolImport
      executeLolImport();

      deleteImportTable();
      testLolImportJobDownloadDefault();

      JobExecution jobExecution =
            lolImportJobLauncher.launchStep(
                  "checkJobRunning",
                  new JobParametersBuilder(qualifierParamCsMulticlient)
                     .addString("uuid", UUID.randomUUID().toString())
                     .toJobParameters());
      assertThat("Step is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testInvalidSchemaIncomingFile() throws Exception {

      // table will be deleted, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));
      JobExecution jobExecution = lolImportJobLauncher.launchStep("download", qualifierParamCsMulticlient);
      assertThat("Job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
      CollectionUtils.filter(stepExecutions, new Predicate() {
         @Override
         public boolean evaluate(Object object) {
            StepExecution stepExecution = object != null ?  (StepExecution) object : null;
            return stepExecution != null && "download".equals(stepExecution.getStepName());
         }
      });

      // exists step execution download?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      StepExecution stepExecution = stepExecutions.iterator().next();
      // is it failed
      assertThat("download didn't completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      final TestSource invalidSchema = testFiles.get(5);
      // is the invalid source 123456789001_invalid_150828.xml dowloaded?
      assertThat("Invalid Rueckliferung "+ invalidSchema.getResource().getFilename() +" not downloaded",
            new UrlResource(workDir + "/cs_axelclient/" + invalidSchema.getResource().getFilename())
            .exists(),
            is(true));

      jobExecution = lolImportJobLauncher.launchStep("collectFiles",
            new JobParametersBuilder(qualifierParamCsMulticlient)
               .addString("uuid", UUID.randomUUID().toString())
               .toJobParameters(),
            new ExecutionContext());
      assertThat("Job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      stepExecutions = jobExecution.getStepExecutions();
      // exists step execution collectFiles?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      stepExecution = stepExecutions.iterator().next();
      // is it failed
      assertThat("collectFiles didn't completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      // is the invalid file ignored to be entered in incoming files list in job execution context?
      List<Resource> incomingResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES);
      assertThat("incomingResources are empty", incomingResources, not(empty()));
      assertThat("incomingResources are not 5", incomingResources.size(), is(5));
      assertThat("123456789001_invalid_150828.xml recognized as valid incoming file",
            CollectionUtils.countMatches(incomingResources, new Predicate() {
               @Override
               public boolean evaluate(Object object) {
                  return object != null && ((Resource)object).getFilename().equals(invalidSchema.getResource().getFilename());
               }
            }),
            is(0));

      // is the invalid file deleted from work dir?
      assertThat("Invalid Rueckliferung "+ invalidSchema.getResource().getFilename() +" not deleted from work dir",
            new UrlResource(workDir + "/cs_axelclient/" + invalidSchema.getResource().getFilename())
            .exists(),
            is(false));

   }

   @SuppressWarnings("unchecked")
   @Test
   public void testNotParsableIncomingFile() throws Exception {

      // table will be deleted, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));
      JobExecution jobExecution = lolImportJobLauncher.launchStep("download", qualifierParamCsMulticlient);
      assertThat("Job is completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
      CollectionUtils.filter(stepExecutions, new Predicate() {
         @Override
         public boolean evaluate(Object object) {
            StepExecution stepExecution = object != null ?  (StepExecution) object : null;
            return stepExecution != null && "download".equals(stepExecution.getStepName());
         }
      });

      // exists step execution download?
      assertThat("stepExecutions should be just 1", stepExecutions.size(), equalTo(1));
      StepExecution stepExecution = stepExecutions.iterator().next();
      assertThat("The one step should have been a download", stepExecution.getStepName(), equalTo("download"));
      assertThat("ExitStatus should have been completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      final TestSource notParseable = testFiles.get(6);
      System.out.println(notParseable.getResource().getFile().getAbsolutePath());
      // is the invalid source 123456789001_invalid_150829.xml dowloaded?
      assertThat("File not found "+ notParseable.getResource().getFilename() +" not downloaded",
            new UrlResource(workDir + "/cs_axelclient/" + notParseable.getResource().getFilename())
            .exists(),
            is(true));

      jobExecution = lolImportJobLauncher.launchStep("collectFiles",
            new JobParametersBuilder(qualifierParamCsMulticlient)
               .addString("uuid", UUID.randomUUID().toString())
               .toJobParameters(),
            new ExecutionContext());
      assertThat("Job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      stepExecutions = jobExecution.getStepExecutions();
      // exists step execution collectFiles?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      stepExecution = stepExecutions.iterator().next();
      // is it failed
      assertThat("collectFiles didn't completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      // is the invalid file ignored to be entered in incoming files list in job execution context?
      List<Resource> incomingResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES);
      assertThat("incomingResources are empty", incomingResources, not(empty()));
      assertThat("incomingResources are not 5", incomingResources.size(), is(5));
      assertThat("123456789001_invalid_150829.xml recognized as valid incoming file",
            CollectionUtils.countMatches(incomingResources, new Predicate() {
               @Override
               public boolean evaluate(Object object) {
                  return object != null && ((Resource)object).getFilename().equals(notParseable.getResource().getFilename());
               }
            }),
            is(0));

      // is the invalid file deleted from work dir?
      assertThat("Invalid Rueckliferung "+ notParseable.getResource().getFilename() +" not deleted from work dir",
            new UrlResource(workDir + "/cs_axelclient/" + notParseable.getResource().getFilename())
            .exists(),
            is(false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testEmptyIncomingFile() throws Exception {

      // table will be deleted, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));
      JobExecution jobExecution = lolImportJobLauncher.launchStep("download", qualifierParamCsMulticlient);
      assertThat("Job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
      CollectionUtils.filter(stepExecutions, new Predicate() {
         @Override
         public boolean evaluate(Object object) {
            StepExecution stepExecution = object != null ?  (StepExecution) object : null;
            return stepExecution != null && "download".equals(stepExecution.getStepName());
         }
      });

      // exists step execution download?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      StepExecution stepExecution = stepExecutions.iterator().next();
      // is it failed
      assertThat("download didn't completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      final TestSource empty = testFiles.get(7);
      // is the invalid source 123456789001_invalid_150830.xml dowloaded?
      assertThat("Invalid Rueckliferung "+ empty.getResource().getFilename() +" not downloaded",
            new UrlResource(workDir + "/cs_axelclient/" + empty.getResource().getFilename())
            .exists(),
            is(true));

      jobExecution = lolImportJobLauncher.launchStep("collectFiles",
            new JobParametersBuilder(qualifierParamCsMulticlient)
               .addString("uuid", UUID.randomUUID().toString())
               .toJobParameters(),
            new ExecutionContext());
      assertThat("Job is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      stepExecutions = jobExecution.getStepExecutions();
      // exists step execution collectFiles?
      assertThat("stepExecutions are not == 1", stepExecutions.size(), equalTo(1));
      stepExecution = stepExecutions.iterator().next();
      // is it failed
      assertThat("collectFiles didn't completed", stepExecution.getExitStatus(), is(ExitStatus.COMPLETED));

      // is the invalid file ignored to be entered in incoming files list in job execution context?
      List<Resource> incomingResources = (List<Resource>) BatchExecutionUtils.getFromJobExecutionContext(stepExecution, LolParameters.INCOMING_FILES);
      assertThat("incomingResources are empty", incomingResources, not(empty()));
      assertThat("incomingResources are not 5", incomingResources.size(), is(5));
      assertThat("123456789001_invalid_150830.xml recognized as valid incoming file",
            CollectionUtils.countMatches(incomingResources, new Predicate() {
               @Override
               public boolean evaluate(Object object) {
                  return object != null && ((Resource)object).getFilename().equals(empty.getResource().getFilename());
               }
            }),
            is(0));

      // is the invalid file deleted from work dir?
      assertThat("Invalid Rueckliferung "+ empty.getResource().getFilename() +" not deleted from work dir",
            new UrlResource(workDir + "/cs_axelclient/" + empty.getResource().getFilename())
            .exists(),
            is(false));
   }

   
   @Test
   public void testProcessedMappingFail() throws Exception {
      MappingServiceWrapper mappingServiceWrapper = context.getBean(MappingServiceWrapper.class);
      mappingServiceWrapper.setMappingService(exceptionThorwingMappingApi);
      mappingServiceWrapper.beforeStep(mockedLolImportStepExecution);
      LolMappingProcessor processor = new LolMappingProcessor();
      processor.setMappingServiceWrapper(mappingServiceWrapper);
      processor.setStepExecution(mockedLolImportStepExecution);
      LolImportData lolImportData = processor.process(mockedLolDebitor);
      assertThat("lolImportData is null", lolImportData, not(nullValue()));
      assertThat("mapping id is not null", lolImportData.getMappingId(), is(-1L));
   }

   @Test
   public void testProcessedMappingEmpty() throws Exception {
      MappingServiceWrapper mappingServiceWrapper = context.getBean(MappingServiceWrapper.class);
      mappingServiceWrapper.setMappingService(emptyReturningMappingApi);
      mappingServiceWrapper.beforeStep(mockedLolImportStepExecution);
      LolMappingProcessor processor = new LolMappingProcessor();
      processor.setMappingServiceWrapper(mappingServiceWrapper);
      processor.setStepExecution(mockedLolImportStepExecution);
      LolImportData lolImportData = processor.process(mockedLolDebitor);
      assertThat("lolImportData is null", lolImportData, not(nullValue()));
      assertThat("mapping id is not null", lolImportData.getMappingId(), is(-1L));
   }

   @Test(expected = JobParametersInvalidException.class)
   // qualifier as job parameter is mandatory
   public void testNoJobParams() throws Exception {
      // table will be recreated, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));
      getJobLauncher().launchJob();
   }

   @Test
   public void testLoginCrefoStepNotauthorizing() {
      LoginServiceWrapper loginServiceWrapper = context.getBean(LoginServiceWrapper.class);
      loginServiceWrapper.setLoginService(unAuthorizingLoginApi);
      JobExecution jobExecution = getJobLauncher().launchStep(getLoginCrefoStepName(), qualifierParamCsMulticlient);
      assertThat("Job is not failed", jobExecution.getStatus(), is(BatchStatus.FAILED));
      StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
      assertThat("Step logingBobiksystemCheck is not failed", stepExecution.getStatus(), is(BatchStatus.FAILED));
      assertThat("Security token is set.", BatchExecutionUtils.getEnviromentSecurityToken(stepExecution), is(nullValue()));
   }

   @Test
   public void testLoginCrefoStepEmptyToken() {
      LoginServiceWrapper loginServiceWrapper = context.getBean(LoginServiceWrapper.class);
      loginServiceWrapper.setLoginService(emptyReturningLoginApi);
      JobExecution jobExecution = getJobLauncher().launchStep(getLoginCrefoStepName(), qualifierParamCsMulticlient);
      assertThat("Job is not failed", jobExecution.getStatus(), is(BatchStatus.FAILED));
      StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
      assertThat("Step logingBobiksystemCheck is not failed", stepExecution.getStatus(), is(BatchStatus.FAILED));
      assertThat("Security token is set.", BatchExecutionUtils.getEnviromentSecurityToken(stepExecution), is(nullValue()));
   }

   @Override
   protected JobLauncherTestUtils getJobLauncher() {
      return lolImportJobLauncher;
   }

   @Override
   protected String getLoginCrefoStepName() {
      return "loginBobiksystemImport";
   }

}

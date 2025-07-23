package de.microtools.cs.lol.loader;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.conf.LolParameters;
import de.microtools.cs.lol.loader.application.domain.LolDebitorTypeErw;
import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.listener.CacheAware;
import de.microtools.cs.lol.mapping.api.LolMappingAPI;
import de.microtools.cs.lol.mapping.model.CompositeKey;
import de.microtools.cs.lol.mapping.model.DoubleKeyEntry;
import de.microtools.cs.lol.mapping.model.MappingEntry;
import de.microtools.n5.core.grapa.client.LoginApi;
import de.microtools.n5.core.grapa.client.UnauthorizedException;
import de.microtools.n5.infrastructure.batching.admin.service.BatchJobService;
import de.microtools.n5.infrastructure.batching.application.spring.util.BatchExecutionUtils;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystemException;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
      locations = {
            "classpath*:META-INF/spring/application-context.xml"
         })
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"test"})
@Rollback
public abstract class LolImportCommonTest {

   private static final Logger logger = LoggerFactory.getLogger(LolImportCommonTest.class);
   public static final String FAKE_FOLDER = "/fakeFolder/";
   @Autowired
   protected LolImportJobLauncher lolImportJobLauncher;
   @Autowired
   protected CheckBusinessContactJobLauncher checkBusinessContactJobLauncher;
   @Autowired
   @Qualifier("lolJdbcTemplate")
   protected JdbcTemplate jdbcTemplate;
   @Value("${lol.batch.jdbc.table.1}")
   protected String importTable_1;
   @Value("${lol.batch.jdbc.schema}")
   protected String schema;
   @Value("${lol.batch.work.dir}")
   protected String workDir;
   @Autowired
   protected ApplicationContext context;
   @Autowired
   protected BatchJobService jobService;
   @Autowired
   protected JobRegistry jobRegistry;
   protected static final DateFormatter dateTimeFormatter = new DateFormatter("yyyy-MM-dd'T'HH:mm:ss");
   protected static final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd");
   protected static final DateFormatter timeFormatter = new DateFormatter("HH:mm:ss.SSS");
   protected static final Date testImportDate = new Date();
   protected static FakeFtpServer fakeFtpServer;
   protected static UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
   protected static int testEntryCount = 0;
   protected final static JobParameters qualifierParamCsMulticlient =
         new JobParametersBuilder()
            .addParameter(
                  BatchExecutionUtils.ENV_QUAIFIER_KEY,
                  new JobParameter("cs_axelclient"))
            .toJobParameters();

   protected final static JobParameters qualifierParamCsGwgAbruf =
           new JobParametersBuilder()
                   .addParameter(
                           BatchExecutionUtils.ENV_QUAIFIER_KEY,
                           new JobParameter("cs_gogoabruf"))
                   .toJobParameters();
   protected static final List<TestSource> testFiles =
         ImmutableList.of(
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150828.xml"), "2015-08-28T00:00:00", 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150908.xml"), "2015-09-08T00:00:00", 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150915.xml"), "2015-09-15T00:00:00", 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150922.xml"), "2015-09-22T00:00:00", 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150929.xml"), "2015-09-29T00:00:00", 21),
                       new TestSource(new ClassPathResource("data/123456789001_150828.xml"), "2015-08-28T00:00:00", 0),
                       new TestSource(new ClassPathResource("data/123456789001_150829.xml"), "2015-08-29T00:00:00", 0),
                       new TestSource(new ClassPathResource("data/123456789001_150830.xml"), "2015-09-30T00:00:00", 0));
   @Mock
   protected LoginApi unAuthorizingLoginApi;
   @Mock
   protected LoginApi emptyReturningLoginApi;
   @Mock
   protected LoginApi authorizingLoginApi;
   @Mock
   protected LolMappingAPI exceptionThorwingMappingApi;
   @Mock
   protected LolMappingAPI emptyReturningMappingApi;
   @Mock
   protected StepExecution mockedLolImportStepExecution;
   @Mock
   protected StepExecution mockedCheckBusinessContactStepExecution;
   @Mock
   protected JobExecution mockedJobExecution;
   @Mock
   protected ExecutionContext mockedJobExecutionContext;
   @Mock
   protected ExecutionContext mockedStepExecutionContext;
   @Mock
   protected JobParameters mockedJobParams;
   @Mock
   protected JobInstance mockedJobInstance;
   @Mock
   protected LolDebitorTypeErw mockedLolDebitor;
   @Mock
   protected LolImportData mockedLolImportData;
   @Mock
   protected UrlResource mockedResource;

   protected static final String TEST_USER = "standard01";
   protected static final String TEST_PASSWORD = "testpwd000";
   protected static final String TEST_QUALIFIER = "cs_axelclient";
   protected static final String TEST_TOKEN = "cs_mulsecurityticlient";
   protected static final String TEST_MITGLIEDNUMMER = "123456789001";
   protected static final String TEST_MANDANT = "100";
   protected static final String TEST_BUCHUNGSKREIS = "1100";
   protected static final String TEST_IMPORT_FILE = "123456789001_100_1100_150828.xml";
   protected static final String TEST_JOB_NAME = "lolImport";
   protected static final Long TEST_CS_ID = 1L;
   protected static final Long TEST_MAPPING_ID = 1L;
   protected static final String TEST_CREFONUMMER = "1234567879";
   protected static final String TEST_NAME_1 = "name_1";

   @BeforeClass
   public static void initBeforeClass() throws Exception {
      // FTP Mock
      fakeFtpServer = new FakeFtpServer();
      // get the first free port
      fakeFtpServer.setServerControlPort(0);
      UserAccount userAccount = new UserAccount("spring", "spring", "/");
      logger.info("FtpServer userAccount: {0}", userAccount);
      fakeFtpServer.addUserAccount(userAccount);

      try {
         fileSystem.add(new DirectoryEntry(FAKE_FOLDER));
      } catch (FileSystemException e) {
         // Junge mir ist das so was von egal ob es schon existiert, du Idiot! Bad Practice deswegen gleich eine Exception zu schmeissen!
      }
      fileSystem.setCreateParentDirectoriesAutomatically(false);

      fakeFtpServer.setFileSystem(fileSystem);
      logger.info("FtpServer file system: {0}", fakeFtpServer.getFileSystem());

      fakeFtpServer.start();
      int port = fakeFtpServer.getServerControlPort();
      // set the server port for client
      System.setProperty("lol.ftp.host", "localhost");
      System.setProperty("lol.ftp.port", String.valueOf(port));
      System.setProperty("lol.ftp.user", "spring");
      System.setProperty("lol.ftp.password", "spring");
      logger.info("Binding port is: " + port);
   }


   @Before
   public void setUp() throws Exception {

      // empty the FTP Folder
      testEntryCount = 0;
      for (Object o : fileSystem.listFiles(FAKE_FOLDER)) {
         FileEntry fileEntry = (FileEntry) o;
         fileSystem.delete(fileEntry.getPath());
      }

      for (TestSource testFile : testFiles) {
         fileSystem.add(new FileEntry(FAKE_FOLDER + testFile.getResource().getFile().getName(), IOUtils.toString(testFile.getResource().getURI())));
         testEntryCount += testFile.getCount();
      }

      MockitoAnnotations.initMocks(this);
      // mock login api
      when(this.unAuthorizingLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD))
         .thenThrow(new UnauthorizedException("User " + TEST_USER + " unauthorized."));
      when(this.unAuthorizingLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD, null))
         .thenThrow(new UnauthorizedException("User " + TEST_USER + " unauthorized."));
      when(this.emptyReturningLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD))
         .thenReturn(null);
      when(this.emptyReturningLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD, null))
         .thenReturn(null);
      when(this.authorizingLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD))
         .thenReturn(TEST_TOKEN);
      when(this.authorizingLoginApi
            .login(TEST_QUALIFIER, TEST_USER, TEST_PASSWORD, null))
         .thenReturn(TEST_TOKEN);

      // mock mapping api
      when(this.exceptionThorwingMappingApi
            .getRelevantKeys())
         .thenReturn(ImmutableList.of("mitgliedsnummer", "mandant", "buchungskreis"));
      when(this.exceptionThorwingMappingApi
            .putUIDs(anyListOf(CompositeKey.class)))
         .thenThrow(new RuntimeException("putUIDs failed"));
      when(this.exceptionThorwingMappingApi
            .getClientMappings(anyListOf(Long.class)))
         .thenThrow(new RuntimeException("getClientMappings failed"));

      // emptyReturningMappingApi
      when(this.emptyReturningMappingApi
            .getRelevantKeys())
         .thenReturn(ImmutableList.of("mitgliedsnummer", "mandant", "buchungskreis"));
      when(this.emptyReturningMappingApi
            .putUIDs(anyListOf(CompositeKey.class)))
         .thenReturn(Collections.<DoubleKeyEntry>emptyList());
      when(this.emptyReturningMappingApi
            .getClientMappings(anyListOf(Long.class)))
         .thenReturn(Collections.<MappingEntry>emptyList());

      // mock job instance
      when(this.mockedJobInstance
            .getJobName())
         .thenReturn(TEST_JOB_NAME);

      // mock stepExecution
      // mockedLolImportStepExecution
      when(this.mockedLolImportStepExecution
            .getJobExecution())
         .thenReturn(mockedJobExecution);
      when(this.mockedLolImportStepExecution
            .getJobExecution()
            .getExecutionContext())
         .thenReturn(mockedJobExecutionContext);
      when(this.mockedLolImportStepExecution
            .getExecutionContext())
         .thenReturn(mockedStepExecutionContext);
      when(this.mockedLolImportStepExecution
            .getJobParameters())
         .thenReturn(mockedJobParams);
      when(this.mockedLolImportStepExecution
            .getJobExecution()
            .getJobInstance())
         .thenReturn(mockedJobInstance);
      when(this.mockedLolImportStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(LolParameters.IMPORT_DATE))
         .thenReturn(testImportDate);
      when(this.mockedLolImportStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(LolParameters.IMPORT_MODE))
         .thenReturn(true);
      when(this.mockedLolImportStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(BatchExecutionUtils.ENV_SEC_TOKEN_KEY))
         .thenReturn(TEST_TOKEN);

      // mockedCheckBusinessContactStepExecution
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution())
         .thenReturn(mockedJobExecution);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution()
            .getExecutionContext())
         .thenReturn(mockedJobExecutionContext);
      when(this.mockedCheckBusinessContactStepExecution
            .getExecutionContext())
         .thenReturn(mockedStepExecutionContext);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobParameters())
         .thenReturn(mockedJobParams);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobParameters()
            .getString(BatchExecutionUtils.ENV_QUAIFIER_KEY))
         .thenReturn(TEST_QUALIFIER);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution()
            .getJobInstance())
         .thenReturn(mockedJobInstance);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(LolParameters.IMPORT_DATE))
         .thenReturn(testImportDate);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(LolParameters.IMPORT_MODE))
         .thenReturn(true);
      when(this.mockedCheckBusinessContactStepExecution
            .getJobExecution()
            .getExecutionContext()
            .get(BatchExecutionUtils.ENV_SEC_TOKEN_KEY))
         .thenReturn(TEST_TOKEN);

      // mock resource
      when(mockedResource.getFilename())
         .thenReturn(TEST_IMPORT_FILE);

      // mock mockedLolDebitor
      when(mockedLolDebitor.getMitgliedsnummer())
         .thenReturn(TEST_MITGLIEDNUMMER);
      when(mockedLolDebitor.getMandant())
         .thenReturn(TEST_MANDANT);
      when(mockedLolDebitor.getBuchungskreis())
         .thenReturn(TEST_BUCHUNGSKREIS);
      when(mockedLolDebitor.getResource())
         .thenReturn(mockedResource);

      // mock LolImportData
      when(mockedLolImportData.getId())
         .thenReturn(TEST_CS_ID);
      when(mockedLolImportData.getMappingId())
         .thenReturn(TEST_MAPPING_ID);
      when(mockedLolImportData.getBobiknummer())
         .thenReturn(TEST_CREFONUMMER);
      when(mockedLolImportData.getName1())
         .thenReturn(TEST_NAME_1);
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
      if (fakeFtpServer != null) {
         fakeFtpServer.stop();
      }
   }

   @After
   public void tearDown() throws Exception {
      if (context != null) {
         // reset the caches
         Map<String, CacheAware> caches = context.getBeansOfType(CacheAware.class);
         for(CacheAware cache : caches.values()) {
            cache.invalidate(null);
         }
      }
   }

   // for the SFTP Mock Server
   public static int findFreePort() {
      try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
         socket.setReuseAddress(true);
         return socket.getLocalPort();
      } catch (Exception e) {
         return 443;
      }
   }

   protected JobExecution executeLolImport() throws Exception {
      // table will be deleted, also the initial count shall be 0
      assertThat("Initial count of entries in lol import table is not 0", getImportTableEntryCount(), is(0));

      Collection<String> jobNames = jobRegistry.getJobNames();
      assertThat("Job locator didn't know lolImport,lolCheckBusinessContact", jobNames, contains("lolImport", "lolCheckBusinessContact"));

      // star lolImport
      JobExecution jobExecution =
            lolImportJobLauncher.launchJob(
               new JobParametersBuilder(qualifierParamCsMulticlient)
               .addString("uuid", UUID.randomUUID().toString())
               .toJobParameters());
      // started? Synchronized task executor
      assertThat("lolImport is not completed", jobExecution.getStatus(), is(BatchStatus.COMPLETED));
      // correct entry count after import?
      assertThat("Count of imported lol entries is not " + testEntryCount, getImportTableEntryCount(), is(testEntryCount));

      return jobExecution;
   }

   protected void deleteImportTable() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, schema +"."+ importTable_1);
      assertThat("Import table is not empty.", getImportTableEntryCount(), is(0));
   }

   protected  int getImportTableEntryCount() {
      return JdbcTestUtils.countRowsInTable(jdbcTemplate, schema +"."+ importTable_1);
   }

   protected  int getImportTableEntryCountCheckedBC() {
      return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, schema +"."+ importTable_1, "BUSINESSCONTACT = 1 AND (1,MAPPINGID) IN ((1,1000),(1,2000),(1,3000))");
   }

   protected  int getImportTableEntryCountNotCheckedBC() {
      return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, schema +"."+ importTable_1, "BUSINESSCONTACT = 0 AND (1,MAPPINGID) IN ((1,1000),(1,2000),(1,3000))");
   }

   @Data
   protected static class TestSource {
      private Resource resource;
      private Date reportDate;
      private int count;

      public TestSource(ClassPathResource resource, String reportDate, int count) {
         try {
            this.resource = resource;
            this.reportDate = dateTimeFormatter.parse(reportDate, Locale.GERMAN);
            this.count = count;
         } catch (Exception e) {
            fail(e.getMessage());
         }
      }
   }

   protected abstract JobLauncherTestUtils getJobLauncher();

   protected abstract String getLoginCrefoStepName();

}

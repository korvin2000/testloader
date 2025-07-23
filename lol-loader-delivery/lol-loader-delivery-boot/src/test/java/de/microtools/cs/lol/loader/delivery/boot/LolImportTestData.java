/*
 * @File: LolImportTestData.java
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
package de.microtools.cs.lol.loader.delivery.boot;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;

import de.microtools.n5.infrastructure.batching.api.domain.BatchStatusInfo;
import de.microtools.n5.infrastructure.batching.api.domain.JobExecutionInfo;
import de.microtools.n5.infrastructure.batching.api.domain.JobInfo;
import de.microtools.n5.infrastructure.batching.api.domain.JobInstanceInfo;
import de.microtools.n5.infrastructure.batching.api.exception.JobExecutionException;
import de.microtools.n5.infrastructure.batching.api.util.JobParametersExtractor;
import de.microtools.n5.infrastructure.batching.application.spring.mapping.JsonObjectMapper;
import lombok.Data;

public class LolImportTestData {

   public static final JsonObjectMapper objectMapper = JsonObjectMapper.configure();

   public static final int testEntryCount = 109;
   public static final List<TestSource> testFiles =
         ImmutableList.of(
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150828.xml"), 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150908.xml"), 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150915.xml"), 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150922.xml"), 22),
                       new TestSource(new ClassPathResource("data/123456789001_100_1100_150929.xml"), 21));

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   @SuppressWarnings("serial")
   // list jobs
   private static List<JobInfo> listJobs =
         new ArrayList<JobInfo>(){{
            add(new JobInfo("lolImport", 0, true, true));
            add(new JobInfo("lolCheckBusinessContact", 1, true, true));
         }};

     public static final JobExecutionException listJobsException = new JobExecutionException("listJobs JobExecutionException");


     public static String listJobsJSON() throws Exception {
        return objectMapper.writeValueAsString(listJobs);
     }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // launch job
    public static final String lolImportJobName = "lolImport";
    public static final String checkBusinessContactJobName = "lolCheckBusinessContact";
    public static final Long lolImportJobInstanceId = 0L;

    public static final String lolImportJobParams = "download=true";

    public static final JobExecutionInfo lolImportJobExecutionInfo = new JobExecutionInfo(0L, 0L, 6, new JobParametersExtractor().fromString(lolImportJobParams),
          new JobInstanceInfo(1L, lolImportJobName), BatchStatusInfo.COMPLETED, new Date(), null);

    public static final JobExecutionInfo checkBusinessContactJobExecutionInfo = new JobExecutionInfo(1L, 1L, 3, null,
          new JobInstanceInfo(0L, checkBusinessContactJobName), BatchStatusInfo.COMPLETED, new Date(), null);

    public static String launchCheckBusinessContactExecutionInfoJSON() throws Exception {
       return objectMapper.writeValueAsString(checkBusinessContactJobExecutionInfo);
    }

    public static String launchLolImportExecutionInfoJSON() throws Exception {
       return objectMapper.writeValueAsString(lolImportJobExecutionInfo);
    }

   @Data
   public static class TestSource {
      private Resource resource;
      private int count;

      public TestSource(ClassPathResource resource, int count) {
         try {
            this.resource = resource;
            this.count = count;
         } catch (Exception e) {
            fail(e.getMessage());
         }
      }
   }
}

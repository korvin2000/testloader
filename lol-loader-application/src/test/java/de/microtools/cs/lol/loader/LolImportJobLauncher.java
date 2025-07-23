package de.microtools.cs.lol.loader;

import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class LolImportJobLauncher extends JobLauncherTestUtils {

   @Override
   @Autowired
   @Qualifier("lolImport")
   public void setJob(Job job) {
      super.setJob(job);
   }

}

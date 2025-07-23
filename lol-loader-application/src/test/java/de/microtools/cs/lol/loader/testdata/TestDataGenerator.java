package de.microtools.cs.lol.loader.testdata;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestDataGenerator {

   private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);

   public static void main(String[] args) throws Exception {

     URL templateFolder = Thread.currentThread().getContextClassLoader().getResource("templates");
     List<File> templates = new ArrayList<>(FileUtils.listFiles(new File(templateFolder.getFile()), new String[]{"xml"}, false));

     File targetFolder = new File(templateFolder.getFile()+"\\generated");
     FileUtils.forceMkdir(targetFolder);
     FileUtils.cleanDirectory(targetFolder);

     for (File template : templates) {
        for (int i = 0; i < 10; i++) {
           String originMitgliednummer = getMitgliedManadantBuchungskreis(template)[0];
           String newMitgliednummer = originMitgliednummer.substring(0, originMitgliednummer.length()-1)+i;
           File testFile = generate(template, newMitgliednummer, RandomUtils.nextInt(0, 50)+"", RandomUtils.nextInt(0, 50)+"", targetFolder);
           logger.info("generated testfile {0} for template {1}", testFile.getName(), template.getName());
        }
     }
   }

   protected static File generate(File template, String mitgliednummer, String mandant, String buchungskreis, File targetFolder) throws Exception {
      mandant = StringUtils.defaultIfBlank(mandant, "0");
      buchungskreis = StringUtils.defaultIfBlank(buchungskreis, "0");

      String originFileName = template.getName();
      String[] mitgliedManadantBuchungskreis = getMitgliedManadantBuchungskreis(template);
      String originMitgliednummer = mitgliedManadantBuchungskreis[0];
      String resultFilename = originFileName.replace(originMitgliednummer, mitgliednummer);
      String filenameWithoutMitgliedMandantBuchungskreis = StringUtils.substringAfter(resultFilename, "_");
      resultFilename =  mitgliednummer + "_" + mandant + "_" + buchungskreis + "_" + filenameWithoutMitgliedMandantBuchungskreis;

      File result = new File(targetFolder, resultFilename);
      List<String> replacedLines;
      try (OutputStream out = new FileOutputStream(result)) {
         try (InputStream in = new FileInputStream(template)) {
            List<String> originLines = IOUtils.readLines(in);
            replacedLines = new ArrayList<>();
            // <mitgliedsnummer>425977144001</mitgliedsnummer><mandant>0</mandant><buchungskreis>0</buchungskreis><bobiknummer>4070580115</bobiknummer>
            for (String line : originLines) {
               replacedLines.add(line
                  .replaceAll("<mitgliedsnummer>\\d{12}</mitgliedsnummer>", "<mitgliedsnummer>"+mitgliednummer+"</mitgliedsnummer>")
                  .replaceAll("<mandant>\\d+</mandant>", "<mandant>"+mandant+"</mandant>")
                  .replaceAll("<buchungskreis>\\d+</buchungskreis>", "<buchungskreis>"+buchungskreis+"</buchungskreis>")
                  .replaceAll("<bobiknummer>\\d+</bobiknummer>", "<bobiknummer>"+RandomUtils.nextLong(1070580115L, 9070580115L)+"</bobiknummer>"));
            }
            IOUtils.writeLines(replacedLines, null, out, StandardCharsets.UTF_8.toString());
         } catch (Exception e) {
            logger.error(".generate: failed read due to " + e.getMessage(), e);
         }
      } catch (Exception e) {
         logger.error(".generate: failed write due to " + e.getMessage(), e);
      }
      return result;
   }


   protected static String[] getMitgliedManadantBuchungskreis(File template) {

      String originFileName = template.getName();
      String[] filenameTokens = originFileName.split("_");
      String originMitgliednummer = null;
      String originMandantennummer = null;
      String originBuchungskreis = null;
      // ^\\d{12}(_\\d+|)*_\\d{6}\\.xml$
      if (filenameTokens.length == 4) {
         originMitgliednummer = filenameTokens[0];
         originMandantennummer = filenameTokens[1];
         originBuchungskreis = filenameTokens[2];
      } else if (filenameTokens.length == 3) {
         originMitgliednummer = filenameTokens[0];
         originMandantennummer = filenameTokens[1];
      } else if (filenameTokens.length == 2) {
         originMitgliednummer = filenameTokens[0];
      } else {
         throw new IllegalArgumentException("File name " + originFileName + " not parsable");
      }

      if (originMandantennummer == null && originBuchungskreis != null) {
         throw new IllegalArgumentException("File name " + originFileName + " not parsable");
      }
      return new String[]{originMitgliednummer, originMandantennummer, originBuchungskreis};
   }

}

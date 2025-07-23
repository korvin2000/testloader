package de.microtools.cs.lol.loader.application.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.domain.LolRuecklieferungErw;
import de.microtools.cs.lol.loader.application.exception.ExecutionAlreadyRunningException;
import de.microtools.cs.lol.loader.application.exception.NoSuchItemException;
import de.microtools.n5.infrastructure.batching.application.spring.conf.BatchApplicationContextProvider;
import de.microtools.n5.infrastructure.batching.application.spring.writer.StaxEventItemWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;

@Setter
public class DataSetService implements InitializingBean {

   private static final Logger logger = LoggerFactory.getLogger(DataSetService.class);
   @Value("${lol.dataset.regex}")
   private String dataSetRegEx;
   @Value("${lol.dataset.entry.regex}")
   private String dataSetEntryRegEx;
   private FileSystemResource dataSetDirectory;
   private static final FileSystem defaultFileSystem = FileSystems.getDefault();
   private DatatypeFactory datatypeFactory;

   /**
    * Gets the list of all found {@link DataSet}s in {@link #dataSetDirectory} which satisfy the {@link #dataSetRegEx}
    *
    * @return List of {@link DataSet}s. The returned data sets has no content.
    */
   public List<DataSet> getDataSets() {
      try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(defaultFileSystem.getPath(dataSetDirectory.getPath()),
            new DirectoryStream.Filter<Path>() {
               @Override
               public boolean accept(Path path) throws IOException {
                  return path != null &&
                           Files.isRegularFile(path) &&
                           path.getFileName() != null &&
                           path.getFileName().toString().matches(dataSetRegEx);
               }
            })) {
            return
                  FluentIterable
                     .of(dirStream)
                     .transform(new Transformer<Path, DataSet>() {
                        @Override
                        public DataSet transform(Path path) {
                           return DataSet.of(path);
                        }
                     })
                     .toList();
      } catch (Throwable e) {
         logger.error(".getDataSet: failed due to {0}.", new Object[]{e.getLocalizedMessage()}, e);
         throw Throwables.propagate(e);
      }
   }

   /**
    *  Gets the {@link DataSet} with the given name. The returned data set has content.
    *
    * @param setName The set name to find
    *
    * @param updateStichtag if {@code true}, the the Stichtag information of dataset will be updated backwards from current date
    *
    * @return The found {@link DataSet}
    */
   public DataSet getDataSet(final String setName, final boolean updateStichtag) {
      // do we have any matching dataset?
      List<DataSet> foundDataSets =
            FluentIterable
               .of(getDataSets())
               .filter(new Predicate<DataSet>() {
                  @Override
                  public boolean evaluate(DataSet dataSet) {
                     return dataSet != null && dataSet.getFileName().equalsIgnoreCase(setName);
                  }
               })
               .toList();
      if (CollectionUtils.isEmpty(foundDataSets)) {
         throw new NoSuchItemException("No dataset found with name " + setName);
      }

      // source data set
      final Path sourceDataSetZipFile = foundDataSets.get(0).getPath();
      // specify the path to the source data set zip file as FileSystem
      URI sourceDataSetZipUri = URI.create(ResourceUtils.JAR_URL_PREFIX + sourceDataSetZipFile.toUri() + ResourceUtils.JAR_URL_SEPARATOR);
      // create / open the source data set zip file System
      Path resultDataSetZipFileTemp = null;
      try (FileSystem sourceDataSetZipfs = FileSystems.newFileSystem(sourceDataSetZipUri, ImmutableMap.<String, String>of())) {
         // filter the source data set zip entry files which match the dataSetEntryRegEx
         try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(sourceDataSetZipfs.getPath("/"),
               new DirectoryStream.Filter<Path>() {
                  @Override
                  public boolean accept(Path path) throws IOException {
                     return path != null &&
                           Files.isRegularFile(path) &&
                           path.getFileName().toString().matches(dataSetEntryRegEx);
                     }
               })) {
            List<Path> sourceDataSetZipEntries = FluentIterable.of(dirStream).toList();
            // 404 if source dataset has no matching entries
            if (CollectionUtils.isEmpty(sourceDataSetZipEntries)) {
               throw new NoSuchItemException(String.format("dataset %s has no entry which match %s.", setName, dataSetEntryRegEx));
            }
            // reverse ordering the source data set zip entries to modify stich tag in order
            Collections.sort(sourceDataSetZipEntries, new Comparator<Path>() {
               @Override
               public int compare(Path o1, Path o2) {
                  String fileName1 = "n/a";
                  String fileName2 = "n/a";
                  try {
                     fileName1 = o1.toString();
                     fileName2 = o2.toString();
                     String itemcount1 = fileName1.substring(fileName1.lastIndexOf("_")+1, fileName1.lastIndexOf("."));
                     String itemcount2 = fileName2.substring(fileName2.lastIndexOf("_")+1, fileName2.lastIndexOf("."));
                     if (NumberUtils.isDigits(itemcount1) && NumberUtils.isDigits(itemcount2)) {
                        return ObjectUtils.compare(NumberUtils.toInt(itemcount1), NumberUtils.toInt(itemcount2), false) * -1;
                     } else {
                        logger.error(".getDataSet: failed to order files {0} and {1} due to not properly naming. Lexically ordering will be used.", new Object[]{fileName1, fileName2});
                        return o1.compareTo(o2) * -1;
                     }
                  } catch (Exception e) {
                     logger.error(".getDataSet: failed to order files {0} and {1} due to not perperly naming. Lexically ordering will be used. Error:",
                           new Object[]{fileName1, fileName2, ExceptionUtils.getRootCauseMessage(e)},
                           e);
                     return o1.compareTo(o2) * -1;
                  }
               }
            });

            logger.info(
               ".getDataSet: using dataset {0} with ordered entries {1}",
               sourceDataSetZipFile.toAbsolutePath(),
               IterableUtils
               .toString(
                     sourceDataSetZipEntries,
                     new Transformer<Path, String>() {
                        @Override
                        public String transform(Path resource) {
                           return resource.toAbsolutePath().toString();
                        }
                     })
               );

            // create / open the result zip file System
            resultDataSetZipFileTemp = Files.createTempFile(setName.replaceAll("(?i).(zip|gz|jar)", "")+"_", ".zip");
            // delete the temp file as will be recreated by FileSystem, we need only it's unique name here
            Files.delete(resultDataSetZipFileTemp);
            final URI resultDataSetZipUri = URI.create(ResourceUtils.JAR_URL_PREFIX + resultDataSetZipFileTemp.toUri() + ResourceUtils.JAR_URL_SEPARATOR);
            // create / open the target data set zip file System
            try (FileSystem targetDataSetZipfs = FileSystems.newFileSystem(resultDataSetZipUri, ImmutableMap.of("create", "true", "encoding", "UTF-8"))) {
               final GregorianCalendar now = new GregorianCalendar();
               // substract 1 month, the first loop will decrement it
               now.add(Calendar.MONTH, 1);
               // iterate over source entries
               IterableUtils
                  .forEach(sourceDataSetZipEntries, new Closure<Path>() {
                        @Override
                        public void execute(Path sourceDataSetZipEntryPath) {
                              Path targetDataSetZipEntryTemp = null;
                              StaxEventItemReader<LolRuecklieferungErw> ruecklieferungReader = null;
                              StaxEventItemWriter<LolRuecklieferungErw> ruecklieferungWriter = null;
                              try {
                                 if (updateStichtag) {
                                    String sourceDataSetZipNamePrefix = sourceDataSetZipEntryPath.getFileName().toString().replaceAll("(?i).xml", "")+"_";
                                    // create temp target zip entry with the same name
                                    targetDataSetZipEntryTemp = Files.createTempFile(sourceDataSetZipNamePrefix, ".xml");
                                    // create resource for stax event reader
                                    UrlResource resourceRead =
                                          new UrlResource(
                                                ResourceUtils.JAR_URL_PREFIX +
                                                ResourceUtils.FILE_URL_PREFIX +
                                                sourceDataSetZipFile.toFile().getAbsolutePath() +
                                                ResourceUtils.JAR_URL_SEPARATOR +
                                                sourceDataSetZipEntryPath.getFileName());

                                    logger.info(".getDataSet: begin to read {0}", resourceRead.getURL());
                                    ruecklieferungReader = getRuecklieferungReader();
                                    // read the zip entry
                                    ruecklieferungReader.setResource(resourceRead);
                                    ruecklieferungReader.open(new ExecutionContext());
                                    LolRuecklieferungErw ruecklieferung = ruecklieferungReader.read();
                                    ruecklieferungReader.close();

                                    // substract the mount for Stichtag
                                    now.add(Calendar.MONTH, -1);
                                    XMLGregorianCalendar stichTag = datatypeFactory.newXMLGregorianCalendar(now);
                                    ruecklieferung.setDatum(stichTag);

                                    logger.info(".getDataSet: begin to write {0} with Stichtag {1}", targetDataSetZipEntryTemp.toAbsolutePath(), stichTag.toString());
                                    // write the modified data to target zip entry temp
                                    ruecklieferungWriter = getRuecklieferungWriter();
                                    ruecklieferungWriter.setResource(new PathResource(targetDataSetZipEntryTemp));
                                    ruecklieferungWriter.open(new ExecutionContext());
                                    ruecklieferungWriter.write(Collections.singletonList(ruecklieferung));
                                    ruecklieferungWriter.close();
                                 }
                                 // cop the temp transformed file / source zip entry in to target zip
                                 Path sourceDataSetZipEntry = updateStichtag ? targetDataSetZipEntryTemp : sourceDataSetZipEntryPath;
                                 Path targetDataSetZipEntry = targetDataSetZipfs.getPath("/" + sourceDataSetZipEntryPath.getFileName());
                                 Files.copy(sourceDataSetZipEntry, targetDataSetZipEntry, StandardCopyOption.REPLACE_EXISTING);
                              } catch(Throwable e) {
                                 logger.error("failed to transform the zip entry {0}, due to {1}",
                                       new Object[]{ExceptionUtils.getRootCauseMessage(e), sourceDataSetZipEntryPath.toAbsolutePath()}, e);
                                 throw Throwables.propagate(e);
                              } finally {
                                 try {
                                    if (ruecklieferungReader != null) ruecklieferungReader.close();
                                    if (ruecklieferungWriter != null) ruecklieferungWriter.close();
                                 } catch (Exception e) {
                                    // ignore
                                 }
                                 if (targetDataSetZipEntryTemp != null &&
                                       !FileUtils.deleteQuietly(targetDataSetZipEntryTemp.toFile())) {
                                    targetDataSetZipEntryTemp.toFile().deleteOnExit();
                                 }
                              }
                           }
               });
            }
         } catch(Throwable e) {
            logger.error("transformation failed due to {0}", new Object[]{ExceptionUtils.getRootCauseMessage(e)}, e);
            throw Throwables.propagate(e);
         }
         try (InputStream input = new FileInputStream(resultDataSetZipFileTemp.toFile())) {
            DataSet result = DataSet.of(setName, IOUtils.toByteArray(input));
            logger.info(".getDataSet: returned result dataset {0}.", result);
            return result;
         } catch(Exception e) {
            logger.error("failed to read content of {0} due to {1}",
                  new Object[]{resultDataSetZipFileTemp.getFileName(), ExceptionUtils.getRootCauseMessage(e)}, e);
            throw Throwables.propagate(e);
         } finally {
            if (resultDataSetZipFileTemp != null &&
                  !FileUtils.deleteQuietly(resultDataSetZipFileTemp.toFile())) {
               resultDataSetZipFileTemp.toFile().deleteOnExit();
            }
         }
      } catch (Throwable e) {
         // Occurs while parallel calling
         if (e instanceof FileSystemAlreadyExistsException) {
            String msg = String.format("dataset %s is already under transformation. Please try later.", sourceDataSetZipFile.getFileName());
            logger.error(msg);
            throw new ExecutionAlreadyRunningException(msg);
         } else {
            logger.error("failed due transform {0} due to {1}.",
                  new Object[]{sourceDataSetZipFile.getFileName(), ExceptionUtils.getRootCauseMessage(e)}, e);
            throw Throwables.propagate(e);
         }
      }
   }

   @Getter
   @ToString(of={"fileName", "fileSize"})
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {
      "fileName",
      "fileSize"
   })
   public static class DataSet implements Serializable {
      private static final long serialVersionUID = -5031118244742137461L;
      private String fileName;
      private Long fileSize;
      @XmlTransient
      private byte[] content;
      @XmlTransient
      private Path path;

      private DataSet(Path path, String fileName, byte[] content)  {
         this.content = content;
         this.path = path;
         this.fileName = fileName;
         this.fileSize = content != null ? Long.valueOf(content.length) : null;
         // override the info from path if it is given and fileName is null
         if (path != null && fileName == null) {
            try {
               File file = path.toFile();
               this.fileName = file.getName();
               this.fileSize = content == null ? file.length() : content.length;
            } catch (UnsupportedOperationException e) {
                // not a file object
            }
         }
      }

      public static DataSet of(Path path) {
         return new DataSet(path, null, null);
      }

      public static DataSet of(String fileName, byte[] content) {
         return new DataSet(null, fileName, content);
      }

      public DataSet() {
         this(null, null, null);
      }
   }

   @Override
   public void afterPropertiesSet() throws Exception {
     Assert.notNull(dataSetDirectory, "dataSetDirectory must not be null.");
     if (!dataSetDirectory.exists()) {
        Assert.isTrue(
              dataSetDirectory.getFile().mkdirs(),
              String.format(" could not create directory %s.", dataSetDirectory.getPath()));
     }
     datatypeFactory = DatatypeFactory.newInstance();
   }

   /**
    * Gets prototype instance of {@link StaxEventItemReader ruecklieferungReader}
    */
   @SuppressWarnings("unchecked")
   private static StaxEventItemReader<LolRuecklieferungErw> getRuecklieferungReader() {
      ApplicationContext context = BatchApplicationContextProvider.get();
      Assert.notNull(context, "context must not be null.");
      StaxEventItemReader<LolRuecklieferungErw> reader = context.getBean("ruecklieferungReader", StaxEventItemReader.class);
      Assert.notNull(reader, "ruecklieferungReader must not be null.");
      return reader;
   }

   /**
    * Gets prototype instance of {@link StaxEventItemWriter ruecklieferungWriter}
    */
   @SuppressWarnings("unchecked")
   private static StaxEventItemWriter<LolRuecklieferungErw> getRuecklieferungWriter() {
      ApplicationContext context = BatchApplicationContextProvider.get();
      Assert.notNull(context, "context must not be null.");
      StaxEventItemWriter<LolRuecklieferungErw> writer = context.getBean("ruecklieferungWriter", StaxEventItemWriter.class);
      Assert.notNull(writer, "ruecklieferungWriter must not be null.");
      return writer;
   }
}

package de.microtools.cs.lol.loader.resources;


import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import de.microtools.cs.lol.loader.application.service.DataSetService;
import de.microtools.cs.lol.loader.application.service.DataSetService.DataSet;
import de.microtools.n5.infrastructure.batching.application.spring.register.BatchRestResourceAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Path("/dataset")
@Component
public class DataSetResource implements BatchRestResourceAware {

   private final static Logger logger = LoggerFactory.getLogger(DataSetResource.class);

   @Autowired
   private DataSetService dataSetService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response listDataSets() {
      logger.debug(".listDataSets: received get request.");
      return Response.ok(dataSetService.getDataSets()).build();
   }

   @GET
   @Path("/{setName}")
   @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
   public Response getDataSet(
         @PathParam("setName") String setName,
         @DefaultValue("true") @QueryParam("updateStichtag") boolean updateStichtag) {
      if (logger.isDebugEnabled()) {
         logger.debug(".getDataSet: received get request with parameter setName : {0}, updateStichtag {1}.", setName, updateStichtag);
      }
      DataSet dataSet = dataSetService.getDataSet(setName, updateStichtag);
      return Response
               .ok(dataSet.getContent(), MediaType.APPLICATION_OCTET_STREAM)
               .header("content-disposition", "attachment; filename = " + dataSet.getFileName())
               .build();
   }

}

/*
 * @File: LolValidationHandler.java
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
package de.microtools.cs.lol.loader.application.validator;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
/**
 * Validation Handler for logging of validation errors
 *
 * @author KostikX
 *
 */
public class LolValidationHandler implements ValidationEventHandler {

   private static final Logger logger = LoggerFactory.getLogger(LolValidationHandler.class);

   @Override
   public boolean handleEvent(ValidationEvent event) {
     logger.error(".handleEvent: Lol file schema validation failed: {0}", event.getMessage());
     return false;
   }
}

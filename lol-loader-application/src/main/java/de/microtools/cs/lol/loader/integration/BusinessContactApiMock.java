package de.microtools.cs.lol.loader.integration;

import de.microtools.n5.core.businesscontact.domain.BusinessContact;
import de.microtools.n5.core.party.api.BusinessContactAPI;

public class BusinessContactApiMock extends BusinessContactAPI {

   @Override
   public boolean isBusinessContactExists(String crefoNr) {
      return false;
   }

   @Override
   public boolean saveBusinessContact(BusinessContact bc) {
      return true;
   }

   @Override
   public boolean ping() {
      return true;
   }
}

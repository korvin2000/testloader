/*
 * @File: LolMappingMock.java
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
package de.microtools.cs.lol.loader.integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.microtools.cs.lol.mapping.api.LolMappingAPI;
import de.microtools.cs.lol.mapping.model.CompositeKey;
import de.microtools.cs.lol.mapping.model.CsClient;
import de.microtools.cs.lol.mapping.model.DoubleKeyEntry;
import de.microtools.cs.lol.mapping.model.MappingEntry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LolMappingMock implements LolMappingAPI {

   @Override
   public String ping() {
      return "pong";
   }

   @Override
   public List<Long> deleteUIDs(List<Long> arg0) {
      return null;
   }

   @Override
   public List<MappingEntry> getClientMappings(List<Long> arg0) {
      return ImmutableList.of(new MappingEntry(new CsClient(1L, "TestClient", "TestClientImportUser")));
   }

   @Override
   public List<MappingEntry> getClientMappings(List<Long> uids, boolean thin) {
   return getClientMappings(uids);
   }

   @Override
   public List<MappingEntry> getClientMappings(int arg0, int arg1, String arg2, Map<String, String> filter) {
      // get all client mappings
      if (filter == null) {
         MappingEntry entry1 = new MappingEntry(new CsClient(1L, "TestClient1", "TestClientImportUser1"));
         entry1.setCompositeId(new CompositeKey(ImmutableMap.of("mitgliedsnummer", "mt1", "mandant", "ma1", "buchungskreis", "b1")));
         entry1.setUid(1000L);

         MappingEntry entry2 = new MappingEntry(new CsClient(1L, "TestClient2", "TestClientImportUser2"));
         entry2.setCompositeId(new CompositeKey(ImmutableMap.of("mitgliedsnummer", "mt2", "mandant", "ma2", "buchungskreis", "b2")));
         entry2.setUid(2000L);

         MappingEntry entry3 = new MappingEntry(new CsClient(1L, "TestClient3", "TestClientImportUser3"));
         entry3.setCompositeId(new CompositeKey(ImmutableMap.of("mitgliedsnummer", "mt3", "mandant", "ma3", "buchungskreis", "b3")));
         entry3.setUid(3000L);

         return ImmutableList.of(entry1, entry2, entry3);
      } else {
         return null;
      }
   }

   @Override
   public List<CsClient> getClients() {
      return null;
   }

   @Override
   public List<String> getRelevantKeys() {
      return ImmutableList.of("mitgliedsnummer", "mandant", "buchungskreis");
   }

   @Override
   public List<DoubleKeyEntry> getUIDs() {
      return null;
   }

   @Override
   public List<Long> getUIDs(String arg0) {
      return null;
   }

   @Override
   public List<Long> mapClient(String arg0, List<Long> arg1) {
      return null;
   }

   @Override
   public List<DoubleKeyEntry> putUIDs(List<CompositeKey> arg0) {
      return Collections.singletonList(new DoubleKeyEntry(null, 1000L));
   }

   @Override
   public void setAccessToken(String arg0) {
   }

   @Override public List<String> getMandanten(Long clientId) {

      return Collections.emptyList();
   }

   @Override
   public List<Long> unmapClient(List<Long> arg0) {
      return null;
   }

   @Override
   public void reloadMappings() {
      //do nothing
   }

}

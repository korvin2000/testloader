package de.microtools.cs.lol.loader;

import de.microtools.cs.lol.loader.application.domain.LolImportData;
import de.microtools.cs.lol.loader.application.util.LolUtils;
import de.microtools.n5.core.businesscontact.domain.*;
import de.microtools.n5.core.businesscontact.domain.Identifier.IdentifierClass;
import de.microtools.n5.core.businesscontact.domain.NamePart.FieldType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LolUtilsTest {


   @Test
   public void testToAddressNull() throws Exception {
      assertThat("null delivers not null", LolUtils.toAddress(null), is(nullValue()));
   }

   @Test
   public void testToAddressEmpty() throws Exception {
      Address address = LolUtils.toAddress(new LolImportData());
      assertThat("address is null", address, not(nullValue()));
      assertThat("address type is not null", address.getAddressType(), is(nullValue()));
      assertThat("address zip is not null", address.getZip(), is(nullValue()));
      assertThat("address city is not null", address.getCity(), is(nullValue()));
      assertThat("address country is not null", address.getCountryCode(), is(nullValue()));
   }

   @Test
   public void testToAddress() throws Exception {
      Address address = LolUtils.toAddress(LolImportData.create().plz("plz").ort("ort").land("land"));
      assertThat("address is null", address, not(nullValue()));
      assertThat("address type is not null", address.getAddressType(), is(nullValue()));
      assertThat("address zip is not null", address.getZip(), is("plz"));
      assertThat("address city is not null", address.getCity(), is("ort"));
      assertThat("address country is not null", address.getCountryCode(), is("land"));
   }

   @Test
   public void testToNameNull() throws Exception {
      assertThat("null delivers not null", LolUtils.toName(null), is(nullValue()));
   }

   @Test
   public void testToNameEmpty() throws Exception {
      Name name = LolUtils.toName(new LolImportData());
      assertThat("name is null", name, not(nullValue()));
      assertThat("name type is not null", name.getNameType(), is(nullValue()));
      assertThat("name set is not empty", name.getNameSet(), is(empty()));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testToNameAll() throws Exception {
      LolImportData lolData = LolImportData.create().name1("name1").name2("name2").name3("name3").name4("name4");
      Name name = LolUtils.toName(lolData);
      assertThat("name is null", name, not(nullValue()));
      assertThat("name type is not null", name.getNameType(), is(nullValue()));
      assertThat("name set size is not 4", name.getNameSet().size(), is(4));
      assertThat("name parts not as expected",
            name.getNameSet(),
            containsInAnyOrder(
                        new NamePartMatcher("name1", FieldType.Name, 1),
                        new NamePartMatcher("name2", FieldType.Name, 2),
                        new NamePartMatcher("name3", FieldType.Name, 3),
                        new NamePartMatcher("name4", FieldType.Name, 4)
            ));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testToName() throws Exception {
      LolImportData lolData = LolImportData.create().name1("name1").name2("name2");
      Name name = LolUtils.toName(lolData);
      assertThat("name is null", name, not(nullValue()));
      assertThat("name type is not null", name.getNameType(), is(nullValue()));
      assertThat("name set size is not 4", name.getNameSet().size(), is(2));
      assertThat("name parts not as expected",
            name.getNameSet(),
            containsInAnyOrder(
                        new NamePartMatcher("name1", FieldType.Name, 1),
                        new NamePartMatcher("name2", FieldType.Name, 2)
            ));
   }


   @Test
   public void testToBusinessContactNull() throws Exception {
      assertThat("null delivers not null", LolUtils.toBusinessContact(null), is(nullValue()));
   }

   @Test
   public void testToBusinessContactEmpty() throws Exception {
      BusinessContact businessContact = LolUtils.toBusinessContact(new LolImportData());
      assertThat("businessContact is null", businessContact, not(nullValue()));
      assertThat("businessContact name is not null", businessContact.getName(), not(nullValue()));
      assertThat("businessContact name is not null", businessContact.getName().getNameSet(), is(empty()));
      assertThat("businessContact identifiers size is not 1", businessContact.getIdentifiers().size(), is(1));
      Identifier identifier = businessContact.getIdentifiers().get(0);
      assertThat("businessContact identifier is null", identifier, not(nullValue()));
      assertThat("businessContact identifier type is not crefoNr", identifier.getIdentifierType(), is(IdentifierClass.CrefoNo));
      assertThat("businessContact identifieris not null", identifier.getIdentifier(), is(nullValue()));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testToBusinessContactAll() throws Exception {
      LolImportData lolData =
            LolImportData
               .create()
               .name1("name1")
               .name2("name2")
               .name3("name3")
               .name4("name4")
               .creforNr("crefoNr")
               .ort("ort")
               .plz("plz")
               .land("land");

      BusinessContact businessContact = LolUtils.toBusinessContact(lolData);
      assertThat("businessContact is null", businessContact, not(nullValue()));
      // test name
      assertThat("businessContact is  null", businessContact.getName(), not(nullValue()));
      assertThat("name type is not null", businessContact.getName().getNameType(), is(nullValue()));
      assertThat("name set size is not 4", businessContact.getName().getNameSet().size(), is(4));
      assertThat("name parts not as expected",
            businessContact.getName().getNameSet(),
            containsInAnyOrder(
                        new NamePartMatcher("name1", FieldType.Name, 1),
                        new NamePartMatcher("name2", FieldType.Name, 2),
                        new NamePartMatcher("name3", FieldType.Name, 3),
                        new NamePartMatcher("name4", FieldType.Name, 4)
            ));

      // test address
      assertThat("businessContact addresses is null", businessContact.getAddresses(), not(nullValue()));
      assertThat("businessContact addresses size is not 1", businessContact.getAddresses().size(), is(1));
      Address address = businessContact.getAddresses().get(0);
      assertThat("businessContact address is null", address, not(nullValue()));
      assertThat("businessContact address type is null", address.getAddressType(), is(nullValue()));
      assertThat("businessContact address zip is not zip", address.getZip(), is("plz"));
      assertThat("businessContact address city is not ort", address.getCity(), is("ort"));
      assertThat("businessContact address country is not land", address.getCountryCode(), is("land"));


      // test identifier
      assertThat("businessContact identifiers is null", businessContact.getIdentifiers(), not(nullValue()));
      assertThat("businessContact identifiers size is not 1", businessContact.getIdentifiers().size(), is(1));
      Identifier identifier = businessContact.getIdentifiers().get(0);
      assertThat("businessContact identifier is null", identifier, not(nullValue()));
      assertThat("businessContact identifier type is not crefoNr", identifier.getIdentifierType(), is(IdentifierClass.CrefoNo));
      assertThat("businessContact identifieris not crefoNr", identifier.getIdentifier(), is("crefoNr"));
   }


   private static class NamePartMatcher implements Matcher<NamePart> {
      String namePartValue;
      FieldType fieldType;
      int ordinal;

      public NamePartMatcher(String namePartValue, FieldType fieldType, int ordinal) {
         this.namePartValue = namePartValue;
         this.fieldType = fieldType;
         this.ordinal = ordinal;
      }

      @Override
      public void describeTo(Description description) {

      }
      @Override
      public boolean matches(Object item) {
         if (item == null || ! (item instanceof NamePart)) {
            return false;
         }
         NamePart namePart = (NamePart)item;
         return namePart.getFieldType().equals(fieldType) &&
               namePart.getNameValue().equals(namePartValue) &&
               namePart.getOrdinal() == ordinal;
      }

      @Override
      public void describeMismatch(Object item, Description mismatchDescription) {
      }

      @Override
      public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
      }
   }

}

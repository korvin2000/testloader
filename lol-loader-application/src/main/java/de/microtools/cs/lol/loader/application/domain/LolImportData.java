/*
 * @File: Lol2ImportData.java
 *
 * COpyright (c) 2013 test microtools.
 * Hellersbergstr. 12; 41460 Neuss; Germany.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 *
 *
 */
package de.microtools.cs.lol.loader.application.domain;

import de.microtools.n5.infrastructure.batching.application.spring.validation.ItemFieldValidationAware;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LolImportData implements Serializable, ItemFieldValidationAware {
   private static final long serialVersionUID = -6954816934116184637L;
   private Long id;
   private Long mappingId;
   @Getter(AccessLevel.NONE)
   @Setter(AccessLevel.NONE)
   private Date stichtag;
   @Getter(AccessLevel.NONE)
   @Setter(AccessLevel.NONE)
   private Date importdatum;
   private String mitgliedsnummer;
   private String mandant;
   private String buchungskreis;
   private String bobiknummer;
   private String debitornummer;
   private String name1;
   private String name2;
   private String name3;
   private String name4;
   private String plz;
   private String ort;
   private String land;
   private String inso;
   private String branchenBezeichnung;
   private String branchenCode;
   private String branchenArt;
   private String branchenLand;
   private BigDecimal lolIndex;
   private BigDecimal anzahlBelegeLieferant2;
   private BigDecimal anzahlBelegePoolOhneLieferant2;
   private BigDecimal anzahlBelegeLieferant4;
   private BigDecimal anzahlBelegePoolOhneLieferant4;
   private BigDecimal anzahlBelegeLieferant8;
   private BigDecimal anzahlBelegePoolOhneLieferant8;
   private BigDecimal anzahlBelegeLieferant12;
   private BigDecimal anzahlBelegePoolOhneLieferant12;
   private BigDecimal anzahlBelegeBranche;
   private BigDecimal anzahlCrefosBranche;
   private BigDecimal betragLieferant2;
   private String betragLieferant2W;
   private BigDecimal betragPoolOhneLieferant2;
   private String betragPoolOhneLieferant2W;
   private BigDecimal betragLieferant4;
   private String betragLieferant4W;
   private BigDecimal betragPoolOhneLieferant4;
   private String betragPoolOhneLieferant4W;
   private BigDecimal betragLieferant8;
   private String betragLieferant8W;
   private BigDecimal betragPoolOhneLieferant8;
   private String betragPoolOhneLieferant8W;
   private BigDecimal betragLieferant12;
   private String betragLieferant12W;
   private BigDecimal betragPoolOhneLieferant12;
   private String betragPoolOhneLieferant12W;
   private BigDecimal betragsvolumenOp;
   private String betragsvolumenOpW;
   private BigDecimal betragBranche;
   private String betragBrancheW;
   private BigDecimal tageSollLieferant2;
   private BigDecimal tageSollPoolOhneLieferant2;
   private BigDecimal tageIstLieferant2;
   private BigDecimal tageIstPoolOhneLieferant2;
   private BigDecimal tageDiffLieferant2;
   private BigDecimal tageDiffPoolOhneLieferant2;
   private BigDecimal tageSollLieferant4;
   private BigDecimal tageSollPoolOhneLieferant4;
   private BigDecimal tageIstLieferant4;
   private BigDecimal tageIstPoolOhneLieferant4;
   private BigDecimal tageDiffLieferant4;
   private BigDecimal tageDiffPoolOhneLieferant4;
   private BigDecimal tageSollLieferant8;
   private BigDecimal tageSollPoolOhneLieferant8;
   private BigDecimal tageIstLieferant8;
   private BigDecimal tageIstPoolOhneLieferant8;
   private BigDecimal tageDiffLieferant8;
   private BigDecimal tageDiffPoolOhneLieferant8;
   private BigDecimal tageSollLieferant12;
   private BigDecimal tageSollPoolOhneLieferant12;
   private BigDecimal tageIstLieferant12;
   private BigDecimal tageIstPoolOhneLieferant12;
   private BigDecimal tageDiffLieferant12;
   private BigDecimal tageDiffPoolOhneLieferant12;
   private BigDecimal tageSollBranche;
   private BigDecimal tageIstBranche;
   private BigDecimal tageDiffBranche;
   private String filename;
   private Integer businessContact = 0;
   private String itemFieldTValidationErrors;
   private transient Resource resource;

   public LolImportData(Date importdatum) {
      super();
      this.importdatum = importdatum != null ? new Date(importdatum.getTime()) : null;
   }

   public LolImportData() {
      super();
   }

   public Date getStichtag() {
      return stichtag != null ? new Date(stichtag.getTime()) : null;
   }

   public Date getImportdatum() {
      return importdatum != null ? new Date(importdatum.getTime()) : null;
   }

   public void setStichtag(Date stichtag) {
      this.stichtag = stichtag != null ? new Date(stichtag.getTime()) : null;
   }

   public void setImportdatum(Date importdatum) {
      this.importdatum = importdatum != null ? new Date(importdatum.getTime()) : null;
   }

   @Override
   public void addItemFieldValidationError(String error) {
      if (StringUtils.isNotBlank(error)) {
         String errors = getItemFieldValidationErrors();
         if (StringUtils.isNotBlank(errors)) {
            errors = StringUtils.join(new String[] { errors, error }, ". --- ");
         } else {
            errors = "Field validation failed: " + error;
         }
         itemFieldTValidationErrors = errors;
      }
   }

   @Override
   public String getItemFieldValidationErrors() {
      return null;
   }

   public String toStringForBusinessContact() {
      return
            new ToStringBuilder(ToStringStyle.SIMPLE_STYLE)
                  .append(bobiknummer)
                  .append(name1)
                  .append(name2)
                  .append(name3)
                  .append(name4)
                  .append(plz)
                  .append(ort)
                  .append(land)
                  .toString();
   }

   public static LolImportData create() {
      return new LolImportData();
   }

   public LolImportData name1(String name1) {
      this.setName1(name1);
      return this;
   }

   public LolImportData name2(String name2) {
      this.setName2(name2);
      return this;
   }

   public LolImportData name3(String name3) {
      this.setName3(name3);
      return this;
   }

   public LolImportData name4(String name4) {
      this.setName4(name4);
      return this;
   }

   public LolImportData plz(String plz) {
      this.setPlz(plz);
      return this;
   }

   public LolImportData ort(String ort) {
      this.setOrt(ort);
      return this;
   }
   public LolImportData land(String land) {
      this.setLand(land);
      return this;
   }
   public LolImportData creforNr(String crefoNr) {
      this.setBobiknummer(crefoNr);
      return this;
   }

}

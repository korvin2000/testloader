//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.18 um 09:24:01 AM CET 
//


package de.microtools.cs.lol.loader.application.domain;

import lombok.ToString;
import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für LolDebitorTypeErw complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LolDebitorTypeErw">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mitgliedsnummer" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}MitgliedsnummerType"/>
 *         &lt;element name="mandant" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}MandantType"/>
 *         &lt;element name="buchungskreis" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BuchungskreisType"/>
 *         &lt;element name="bobiknummer" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BobiknummerType"/>
 *         &lt;element name="debitornummer" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}DebitornummerType"/>
 *         &lt;element name="name" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}NameType"/>
 *         &lt;element name="plz" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}PlzType"/>
 *         &lt;element name="ort" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}OrtType"/>
 *         &lt;element name="land" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}LandType"/>
 *         &lt;element name="anzahlBelegeLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegePoolOhneLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="betragPoolOhneLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="tageSollLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageSollPoolOhneLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstPoolOhneLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffPoolOhneLieferant12" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegeLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegePoolOhneLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="betragPoolOhneLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="tageSollLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageSollPoolOhneLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstPoolOhneLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffPoolOhneLieferant8" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegeLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegePoolOhneLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="betragPoolOhneLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="tageSollLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageSollPoolOhneLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstPoolOhneLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffPoolOhneLieferant4" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegeLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegePoolOhneLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="betragPoolOhneLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="tageSollLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageSollPoolOhneLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstPoolOhneLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffPoolOhneLieferant2" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragsvolumenOP" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="lolIndex" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}LOLIndexType" minOccurs="0"/>
 *         &lt;element name="inso" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}InsoType" minOccurs="0"/>
 *         &lt;element name="branchenBezeichnung" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BranchenBezeichnungType" minOccurs="0"/>
 *         &lt;element name="branchenCode" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BranchenCodeType" minOccurs="0"/>
 *         &lt;element name="branchenArt" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BranchenArtType" minOccurs="0"/>
 *         &lt;element name="branchenLand" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}LandType" minOccurs="0"/>
 *         &lt;element name="anzahlBelegeBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="betragBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragType" minOccurs="0"/>
 *         &lt;element name="tageSollBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageIstBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="tageDiffBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *         &lt;element name="anzahlCrefosBranche" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}AnzahlType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LolDebitorTypeErw", propOrder = {
    "mitgliedsnummer",
    "mandant",
    "buchungskreis",
    "bobiknummer",
    "debitornummer",
    "name",
    "plz",
    "ort",
    "land",
    "anzahlBelegeLieferant12",
    "anzahlBelegePoolOhneLieferant12",
    "betragLieferant12",
    "betragPoolOhneLieferant12",
    "tageSollLieferant12",
    "tageSollPoolOhneLieferant12",
    "tageIstLieferant12",
    "tageIstPoolOhneLieferant12",
    "tageDiffLieferant12",
    "tageDiffPoolOhneLieferant12",
    "anzahlBelegeLieferant8",
    "anzahlBelegePoolOhneLieferant8",
    "betragLieferant8",
    "betragPoolOhneLieferant8",
    "tageSollLieferant8",
    "tageSollPoolOhneLieferant8",
    "tageIstLieferant8",
    "tageIstPoolOhneLieferant8",
    "tageDiffLieferant8",
    "tageDiffPoolOhneLieferant8",
    "anzahlBelegeLieferant4",
    "anzahlBelegePoolOhneLieferant4",
    "betragLieferant4",
    "betragPoolOhneLieferant4",
    "tageSollLieferant4",
    "tageSollPoolOhneLieferant4",
    "tageIstLieferant4",
    "tageIstPoolOhneLieferant4",
    "tageDiffLieferant4",
    "tageDiffPoolOhneLieferant4",
    "anzahlBelegeLieferant2",
    "anzahlBelegePoolOhneLieferant2",
    "betragLieferant2",
    "betragPoolOhneLieferant2",
    "tageSollLieferant2",
    "tageSollPoolOhneLieferant2",
    "tageIstLieferant2",
    "tageIstPoolOhneLieferant2",
    "tageDiffLieferant2",
    "tageDiffPoolOhneLieferant2",
    "betragsvolumenOP",
    "lolIndex",
    "inso",
    "branchenBezeichnung",
    "branchenCode",
    "branchenArt",
    "branchenLand",
    "anzahlBelegeBranche",
    "betragBranche",
    "tageSollBranche",
    "tageIstBranche",
    "tageDiffBranche",
    "anzahlCrefosBranche"
})
@XmlRootElement(name = "lolDebitor")
@ToString
public class LolDebitorTypeErw implements ResourceAware {

    @XmlElement(required = true)
    protected String mitgliedsnummer;
    @XmlElement(required = true)
    protected String mandant;
    @XmlElement(required = true)
    protected String buchungskreis;
    @XmlElement(required = true)
    protected String bobiknummer;
    @XmlElement(required = true)
    protected String debitornummer;
    @XmlElement(required = true)
    protected NameType name;
    @XmlElement(required = true)
    protected String plz;
    @XmlElement(required = true)
    protected String ort;
    @XmlElement(required = true)
    protected String land;
    protected BigDecimal anzahlBelegeLieferant12;
    protected BigDecimal anzahlBelegePoolOhneLieferant12;
    protected BetragType betragLieferant12;
    protected BetragType betragPoolOhneLieferant12;
    protected BigDecimal tageSollLieferant12;
    protected BigDecimal tageSollPoolOhneLieferant12;
    protected BigDecimal tageIstLieferant12;
    protected BigDecimal tageIstPoolOhneLieferant12;
    protected BigDecimal tageDiffLieferant12;
    protected BigDecimal tageDiffPoolOhneLieferant12;
    protected BigDecimal anzahlBelegeLieferant8;
    protected BigDecimal anzahlBelegePoolOhneLieferant8;
    protected BetragType betragLieferant8;
    protected BetragType betragPoolOhneLieferant8;
    protected BigDecimal tageSollLieferant8;
    protected BigDecimal tageSollPoolOhneLieferant8;
    protected BigDecimal tageIstLieferant8;
    protected BigDecimal tageIstPoolOhneLieferant8;
    protected BigDecimal tageDiffLieferant8;
    protected BigDecimal tageDiffPoolOhneLieferant8;
    protected BigDecimal anzahlBelegeLieferant4;
    protected BigDecimal anzahlBelegePoolOhneLieferant4;
    protected BetragType betragLieferant4;
    protected BetragType betragPoolOhneLieferant4;
    protected BigDecimal tageSollLieferant4;
    protected BigDecimal tageSollPoolOhneLieferant4;
    protected BigDecimal tageIstLieferant4;
    protected BigDecimal tageIstPoolOhneLieferant4;
    protected BigDecimal tageDiffLieferant4;
    protected BigDecimal tageDiffPoolOhneLieferant4;
    protected BigDecimal anzahlBelegeLieferant2;
    protected BigDecimal anzahlBelegePoolOhneLieferant2;
    protected BetragType betragLieferant2;
    protected BetragType betragPoolOhneLieferant2;
    protected BigDecimal tageSollLieferant2;
    protected BigDecimal tageSollPoolOhneLieferant2;
    protected BigDecimal tageIstLieferant2;
    protected BigDecimal tageIstPoolOhneLieferant2;
    protected BigDecimal tageDiffLieferant2;
    protected BigDecimal tageDiffPoolOhneLieferant2;
    protected BetragType betragsvolumenOP;
    protected BigDecimal lolIndex;
    protected String inso;
    protected String branchenBezeichnung;
    protected String branchenCode;
    protected String branchenArt;
    protected String branchenLand;
    protected BigDecimal anzahlBelegeBranche;
    protected BetragType betragBranche;
    protected BigDecimal tageSollBranche;
    protected BigDecimal tageIstBranche;
    protected BigDecimal tageDiffBranche;
    protected BigDecimal anzahlCrefosBranche;

    @XmlTransient
    protected Resource resource;
    @XmlTransient
    protected Date reportDate;


    /**
     * Ruft den Wert der mitgliedsnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMitgliedsnummer() {
        return mitgliedsnummer;
    }

    /**
     * Legt den Wert der mitgliedsnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMitgliedsnummer(String value) {
        this.mitgliedsnummer = value;
    }

    /**
     * Ruft den Wert der mandant-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMandant() {
        return mandant;
    }

    /**
     * Legt den Wert der mandant-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMandant(String value) {
        this.mandant = value;
    }

    /**
     * Ruft den Wert der buchungskreis-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBuchungskreis() {
        return buchungskreis;
    }

    /**
     * Legt den Wert der buchungskreis-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBuchungskreis(String value) {
        this.buchungskreis = value;
    }

    /**
     * Ruft den Wert der bobiknummer-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBobiknummer() {
        return bobiknummer;
    }

    /**
     * Legt den Wert der bobiknummer-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBobiknummer(String value) {
        this.bobiknummer = value;
    }

    /**
     * Ruft den Wert der debitornummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDebitornummer() {
        return debitornummer;
    }

    /**
     * Legt den Wert der debitornummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDebitornummer(String value) {
        this.debitornummer = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NameType }
     *     
     */
    public NameType getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NameType }
     *     
     */
    public void setName(NameType value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der plz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlz() {
        return plz;
    }

    /**
     * Legt den Wert der plz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlz(String value) {
        this.plz = value;
    }

    /**
     * Ruft den Wert der ort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrt() {
        return ort;
    }

    /**
     * Legt den Wert der ort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrt(String value) {
        this.ort = value;
    }

    /**
     * Ruft den Wert der land-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLand() {
        return land;
    }

    /**
     * Legt den Wert der land-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLand(String value) {
        this.land = value;
    }

    /**
     * Ruft den Wert der anzahlBelegeLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegeLieferant12() {
        return anzahlBelegeLieferant12;
    }

    /**
     * Legt den Wert der anzahlBelegeLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegeLieferant12(BigDecimal value) {
        this.anzahlBelegeLieferant12 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegePoolOhneLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegePoolOhneLieferant12() {
        return anzahlBelegePoolOhneLieferant12;
    }

    /**
     * Legt den Wert der anzahlBelegePoolOhneLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegePoolOhneLieferant12(BigDecimal value) {
        this.anzahlBelegePoolOhneLieferant12 = value;
    }

    /**
     * Ruft den Wert der betragLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragLieferant12() {
        return betragLieferant12;
    }

    /**
     * Legt den Wert der betragLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragLieferant12(BetragType value) {
        this.betragLieferant12 = value;
    }

    /**
     * Ruft den Wert der betragPoolOhneLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragPoolOhneLieferant12() {
        return betragPoolOhneLieferant12;
    }

    /**
     * Legt den Wert der betragPoolOhneLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragPoolOhneLieferant12(BetragType value) {
        this.betragPoolOhneLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageSollLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollLieferant12() {
        return tageSollLieferant12;
    }

    /**
     * Legt den Wert der tageSollLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollLieferant12(BigDecimal value) {
        this.tageSollLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageSollPoolOhneLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollPoolOhneLieferant12() {
        return tageSollPoolOhneLieferant12;
    }

    /**
     * Legt den Wert der tageSollPoolOhneLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollPoolOhneLieferant12(BigDecimal value) {
        this.tageSollPoolOhneLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageIstLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstLieferant12() {
        return tageIstLieferant12;
    }

    /**
     * Legt den Wert der tageIstLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstLieferant12(BigDecimal value) {
        this.tageIstLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageIstPoolOhneLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstPoolOhneLieferant12() {
        return tageIstPoolOhneLieferant12;
    }

    /**
     * Legt den Wert der tageIstPoolOhneLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstPoolOhneLieferant12(BigDecimal value) {
        this.tageIstPoolOhneLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageDiffLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffLieferant12() {
        return tageDiffLieferant12;
    }

    /**
     * Legt den Wert der tageDiffLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffLieferant12(BigDecimal value) {
        this.tageDiffLieferant12 = value;
    }

    /**
     * Ruft den Wert der tageDiffPoolOhneLieferant12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffPoolOhneLieferant12() {
        return tageDiffPoolOhneLieferant12;
    }

    /**
     * Legt den Wert der tageDiffPoolOhneLieferant12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffPoolOhneLieferant12(BigDecimal value) {
        this.tageDiffPoolOhneLieferant12 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegeLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegeLieferant8() {
        return anzahlBelegeLieferant8;
    }

    /**
     * Legt den Wert der anzahlBelegeLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegeLieferant8(BigDecimal value) {
        this.anzahlBelegeLieferant8 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegePoolOhneLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegePoolOhneLieferant8() {
        return anzahlBelegePoolOhneLieferant8;
    }

    /**
     * Legt den Wert der anzahlBelegePoolOhneLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegePoolOhneLieferant8(BigDecimal value) {
        this.anzahlBelegePoolOhneLieferant8 = value;
    }

    /**
     * Ruft den Wert der betragLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragLieferant8() {
        return betragLieferant8;
    }

    /**
     * Legt den Wert der betragLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragLieferant8(BetragType value) {
        this.betragLieferant8 = value;
    }

    /**
     * Ruft den Wert der betragPoolOhneLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragPoolOhneLieferant8() {
        return betragPoolOhneLieferant8;
    }

    /**
     * Legt den Wert der betragPoolOhneLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragPoolOhneLieferant8(BetragType value) {
        this.betragPoolOhneLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageSollLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollLieferant8() {
        return tageSollLieferant8;
    }

    /**
     * Legt den Wert der tageSollLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollLieferant8(BigDecimal value) {
        this.tageSollLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageSollPoolOhneLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollPoolOhneLieferant8() {
        return tageSollPoolOhneLieferant8;
    }

    /**
     * Legt den Wert der tageSollPoolOhneLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollPoolOhneLieferant8(BigDecimal value) {
        this.tageSollPoolOhneLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageIstLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstLieferant8() {
        return tageIstLieferant8;
    }

    /**
     * Legt den Wert der tageIstLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstLieferant8(BigDecimal value) {
        this.tageIstLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageIstPoolOhneLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstPoolOhneLieferant8() {
        return tageIstPoolOhneLieferant8;
    }

    /**
     * Legt den Wert der tageIstPoolOhneLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstPoolOhneLieferant8(BigDecimal value) {
        this.tageIstPoolOhneLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageDiffLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffLieferant8() {
        return tageDiffLieferant8;
    }

    /**
     * Legt den Wert der tageDiffLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffLieferant8(BigDecimal value) {
        this.tageDiffLieferant8 = value;
    }

    /**
     * Ruft den Wert der tageDiffPoolOhneLieferant8-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffPoolOhneLieferant8() {
        return tageDiffPoolOhneLieferant8;
    }

    /**
     * Legt den Wert der tageDiffPoolOhneLieferant8-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffPoolOhneLieferant8(BigDecimal value) {
        this.tageDiffPoolOhneLieferant8 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegeLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegeLieferant4() {
        return anzahlBelegeLieferant4;
    }

    /**
     * Legt den Wert der anzahlBelegeLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegeLieferant4(BigDecimal value) {
        this.anzahlBelegeLieferant4 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegePoolOhneLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegePoolOhneLieferant4() {
        return anzahlBelegePoolOhneLieferant4;
    }

    /**
     * Legt den Wert der anzahlBelegePoolOhneLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegePoolOhneLieferant4(BigDecimal value) {
        this.anzahlBelegePoolOhneLieferant4 = value;
    }

    /**
     * Ruft den Wert der betragLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragLieferant4() {
        return betragLieferant4;
    }

    /**
     * Legt den Wert der betragLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragLieferant4(BetragType value) {
        this.betragLieferant4 = value;
    }

    /**
     * Ruft den Wert der betragPoolOhneLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragPoolOhneLieferant4() {
        return betragPoolOhneLieferant4;
    }

    /**
     * Legt den Wert der betragPoolOhneLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragPoolOhneLieferant4(BetragType value) {
        this.betragPoolOhneLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageSollLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollLieferant4() {
        return tageSollLieferant4;
    }

    /**
     * Legt den Wert der tageSollLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollLieferant4(BigDecimal value) {
        this.tageSollLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageSollPoolOhneLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollPoolOhneLieferant4() {
        return tageSollPoolOhneLieferant4;
    }

    /**
     * Legt den Wert der tageSollPoolOhneLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollPoolOhneLieferant4(BigDecimal value) {
        this.tageSollPoolOhneLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageIstLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstLieferant4() {
        return tageIstLieferant4;
    }

    /**
     * Legt den Wert der tageIstLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstLieferant4(BigDecimal value) {
        this.tageIstLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageIstPoolOhneLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstPoolOhneLieferant4() {
        return tageIstPoolOhneLieferant4;
    }

    /**
     * Legt den Wert der tageIstPoolOhneLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstPoolOhneLieferant4(BigDecimal value) {
        this.tageIstPoolOhneLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageDiffLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffLieferant4() {
        return tageDiffLieferant4;
    }

    /**
     * Legt den Wert der tageDiffLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffLieferant4(BigDecimal value) {
        this.tageDiffLieferant4 = value;
    }

    /**
     * Ruft den Wert der tageDiffPoolOhneLieferant4-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffPoolOhneLieferant4() {
        return tageDiffPoolOhneLieferant4;
    }

    /**
     * Legt den Wert der tageDiffPoolOhneLieferant4-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffPoolOhneLieferant4(BigDecimal value) {
        this.tageDiffPoolOhneLieferant4 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegeLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegeLieferant2() {
        return anzahlBelegeLieferant2;
    }

    /**
     * Legt den Wert der anzahlBelegeLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegeLieferant2(BigDecimal value) {
        this.anzahlBelegeLieferant2 = value;
    }

    /**
     * Ruft den Wert der anzahlBelegePoolOhneLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegePoolOhneLieferant2() {
        return anzahlBelegePoolOhneLieferant2;
    }

    /**
     * Legt den Wert der anzahlBelegePoolOhneLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegePoolOhneLieferant2(BigDecimal value) {
        this.anzahlBelegePoolOhneLieferant2 = value;
    }

    /**
     * Ruft den Wert der betragLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragLieferant2() {
        return betragLieferant2;
    }

    /**
     * Legt den Wert der betragLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragLieferant2(BetragType value) {
        this.betragLieferant2 = value;
    }

    /**
     * Ruft den Wert der betragPoolOhneLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragPoolOhneLieferant2() {
        return betragPoolOhneLieferant2;
    }

    /**
     * Legt den Wert der betragPoolOhneLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragPoolOhneLieferant2(BetragType value) {
        this.betragPoolOhneLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageSollLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollLieferant2() {
        return tageSollLieferant2;
    }

    /**
     * Legt den Wert der tageSollLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollLieferant2(BigDecimal value) {
        this.tageSollLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageSollPoolOhneLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollPoolOhneLieferant2() {
        return tageSollPoolOhneLieferant2;
    }

    /**
     * Legt den Wert der tageSollPoolOhneLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollPoolOhneLieferant2(BigDecimal value) {
        this.tageSollPoolOhneLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageIstLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstLieferant2() {
        return tageIstLieferant2;
    }

    /**
     * Legt den Wert der tageIstLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstLieferant2(BigDecimal value) {
        this.tageIstLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageIstPoolOhneLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstPoolOhneLieferant2() {
        return tageIstPoolOhneLieferant2;
    }

    /**
     * Legt den Wert der tageIstPoolOhneLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstPoolOhneLieferant2(BigDecimal value) {
        this.tageIstPoolOhneLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageDiffLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffLieferant2() {
        return tageDiffLieferant2;
    }

    /**
     * Legt den Wert der tageDiffLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffLieferant2(BigDecimal value) {
        this.tageDiffLieferant2 = value;
    }

    /**
     * Ruft den Wert der tageDiffPoolOhneLieferant2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffPoolOhneLieferant2() {
        return tageDiffPoolOhneLieferant2;
    }

    /**
     * Legt den Wert der tageDiffPoolOhneLieferant2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffPoolOhneLieferant2(BigDecimal value) {
        this.tageDiffPoolOhneLieferant2 = value;
    }

    /**
     * Ruft den Wert der betragsvolumenOP-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragsvolumenOP() {
        return betragsvolumenOP;
    }

    /**
     * Legt den Wert der betragsvolumenOP-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragsvolumenOP(BetragType value) {
        this.betragsvolumenOP = value;
    }

    /**
     * Ruft den Wert der lolIndex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLolIndex() {
        return lolIndex;
    }

    /**
     * Legt den Wert der lolIndex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLolIndex(BigDecimal value) {
        this.lolIndex = value;
    }

    /**
     * Ruft den Wert der inso-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInso() {
        return inso;
    }

    /**
     * Legt den Wert der inso-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInso(String value) {
        this.inso = value;
    }

    /**
     * Ruft den Wert der branchenBezeichnung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchenBezeichnung() {
        return branchenBezeichnung;
    }

    /**
     * Legt den Wert der branchenBezeichnung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchenBezeichnung(String value) {
        this.branchenBezeichnung = value;
    }

    /**
     * Ruft den Wert der branchenCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchenCode() {
        return branchenCode;
    }

    /**
     * Legt den Wert der branchenCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchenCode(String value) {
        this.branchenCode = value;
    }

    /**
     * Ruft den Wert der branchenArt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchenArt() {
        return branchenArt;
    }

    /**
     * Legt den Wert der branchenArt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchenArt(String value) {
        this.branchenArt = value;
    }

    /**
     * Ruft den Wert der branchenLand-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchenLand() {
        return branchenLand;
    }

    /**
     * Legt den Wert der branchenLand-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchenLand(String value) {
        this.branchenLand = value;
    }

    /**
     * Ruft den Wert der anzahlBelegeBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlBelegeBranche() {
        return anzahlBelegeBranche;
    }

    /**
     * Legt den Wert der anzahlBelegeBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlBelegeBranche(BigDecimal value) {
        this.anzahlBelegeBranche = value;
    }

    /**
     * Ruft den Wert der betragBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragType }
     *     
     */
    public BetragType getBetragBranche() {
        return betragBranche;
    }

    /**
     * Legt den Wert der betragBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragType }
     *     
     */
    public void setBetragBranche(BetragType value) {
        this.betragBranche = value;
    }

    /**
     * Ruft den Wert der tageSollBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageSollBranche() {
        return tageSollBranche;
    }

    /**
     * Legt den Wert der tageSollBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageSollBranche(BigDecimal value) {
        this.tageSollBranche = value;
    }

    /**
     * Ruft den Wert der tageIstBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageIstBranche() {
        return tageIstBranche;
    }

    /**
     * Legt den Wert der tageIstBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageIstBranche(BigDecimal value) {
        this.tageIstBranche = value;
    }

    /**
     * Ruft den Wert der tageDiffBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTageDiffBranche() {
        return tageDiffBranche;
    }

    /**
     * Legt den Wert der tageDiffBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTageDiffBranche(BigDecimal value) {
        this.tageDiffBranche = value;
    }

    /**
     * Ruft den Wert der anzahlCrefosBranche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAnzahlCrefosBranche() {
        return anzahlCrefosBranche;
    }

    /**
     * Legt den Wert der anzahlCrefosBranche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAnzahlCrefosBranche(BigDecimal value) {
        this.anzahlCrefosBranche = value;
    }

    @Override
    public void setResource(Resource resource) {
      this.resource = resource;
   }

   public Resource getResource() {
      return resource;
    }
}

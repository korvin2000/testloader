//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.18 um 09:24:01 AM CET 
//


package de.microtools.cs.lol.loader.application.domain;

import lombok.ToString;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="datum" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="lolDebitoren" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="lolDebitor" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}LolDebitorTypeErw" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "datum",
    "lolDebitoren"
})
@XmlRootElement(name = "lolRuecklieferungErw")
@ToString
public class LolRuecklieferungErw {

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar datum;
    protected LolRuecklieferungErw.LolDebitoren lolDebitoren;

    /**
     * Ruft den Wert der datum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDatum() {
        return datum;
    }

    /**
     * Legt den Wert der datum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDatum(XMLGregorianCalendar value) {
        this.datum = value;
    }

    /**
     * Ruft den Wert der lolDebitoren-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LolRuecklieferungErw.LolDebitoren }
     *     
     */
    public LolRuecklieferungErw.LolDebitoren getLolDebitoren() {
        return lolDebitoren;
    }

    /**
     * Legt den Wert der lolDebitoren-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LolRuecklieferungErw.LolDebitoren }
     *     
     */
    public void setLolDebitoren(LolRuecklieferungErw.LolDebitoren value) {
        this.lolDebitoren = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="lolDebitor" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}LolDebitorTypeErw" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "lolDebitor"
    })
    @XmlRootElement(name = "lolDebitoren")
    @ToString
    public static class LolDebitoren {

        protected List<LolDebitorTypeErw> lolDebitor;

        /**
         * Gets the value of the lolDebitor property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the lolDebitor property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLolDebitor().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LolDebitorTypeErw }
         * 
         * 
         */
        public List<LolDebitorTypeErw> getLolDebitor() {
            if (lolDebitor == null) {
                lolDebitor = new ArrayList<LolDebitorTypeErw>();
            }
            return this.lolDebitor;
        }

    }

}

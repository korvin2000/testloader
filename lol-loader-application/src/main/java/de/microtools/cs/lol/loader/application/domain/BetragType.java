//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.18 um 09:24:01 AM CET 
//


package de.microtools.cs.lol.loader.application.domain;

import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java-Klasse für BetragType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BetragType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd>BetragWertType">
 *       &lt;attribute name="waehrung" use="required" type="{http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd}BetragWaehrungType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BetragType", propOrder = {
    "value"
})
@ToString
public class BetragType {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(name = "waehrung", required = true)
    protected BetragWaehrungType waehrung;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Ruft den Wert der waehrung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BetragWaehrungType }
     *     
     */
    public BetragWaehrungType getWaehrung() {
        return waehrung;
    }

    /**
     * Legt den Wert der waehrung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BetragWaehrungType }
     *     
     */
    public void setWaehrung(BetragWaehrungType value) {
        this.waehrung = value;
    }

}

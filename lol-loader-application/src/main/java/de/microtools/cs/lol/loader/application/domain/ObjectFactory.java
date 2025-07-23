//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.18 um 09:24:01 AM CET 
//


package de.microtools.cs.lol.loader.application.domain;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.microtools.cs.lol.loader.application.domain package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.microtools.cs.lol.loader.application.domain
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LolRuecklieferungErw }
     * 
     */
    public LolRuecklieferungErw createLolRuecklieferungErw() {
        return new LolRuecklieferungErw();
    }

    /**
     * Create an instance of {@link LolRuecklieferungErw.LolDebitoren }
     * 
     */
    public LolRuecklieferungErw.LolDebitoren createLolRuecklieferungErwLolDebitoren() {
        return new LolRuecklieferungErw.LolDebitoren();
    }

    /**
     * Create an instance of {@link NameType }
     * 
     */
    public NameType createNameType() {
        return new NameType();
    }

    /**
     * Create an instance of {@link BetragType }
     * 
     */
    public BetragType createBetragType() {
        return new BetragType();
    }

    /**
     * Create an instance of {@link LolDebitorTypeErw }
     * 
     */
    public LolDebitorTypeErw createLolDebitorTypeErw() {
        return new LolDebitorTypeErw();
    }

}

package de.microtools.cs.lol.loader.application.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlRootElement(name = "lolRuecklieferungErw")
@Data
public class LolRuecklieferungErwDatum {

    protected XMLGregorianCalendar datum;
    @XmlTransient
    protected LolRuecklieferungErw.LolDebitoren lolDebitoren;

}

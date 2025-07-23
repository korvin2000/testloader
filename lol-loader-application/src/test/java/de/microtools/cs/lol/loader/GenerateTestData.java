package de.microtools.cs.lol.loader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class GenerateTestData {

    public static void main(String [] args) throws IOException {

        generate();
    }

    public static void generate() throws IOException {
        int lolDebitor = 1;

        for (int i = 1; i <= 20; i++) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version='1.0' encoding='UTF-8'?>\n" +
                    "<lolRuecklieferungErw xmlns=\"http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://xsd.microtools.de/LOL/LOLRuecklieferungSchemaErw-v1.1.xsd LOLRuecklieferungSchemaErw-v1.1.xsd\">\n" +
                    "<datum>2023-01-19T08:24:30</datum>\n" +
                    "<lolDebitoren>\n");
            for (int y = 0; y < 100000; y ++) {
                xml.append("<lolDebitor><mitgliedsnummer>219117110001</mitgliedsnummer><mandant></mandant><buchungskreis></buchungskreis><bobiknummer>" + (10000000 + lolDebitor) + "</bobiknummer><debitornummer>" + lolDebitor + "</debitornummer><name><name1>TEST " + lolDebitor + "</name1></name><plz>46430</plz><ort>Emmerich</ort><land>DE</land><anzahlBelegeLieferant12>1</anzahlBelegeLieferant12><anzahlBelegePoolOhneLieferant12>201</anzahlBelegePoolOhneLieferant12><betragLieferant12 waehrung=\"EUR\">45925</betragLieferant12><betragPoolOhneLieferant12 waehrung=\"EUR\">4502</betragPoolOhneLieferant12><tageSollLieferant12>15</tageSollLieferant12><tageSollPoolOhneLieferant12>23</tageSollPoolOhneLieferant12><tageIstLieferant12>14</tageIstLieferant12><tageIstPoolOhneLieferant12>20</tageIstPoolOhneLieferant12><tageDiffLieferant12>-1</tageDiffLieferant12><tageDiffPoolOhneLieferant12>-3</tageDiffPoolOhneLieferant12><anzahlBelegePoolOhneLieferant8>135</anzahlBelegePoolOhneLieferant8><betragPoolOhneLieferant8 waehrung=\"EUR\">4922</betragPoolOhneLieferant8><tageSollPoolOhneLieferant8>23</tageSollPoolOhneLieferant8><tageIstPoolOhneLieferant8>19</tageIstPoolOhneLieferant8><tageDiffPoolOhneLieferant8>-4</tageDiffPoolOhneLieferant8><anzahlBelegePoolOhneLieferant4>53</anzahlBelegePoolOhneLieferant4><betragPoolOhneLieferant4 waehrung=\"EUR\">5189</betragPoolOhneLieferant4><tageSollPoolOhneLieferant4>18</tageSollPoolOhneLieferant4><tageIstPoolOhneLieferant4>17</tageIstPoolOhneLieferant4><tageDiffPoolOhneLieferant4>-1</tageDiffPoolOhneLieferant4><anzahlBelegePoolOhneLieferant2>26</anzahlBelegePoolOhneLieferant2><betragPoolOhneLieferant2 waehrung=\"EUR\">5019</betragPoolOhneLieferant2><tageSollPoolOhneLieferant2>20</tageSollPoolOhneLieferant2><tageIstPoolOhneLieferant2>15</tageIstPoolOhneLieferant2><tageDiffPoolOhneLieferant2>-5</tageDiffPoolOhneLieferant2><lolIndex>188</lolIndex><branchenBezeichnung>Herstellung von Strumpfwaren</branchenBezeichnung><branchenCode>14310</branchenCode><branchenArt>WZ 2008</branchenArt><branchenLand>DE</branchenLand><anzahlBelegeBranche>512</anzahlBelegeBranche><betragBranche waehrung=\"EUR\">2485</betragBranche><tageSollBranche>23</tageSollBranche><tageIstBranche>22</tageIstBranche><tageDiffBranche>-1</tageDiffBranche><anzahlCrefosBranche>16</anzahlCrefosBranche></lolDebitor>\n");
                lolDebitor++;
            }

            xml.append("</lolDebitoren>\n" +
                    "</lolRuecklieferungErw>");

            File file = new File("/home/vagrant/Git/lol-loader/lol-loader-application/target/ftp-in/cs_axelclient/400000000_" + (100000 + lolDebitor) + ".xml");
            System.out.println(file.getAbsolutePath());
            FileUtils.writeStringToFile(file, xml.toString());
        }
    }
}

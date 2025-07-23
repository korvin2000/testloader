package de.microtools.cs.lol.loader.application.reader;

import de.microtools.cs.lol.loader.application.domain.LolImportData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LolDataRowMapper implements RowMapper<LolImportData> {

   public static final String ID_COLUMN = "CS_ID";
   public static final String MAPPING_COLUMN = "MAPPINGID";
   public static final String CREFO_COLUMN = "CREFONUMMER";
   public static final String NAME_1_COLUMN = "NAME_1";
   public static final String NAME_2_COLUMN = "NAME_2";
   public static final String NAME_3_COLUMN = "NAME_3";
   public static final String NAME_4_COLUMN = "NAME_4";
   public static final String PLZ_COLUMN = "PLZ";
   public static final String ORT_COLUMN = "ORT";
   public static final String LAND_COLUMN = "LAND";

   @Override
   public LolImportData mapRow(ResultSet rs, int rowNum) throws SQLException {

      LolImportData importData = new LolImportData();
      importData.setId(rs.getLong(ID_COLUMN));
      importData.setMappingId(rs.getLong(MAPPING_COLUMN));
      importData.setBobiknummer(rs.getString(CREFO_COLUMN));
      importData.setName1(rs.getString(NAME_1_COLUMN));
      importData.setName2(rs.getString(NAME_2_COLUMN));
      importData.setName3(rs.getString(NAME_3_COLUMN));
      importData.setName4(rs.getString(NAME_4_COLUMN));
      importData.setPlz(rs.getString(PLZ_COLUMN));
      importData.setOrt(rs.getString(ORT_COLUMN));
      importData.setLand(rs.getString(LAND_COLUMN));
      return importData;
   }

}

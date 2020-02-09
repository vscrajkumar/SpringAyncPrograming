package com.flex.adapter.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.flex.adapter.model.ErpAssembly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class JdbcTemplateAssemblyRepository {

	@Autowired
	private NamedParameterJdbcTemplate  jdbcTemplate;

	public List<ErpAssembly> getProcessInstructionAssembliesListByFilter(String assemblyNumber, int erpCompanyId, int customerId,String timeZone,int startRecord,int maxNumberRecords) {
		
//				final String query="select ewi_assembly_part_id,assembly_number,assembly_revision,created_date from "
//				+ " ( select row_number() over ( order by  ap1.updated_date desc ) as myRow, ap1.ewi_assembly_part_id, "
//				+ "ap1.assembly_number, ap1.assembly_revision, ap1.created_date from tbl_ewi_assembly_part ap1 "
//				+ " inner join ( select distinct ap.ewi_assembly_part_id, ap.assembly_number from tbl_ewi_assembly_part ap "
//				+ " left join tbl_ewi_work_instruction wi on ap.ewi_assembly_part_id =  wi.ewi_assembly_part_id "
//				+ "where (lifecycle_phase is null or upper(lifecycle_phase) != 'OBSOLETE') and "
//				+ " (effective_date is null or (to_char((effective_date at time zone ?),'YYYYMMDD')  <= to_char((current_timestamp at time zone ?),'YYYYMMDD'))) and "
//				+ " (obsolete_date is null or (to_char((obsolete_date at time zone ?),'YYYYMMDD')  >= to_char((current_timestamp at time zone ?),'YYYYMMDD'))) and "
//				+ " upper(ap.active_flag) != 'N' and ap.notification_type='EPI' and ap.customer_id = ? and "
//				+ " ap.assembly_number like  ? and "
//				+ " ap.erp_company_id = ? ) assy on ap1.ewi_assembly_part_id = assy.ewi_assembly_part_id ) "
//				+ " where myRow between ? and ?";
//		List<Map> rows =jdbcTemplate.queryForList(query, new Object[] { timeZone,timeZone,timeZone,timeZone,customerId,erpCompanyId,assemblyNumber});
//	      
//        return employee;	

		  MapSqlParameterSource parameters = new MapSqlParameterSource();
	      parameters.addValue("customerId", customerId);
	      parameters.addValue("assemblyNumber", assemblyNumber);
	      parameters.addValue("erpCompanyId", erpCompanyId);
	      parameters.addValue("startRecord", startRecord);
	      parameters.addValue("maxNumberRecords", maxNumberRecords);

	      List<ErpAssembly> erpAssemblyList = jdbcTemplate.query("select ewi_assembly_part_id,assembly_number,assembly_revision,created_date from ( select row_number() over ( order by  ap1.updated_date desc ) as myRow, ap1.ewi_assembly_part_id, ap1.assembly_number, ap1.assembly_revision, ap1.created_date from tbl_ewi_assembly_part ap1 inner join ( select distinct ap.ewi_assembly_part_id, ap.assembly_number from tbl_ewi_assembly_part ap left join tbl_ewi_work_instruction wi on ap.ewi_assembly_part_id =  wi.ewi_assembly_part_id where (lifecycle_phase is null or upper(lifecycle_phase) != 'OBSOLETE') and (effective_date is null or (to_char((effective_date at time zone 'America/Los_Angeles'),'YYYYMMDD')  <= to_char((current_timestamp at time zone 'America/Los_Angeles'),'YYYYMMDD'))) and (obsolete_date is null or (to_char((obsolete_date at time zone 'America/Los_Angeles'),'YYYYMMDD')  >= to_char((current_timestamp at time zone 'America/Los_Angeles'),'YYYYMMDD'))) and upper(ap.active_flag) != 'N' and ap.notification_type='EPI' and ap.customer_id = :customerId and ap.assembly_number like  :assemblyNumber and ap.erp_company_id = :erpCompanyId ) assy on ap1.ewi_assembly_part_id = assy.ewi_assembly_part_id ) where myRow between :startRecord and :maxNumberRecords",
	              parameters, new RowMapper<ErpAssembly>() {
	                  @Override
	                  public ErpAssembly mapRow(ResultSet resultSet, int i) throws SQLException {
	                      return toErpAssembly(resultSet);
	                  }
	              });
	    		  
	      return erpAssemblyList;
	      
	}
	
	private ErpAssembly toErpAssembly(ResultSet resultSet) throws SQLException {
		ErpAssembly assembly = new ErpAssembly();
		assembly.setEwiAssemblyPartId(resultSet.getInt("ewi_assembly_part_id"));
		assembly.setAssemblyNumber(resultSet.getString("assembly_number"));
		assembly.setAssemblyRevision(resultSet.getString("assembly_revision"));
		assembly.setCreatedDate(resultSet.getString("created_date"));
        return assembly;
    }

}

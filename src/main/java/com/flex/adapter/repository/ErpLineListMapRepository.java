package com.flex.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.flex.adapter.model.ErpLineListMap;
import com.flex.adapter.model.ErpUser;

@Repository
public interface ErpLineListMapRepository extends JpaRepository<ErpLineListMap, Integer> { 

	@Query(value = "select  distinct el.ewi_line_id,el.ewi_business_unit_id,ele.erp_company_id,el.description,el.active_flag from tbl_ewi_line el inner join tbl_ewi_business_unit bu on el.ewi_business_unit_id = bu.ewi_business_unit_id inner join tbl_ewi_line_erp ele on ele.ewi_business_unit_id =  bu.ewi_business_unit_id order by el.description", nativeQuery = true)
	List<ErpLineListMap> getErpLineListMap();

	@Query(value = "select distinct el.ewi_line_id,el.ewi_business_unit_id,ele.erp_company_id,el.description,el.active_flag from tbl_ewi_line el inner join tbl_ewi_business_unit bu on el.ewi_business_unit_id = bu.ewi_business_unit_id inner join tbl_ewi_line_erp ele on ele.ewi_business_unit_id =  bu.ewi_business_unit_id  where ele.erp_company_id = :erpCompanyId and bu.ewi_business_unit_id = :ewiBusinessUnitId and el.active_flag = :activeFlag order by el.description", nativeQuery = true)
	List<ErpLineListMap> getErpLineListByErpIdAndBUId(@Param("erpCompanyId") int erpCompanyId,@Param("ewiBusinessUnitId") int ewiBusinessUnitId, @Param("activeFlag") String activeFlag);
	
}

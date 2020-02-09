package com.flex.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.flex.adapter.model.ErpBusinessUnitsMap;

@Repository
public interface ErpBusinessUnitsMapRepository extends JpaRepository<ErpBusinessUnitsMap, String> { 

	@Query(value = "Select bu.description ,bu.active_flag ,gec.erp_company_code,bu.ewi_business_unit_id,gec.erp_company_id From tbl_ewi_business_unit bu inner join tbl_ewi_line_erp ele on ele.ewi_business_unit_id =  bu.ewi_business_unit_id inner join tbl_gms_erp_company gec on ele.erp_company_id = gec.erp_company_id", nativeQuery = true)
	List<ErpBusinessUnitsMap> getErpBusinessUnitMap();
}

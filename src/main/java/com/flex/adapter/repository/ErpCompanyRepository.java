package com.flex.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.flex.adapter.model.ErpCompany;

@Repository
public interface ErpCompanyRepository extends JpaRepository<ErpCompany, String> {

	@Query(value = "select distinct erp.erp_company_id, erp.erp_company_code, erp.erp_company_name from vw_gms_approved_roles ar inner join tbl_gms_erp_co_role_value erv on erv.gms_user_role_id = ar.user_role_id inner join tbl_gms_erp_company erp on erp.erp_company_code <> 'GMSALL' and erp.erp_company_code <> 'PULSEALL' and erp.active_flag = 'Y' and ( erp.erp_company_id = erv.erp_company_id or exists (select 1 from vw_gms_approved_roles ar inner join tbl_gms_erp_co_role_value erv on erv.gms_user_role_id = ar.user_role_id inner join tbl_gms_erp_company erp on erp.erp_company_id = erv.erp_company_id and erp.erp_company_code = 'GMSALL' where ar.solution_code =:solutionCode and ar.solution_role_code in (:roleCodeList)  and ar.user_id =:userId) ) where ar.solution_code =:solutionCode and ar.solution_role_code in (:roleCodeList)  and ar.user_id =:userId order by erp.erp_company_name", nativeQuery = true)
	List<ErpCompany> getErpCompany(@Param("solutionCode") String solutionCode,@Param("userId") int userId, @Param("roleCodeList") List<String> roleCodeList);
	
	@Query(value = "select distinct erp.erp_company_id, erp.erp_company_code, erp.erp_company_name from vw_gms_approved_roles ar,tbl_gms_erp_co_role_value crv,tbl_gms_erp_company erp where erp.active_flag = 'Y' and erp.erp_company_code = 'GMSALL'  and  erp.erp_company_id = crv.erp_company_id and    ar.solution_code =:solutionCode and  ar.solution_role_code in (:roleCodeList) and ar.user_role_id=crv.gms_user_role_id and ar.user_id=:userId", nativeQuery = true)
	List<ErpCompany> isUserMappingAllERPCompany(@Param("solutionCode") String solutionCode,@Param("userId") int userId, @Param("roleCodeList") List<String> roleCodeList);

	@Query(value = "select distinct erp.erp_company_id, erp.erp_company_code, erp.erp_company_name from tbl_gms_erp_company erp where erp.active_flag = 'Y' order by erp.erp_company_name", nativeQuery = true)
	List<ErpCompany> getAllErpCompany();

}


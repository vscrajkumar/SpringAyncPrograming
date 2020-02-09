
package com.flex.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.flex.adapter.model.ErpUser;

@Repository
public interface ErpUserRepository extends JpaRepository<ErpUser, String> {

	//@Query(value = "select distinct um.user_id, um.username, um.first_name, um.last_name, um.email from vw_gms_approved_roles ar inner join tbl_user_master um on um.user_id = ar.user_id and um.active_flag = 'Y' inner join tbl_gms_erp_co_role_value erv on erv.gms_user_role_id = ar.user_role_id inner join tbl_gms_erp_company erp on erp.erp_company_id = erv.erp_company_id and erp.active_flag = 'Y' where ar.solution_code = :solutionCode and erp.erp_company_code = :erpCode and ar.solution_role_code in (:roleCodeList) order by um.first_name, um.last_name, um.email", nativeQuery = true)
    @Query(value = "select distinct um.user_id, um.username, um.first_name, um.last_name, um.email from vw_gms_approved_roles ar inner join tbl_user_master um on um.user_id = ar.user_id and um.active_flag = 'Y' inner join tbl_gms_erp_co_role_value erv on erv.gms_user_role_id = ar.user_role_id inner join tbl_gms_erp_company erp on erp.erp_company_id = erv.erp_company_id and erp.active_flag = 'Y' where ar.solution_code = :solutionCode and ( erp.erp_company_code = :erpCode or erp.erp_company_code = 'GMSALL') and ar.solution_role_code in (:roleCodeList) order by um.first_name, um.last_name, um.email", nativeQuery = true)
	List<ErpUser> getErpUserList(@Param("solutionCode") String solutionCode,@Param("erpCode") String erpCode, @Param("roleCodeList") List<String> roleCodeList);

	@Query(value = "select distinct um.user_id, um.username, um.first_name, um.last_name, um.email from vw_gms_approved_roles ar inner join tbl_user_master um on um.user_id = ar.user_id and um.active_flag = 'Y' inner join tbl_gms_erp_co_role_value erv on erv.gms_user_role_id = ar.user_role_id inner join tbl_gms_erp_company erp on erp.erp_company_id = erv.erp_company_id and erp.active_flag = 'Y' where ar.solution_code = :solutionCode and erp.erp_company_code in (:erpCodeList) and ar.solution_role_code in (:roleCodeList) and um.username != :userName order by um.first_name, um.last_name, um.email", nativeQuery = true)
	List<ErpUser> getErpApproverList(@Param("solutionCode") String solutionCode,@Param("erpCodeList") List<String> erpCodeList, @Param("roleCodeList") List<String> roleCodeList, @Param("userName") String userName);

}



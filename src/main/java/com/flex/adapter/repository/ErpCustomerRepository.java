package com.flex.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.flex.adapter.model.ErpCustomer;

@Repository
public interface ErpCustomerRepository extends JpaRepository<ErpCustomer, String> {

	@Query(value = "select distinct cus.customer_id, cus.mdss_customer_code, cus.customer_name from vw_gms_approved_roles ar inner join tbl_gms_customer_role_value crv on crv.gms_user_role_id = ar.user_role_id inner join tbl_global_customer cus on cus.customer_id = crv.customer_id and cus.active_flag = 'Y' where ar.solution_code =:solutionCode and ar.user_id =:userId and ar.solution_role_code in (:roleCodeList)   order by cus.customer_name", nativeQuery = true)
	List<ErpCustomer> getErpCustomer(@Param("solutionCode") String solutionCode,@Param("userId") int userId, @Param("roleCodeList") List<String> roleCodeList);
	
	@Query(value = "select distinct cus.customer_id, cus.mdss_customer_code,cus.customer_name from vw_gms_approved_roles ar, tbl_gms_customer_role_value rv,tbl_global_customer cus where  rv.customer_id=cus.customer_id and   ar.user_role_id=rv.gms_user_role_id and ar.user_id=:userId and cus.customer_code=:customerCode and ar.solution_role_code in (:roleCodeList) and ar.solution_code = :solutionCode and cus.active_flag = 'Y'", nativeQuery = true)
	List<ErpCustomer> isAllCustomerMapping(@Param("customerCode") String customerCode,@Param("solutionCode") String solutionCode,@Param("userId") int userId, @Param("roleCodeList") List<String> roleCodeList);

	@Query(value = "select distinct cus.customer_id, cus.mdss_customer_code, cus.customer_name from tbl_global_customer cus where cus.active_flag = 'Y'  order by cus.customer_name", nativeQuery = true)
	List<ErpCustomer> getAllErpCustomer();
	
}
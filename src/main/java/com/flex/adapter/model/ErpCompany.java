package com.flex.adapter.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
public class ErpCompany {
	
	@Id
	@Column(name = "erp_company_code", nullable = false)
	String erpCode;
	
	@Column(name = "erp_company_id", nullable = false)
	int erpId;
	
	@Column(name = "erp_company_name", nullable = false)
	String erpName;
	
	public String getErpCode() {
		return erpCode;
	}
	public void setErpCode(String erpCode) {
		this.erpCode = erpCode;
	}
	public int getErpId() {
		return erpId;
	}
	public void setErpId(int erpId) {
		this.erpId = erpId;
	}
	public String getErpName() {
		return erpName;
	}
	public void setErpName(String erpName) {
		this.erpName = erpName;
	}
}

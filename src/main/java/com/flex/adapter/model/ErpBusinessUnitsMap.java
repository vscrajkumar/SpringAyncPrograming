package com.flex.adapter.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ErpBusinessUnitsMap {
	@Id
	String description;
	String activeFlag;
	String erpCompanyCode;
	long ewiBusinessUnitId;
	long erpCompanyId;

	public ErpBusinessUnitsMap() {
		
	}

	/*public ErpBusinessUnitsMap(String description,  String activeFlag, String erpCompanyCode,long ewi_business_unit_id,long erp_company_id) {

		super();
		this.description = description;
		this.activeFlag = activeFlag;
		this.erpCompanyCode = erpCompanyCode;
		this.ewiBusinessUnitId = ewiBusinessUnitId;
		this.erpCompanyId = erpCompanyId;

	}*/

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getErpCompanyCode() {
		return erpCompanyCode;
	}

	public void setErpCompanyCode(String erpCode) {
		this.erpCompanyCode = erpCode;
	}

	public long getEwiBusinessUnitId() {
		return ewiBusinessUnitId;
	}

	public void setEwiBusinessUnitId(long ewiBusinessUnitId) {
		this.ewiBusinessUnitId = ewiBusinessUnitId;
	}

	public long getErpCompanyId() {
		return erpCompanyId;
	}

	public void setErpCompanyId(long erpCompanyId) {
		this.erpCompanyId = erpCompanyId;
	}


}

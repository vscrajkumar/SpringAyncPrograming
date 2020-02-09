package com.flex.adapter.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ErpLineListMap {
	
	@Id
	long ewiLineId;
	long ewiBusinessUnitId;
	long erpCompanyId;
	String description;
	String activeFlag;
	
	public ErpLineListMap() {
		
	}
	public long getEwiLineId() {
		return ewiLineId;
	}
	public void setEwiLineId(long ewiLineId) {
		this.ewiLineId = ewiLineId;
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

}


package com.flex.adapter.model;

public class AssemblyFilter {

	int userId;
	int erpId;
	int businessUnitId;
	int customerId;
	String securityToken;
	String searchValue;
	String activeFlag;

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getErpId() {
		return erpId;
	}

	public void setErpId(int erpId) {
		this.erpId = erpId;
	}

	public int getBusinessUnitId() {
		return businessUnitId;
	}

	public void setBusinessUnitId(int businessUnitId) {
		this.businessUnitId = businessUnitId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
}

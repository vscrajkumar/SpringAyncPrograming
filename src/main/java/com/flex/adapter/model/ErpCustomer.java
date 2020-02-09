package com.flex.adapter.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ErpCustomer {

	@Id
	@Column(name = "mdss_customer_code", nullable = false)
	String customerCode;
	
	@Column(name = "customer_id", nullable = false)
	int customerId;
	
	@Column(name = "customer_name", nullable = false)
	String customerName;
	
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
}

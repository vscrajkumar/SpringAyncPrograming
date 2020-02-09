package com.flex.adapter.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ErpAssembly {

	@Id
	@Column(name = "ewi_assembly_part_id", nullable = false)
	int ewiAssemblyPartId;
	
	@Column(name = "assembly_number", nullable = false)
	String assemblyNumber;
	
	@Column(name = "assembly_revision", nullable = false)
	String assemblyRevision;
	
	@Column(name = "created_date", nullable = false)
	String createdDate;

	public int getEwiAssemblyPartId() {
		return ewiAssemblyPartId;
	}

	public void setEwiAssemblyPartId(int ewiAssemblyPartId) {
		this.ewiAssemblyPartId = ewiAssemblyPartId;
	}

	public String getAssemblyNumber() {
		return assemblyNumber;
	}

	public void setAssemblyNumber(String assemblyNumber) {
		this.assemblyNumber = assemblyNumber;
	}

	public String getAssemblyRevision() {
		return assemblyRevision;
	}

	public void setAssemblyRevision(String assemblyRevision) {
		this.assemblyRevision = assemblyRevision;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	
	
}

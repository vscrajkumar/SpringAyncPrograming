package com.flex.adapter.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ErpUser {

	@Id
	@Column(name = "user_id", nullable = false)
	String userId;
	
	@Column(name = "username", nullable = false)
	String username;
	
	@Column(name = "first_name", nullable = false)
	String firstName;
	
	@Column(name = "last_name", nullable = false)
	String lastName;
	
	@Column(name = "email", nullable = false)
	String email;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	
}

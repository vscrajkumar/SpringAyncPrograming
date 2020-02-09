package com.flex.adapter.client;

import com.flextronics.flexware.ws.client.authentication.UserMasterWs;

public class UserMasterWsComparable extends UserMasterWs {

	private String masterDataType;

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserMasterWs)) {
			return false;
		}
		UserMasterWs userMasterWs = (UserMasterWs) o;

		if (userMasterWs.getUserId() != null && userId != null && userId.equals(userMasterWs.getUserId())) {
			return true;
		}

		if (userName != null && userMasterWs.getUserName() != null && userName.equals(userMasterWs.getUserName())) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + (this.userName != null ? this.userName.hashCode() : 0);
		hash = 89 * hash + Integer.valueOf(this.userId);
		return hash;
	}

	@Override
	public String toString() {

		return firstName + " " + lastName;
	}

	public String getMasterDataType() {
		return masterDataType;
	}

	public void setMasterDataType(String masterDataType) {
		this.masterDataType = masterDataType;
	}

}

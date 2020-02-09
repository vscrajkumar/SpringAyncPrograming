package com.flex.adapter.model;

public class AttachmentWrapper {

	String _id;
	String filename;
	String contentType;
	Metadata metadata;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public class Metadata {

		String description;
		String lineProcess;
		String processInstruction;
		String lineLayout;
		String procedureName;
		String documentType;
		CreatedOn createdOn;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getLineProcess() {
			return lineProcess;
		}

		public void setLineProcess(String lineProcess) {
			this.lineProcess = lineProcess;
		}

		public String getProcessInstruction() {
			return processInstruction;
		}

		public void setProcessInstruction(String processInstruction) {
			this.processInstruction = processInstruction;
		}

		public String getLineLayout() {
			return lineLayout;
		}

		public void setLineLayout(String lineLayout) {
			this.lineLayout = lineLayout;
		}

		public String getProcedureName() {
			return procedureName;
		}

		public void setProcedureName(String procedureName) {
			this.procedureName = procedureName;
		}

		public String getDocumentType() {
			return documentType;
		}

		public void setDocumentType(String documentType) {
			this.documentType = documentType;
		}

		public CreatedOn getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(CreatedOn createdOn) {
			this.createdOn = createdOn;
		}

		public class CreatedOn {

			String createdDate;
			CreatedBy createdBy;

			public String getCreatedDate() {
				return createdDate;
			}

			public void setCreatedDate(String createdDate) {
				this.createdDate = createdDate;
			}

			public CreatedBy getCreatedBy() {
				return createdBy;
			}

			public void setCreatedBy(CreatedBy createdBy) {
				this.createdBy = createdBy;
			}

			public class CreatedBy {
				String firstName;
				String lastName;
				String email;
				String username;

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

				public String getUsername() {
					return username;
				}

				public void setUsername(String username) {
					this.username = username;
				}

			}

			@Override
			public String toString() {

				return "";
			}
		}
	}

}

package com.flex.adapter.constants;

import java.util.HashMap;
import java.io.File;

public class AppConstants {

	/***************************

	//Production configuration
	
	public static final String FLEXWARE_TARGET = "http://10.10.30.223:28081/GMS-WS/services/";
	public static final String GMSWS_TARGET = "http://saclx027.sac.flextronics.com:28080/GMS-WS/services/";
	public static final String FLEXSTATION_TARGET = "http://10.10.30.222:28080/ewi-ws/";
	public static final String AEPI_TARGET = "http://flexstation.flex.com:8080";
	public static final String EPI_TARGET = "http://services-epi.flex.com:3000";
	public static final String METADATA_IMPORTER_SERVER_IP_ADDRESS = "10.10.229.154";
	public static final String EWI_TARGET_ENV = "ewiprod";


	 
	//QA Configuration
	public static final String FLEXWARE_TARGET = "http://ewiqa.flextronics.com/GMS-WS/services/";
	public static final String GMSWS_TARGET = "http://hkdlx121.hkd.flextronics.com:8080/GMS-WS/services/";
	public static final String FLEXSTATION_TARGET = "http://ewiqa.flextronics.com/ewi-ws/";
	public static final String AEPI_TARGET = "http://hkdvl139.hkd.flex.com:8080";
	public static final String EPI_TARGET = "http://10.219.246.131:3001";
	public static final String METADATA_IMPORTER_SERVER_IP_ADDRESS = "10.201.15.217";
	public static final String EWI_TARGET_ENV = "ewiqa";
	
		//Stage Configuration 
	public static final String FLEXWARE_TARGET = "http://ewiqa.flextronics.com/GMS-WS/services/";
	public static final String GMSWS_TARGET = "http://hkdlx121.hkd.flextronics.com:8080/GMS-WS/services/";
	public static final String FLEXSTATION_TARGET = "http://ewiqa.flextronics.com/ewi-ws/";
	public static final String AEPI_TARGET = "http://sacvl455.sac.flex.com:8080";
	public static final String EPI_TARGET = "http://sacvl455.sac.flex.com:3000";
	public static final String METADATA_IMPORTER_SERVER_IP_ADDRESS = "10.201.15.217";
	public static final String EWI_TARGET_ENV = "ewiqa";
		
	
		***************************/

	//Local Configuration
	public static final String FLEXWARE_TARGET = "http://ewiqa.flextronics.com/GMS-WS/services/";
	public static final String GMSWS_TARGET = "http://hkdlx121.hkd.flextronics.com:8080/GMS-WS/services/";
	public static final String FLEXSTATION_TARGET = "http://ewiqa.flextronics.com/ewi-ws/";
	public static final String AEPI_TARGET = "http://localhost:8080";
	public static final String EPI_TARGET = "http://localhost:3000";
	public static final String METADATA_IMPORTER_SERVER_IP_ADDRESS = "10.201.15.217";
	public static final String EWI_TARGET_ENV = "ewiqa";
	



	


	
	public static final String EPI_APPROVER = "EPIAPPROV";
	public static final String EWI_SOLUTION_ROLE_CODE = "EPIAPPROV";
	public static final String EPI_PROCESS_ENGINEER = "EPIENG";
	public static final String EPI_ADMINISTRATOR = "EPIADMIN";

	public static final int WS_REQUEST_TIMEOUT = 120000; // 2 minutes
	public static final String ERP_COMPANY_CODE = "GMSMD005";
	public static final String EWI_SOLUTION_CODE = "GMSAPP007";
	public static final String CUSTOMER_CODE = "GMSMD007";

	public static final String POST_APPROVE_EWI_DOCUMENT = "/ewi/document/approve";
	public static final String POST_APPROVE_DISABLE_EWI_DOCUMENT = "/ewi/document/approve/disable";

	public static final String USER_VALIATION = "/user/validation";
	public static final String TOKEN_VALIATION = "/user/token/validation";
	public static final String GET_ERPS = "/user/assgined/erps";
	//public static final String GET_BUSINESSUNITS = "/user/assgined/erp/businessunits";
	public static final String POST_GET_BU = "/user/assgined/erp/businessunits";
	public static final String GET_CUSTOMERS = "/user/assgined/customers";
	public static final String GET_DOCUMENT_CNT = "/epi/webui/assembly/summary";
	public static final String GET_DOCUMENT_CNT_FETURE_TASK = "/epi/webui/assembly/summary";
	public static final String GET_DOCUMENT_LINE_CNT = "/epi/webui/assembly/line/summary";
	public static final String GET_LINE_LIST = "/epi/assembly/line/list";
	public static final String GET_CREATOR_LIST = "/epi/instructions/creator/list";
	public static final String GET_APPROVER_LIST = "/epi/instructions/approver/list";
	public static final String POST_ASSEMBLY_UPLOAD = "/epi/upload/assemly";
	public static final String POST_INSTRUCTION_LAYOUT_CHANGE = "/ewi/check/remove/epi/document";
	public static final String POST_GENERATE_EWI_DOCUMENT = "/ewi/generate/document";
	public static final String GET_GENERATE_DOCUMENT_REVISION = "/epi/process/instructions/documentrevision/generate";
	public static final String POST_PROCESS_INSTRUCTIONS_EDIT = "/epi/process/instructions/edit";
	public static final String POST_EMAIL_APPROVE_CONFIRMATION = "/epi/approve/confirmation";
	public static final String POST_EMAIL_APPROVE_NOTIFICATION = "/epi/approve/notification";
	public static final String POST_EMAIL_DISABLE_INSTRUCTION_NOTIFICATION = "/epi/disable/instruction/notification";
	public static final String POST_EMAIL_DISABLE_INSTRUCTION_CONFIRMATION = "/epi/disable/instruction/confirmation";
	public static final String POST_PROCESS_INSTRUCTION = "/epi/process/instruction";
	public static final String POST_PROCESS_INSTRUCTIONS_LIST = "/epi/process/instructions";
	public static final String POST_COPY_INSTRUCTION_ATTACHMENT = "/epi/copy/process/attachments";
	public static final String GET_ATTACHMENT_BY_ID = "/epi/files/general/getfile";
	public static final String POST_UPLOAD_ATTACHMENT = "/epi/fileupload";
	public static final String GET_ASSEMBLY_LIST = "/epi/assembly/list";
	public static final String POST_USER_TOKEN_INSERT = "/epi/webui/user/token/insert";
	public static final String POST_USER_TOKEN_REMOVE = "/epi/webui/user/token/remove";
	public static final String GET_ROLE_WISE_ACCESS = "/epi/user/rolewise/access";
	public static final String POST_USER_LOG_OUT = "/user/logout";
	public static final String POST_LAYOUT_NOTIFICATION = "/epi/layout/notification";
	public static final String Is_AUTHORISED = "/epi/wepui/validate";

	
	public static final String SEPERATOR = "/";
	public static final String EPI_IMPORTER_CODE = "EPIFTP001";

	public static final String PIPE_DELIMITER = "\\|";
	
	public static final String EPI_HOME_DIR = System.getProperty("user.home") + File.separator + "flexstation"
			+ File.separator + "epi" + File.separator;

	public static final String LOGIN = "login";
	public static final String LOGOUT = "logout";
	
	public static final String ALL_CUSTOMER_CODE = "GMSALL";


	public static HashMap<String, String> setupRequestHeaders() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		return headers;
	}

}

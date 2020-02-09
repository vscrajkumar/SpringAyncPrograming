package com.flex.adapter.client;

public class BeanLocator {

	private static AuthenticationWsClient authenticationWSBean;
	private static FlexwareWsClient flexwareWSBean;
	private static CommandCenterWsClient commandCenterWSBean;
	private static AssemblyPartWsClient assemblyPartWSBean;
	private static ConfigurationWsClient configurationWSBean;
	private static SearchUserWsClient searchUserWsClient;
	private static DocumentWsClinet documentWsBean;

	private BeanLocator() {

	}

	public static AuthenticationWsClient getAuthenticationWSBean(String envUrl) {

		if (authenticationWSBean == null) {
			authenticationWSBean = new AuthenticationWsClient(envUrl);
		}
		return authenticationWSBean;
	}

	public static FlexwareWsClient getFlexwareWSBean(String envUrl) {

		if (flexwareWSBean == null) {
			flexwareWSBean = new FlexwareWsClient(envUrl);
		}
		return flexwareWSBean;

	}

	public static CommandCenterWsClient getCommandCenterWSBean(String envUrl) {

		if (commandCenterWSBean == null) {
			commandCenterWSBean = new CommandCenterWsClient(envUrl);
		}
		return commandCenterWSBean;
	}

	public static AssemblyPartWsClient getAssemblyParWSBean(String envUrl) {

		if (assemblyPartWSBean == null) {
			assemblyPartWSBean = new AssemblyPartWsClient(envUrl);
		}
		return assemblyPartWSBean;

	}

	public static ConfigurationWsClient getConfigurationWSBean(String envUrl) {

		if (configurationWSBean == null) {
			configurationWSBean = new ConfigurationWsClient(envUrl);
		}
		return configurationWSBean;
	}

	public static SearchUserWsClient getSearchUserWsClient() {

		if (searchUserWsClient == null) {
			searchUserWsClient = new SearchUserWsClient();
		}

		return searchUserWsClient;

	}

	public static DocumentWsClinet getDocumentWSBean(String envUrl) {

		if (documentWsBean == null) {
			documentWsBean = new DocumentWsClinet(envUrl);
		}
		return documentWsBean;

	}

}

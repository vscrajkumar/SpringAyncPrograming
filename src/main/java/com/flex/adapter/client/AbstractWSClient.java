package com.flex.adapter.client;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public abstract class AbstractWSClient {

	public void configureAddressPort(String endPoint, Object port) {

		BindingProvider provider = (BindingProvider) port;
		provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

	}

	public void configurePortTimeOut(int timeout, Object port) {
		Client proxy = ClientProxy.getClient(port);

		HTTPConduit conduit = (HTTPConduit) proxy.getConduit();

		// HTTPClientPolicy - Properties used to configure a client-side HTTP
		// port
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

		httpClientPolicy.setConnectionTimeout(timeout);
		httpClientPolicy.setReceiveTimeout(timeout * 2);

		conduit.setClient(httpClientPolicy);

	}

	public void configureSecureAccess(Object port, String envUrl) {

		if (envUrl.toLowerCase().startsWith("https:")) {
			TrustManager[] simpleTrustManager = new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} };
			TLSClientParameters tlsParams = new TLSClientParameters();
			tlsParams.setTrustManagers(simpleTrustManager);
			tlsParams.setDisableCNCheck(true);

			Client proxy = ClientProxy.getClient(port);

			HTTPConduit conduit = (HTTPConduit) proxy.getConduit();

			conduit.setTlsClientParameters(tlsParams);
		}
	}
}

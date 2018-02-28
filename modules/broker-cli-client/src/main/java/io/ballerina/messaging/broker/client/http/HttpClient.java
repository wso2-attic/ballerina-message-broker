/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package io.ballerina.messaging.broker.client.http;

import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_CONNECTION_URL_SUFFIX;

/**
 * Http Client to invoke the REST service of the MB.
 *
 * This wil provide interface to invoke a Http backend using {@link HttpRequest} and response will be returned through
 * a {@link HttpResponse}.
 */
public class HttpClient {

    private static final String USER_AGENT = "Broker-CLI-Client/4.0";

    private String url;

    private String encodedCredentials;

    public HttpClient(Configuration configuration) {
        this.url = configuration.getUrl() + BROKER_CONNECTION_URL_SUFFIX;
        this.encodedCredentials = configuration.getEncodedCredentials();

        // turn off SSL verification
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] { new X509UntrustManagerImpl() };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            BrokerClientException exception = new BrokerClientException();
            exception.addMessage("error initializing the connection");
            exception.addMessage(e.getMessage());
            throw exception;
        }
    }

    // Send HTTP request
    public HttpResponse sendHttpRequest(HttpRequest request, String httpMethod) {

        try {

            URL obj = new URL(url + request.getSuffix() + request.getQueryParameters());
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod(httpMethod);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Basic" + encodedCredentials);
            // Disable keep-alive. Since client will send one request per each startup, it won't get any advantage
            con.setRequestProperty("Connection", "close");

            // set http data
            if (Objects.nonNull(request.getPayload())) {
                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(request.getPayload());
                }
            }

            int responseCode = con.getResponseCode();

            BufferedReader in = null;
            StringBuilder response = new StringBuilder();
            try {
                // if non-error code is returned retrieve the inputStream otherwise errorStream
                if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                    if (Objects.isNull(con.getInputStream())) {
                        return new HttpResponse(responseCode, "");
                    }
                    in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                } else {
                    if (Objects.isNull(con.getErrorStream())) {
                        return new HttpResponse(responseCode,
                                String.valueOf(responseCode) + " " + con.getResponseMessage());
                    }
                    in = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
                }

                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                if (response.toString().isEmpty()) {
                    response.append(String.valueOf(responseCode) + " " + con.getResponseMessage());
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            return new HttpResponse(responseCode, response.toString());
        } catch (IOException e) {
            BrokerClientException exception = new BrokerClientException();
            exception.addMessage("error calling broker https service");
            exception.addMessage(e.getMessage());
            throw exception;
        }
    }

    private static class X509UntrustManagerImpl implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}

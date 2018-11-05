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
package io.ballerina.messaging.broker.auth.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Wrapper for @{@link SSLSocketFactory}.
 * Because JNDI instantiate the socket factory internally, a wrapper class is required to provide
 * a SSLContext with a custom Trust Store.
 */
public class LdapSslSocketFactory extends SSLSocketFactory {

    private static SSLContext sslContext;
    private SSLSocketFactory sslSocketFactory;

    public static void setSslContext(SSLContext sslContext) {
        LdapSslSocketFactory.sslContext = sslContext;
    }

    public static SSLContext getSslContext() {
        return sslContext;
    }

    public LdapSslSocketFactory() {
        sslSocketFactory = sslContext.getSocketFactory();
    }

    public static SocketFactory getDefault() {
        return new LdapSslSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslSocketFactory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return sslSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port,
                               InetAddress clientAddress, int clientPort) throws IOException, UnknownHostException {
        return sslSocketFactory.createSocket(host, port, clientAddress, clientPort);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        return sslSocketFactory.createSocket(inetAddress, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress clientAddress, int clientPort) throws IOException {
        return sslSocketFactory.createSocket(address, port, clientAddress, clientPort);
    }
}

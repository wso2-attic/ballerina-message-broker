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

package io.ballerina.messaging.broker.amqp;

import io.ballerina.messaging.broker.common.CommonConstants;
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

/**
 * Factory class for creating the SSL engine.
 */
public class SslHandlerFactory {

    private SSLContext sslContext;

    public SslHandlerFactory(AmqpServerConfiguration configuration) throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = getKeyStore(configuration.getSsl().getKeyStore().getType(),
                                        configuration.getSsl().getKeyStore().getLocation(),
                                        configuration.getSsl().getKeyStore().getPassword());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(configuration.getSsl()
                                                                                         .getKeyStore()
                                                                                         .getCertType());
        keyManagerFactory.init(keyStore, configuration.getSsl().getKeyStore().getPassword().toCharArray());

        KeyStore trustStore = getKeyStore(configuration.getSsl().getTrustStore().getType(),
                                          configuration.getSsl().getTrustStore().getLocation(),
                                          configuration.getSsl().getTrustStore().getPassword());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(configuration.getSsl()
                                                                               .getTrustStore()
                                                                               .getCertType());
        trustManagerFactory.init(trustStore);

        sslContext = SSLContext.getInstance(configuration.getSsl().getProtocol());
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    }

    private KeyStore getKeyStore(String type, String storePath, String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(type);
        InputStream in = null;
        try {
            Path path = getPath(storePath);

            in = Files.newInputStream(path);
            keyStore.load(in, password.toCharArray());
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return keyStore;
    }

    private Path getPath(String storePath) {
        Path path = Paths.get(storePath);

        if (!path.toFile().exists()) {
            String brokerHome = System.getProperty(CommonConstants.MESSAGE_BROKER_HOME_PROPERTY);
            path = Paths.get(brokerHome + File.separator + storePath);
        }
        return path;
    }

    public ChannelHandler create() {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        return new SslHandler(sslEngine);
    }
}

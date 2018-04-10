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

package io.ballerina.messaging.secvault.ciphertool;

import io.ballerina.messaging.secvault.ciphertool.utils.TestUtils;
import io.ballerina.messaging.secvault.ciphertool.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.secvault.SecureVaultConstants;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This class defines the unit test cases for Cipher Tool Utils.
 */
public class UtilsTest {
    private static final Path targetPath = Paths.get("target");
    private static final String CLASS_NAME = "io.ballerina.messaging.secvault.ciphertool.CipherTool";

    @Test
    public void testGetCustomClassLoader() throws ClassNotFoundException {
        URLClassLoader urlClassLoader =
                Utils.getCustomClassLoader(Optional.of(targetPath.toAbsolutePath().toString()));
        Class clazz = urlClassLoader.loadClass(CLASS_NAME);
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testCreateCipherTool() throws SecureVaultException, CipherToolException {
        URLClassLoader urlClassLoader =
                Utils.getCustomClassLoader(Optional.of(targetPath.toAbsolutePath().toString()));
        Path secureVaultYAMLPath = TestUtils.getResourcePath("conf",
                SecureVaultConstants.SECURE_VAULT_CONFIG_YAML_FILE_NAME)
                .orElseThrow(() -> new SecureVaultException("Secure vault YAML path not found"));
        Object cipherTool = Utils.createCipherTool(urlClassLoader, secureVaultYAMLPath);
        Assert.assertNotNull(cipherTool);
    }
}

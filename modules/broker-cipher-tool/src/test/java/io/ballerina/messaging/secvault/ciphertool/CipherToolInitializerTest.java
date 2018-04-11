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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.secvault.SecureVaultConstants;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.nio.file.Path;

/**
 * This class defines the unit test cases for CipherToolInitializer.
 */
public class CipherToolInitializerTest {

    private Path secureVaultYAMLPath;

    @BeforeTest
    public void setup() throws SecureVaultException {
        secureVaultYAMLPath = TestUtils.getResourcePath("conf",
                SecureVaultConstants.SECURE_VAULT_CONFIG_YAML_FILE_NAME)
                .orElseThrow(() -> new SecureVaultException("Secure vault YAML path not found"));
    }

    @Test(expectedExceptions = {CipherToolRuntimeException.class})
    public void testExecuteTestEncryptSecretsWithOddParameters() {
        String[] toolArgs = new String[]{"-customLibPath", "/tmp", "xyz"};
        CipherToolInitializer.execute(toolArgs);
    }

    @Test(expectedExceptions = {CipherToolRuntimeException.class})
    public void testExecuteTestEncryptSecretsWithWrongCommand() {
        String[] toolArgs = new String[]{"-ENCRYPTTEXT", "Ballerina@WSO2"};
        CipherToolInitializer.execute(toolArgs);
    }

    @Test
    public void testExecuteTestEncryptText() {
        String[] toolArgs = new String[]{"-encryptText", "Ballerina@WSO2",
                "-configPath", secureVaultYAMLPath.toString()};
        //Here we could not assert the output as it print as a log.info()
        CipherToolInitializer.execute(toolArgs);
    }

    @Test
    public void testExecuteTestDecryptText() {
        String[] toolArgs = new String[]{"-decryptText",
                "lDaF2UWOEK7NNS3Sbu1jk4OJtGjTD88LWHTUNBiAAEnW/mWGyOZ4wu8xL4B5oVZH1n7oFTac6YBl8Q96VJMfZo0PJ8fAm0ALIEhd"
                        + "qXMOe6e2sUISnrPOf4xqdrRN+N1arEgzmNOZ+51OjznyIL3hiCJ9boYeJX1DufIDpqL0Q/M0g5Tglu4w15bhzCgwzI"
                        + "yL7u3qs5eDwsL+GfV5kRtk9giBU5dkvWxxVfdRzHj7OxpN4JpodN+dcY0fhtgHUS/s0f03fp9ZQqB7pKTl2SYIRjhc"
                        + "SaQxV3Moih7RCLsYdoLiIZRG3uZJUzHEAU2Z/Sd0R8enPIQ64qoWEm3GhCsuxQ==",
                "-configPath", secureVaultYAMLPath.toString()};
        //Here we could not assert the output as it print as a log.info()
        CipherToolInitializer.execute(toolArgs);
    }
}

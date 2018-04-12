# Configuring Secure Vault

The Message Broker protects sensitive data such as a password in configuration files with secure vault. Secure vault 
allows you to store encrypted passwords that are mapped to aliases, i.e., you can use the aliases instead of the actual 
plain text passwords in your configuration files for better security.

There are three files that are needed by the SecureVault:

1. Secure vault configuration file - Configurations that are required for configuring the Secure Vault are given in 
this file. In message broker, these configurations are stored in the broker.yaml. 

    ```yaml
    wso2.securevault:
      secretRepository:
        type: org.wso2.carbon.secvault.repository.DefaultSecretRepository
        parameters:
          privateKeyAlias: ballerina
          keystoreLocation: ${sys:message.broker.home}/resources/security/keystore.jks
          secretPropertiesFile: ${sys:message.broker.home}/conf/security/secrets.properties
      masterKeyReader:
        type: org.wso2.carbon.secvault.reader.DefaultMasterKeyReader
        parameters:
          masterKeyReaderFile: ${sys:message.broker.home}/conf/master-keys.yaml
    ```

2. master-keys.yaml - The default Secure Vault implementation is based on the Java Key Store (JKS). Passwords that are 
needed to access the JKS and Keys are specified in this file. The passwords given in this file should be the base64 
format and the explicit type specifier (!!binary) is a must. The permanent property decides whether to keep this file 
after reading through the Secure Vault. If you set it to false, then the file will be deleted once broker started.

    ```yaml
    permanent: true
    masterKeys:
      keyStorePassword: !!binary YmFsbGVyaW5h
      privateKeyPassword: !!binary YmFsbGVyaW5h
    ```

3. secrets.properties - This file contains the alias with the plain text.

    ```properties
    broker.dataSource.password=plainText ballerina
    broker.keyStore.password=plainText ballerina
    broker.trustStore.password=plainText ballerina
    adminServiceTranport.keyStore.password=plainText ballerina
    adminServiceTranport.cert.password=plainText ballerina
    ```

There is a separate tool called 'ciphertool' to encrypt the secrets.properties file. Once the tool is run, it will 
encrypt all plain text passwords in the secrets.properties file. The Secure Vault reads the aliases and passwords given 
in the secrets.properties file and return the resolved (decrypted) password.

## How to use Secure Vault

1. All passwords in the broker.yaml and admin-service-transports.yaml are already added to secrets.properties. Make 
sure to replace these passwords with your own passwords.

2. Run the cipher tool to encrypt all plain text password in the secrets.properties file. You can find the cipher tool 
script file in the bin folder.
    
    ```text
    Instructions: sh ciphertool.sh [<command> <parameter>]
    
    -- command      -configPath | -encryptText | -decryptText | -customLibPath
    -- parameter    input to the command
    
    Usages:
    
    * Load default secure vault config from [MESSAGE_BROKER_HOME]/conf/broker.yaml and encrypts the secrets specified in the [MESSAGE_BROKER_HOME]/conf/security/secrets.properties file. 
         Eg: ciphertool.sh
    
    * Load secure vault config from given config path and encrypt secrets in the specified secrets.properties file.
         Eg: ciphertool.sh -configPath /home/user/custom/config/secure-vault.yaml
    
    * Load libraries in the given path first and perform the same operation as above.
         Eg: ciphertool.sh -configPath /home/user/custom/config/secure-vault.yaml -customLibPath /home/user/custom/libs
    
    * -encryptText : this option will first encrypt a given text and then prints the base64 encoded
       string of the encoded cipher text in the console.
         Eg: ciphertool.sh -encryptText Ballerina@WSO2
    
    * -decryptText : this option accepts base64 encoded cipher text and prints the decoded plain text in the console.
         Eg: ciphertool.sh -decryptText XxXxXx   
     
    ```

3. You can see all plain text password encrypted in the secrets.properties after execution of cipher tool.

    ```properties
    broker.dataSource.password=cipherText ktX9EB1vdA+Hr5NMe2JcScbWbcnBhF9oat0SwfqEKrDiMs60s6XMeVokCEE/qcTlPgy/oKpSVxy/kOTIUOajO7DqCRBgiWCp06eIrpGGyV5Q7b/WERw52yM7UUyKhZpaJR78UDZ2d5zad3LKMJhQVwIJ0JEMERp2Y5tOx3GUvCzNlsecRWyYGe8shKhXA2yC14y2FoaHJBQKwhMpUheuHIQIuPq903tal444NYg63UyD+vrjKr841BEE+7QxVugatuVap5yeOeNVDNXFNw2YPfFsJTR/QSN8hwhBWFcCPltR4cxd03movvuMLwYO3EWKIc+nR9qZ894ha7nATD6L+g\=\=
    ```

4. Replace all plain text password with alias defines in the secrets.properties file. The alias should write as ${sec:ALIAS_NAME} 
in the relevant configuration yaml file.

    ```yaml
     # Datasource configurations used to communicate with the database.
     dataSource:
      # Database URL.
      url: jdbc:h2:./database/MB_DB
      # Database username.
      user: ballerina
      # Database password.
      password: ${sec:broker.dataSource.password}
    ```
    
5. Start the broker. The encrypted password decrypts by the same key you have defined in the master-key.yaml. Hence, 
   make sure to use the same keystore for both encryption and decryption. 
package com.example;

import java.io.File;

public class Encrypter {

    private final CryptoAES cryptoAESPassword = new CryptoAES();

    public CryptoAES getCryptoAESPassword() {
        return this.cryptoAESPassword;
    }

    public String encryptPassword(String string) throws Exception {
        if (!new File("./src/chave.simetrica").isFile()) {
            cryptoAESPassword.geraChave(new File("./src/chave.simetrica"));
        }
        cryptoAESPassword.geraCifra(string.getBytes(), new File("./src/chave.simetrica"));
        String encryptedString = new String(cryptoAESPassword.getTextoCifrado());
        return encryptedString;
    }
}

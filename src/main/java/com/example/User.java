package com.example;

import java.io.File;

public class User {
    private String username;
    private String password;
    private Encrypter encrypter = new Encrypter();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // getters and setters
    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Encrypter getEncrypter() {
        return this.encrypter;
    }

    public String decryptPassword(String encryptedString) throws Exception {
        this.encrypter.getCryptoAESPassword().geraDecifra(this.encrypter.getCryptoAESPassword().getTextoCifrado(), new File("./src/chave.simetrica"));
        String decryptedString = new String(this.encrypter.getCryptoAESPassword().getTextoDecifrado());
        return decryptedString;
    }

    // toString
    @Override
    public String toString() {
        return "User [username=" + username + ", password=" + password + "]";
    }
}

package com.vaultcli.exceptions;

public class EncryptionException extends VaultException {
    public EncryptionException(String message) {
        super("Šifrování selhalo: " + message);
    }
}
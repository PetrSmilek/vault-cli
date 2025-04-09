package com.vaultcli.exceptions;

// [VAULT] značí prefix vlastních výjimek
public class VaultException extends RuntimeException {
    public VaultException(String message) {
        super("[VAULT] " + message);
    }
}
package com.vaultcli.exceptions;

public class PasswordNotFoundException extends VaultException {
    public PasswordNotFoundException(String serviceName) {
        super("Heslo pro službu '" + serviceName + "' nebylo nalezeno");
    }
}

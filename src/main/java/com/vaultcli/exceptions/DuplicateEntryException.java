package com.vaultcli.exceptions;

public class DuplicateEntryException extends VaultException {
  public DuplicateEntryException(String serviceName) {
    super("Služba '" + serviceName + "' již existuje");
  }
}

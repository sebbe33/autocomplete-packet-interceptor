package com.alexsebbe;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public interface WebRequestEmulatorFactory {
	public WebRequestEmulator createWebRequestEmulator()  throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException;
}

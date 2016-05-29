package com.alexsebbe;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public enum WebRequestEmulatorFactoryImpl implements WebRequestEmulatorFactory {
	INSTANCE;

	public WebRequestEmulator createWebRequestEmulator() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		return new WebRequestEmulator();
	}

}

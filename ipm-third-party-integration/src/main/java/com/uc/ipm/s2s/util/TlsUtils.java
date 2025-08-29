package com.uc.ipm.s2s.util;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public final class TlsUtils {
    private TlsUtils(){}

    public static SslContext buildClientSslContext(String keyStorePath, String keyStorePassword,
                                                   String trustStorePath, String trustStorePassword) throws Exception {
        KeyManagerFactory kmf = null;
        if (keyStorePath != null && !keyStorePath.isBlank()) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(keyStorePath)) {
                ks.load(fis, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
            }
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        }

        TrustManagerFactory tmf = null;
        if (trustStorePath != null && !trustStorePath.isBlank()) {
            KeyStore ts = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(trustStorePath)) {
                ts.load(fis, trustStorePassword != null ? trustStorePassword.toCharArray() : null);
            }
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
        }

        SslContextBuilder builder = SslContextBuilder.forClient();
        if (kmf != null) builder.keyManager(kmf);
        if (tmf != null) builder.trustManager(tmf);
        return builder.build();
    }
}

package com.jamierf.mediamanager.io;

import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class InsecureHttpClientFactory extends HttpClientFactory {

    private class InsecureX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException { }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private final Scheme insecureHttps;

    public InsecureHttpClientFactory(HttpClientConfiguration configuration) throws NoSuchAlgorithmException, KeyManagementException {
        super(configuration);

        final SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[]{ new InsecureX509TrustManager() }, new SecureRandom());

        insecureHttps = new Scheme("https", 443, new SSLSocketFactory(context));
    }

    @Override
    public HttpClient build(DnsResolver resolver) {
        final HttpClient client = super.build(resolver);

        // Register the insecure https handler
        client.getConnectionManager().getSchemeRegistry().register(insecureHttps);

        return client;
    }
}

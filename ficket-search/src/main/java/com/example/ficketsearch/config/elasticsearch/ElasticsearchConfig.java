package com.example.ficketsearch.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.elasticsearch.host}")
    private String host;

    @Value("${spring.elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.ca-certificate-path}")
    private String certificateBase64;

    /**
     * ElasticsearchClient Bean 생성
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        // RestClient 설정
        RestClient restClient = restClient();

        // ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java8 시간 관련 클래스 처리

        // RestClientTransport를 사용하여 ElasticsearchTransport로 변환
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

        // ElasticsearchClient를 생성
        return new ElasticsearchClient(transport);
    }

    /**
     * Elasticsearch RestClient Bean 생성
     */
    @Bean
    public RestClient restClient() throws Exception {
        // CA 인증서 로드
        SSLContext sslContext = getSSLContext(); // getSSLContext()를 호출하여 SSLContext 객체 생성

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "https"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                        .setSSLContext(sslContext)  // getSSLContext()에서 반환된 SSLContext 설정
                        .setDefaultCredentialsProvider(credentialsProvider()));

        return builder.build();
    }

    private CredentialsProvider credentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

    private SSLContext getSSLContext() throws Exception {
        byte[] decodedCertificate = Base64.getMimeDecoder().decode(certificateBase64);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate ca;
        try (InputStream certificateInputStream = new ByteArrayInputStream(decodedCertificate)) {
            ca = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
        return sslContext;
    }
}

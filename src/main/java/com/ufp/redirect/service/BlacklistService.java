package com.ufp.redirect.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.*;
import java.nio.file.*;

import com.ufp.domainentry.domain.DomainEntry;

import org.apache.log4j.Logger;

@Service
public class BlacklistService implements InitializingBean {
    private static Logger logger = Logger.getLogger(BlacklistService.class);
    private CloseableHttpClient httpClient;

    @Value("${timeout.seconds}")
    private int timeoutInSeconds;
    
    public void afterPropertiesSet() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeoutInSeconds*1_000)
            .setConnectionRequestTimeout(0)
            .setSocketTimeout(60_000)
            .build();
        
        this.httpClient = HttpClientBuilder.create()
            .disableAutomaticRetries()
            //.disableRedirectHandling()
            .disableConnectionState()
            .setDefaultRequestConfig(requestConfig)
            .disableCookieManagement()
            .build();
    }

    class QueryResult {
        private int resultCode;
        private String redirect;

        public int getResultCode() {
            return this.resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public String getRedirect() {
            return this.redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }
    }

    public DomainEntry getRedirects(DomainEntry domainEntry) {
        domainEntry.setWorking(LocalDateTime.now());
        String host = domainEntry.getDomain();
        QueryResult queryResult = getQueryResult(new HttpGet(String.format("http://%s", domainEntry.getDomain())));
        if (queryResult.getResultCode() > 0) {
            domainEntry.setHttpStatusCode(queryResult.getResultCode());
        }
        if (StringUtils.isNotBlank(queryResult.getRedirect())) {
            domainEntry.setHttpRedirect(queryResult.getRedirect());
        }
        domainEntry.setHttpLastTime(LocalDateTime.now());
        
        queryResult = getQueryResult(new HttpGet(String.format("https://%s", domainEntry.getDomain())));
        if (queryResult.getResultCode() > 0) {
            domainEntry.setHttpsStatusCode(queryResult.getResultCode());
        }
        if (StringUtils.isNotBlank(queryResult.getRedirect())) {
            domainEntry.setHttpsRedirect(queryResult.getRedirect());
        }
        domainEntry.setHttpsLastTime(LocalDateTime.now());
        
        return domainEntry;
    }

    private QueryResult getQueryResult(HttpGet httpGet) {
        QueryResult queryResult = new QueryResult();
        HttpClientContext context = HttpClientContext.create();
            
        try (CloseableHttpResponse response = httpClient.execute(httpGet, context);) {
            StatusLine statusLine = response.getStatusLine();
            String statusPhrase = statusLine.getReasonPhrase();
            int statusCode = statusLine.getStatusCode();
            queryResult.setResultCode(statusCode);
            

            URI initialURI = httpGet.getURI();
            URI finalURI = null;
            List<URI> locations = context.getRedirectLocations();
            if (locations != null)
                finalURI = locations.get(locations.size()-1);
            if (!initialURI.equals(finalURI))
                queryResult.setRedirect(finalURI.getHost());
        } catch (Exception e) {
            queryResult.setResultCode(e.getClass().hashCode());
        }
        return queryResult;
    }
}

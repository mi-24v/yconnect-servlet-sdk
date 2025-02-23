/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2016 Yahoo Japan Corporation. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jp.co.yahoo.yconnect.core.http;

import jp.co.yahoo.yconnect.core.util.YConnectLogger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * HTTP Client Class for YConnect
 *
 * @author Copyright (C) 2016 Yahoo Japan Corporation. All Rights Reserved.
 */
public class YHttpClient {

    /**
     * SSL証明書チェック
     */
    private static boolean checkSSL = true; // default true
    /**
     * HTTP Proxy
     */
    private static HttpHost httpProxy = null;
    /**
     * {@link HttpURLConnection}インスタンス
     */
    private CloseableHttpClient httpClient = null;
    /**
     * レスポンスのHTTPステータスコード
     */
    private int responseCode;

    /**
     * レスポンスヘッダ<br>
     * フィールド名と値の{@link HashMap}
     */
    private final HttpHeaders responseHeaders;
    /**
     * レスポンスのHTTP応答メッセージ
     */
    private String responseMessage;
    /**
     * レスポンスボディ
     */
    private String responseBody;

    /**
     * HttpClientのコンストラクタです。
     */
    public YHttpClient() {
        // 初期化
        responseCode = 0;
        responseMessage = "";
        responseBody = "";
        responseHeaders = new HttpHeaders();
    }

    /**
     * ステータスコードを返します。
     *
     * @return ステータスコード
     */
    public int getStatusCode() {
        return responseCode;
    }

    /**
     * ステータスメッセージを返します。
     *
     * @return ステータスメッセージ
     */
    public String getStatusMessage() {
        return responseMessage;
    }

    /**
     * レスポンスヘッダを返します。
     *
     * @return レスポンスヘッダ
     */
    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * レスポンスボディを返します。
     *
     * @return レスポンスボディ
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * SSL証明書のチェックを無効化します。
     *
     * @param builder HTTP Client Builder
     */
    private static HttpClientBuilder ignoreSSLCertification(HttpClientBuilder builder) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(keyStore, (TrustStrategy) (chain, authType) -> true)
                    .build();

            SSLConnectionSocketFactory sslSocketFactory =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            builder.setSSLSocketFactory(sslSocketFactory);
            return builder;
        } catch (Exception e) {
            YConnectLogger.error(YHttpClient.class, e.getMessage());
            e.printStackTrace();
        }
        return builder;
    }

    /**
     * GETメソッドによるHTTPリクエストをします。
     *
     * @param urlString      URL文字列
     * @param parameters     {@link HttpParameters} の {@link ArrayList}
     * @param requestHeaders リクエストヘッダの {@link HashMap}
     */
    public void requestGet(
            String urlString, HttpParameters parameters, HttpHeaders requestHeaders) {

        // リクエストパラメータ設定
        if (parameters != null) {
            String queryString = parameters.toQueryString();
            urlString += "?" + queryString;
        }

        YConnectLogger.debug(this, "URL: " + urlString);

        // リクエストヘッダ設定
        HttpGet method = new HttpGet(urlString);
        if (requestHeaders != null) {
            for (String key : requestHeaders.keySet()) {
                String value = requestHeaders.get(key);
                method.setHeader(key, value);
                YConnectLogger.debug(this, key + ": " + value);
            }
        }

        request(urlString, method);
    }

    /**
     * POSTメソッドによるHTTPリクエストをします。
     *
     * @param urlString      URL文字列
     * @param parameters     {@link HttpParameters} の {@link ArrayList}
     * @param requestHeaders リクエストヘッダの {@link HashMap}
     */
    public void requestPost(
            String urlString, HttpParameters parameters, HttpHeaders requestHeaders) {

        YConnectLogger.debug(this, "URL: " + urlString);

        HttpPost method = new HttpPost(urlString);

        // リクエストヘッダ設定
        if (requestHeaders != null) {
            for (String key : requestHeaders.keySet()) {
                String value = requestHeaders.get(key);
                method.setHeader(key, value);
                YConnectLogger.debug(this, key + ": " + value);
            }
        }

        // リクエストパラメータ設定
        try {
            String queryString = parameters.toQueryString();

            StringEntity paramEntity = new StringEntity(queryString);
            paramEntity.setChunked(false);
            paramEntity.setContentType("application/x-www-form-urlencoded");
            method.setEntity(paramEntity);

            YConnectLogger.debug(this, "POST Body: " + queryString);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return;
        }

        request(urlString, method);
    }

    /**
     * エンドポイントに対してHTTPリクエストします。
     *
     * @param urlString URL文字列
     * @param request   リクエストオブジェクト
     */
    private void request(String urlString, HttpRequestBase request) {
        if (urlString.startsWith("https")) {
            setSSLConfiguration();
        }

        try {
            HttpResponse httpResponse = httpClient.execute(request);

            // レスポンスコード、メッセージ取得
            responseCode = httpResponse.getStatusLine().getStatusCode();
            responseMessage = httpResponse.getStatusLine().getReasonPhrase();

            YConnectLogger.debug(this, "responseCode: " + responseCode);
            YConnectLogger.debug(this, "responseMessage: " + responseMessage);

            // レスポンスヘッダ取得
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                responseHeaders.put(header.getName(), header.getValue());
            }

            YConnectLogger.debug(this, "responseHeaders: " + responseHeaders);

            // レスポンスボディ取得
            HttpEntity httpEntity = httpResponse.getEntity();
            responseBody = EntityUtils.toString(httpEntity);
            httpEntity.getContent().close();

            YConnectLogger.debug(this, "responseBody: " + responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * SSLの設定を行います。
     */
    private void setSSLConfiguration() {
        if (!checkSSL) {
            YConnectLogger.debug(this, "HTTPS ignore SSL Certification");
            httpClient = ignoreSSLCertification(HttpClientBuilder.create())
                    .setProxy(httpProxy)
                    .build();
            return;
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            httpClient = HttpClientBuilder.create()
                    .setSSLContext(sslContext)
                    .setProxy(httpProxy)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e1) {
            e1.printStackTrace();
        }
    }

    public static void enableSSLCheck() {
        checkSSL = true;
    }

    public static void disableSSLCheck() {
        checkSSL = false;
    }

    public static void setProxy(String proxyHost, int proxyPort) {
        httpProxy = new HttpHost(proxyHost, proxyPort);
    }
}

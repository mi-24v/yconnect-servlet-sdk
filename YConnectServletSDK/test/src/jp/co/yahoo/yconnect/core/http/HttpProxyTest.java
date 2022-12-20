/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2021 Yahoo Japan Corporation. All Rights Reserved.
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

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.socket.PortFactory;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpProxyTest {
    @Rule
    public MockServerRule r = new MockServerRule(this, proxyPort);

    private static final int proxyPort = PortFactory.findFreePort();
    private static final String expectBody = "test_proxyed_request";

    @Test
    public void testRequestProxied() {
        try (MockServerClient mockServer = new MockServerClient("localhost", proxyPort)) {
            mockServer.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/")
                    )
                    .respond(
                            response()
                                    .withBody(expectBody)
                    );
            YHttpClient.setProxy("localhost", proxyPort);
            YHttpClient.disableSSLCheck();
            YHttpClient client = new YHttpClient();
            client.requestGet("https://example.com", null, null);

            assertEquals(client.getResponseBody(), expectBody);
        }
    }
}
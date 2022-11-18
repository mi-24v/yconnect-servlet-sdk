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

package jp.co.yahoo.yconnect.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * YConnect Logger Class
 *
 * @author Copyright (C) 2016 Yahoo Japan Corporation. All Rights Reserved.
 */
public class YConnectLogger {

    private static String CONF_FILE = "yconnect_log_conf.xml";

    public static void setFilePath(String path) {
        YConnectLogger.CONF_FILE = path;
    }

    static {
        if (System.getProperty("log4j2.configurationFile") == null) {
            System.setProperty("log4j2.configurationFile", CONF_FILE);
        }
        if (System.getenv("YCONNECT_LOGGING_CONF_PATH") != null) {
            System.setProperty(
                    "log4j2.configurationFile", System.getenv("YCONNECT_LOGGING_CONF_PATH"));
        }
    }

    private static final Logger log = LogManager.getLogger(YConnectLogger.class.getName());

    public static void debug(Object object, String message) {
        log.debug("{} ({})", message, object.getClass().getName());
    }

    public static void info(Object object, String message) {
        log.info("{} ({})", message, object.getClass().getName());
    }

    public static void warn(Object object, String message) {
        log.warn("{} ({})", message, object.getClass().getName());
    }

    public static void error(Object object, String message) {
        log.error("{} ({})", message, object.getClass().getName());
    }

    public static void fatal(Object object, String message) {
        log.fatal("{} ({})", message, object.getClass().getName());
    }
}

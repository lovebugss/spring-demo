package com.example.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author renjp
 * @date 2021/8/20 13:56
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {
    public static Map<String, Object> queryStrToMap(String queryStr) {
        Map<String, Object> result = new HashMap<>(16);
        if (queryStr != null && queryStr.length() > 0) {
            String[] pairs = queryStr.split("&");
            try {
                for (String pair : pairs) {

                    int idx = pair.indexOf("=");
                    result.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            } catch (UnsupportedEncodingException ignore) {
                ignore.printStackTrace();
            }
        }
        return result;
    }

}

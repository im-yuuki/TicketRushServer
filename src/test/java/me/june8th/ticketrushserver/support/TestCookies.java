package me.june8th.ticketrushserver.support;

import jakarta.servlet.http.Cookie;
import me.june8th.ticketrushserver.utils.CookieUtils;

public abstract class TestCookies {

    public static Cookie accessToken(String value) {
        return new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, value);
    }

    public static Cookie operationId(String value) {
        return new Cookie(CookieUtils.OPERATION_ID_COOKIE_NAME, value);
    }

}

package potato.backend.global.constant;

public class SecurityConstant {

    // JWT
    public static final String REFRESH_TOKEN_COOKIE_NAME = "RTOKEN";
    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 60 * 30;
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 60 * 60 * 24 * 14;

    // OAuth2
    public static final long OAUTH2_TOKEN_EXPIRATION_SECONDS = 60 * 5;
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "OAUTH2_AUTHORIZATION_REQUEST";
    public static final String NEXT_URL_COOKIE_NAME = "NEXT_URL";
    public static final String NEXT_PARAM = "next";
}

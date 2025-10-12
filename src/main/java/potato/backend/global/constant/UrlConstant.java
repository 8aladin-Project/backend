package potato.backend.global.constant;

import java.util.List;

public class UrlConstant {

    public static final String LIVE_CLIENT_URL = "https://8aladin.shop";
    public static final String LIVE_SERVER_URL = "https://api.8aladin.shop";

    public static final String LOCAL_CLIENT_URL = "http://localhost:3000";
    public static final String LOCAL_SERVER_URL = "http://localhost:8080";

    public static final String DEFAULT_CLIENT_URL = LOCAL_CLIENT_URL; // 개발 중이므로 로컬로 설정

    public static final List<String> ALLOWED_CLIENT_URLS = List.of(
            LIVE_CLIENT_URL,
            LOCAL_CLIENT_URL,
            LOCAL_SERVER_URL,
            LIVE_SERVER_URL);

    public static final String LIVE_CLIENT_HOST = "8aladin.shop";
}

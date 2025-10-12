package potato.backend.global.util;

import com.google.common.net.InternetDomainName;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlUtil {

    public static String getRegistrableDomain(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return null;
        }

        try {
            InternetDomainName domainName = InternetDomainName.from(hostname);

            if (domainName.isUnderPublicSuffix()) {
                return domainName.topPrivateDomain().toString();
            } else {
                return domainName.toString();
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

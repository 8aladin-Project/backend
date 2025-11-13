package potato.backend.global.util;


import java.util.UUID;

public class FileNameUtils {
    public static String uniqueName(String original) {
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i > -1) ext = original.substring(i);
        return UUID.randomUUID() + ext;
    }
}
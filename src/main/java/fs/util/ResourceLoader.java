package fs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ResourceLoader {
    public static String loadAsString(String name, ClassLoader classLoader) {
        try {
            try (InputStream is = classLoader.getResourceAsStream(name)) {
                if (is == null) {
                    throw new RuntimeException("Could not find resource: " + name);
                }

                Scanner scanner = new Scanner(is).useDelimiter("\\Z");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load resource: " + name, e);
        }
    }
}

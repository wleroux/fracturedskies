package fs.util;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class ResourceLoader {
    public static String loadAsString(String name, ClassLoader classLoader) {
        try {
            try (InputStream is = classLoader.getResourceAsStream(name)) {
                if (is == null) {
                    throw new RuntimeException("Could not findComponentAt resource: " + name);
                }

                Scanner scanner = new Scanner(is).useDelimiter("\\Z");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load resource: " + name, e);
        }
    }

    public static ByteBuffer loadAsByteBuffer(String name, ClassLoader classLoader) {
        try {
            try (InputStream is = classLoader.getResourceAsStream(name)) {
                if (is == null) {
                    throw new RuntimeException("Could not findComponentAt resource: " + name);
                }

                try (ReadableByteChannel channel = Channels.newChannel(is)) {
                    ByteBuffer buffer = BufferUtils.createByteBuffer(8);

                    while (true) {
                        int read = channel.read(buffer);
                        if (read == -1) {
                            break;
                        }
                        if (buffer.remaining() == 0) {
                            ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 2);
                            buffer.flip();
                            newBuffer.put(buffer);
                            buffer = newBuffer;
                        }
                    }

                    buffer.flip();
                    return buffer;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load resource: " + name, e);
        }
    }
}

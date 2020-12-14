package crypto.util;

/**
 * @since 1.1
 */
public class MessageUtil {
    public static final int US_ASCII_CHAR_BYTE_LENGTH = 8;

    /**
     * Convert a given message to a byte array with US ASCII byte length of any char
     *
     * @since 1.1
     */
    public int[] convertMessageToByteArray(String message) {
        char[] chars = message.toCharArray();
        int[] bytes;

        StringBuilder builder = new StringBuilder();
        String buffer;
        for (int i = 0; i < message.length(); i++) {
            buffer = Integer.toString(chars[i], 2);
            while (buffer.length() != US_ASCII_CHAR_BYTE_LENGTH) {
                buffer = "0".concat(buffer);
            }
            builder.append(buffer);
        }

        bytes = builder.toString().chars().map(x -> x - '0').toArray();

        return bytes;
    }
}
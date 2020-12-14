package crypto.compress;

import crypto.util.PrintUtil;

import java.io.PrintStream;

public final class LempelZivCompress {

    private final PrintUtil printUtil;

    public LempelZivCompress(PrintStream stream) {
        printUtil = new PrintUtil(stream);
    }

    /**
     * Compress a given message by the Lempel and Ziv way.
     *
     * @param capacity should not be more than 9
     * @return a compressed message
     * @since 1.0
     */
    public String compress(String message, int capacity) {
        int bufferCapacity = capacity;
        int dictionaryCapacity = capacity;
        char[] dictionary = new char[dictionaryCapacity];
        char[] buffer = new char[bufferCapacity];
        char[] messageChars = message.toCharArray();
        StringBuilder encodedMessage = new StringBuilder();

        fillBuffer(buffer, messageChars);

        printUtil.println("Start compressing");

        while (buffer[0] != 0) {
            int p;
            int q = 0;
            char c = buffer[0];
            if ((p = hasChar(dictionary, buffer[0])) != 0) {
                q = getSequenceLength(dictionary, buffer, p - 1);
                int i = 0;
                do {
                    shiftLeft(dictionary, buffer, messageChars);
                    i++;
                } while (i < q);
                c = buffer[0];
            }

            encodedMessage.append(p);
            encodedMessage.append(q);
            encodedMessage.append(c);

            shiftLeft(dictionary, buffer, messageChars);

            printUtil.print("Message: ").printArray(messageChars).println()
                    .print("Buffer: ").printArray(buffer).println()
                    .print("Dictionary: ").printArray(dictionary).println()
                    .print("Encoded message: ").print(encodedMessage.toString()).println()
                    .println();
        }

        return encodedMessage.toString();
    }

    /**
     * Decompress a given message by the Lempel and Ziv way.
     *
     * @param bufferCapacity should not be more than 9
     * @return a decompressed message
     * @since 1.0
     */
    public String decompress(String message, int bufferCapacity) {
        StringBuilder decodedMessage = new StringBuilder();
        char[] messageChars = message.toCharArray();
        char[] buffer = new char[bufferCapacity];

        printUtil.println("Start decompressing");

        while (messageChars[0] != '\0') {
            int p = Integer.parseInt(Character.toString(messageChars[0]));
            int q = Integer.parseInt(Character.toString(messageChars[1]));
            char c = messageChars[2];

            decodeTriadInBuffer(buffer, messageChars, decodedMessage, p, q, c);

            printUtil.print("Message: ").printArray(messageChars).println()
                    .print("Buffer: ").printArray(buffer).println()
                    .println();
        }

        while (buffer[0] != '\0') {
            shiftLeftToDecodedMessage(buffer, decodedMessage, 1);
        }

        return decodedMessage.toString();
    }

    /**
     * @return a position of the symbol in the buffer
     * @since 1.0
     */
    private int hasChar(char[] array, char symbol) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == symbol) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * @return a length of sequence of the buffer in the dictionary
     * @since 1.0
     */
    private int getSequenceLength(char[] dictionary, char[] buffer, int currentPosition) {
        for (int i = 0; currentPosition + i < dictionary.length; i++) {
            if (dictionary[currentPosition + i] != buffer[i]) {
                return i;
            }
            if (currentPosition + i == dictionary.length - 1)
                return i + 1;
        }
        return 0;
    }

    /**
     * Fill the buffer with characters of a given message
     *
     * @since 1.0
     */
    private void fillBuffer(char[] buffer, char[] message) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = message[0];
            for (int j = 1; j < message.length; j++) {
                message[j - 1] = message[j];
                if (j == message.length - 1) {
                    message[j] = 0;
                }
            }
        }
    }

    /**
     * Shift to the left three containers of the characters with moving the characters towards each other
     *
     * @since 1.0
     */
    private void shiftLeft(char[] dictionary, char[] buffer, char[] message) {
        for (int j = 1; j < dictionary.length; j++) {
            dictionary[j - 1] = dictionary[j];
            if (j == dictionary.length - 1) {
                dictionary[j] = buffer[0];
            }
        }
        for (int j = 1; j < buffer.length; j++) {
            buffer[j - 1] = buffer[j];
            if (j == buffer.length - 1) {
                buffer[j] = message[0];
            }
        }
        for (int j = 1; j < message.length; j++) {
            message[j - 1] = message[j];
            if (j == message.length - 1) {
                message[j] = 0;
            }
        }
    }

    /**
     * Shift to the left three containers of the characters with moving the characters towards each other
     *
     * @param count a number of shifts
     * @since 1.0
     */
    private void shiftLeft(char[] buffer, int count) {
        for (int i = 0; i < count; i++) {
            for (int j = 1; j < buffer.length; j++) {
                buffer[j - 1] = buffer[j];
                if (j == buffer.length - 1) {
                    buffer[j] = '\0';
                }
            }
        }
    }

    /**
     * Shift from the buffer to the decoded message
     *
     * @since 1.0
     */
    private void shiftLeftToDecodedMessage(char[] buffer, StringBuilder decodedMessage, int count) {
        for (int i = 0; i < count; i++) {
            if (buffer[0] != '\0') {
                decodedMessage.append(buffer[0]);
            }

            for (int j = 1; j < buffer.length; j++) {
                buffer[j - 1] = buffer[j];
                if (j == buffer.length - 1) {
                    buffer[j] = '\0';
                }
            }
        }
    }

    /**
     * Decode triad as position of char in the buffer, length of sequence in the buffer and character
     *
     * @since 1.0
     */
    private void decodeTriadInBuffer(char[] buffer, char[] message, StringBuilder decodedMessage,
                                     int p, int q, char c) {
        if (p != 0 || q != 0) {
            for (int i = 0; i < q; i++) {

                char buf = buffer[p - 1];

                shiftLeftToDecodedMessage(buffer, decodedMessage, 1);
                buffer[buffer.length - 1] = buf;
            }
        }
        shiftLeftToDecodedMessage(buffer, decodedMessage, 1);
        buffer[buffer.length - 1] = c;
        shiftLeft(message, 3);
    }
}
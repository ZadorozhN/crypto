package crypto.compress;

import crypto.entropy.EntropyUtil;
import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HoffmanCode {

    private final PrintUtil printUtil;

    public HoffmanCode(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
    }

    /**
     * Return the sequence of characters which will be used to encode message
     * @since 1.0
     * @return a map with sequence of characters
     */
    public Map<Character, String> getHoffmanBinaryCodes(Map<Character, Double> probabilities) {
        Map<Character, StringBuilder> hoffmanBinaryCodesBuilder = new HashMap<>();
        Map<Character, String> hoffmanBinaryCodes = new HashMap<>();

        List<Map.Entry<Character, Double>> entries =
                probabilities.entrySet().stream().sorted(EntropyUtil.getComparatorForEntropy()).collect(Collectors.toList());
        entries.forEach(e -> hoffmanBinaryCodesBuilder.put(e.getKey(), new StringBuilder()));
        generateHoffmanBinaryCodes(hoffmanBinaryCodesBuilder, entries, 1d);
        hoffmanBinaryCodesBuilder.forEach((key, value) -> hoffmanBinaryCodes.put(key, value.toString()));

        return hoffmanBinaryCodes;
    }


    /**
     * Help method which helps generate hoffman binary codes.
     * Generate sequence of ones and zeros
     * @since 1.0
     */
    private void generateHoffmanBinaryCodes(Map<Character, StringBuilder> hoffmanBinaryCodes,
                                            List<Map.Entry<Character, Double>> entries, double pivotProbability) {
        if (entries.size() < 2) return;

        List<Map.Entry<Character, Double>> great = new ArrayList<>();
        List<Map.Entry<Character, Double>> less = new ArrayList<>();

        double probability = 0;
        for (var item : entries) {
            if (probability < pivotProbability / 2) {
                hoffmanBinaryCodes.get(item.getKey()).append('1');
                great.add(item);
            } else {
                hoffmanBinaryCodes.get(item.getKey()).append('0');
                less.add(item);
            }
            probability += item.getValue();
        }

        generateHoffmanBinaryCodes(hoffmanBinaryCodes, great, pivotProbability / 2);
        generateHoffmanBinaryCodes(hoffmanBinaryCodes, less, pivotProbability / 2);
    }

    /**
     * Build a string replacing characters with its sequence of ones and zeros
     * @since 1.0
     * @return a sequence of ones and zeros
     */
    public String encodeMessageByHoffman(Map<Character, String> hoffmanBinaryCodes, String message) {
        StringBuilder encodedMessage = new StringBuilder();

        message.chars().forEachOrdered(x -> encodedMessage.append(hoffmanBinaryCodes.get((char) x)));

        return encodedMessage.toString();
    }

    /**
     * Build a string replacing sequence of ones and zeros with its characters
     * @since 1.0
     * @return a decoded word
     */
    public String decodeMessageByHoffman(Map<Character, String> hoffmanBinaryCodes, String message) {
        StringBuilder decodedMessage = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        char[] messageChars = message.toCharArray();

        for (int i = 0; i < message.length(); i++) {
            buffer.append(messageChars[i]);
            if (hoffmanBinaryCodes.containsValue(buffer.toString())) {
                decodedMessage.append(hoffmanBinaryCodes.keySet().stream()
                        .filter(x -> hoffmanBinaryCodes.get(x).equals(buffer.toString())).findFirst().orElse('^'));
                buffer.delete(0, buffer.length());
            }
        }

        return decodedMessage.toString();
    }
}

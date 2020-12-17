package crypto.compress;

import crypto.entropy.EntropyUtil;
import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShannonFanoCode {

    private final PrintUtil printUtil;

    public ShannonFanoCode(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
    }

    /**
     * Return the sequence of characters which will be used to encode message
     *
     * @return a map with sequence of characters
     * @since 1.0
     */
    public Map<Character, String> getShannonFanoBinaryCodes(Map<Character, Double> probabilities) {
        Map<Character, StringBuilder> hoffmanBinaryCodesBuilder = new HashMap<>();
        Map<Character, String> hoffmanBinaryCodes = new HashMap<>();

        List<Map.Entry<Character, Double>> entries =
                probabilities.entrySet().stream().sorted(EntropyUtil.getComparatorForEntropy()).collect(Collectors.toList());
        entries.forEach(e -> hoffmanBinaryCodesBuilder.put(e.getKey(), new StringBuilder()));
        generateShannonFanoBinaryCodes(hoffmanBinaryCodesBuilder, entries, 1d);
        hoffmanBinaryCodesBuilder.forEach((key, value) -> hoffmanBinaryCodes.put(key, value.toString()));

        return hoffmanBinaryCodes;
    }


    /**
     * Help method which helps generate shannon fano binary codes.
     * Generate sequence of ones and zeros
     *
     * @since 1.0
     */
    private void generateShannonFanoBinaryCodes(Map<Character, StringBuilder> shannonFanoBinaryCodes,
                                                List<Map.Entry<Character, Double>> entries, double pivotProbability) {
        if (entries.size() < 2) return;

        List<Map.Entry<Character, Double>> great = new ArrayList<>();
        List<Map.Entry<Character, Double>> less = new ArrayList<>();

        double probability = 0;
        for (var item : entries) {
            if (probability < pivotProbability / 2) {
                shannonFanoBinaryCodes.get(item.getKey()).append('1');
                great.add(item);
            } else {
                shannonFanoBinaryCodes.get(item.getKey()).append('0');
                less.add(item);
            }
            probability += item.getValue();
        }

        generateShannonFanoBinaryCodes(shannonFanoBinaryCodes, great, pivotProbability / 2);
        generateShannonFanoBinaryCodes(shannonFanoBinaryCodes, less, pivotProbability / 2);
    }

    /**
     * Build a string replacing characters with its sequence of ones and zeros
     *
     * @return a sequence of ones and zeros
     * @since 1.0
     */
    public String encodeMessageByShannonFano(Map<Character, String> shannonFanoBinaryCodes, String message) {
        StringBuilder encodedMessage = new StringBuilder();

        message.chars().forEachOrdered(x -> encodedMessage.append(shannonFanoBinaryCodes.get((char) x)));

        return encodedMessage.toString();
    }

    /**
     * Build a string replacing sequence of ones and zeros with its characters
     *
     * @return a decoded word
     * @since 1.0
     */
    public String decodeMessageByShannonFano(Map<Character, String> shannonFanoBinaryCodes, String message) {
        StringBuilder decodedMessage = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        char[] messageChars = message.toCharArray();

        for (int i = 0; i < message.length(); i++) {
            buffer.append(messageChars[i]);
            if (shannonFanoBinaryCodes.containsValue(buffer.toString())) {
                decodedMessage.append(shannonFanoBinaryCodes.keySet().stream()
                        .filter(x -> shannonFanoBinaryCodes.get(x).equals(buffer.toString())).findFirst().orElse('^'));
                buffer.delete(0, buffer.length());
            }
        }

        return decodedMessage.toString();
    }
}
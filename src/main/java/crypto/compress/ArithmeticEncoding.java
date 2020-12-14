package crypto.compress;

import crypto.entropy.EntropyUtil;
import crypto.util.PrintUtil;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use the ArithmeticEncodingDecimal instead of ArithmeticEncoding if the length of sequence more than 10
 * @version 1.0
 * @deprecated use the ArithmeticEncodingDecimal instead of
 */
public final class ArithmeticEncoding {

    private final PrintUtil printUtil;
    private final EntropyUtil entropyUtil;

    public ArithmeticEncoding(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
        this.entropyUtil = new EntropyUtil(stream);
    }

    /**
     * Encode a given message by the arithmetic way
     *
     * @return a floating-point number that identifies a given word
     * @since 1.0
     */
    public double encode(String message) {
        Map<Character, Double> entropy = entropyUtil.getProbabilitiesOfCharsByMessage(message, message.chars().toArray());
        List<Map.Entry<Character, Double>> letters = entropy.entrySet().stream()
                .sorted(EntropyUtil.getComparatorForEntropy().reversed()).collect(Collectors.toList());

        LinkedHashMap<Character, Properties> lettersAndTheirProperties = new LinkedHashMap<>();

        double lowerBorder = 0;
        for (var i : letters) {
            lettersAndTheirProperties.put(i.getKey(),
                    new Properties(lowerBorder, i.getValue() + lowerBorder,
                            lowerBorder, i.getValue() + lowerBorder, i.getValue()));
            lowerBorder += i.getValue();
            printUtil.println(i.getKey() + " : " + lettersAndTheirProperties.get(i.getKey()).lowerBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).higherBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).probability);
        }

        return marking(lettersAndTheirProperties, message.toCharArray(), entropy, 0);
    }

    /**
     * Decode a given message by the arithmetic way
     *
     * @return a decoded word
     * @since 1.0
     */
    public String decode(Map<Character, Double> probabilities, double encodedWord, int numberOfCharsInMessage) {
        StringBuilder decodedMessage = new StringBuilder();
        List<Map.Entry<Character, Double>> letters = probabilities.entrySet().stream()
                .sorted(EntropyUtil.getComparatorForEntropy().reversed()).collect(Collectors.toList());

        LinkedHashMap<Character, Properties> lettersAndTheirProperties = new LinkedHashMap<>();

        double lowerBorder = 0;
        for (var i : letters) {
            lettersAndTheirProperties.put(i.getKey(),
                    new Properties(lowerBorder, i.getValue() + lowerBorder,
                            lowerBorder, i.getValue() + lowerBorder, i.getValue()));
            lowerBorder += i.getValue();
            printUtil.println(i.getKey() + " : " + lettersAndTheirProperties.get(i.getKey()).lowerBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).higherBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).probability);
        }

        double higher = 0;
        double lower = 0;
        boolean isFound = false;
        for (int i = 0; i < numberOfCharsInMessage; i++) {
            printUtil.println("step " + i);

            for (var j : lettersAndTheirProperties.entrySet()) {
                if (encodedWord > j.getValue().lowerBorder && encodedWord < j.getValue().higherBorder) {
                    decodedMessage.append(j.getKey());
                    higher = lettersAndTheirProperties.get(j.getKey()).higherBorder;
                    lower = lettersAndTheirProperties.get(j.getKey()).lowerBorder;
                    isFound = true;
                }
            }

            if (isFound) {
                for (var k : probabilities.keySet()) {
                    lettersAndTheirProperties.get(k).higherBorder = lower + (higher - lower)
                            * lettersAndTheirProperties.get(k).startHigherBorder;
                    lettersAndTheirProperties.get(k).lowerBorder = lower + (higher - lower)
                            * lettersAndTheirProperties.get(k).startLowerBorder;
                }
            }

            for (var j : lettersAndTheirProperties.entrySet()) {
                printUtil.println(j.getKey() + " : " + lettersAndTheirProperties.get(j.getKey()).lowerBorder
                        + " / " + lettersAndTheirProperties.get(j.getKey()).higherBorder
                        + " / " + lettersAndTheirProperties.get(j.getKey()).probability);
            }
        }

        return decodedMessage.toString();
    }

    /**
     * Util method that marks intervals during an encoding
     *
     * @return a code of characters sequence
     * @since 1.0
     */
    private double marking(Map<Character, Properties> map, char[] message,
                           Map<Character, Double> probabilities, int currentLetter) {
        double code;
        double higher = map.get(message[currentLetter]).higherBorder;
        double lower = map.get(message[currentLetter]).lowerBorder;
        for (var i : probabilities.keySet()) {
            map.get(i).higherBorder = lower + (higher - lower) * map.get(i).startHigherBorder;
            map.get(i).lowerBorder = lower + (higher - lower) * map.get(i).startLowerBorder;
        }

        printUtil.println("step " + currentLetter);
        for (var i : map.entrySet()) {
            printUtil.println(i.getKey() + " : " + i.getValue().lowerBorder
                    + " / " + i.getValue().higherBorder + " / " + i.getValue().probability);
        }

        if (currentLetter < message.length - 1) {
            code = marking(map, message, probabilities, currentLetter + 1);
        } else {
            double littleRest = map.get(message[currentLetter]).lowerBorder / Math.pow(10, message.length);
            code = map.get(message[currentLetter]).lowerBorder + littleRest;
        }
        return code;
    }

    private static class Properties {
        private double lowerBorder;
        private final double startLowerBorder;
        private double higherBorder;
        private final double startHigherBorder;
        private final double probability;

        private Properties(double lowerBorder, double higherBorder, double startLowerBorder,
                           double startHigherBorder, double probability) {
            this.lowerBorder = lowerBorder;
            this.higherBorder = higherBorder;
            this.probability = probability;
            this.startHigherBorder = startHigherBorder;
            this.startLowerBorder = startLowerBorder;
        }
    }
}
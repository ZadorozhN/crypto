package crypto.compress;

import crypto.entropy.EntropyUtil;
import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ArithmeticEncodingDecimal {

    private final PrintUtil printUtil;
    private final EntropyUtil entropyUtil;

    public ArithmeticEncodingDecimal(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
        this.entropyUtil = new EntropyUtil(stream);
    }

    /**
     * Encode a given message by the arithmetic way
     * @since 1.0
     * @return a floating-point number that identifies a given word
     */
    public BigDecimal encode(String message) {
        Map<Character, Double> entropy = entropyUtil.getProbabilitiesOfCharsByMessage(message, message.chars().toArray());
        List<Map.Entry<Character, Double>> letters = entropy.entrySet().stream()
                .sorted(EntropyUtil.getComparatorForEntropy().reversed()).collect(Collectors.toList());

        LinkedHashMap<Character, ArithmeticEncodingDecimal.Properties> lettersAndTheirProperties = new LinkedHashMap<>();

        BigDecimal lowerBorder = new BigDecimal(0);

        for (var i : letters) {
            lettersAndTheirProperties.put(i.getKey(),
                    new Properties(lowerBorder,
                            lowerBorder.add(BigDecimal.valueOf(i.getValue())),
                            lowerBorder, lowerBorder.add(BigDecimal.valueOf(i.getValue())),
                            BigDecimal.valueOf(i.getValue())));
            lowerBorder = lowerBorder.add(BigDecimal.valueOf(i.getValue()));
            printUtil.println(i.getKey() + " : " + lettersAndTheirProperties.get(i.getKey()).lowerBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).higherBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).probability);
        }

        return marking(lettersAndTheirProperties, message.toCharArray(), entropy, 0);
    }

    /**
     * Decode a given message by the arithmetic way
     * @since 1.0
     * @return a decoded word
     */
    public String decode(Map<Character, Double> entropy, BigDecimal encodedWord, int numberOfCharsInMessage) {
        StringBuilder decodedMessage = new StringBuilder();
        List<Map.Entry<Character, Double>> letters = entropy.entrySet().stream()
                .sorted(EntropyUtil.getComparatorForEntropy().reversed()).collect(Collectors.toList());

        LinkedHashMap<Character, ArithmeticEncodingDecimal.Properties> lettersAndTheirProperties = new LinkedHashMap<>();

        BigDecimal lowerBorder = new BigDecimal(0);

        for (var i : letters) {
            lettersAndTheirProperties.put(i.getKey(),
                    new Properties(lowerBorder,
                            lowerBorder.add(BigDecimal.valueOf(i.getValue())),
                            lowerBorder, lowerBorder.add(BigDecimal.valueOf(i.getValue())),
                            BigDecimal.valueOf(i.getValue())));
            lowerBorder = lowerBorder.add(BigDecimal.valueOf(i.getValue()));
            printUtil.println(i.getKey() + " :-: " + lettersAndTheirProperties.get(i.getKey()).lowerBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).higherBorder
                    + " / " + lettersAndTheirProperties.get(i.getKey()).probability);
        }

        for (int i = 0; i < numberOfCharsInMessage; i++) {
            printUtil.println("step " + i);

            for (var j : lettersAndTheirProperties.entrySet()) {
                if (j.getValue().lowerBorder.compareTo(encodedWord) < 0 && j.getValue().higherBorder.compareTo(encodedWord) > 0) {
                    decodedMessage.append(j.getKey());
                    BigDecimal higher = lettersAndTheirProperties.get(j.getKey()).higherBorder;
                    BigDecimal lower = lettersAndTheirProperties.get(j.getKey()).lowerBorder;
                    for (var k : entropy.keySet()) {
                        lettersAndTheirProperties.get(k).higherBorder =
                                lower.add((higher.subtract(lower)).multiply(lettersAndTheirProperties
                                        .get(k).startHigherBorder));
                        lettersAndTheirProperties.get(k).lowerBorder =
                                lower.add((higher.subtract(lower)).multiply(lettersAndTheirProperties
                                        .get(k).startLowerBorder));
                    }
                }

                printUtil.println(j.getKey() + " :-: " + lettersAndTheirProperties.get(j.getKey()).lowerBorder
                        + " / " + lettersAndTheirProperties.get(j.getKey()).higherBorder
                        + " / " + lettersAndTheirProperties.get(j.getKey()).probability);
            }
        }

        return decodedMessage.toString();
    }

    /**
     * Util method that marks intervals during an encoding
     * @since 1.0
     * @return a code of characters sequence
     */
    private BigDecimal marking(Map<Character, ArithmeticEncodingDecimal.Properties> map, char[] message,
                               Map<Character, Double> entropy, int currentLetter) {
        BigDecimal code;
        BigDecimal higher = map.get(message[currentLetter]).higherBorder;
        BigDecimal lower = map.get(message[currentLetter]).lowerBorder;
        for (var i : entropy.keySet()) {
            map.get(i).higherBorder = lower.add((higher.subtract(lower)).multiply(map.get(i).startHigherBorder));
            map.get(i).lowerBorder = lower.add((higher.subtract(lower)).multiply(map.get(i).startLowerBorder));
        }

        printUtil.println("step " + currentLetter);
        for (var i : map.entrySet()) {
            printUtil.println(i.getKey() + " : " + i.getValue().lowerBorder
                    + " / " + i.getValue().higherBorder
                    + " / " + i.getValue().probability);
        }

        if (currentLetter < message.length - 1) {
            code = marking(map, message, entropy, currentLetter + 1);
        } else {
            code = map.get(message[currentLetter]).lowerBorder;
        }

        return code;
    }

    private static class Properties {
        private BigDecimal lowerBorder;
        private final BigDecimal startLowerBorder;
        private BigDecimal higherBorder;
        private final BigDecimal startHigherBorder;
        private final BigDecimal probability;

        private Properties(BigDecimal lowerBorder, BigDecimal higherBorder, BigDecimal startLowerBorder,
                           BigDecimal startHigherBorder, BigDecimal probability) {
            this.lowerBorder = lowerBorder;
            this.higherBorder = higherBorder;
            this.probability = probability;
            this.startHigherBorder = startHigherBorder;
            this.startLowerBorder = startLowerBorder;
        }
    }
}

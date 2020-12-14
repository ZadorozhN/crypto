package crypto.entropy;

import crypto.alphabet.Alphabet;
import crypto.util.PrintUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public final class EntropyUtil {

    private final PrintUtil printUtil;

    public EntropyUtil(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
    }

    /**
     * @return a entropy of an alphabet on the base of a given file
     * @since 1.0
     */
    public double calculateEntropy(String pathToFile, int[] alphabet) {
        Map<Character, Double> probabilities = getProbabilitiesOfChars(pathToFile, alphabet);

        return -probabilities.values().stream().mapToDouble(i -> i * Math.log(i) / Math.log(2)).sum();

    }

    /**
     * Calculate a entropy of an alphabet with the condition that all probabilities of symbols equal to each other
     *
     * @return a entropy of an alphabet
     * @since 1.0
     */
    public double calculateEntropyWithEqualProbabilityOfSymbols(int lengthOfAlphabet) {
        return -IntStream.range(0, lengthOfAlphabet)
                .mapToDouble(i -> 1d / lengthOfAlphabet * Math.log(1d / lengthOfAlphabet) / Math.log(2)).sum();
    }

    /**
     * Calculate a entropy of an alphabet with the condition that all probabilities of symbols equal to each other
     * considering a mistake chance
     *
     * @return a entropy of an alphabet
     * @since 1.0
     */
    public double calculateEffectiveEntropyForBinaryAlphabet(double mistakeChance) {
        double lossOfInformation = 0;

        if (mistakeChance != 0) {
            lossOfInformation = -mistakeChance * Math.log(mistakeChance) / Math.log(2)
                    - (1 - mistakeChance) * Math.log(1 - mistakeChance) / Math.log(2);
        }

        return 1 - lossOfInformation;
    }

    /**
     * @return probabilities of all chars of the given alphabet of a given file
     * @since 1.0
     */
    public Map<Character, Double> getProbabilitiesOfChars(String pathToFile, int[] alphabet) {
        Map<Integer, FrequencyOfLetter> frequency = new HashMap<>();
        Map<Character, Double> probabilities = new HashMap<>();
        int available = 0;

        Arrays.stream(alphabet).forEach(c -> frequency.put(c, new FrequencyOfLetter()));
        try (FileInputStream fileStream = new FileInputStream(pathToFile)) {
            int c;
            available = fileStream.available();
            while ((c = fileStream.read()) != -1) {
                if (frequency.containsKey(c)) {
                    frequency.get(c).addFrequency();
                }
            }
        } catch (IOException e) {
            printUtil.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        final int total = available;
        Arrays.stream(alphabet).forEach(c -> probabilities.put((char) c, (double) frequency.get(c).getFrequency() / total));

        return probabilities;
    }

    /**
     * @return probabilities of all chars of the given alphabet of a given message
     * @since 1.0
     */
    public Map<Character, Double> getProbabilitiesOfCharsByMessage(String message, int[] alphabet) {
        Map<Integer, FrequencyOfLetter> frequency = new HashMap<>();
        Map<Character, Double> probabilities = new HashMap<>();
        Arrays.stream(alphabet).forEach(c -> frequency.put(c, new FrequencyOfLetter()));

        char[] messageChars = message.toCharArray();
        for (char messageChar : messageChars) {
            if (frequency.containsKey((int) messageChar))
                frequency.get((int) messageChar).addFrequency();
        }

        Arrays.stream(alphabet).forEach(c -> probabilities.put((char) c, (double) frequency.get(c).getFrequency() / messageChars.length));

        return probabilities;
    }

    /**
     * @return an integer array of an alphabet characters codes
     * @since 1.0
     */
    public int[] initializeAlphabet(Alphabet language) {

        switch (language) {
            case RUSSIAN:
                return "абвгдеёжзийклмнопрстуфхцчшщъыьэюя".chars().toArray();
            case BINARY:
                return "01".chars().toArray();
            default:
                return "abcdefghijklmnopqrstuvwxyz".chars().toArray();
        }
    }

    public static Comparator<Map.Entry<Character, Double>> getComparatorForEntropy() {
        return (o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());
        };
    }
}
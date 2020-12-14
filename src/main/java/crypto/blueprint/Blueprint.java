package crypto.blueprint;

import crypto.compress.ArithmeticEncoding;
import crypto.compress.ArithmeticEncodingDecimal;
import crypto.compress.HoffmanCode;
import crypto.compress.LempelZivCompress;
import crypto.entropy.EntropyUtil;
import crypto.interleaving.BlockInterleaving;
import crypto.recoverycode.CycleCode;
import crypto.recoverycode.HammingCode;
import crypto.recoverycode.IterativeCode;
import crypto.recoverycode.ModifiedHammingCode;
import crypto.util.MessageUtil;
import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Provide to use samples of all algorithms of this library
 * @since 1.1
 */
public class Blueprint {

    private PrintStream stream;
    private PrintUtil printUtil;
    private MessageUtil messageUtil;
    private Random random;

    public Blueprint(PrintStream stream) {
        this.stream = stream;
        this.printUtil = new PrintUtil(stream);
        this.messageUtil = new MessageUtil();
        this.random = new Random();
    }

    /**
     * Hamming code usage blueprint
     *
     * @since 1.1
     */
    public void hammingCodeBlueprint(String message, int numberOfMistakes) {
        HammingCode hammingCode = new HammingCode(stream);

        int[] informationWord = messageUtil.convertMessageToByteArray(message);
        int numberOfInformationWordBytes = informationWord.length;
        int numberOfRedundantBytes = (int) Math.ceil(Math.log(numberOfInformationWordBytes) / Math.log(2) + 1);
        int[][] checkMatrix = hammingCode.getCheckMatrix(numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] redundantBytes = hammingCode.calculateRedundantBytes(checkMatrix, informationWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] encodedWord = hammingCode.encodeMessage(checkMatrix, informationWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);

        // sending and receiving;

        int[] receivedEncodedWord = encodedWord.clone();
        List<Integer> bytesWithMistakes = new ArrayList<>();

        for (int i = 0; i < numberOfMistakes; i++) {
            int randomPosition = Math.abs(random.nextInt()) % receivedEncodedWord.length;
            if (!bytesWithMistakes.contains(randomPosition)) {
                if (receivedEncodedWord[randomPosition] == 0) {
                    receivedEncodedWord[randomPosition] = 1;
                } else {
                    receivedEncodedWord[randomPosition] = 0;
                }
                printUtil.print("mistake in ").println(randomPosition);
                bytesWithMistakes.add(randomPosition);
            } else {
                i--;
            }
        }

        int[] receivedRedundantBytes = hammingCode.getRedundantBytes(receivedEncodedWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] calculatedRedundantBytes = hammingCode.calculateRedundantBytes(checkMatrix, receivedEncodedWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] syndrome = hammingCode.getSyndrome(receivedRedundantBytes, calculatedRedundantBytes);
        int[] recoveryBytes = hammingCode.getRecoveryBytes(syndrome, checkMatrix,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] recoveredMessage = hammingCode.recoverMessage(receivedEncodedWord, recoveryBytes);

        printUtil.println("the sequence of bytes is")
                .printArray(informationWord).println()
                .println("the check matrix is")
                .printMatrix(checkMatrix)
                .println("the calculated redundant bytes are")
                .printArray(redundantBytes).println()
                .println("the encoded word is")
                .printArray(encodedWord).println()
                .println("the sent word is")
                .printArray(encodedWord).println()
                .println("the received word is")
                .printArray(receivedEncodedWord).println()
                .println("the redundant bytes are")
                .printArray(receivedRedundantBytes).println()
                .println("the calculated redundant bytes are")
                .printArray(calculatedRedundantBytes).println()
                .println("the syndrome is")
                .printArray(syndrome).println()
                .println("the sent word is")
                .printArray(encodedWord).println()
                .println("the received word is")
                .printArray(receivedEncodedWord).println()
                .println("the recovery bytes are")
                .printArray(recoveryBytes).println()
                .println("the recovered message is")
                .printArray(recoveredMessage).println();
    }

    /**
     * Modified hamming code usage blueprint
     *
     * @since 1.1
     */
    public void modifiedHammingCodeBlueprint(String message, int numberOfMistakes) {
        HammingCode hammingCode = new HammingCode(stream);
        ModifiedHammingCode modifiedHammingCode = new ModifiedHammingCode(stream);

        int[] informationWord = messageUtil.convertMessageToByteArray(message);
        int numberOfInformationWordBytes = informationWord.length;
        int numberOfRedundantBytes = (int) Math.ceil(Math.log(numberOfInformationWordBytes) / Math.log(2) + 1);
        int[][] checkMatrix = hammingCode.getCheckMatrix(numberOfInformationWordBytes, numberOfRedundantBytes);
        int[][] modifiedCheckMatrix = modifiedHammingCode.modifyCheckMatrix(checkMatrix);
        numberOfRedundantBytes++;
        int[] redundantBytes = hammingCode.calculateRedundantBytes(modifiedCheckMatrix, informationWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] encodedWord = hammingCode.encodeMessage(modifiedCheckMatrix, informationWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);

        // sending and receiving;

        int[] receivedEncodedWord = encodedWord.clone();
        List<Integer> bytesWithMistakes = new ArrayList<>();

        for (int i = 0; i < numberOfMistakes; i++) {
            int randomPosition = Math.abs(random.nextInt()) % receivedEncodedWord.length;
            if (!bytesWithMistakes.contains(randomPosition)) {
                if (receivedEncodedWord[randomPosition] == 0) {
                    receivedEncodedWord[randomPosition] = 1;
                } else {
                    receivedEncodedWord[randomPosition] = 0;
                }
                printUtil.print("mistake in ").println(randomPosition);
                bytesWithMistakes.add(randomPosition);
            } else {
                i--;
            }
        }

        int[] receivedRedundantBytes = hammingCode.getRedundantBytes(receivedEncodedWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] calculatedRedundantBytes = hammingCode.calculateRedundantBytes(modifiedCheckMatrix, receivedEncodedWord,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] syndrome = hammingCode.getSyndrome(receivedRedundantBytes, calculatedRedundantBytes);
        int[] recoveryBytes = hammingCode.getRecoveryBytes(syndrome, modifiedCheckMatrix,
                numberOfInformationWordBytes, numberOfRedundantBytes);
        int[] recoveredMessage = hammingCode.recoverMessage(receivedEncodedWord, recoveryBytes);

        printUtil.println("the sequence of bytes is")
                .printArray(informationWord).println()
                .println("the check matrix is")
                .printMatrix(checkMatrix)
                .println("the modified check matrix is")
                .printMatrix(modifiedCheckMatrix).println()
                .println("the number of redundant bytes was incremented")
                .println("the calculated redundant bytes are")
                .printArray(redundantBytes).println()
                .println("the encoded word is")
                .printArray(encodedWord).println()
                .println("the sent word is")
                .printArray(encodedWord).println()
                .println("the received word is")
                .printArray(receivedEncodedWord).println()
                .println("the redundant bytes are")
                .printArray(receivedRedundantBytes).println()
                .println("the calculated redundant bytes are")
                .printArray(calculatedRedundantBytes).println()
                .println("the syndrome is")
                .printArray(syndrome).println()
                .println("the sent word is")
                .printArray(encodedWord).println()
                .println("the received word is")
                .printArray(receivedEncodedWord).println()
                .println("the recovery bytes are")
                .printArray(recoveryBytes).println()
                .println("the recovered message is")
                .printArray(recoveredMessage).println();
    }

    /**
     * Iterative code usage blueprint
     *
     * @since 1.1
     */
    public void iterativeCodeBlueprint(String message, int numberOfRows, int numberOfColumns, int numberOfMistakes) {
        IterativeCode iterativeCode = new IterativeCode(stream);

        int[] chars = messageUtil.convertMessageToByteArray(message);
        int[][] matrix = iterativeCode.buildMatrix(chars, numberOfRows, numberOfColumns);
        int[] encodedMessage = iterativeCode.convertMatrixToMessage(matrix, numberOfRows, numberOfColumns);

        //sending and receiving

        int[] receivedEncodedMessage = encodedMessage.clone();
        List<Integer> bytesWithMistakes = new ArrayList<>();

        for (int i = 0; i < numberOfMistakes; i++) {
            int randomPosition = Math.abs(random.nextInt()) % (numberOfRows * numberOfColumns);
            if (!bytesWithMistakes.contains(randomPosition)) {
                if (receivedEncodedMessage[randomPosition] == 0) {
                    receivedEncodedMessage[randomPosition] = 1;
                } else {
                    receivedEncodedMessage[randomPosition] = 0;
                }
                printUtil.print("mistake in ").println(randomPosition);
                bytesWithMistakes.add(randomPosition);
            } else {
                i--;
            }
        }

        int[] receivedEncodedMessageWithoutRedundantBytes = iterativeCode
                .getMessageWithoutRedundantBytes(receivedEncodedMessage, numberOfRows, numberOfColumns);
        int[] receivedRedundantBytes = iterativeCode
                .getRedundantBytesFromMessage(receivedEncodedMessage, numberOfRows, numberOfColumns);
        int[] calculatedRedundantBytes = iterativeCode
                .calculateRedundantBytes(receivedEncodedMessageWithoutRedundantBytes, numberOfRows, numberOfColumns);
        int[] syndrome = iterativeCode.getSyndrome(receivedRedundantBytes, calculatedRedundantBytes,
                receivedEncodedMessage, numberOfRows, numberOfColumns);
        int[] recoveryBytes = iterativeCode.getRecoveryBytes(syndrome, numberOfRows, numberOfColumns);
        int[] recoveredMessage = iterativeCode.recoverMessage(receivedEncodedMessage, recoveryBytes);

        printUtil.println("the message is")
                .println(message)
                .println("the encoded matrix is")
                .printMatrix(matrix)
                .println("the encoded message is")
                .printArray(encodedMessage).println()
                .println("the received encoded message is")
                .printArray(receivedEncodedMessage).println()
                .println("the received encoded message without redundant bytes is")
                .printArray(receivedEncodedMessageWithoutRedundantBytes).println()
                .println("the received redundant bytes are")
                .printArray(receivedRedundantBytes).println()
                .println("the calculated redundant bytes are")
                .printArray(calculatedRedundantBytes).println()
                .println("the syndrome is")
                .printArray(syndrome).println()
                .println("the encoded message is")
                .printArray(encodedMessage).println()
                .println("the received encoded message is")
                .printArray(receivedEncodedMessage).println()
                .println("the recovery bytes are")
                .printArray(recoveryBytes).println()
                .println("the recovered message is")
                .printArray(recoveredMessage).println();
    }

    /**
     * Cycle code usage blueprint
     *
     * @since 1.1
     */
    public void cycleCodeBlueprint(int[] message, int numberOfInformationBytes, int codeWordLength,
                                   int[] generatingPolynomial, int numberOfMistakes) {

        CycleCode cycleCode = new CycleCode(stream);

        int[] polynomial = message.clone();

        int[] encodedMessage = cycleCode.encode(numberOfInformationBytes, codeWordLength,
                polynomial, generatingPolynomial);
        int[][] generatingMatrix = cycleCode.getGeneratingMatrix(numberOfInformationBytes,
                codeWordLength, generatingPolynomial);

        // sending and receiving

        int[] receivedEncodedMessage = encodedMessage.clone();
        List<Integer> bytesWithMistakes = new ArrayList<>();

        for (int i = 0; i < numberOfMistakes; i++) {
            int randomPosition = Math.abs(random.nextInt()) % receivedEncodedMessage.length;
            if (!bytesWithMistakes.contains(randomPosition)) {
                if (receivedEncodedMessage[randomPosition] == 0) {
                    receivedEncodedMessage[randomPosition] = 1;
                } else {
                    receivedEncodedMessage[randomPosition] = 0;
                }
                printUtil.print("mistake in ").println(randomPosition);
                bytesWithMistakes.add(randomPosition);
            } else {
                i--;
            }
        }

        int[] syndrome = cycleCode.calculateSyndrome(receivedEncodedMessage, generatingPolynomial, numberOfInformationBytes);
        int[] recoveryBytes = cycleCode.getRecoveryBytes(generatingMatrix, syndrome, numberOfInformationBytes);
        int[] recoveredMessage = cycleCode.recoverMessage(receivedEncodedMessage, recoveryBytes);

        printUtil.println("the message is")
                .printArray(polynomial).println()
                .println("the polynomial of message is")
                .printArray(polynomial).println()
                .println("the encoded message is")
                .printArray(encodedMessage).println()
                .println("the generating matrix is")
                .printMatrix(generatingMatrix)
                .println("the sent message is")
                .printArray(encodedMessage).println()
                .println("the received message is")
                .printArray(receivedEncodedMessage).println()
                .println("the syndrome is")
                .printArray(syndrome).println()
                .println("the encoded message is")
                .printArray(encodedMessage).println()
                .println("the received encoded message is")
                .printArray(receivedEncodedMessage).println()
                .println("the recovery bytes are")
                .printArray(recoveryBytes).println()
                .println("the recovered message is")
                .printArray(recoveredMessage).println();
    }

    /**
     * Arithmetic encoding decimal code usage blueprint
     *
     * @since 1.1
     */
    public void arithmeticEncodingDecimalBlueprint(String message) {
        ArithmeticEncodingDecimal arithmeticEncodingDecimal = new ArithmeticEncodingDecimal(stream);
        EntropyUtil entropyUtil = new EntropyUtil(stream);

        BigDecimal encodedMessage = arithmeticEncodingDecimal.encode(message);
        Map<Character, Double> probabilities = entropyUtil
                .getProbabilitiesOfCharsByMessage(message, message.chars().toArray());
        String decodedMessage = arithmeticEncodingDecimal.decode(probabilities, encodedMessage, message.length());

        printUtil.println("the message is")
                .println(message)
                .println("the encoded message is")
                .println(encodedMessage.toString())
                .println("the decoded message is")
                .println(decodedMessage);
    }

    /**
     * Arithmetic encoding code usage blueprint
     *
     * @since 1.1
     */
    public void arithmeticEncodingBlueprint(String message) {
        ArithmeticEncoding arithmeticEncodingDecimal = new ArithmeticEncoding(stream);
        EntropyUtil entropyUtil = new EntropyUtil(stream);

        double encodedMessage = arithmeticEncodingDecimal.encode(message);
        Map<Character, Double> probabilities = entropyUtil
                .getProbabilitiesOfCharsByMessage(message, message.chars().toArray());
        String decodedMessage = arithmeticEncodingDecimal.decode(probabilities, encodedMessage, message.length());

        printUtil.println("the message is")
                .println(message)
                .println("the encoded message is")
                .println(String.valueOf(encodedMessage))
                .println("the decoded message is")
                .println(decodedMessage);
    }

    /**
     * Hoffman code usage blueprint
     *
     * @since 1.1
     */
    public void hoffmanCodeBlueprint(String message, int[] alphabet) {
        HoffmanCode hoffmanCode = new HoffmanCode(stream);
        EntropyUtil entropyUtil = new EntropyUtil(stream);

        Map<Character, Double> probabilities = entropyUtil.getProbabilitiesOfCharsByMessage(message, alphabet);
        Map<Character, String> hoffmanCodes = hoffmanCode.getHoffmanBinaryCodes(probabilities);
        String encodedMessage = hoffmanCode.encodeMessageByHoffman(hoffmanCodes, message);
        String decodedMessage = hoffmanCode.decodeMessageByHoffman(hoffmanCodes, encodedMessage);
        int lengthOfWordAsciiCodingWay = message.length() * MessageUtil.US_ASCII_CHAR_BYTE_LENGTH;

        printUtil.println("the message is")
                .println(message)
                .println("the probabilities of chars are")
                .printMap(probabilities)
                .println("the hoffman chars codes are")
                .printMap(hoffmanCodes).println()
                .println("the encoded message")
                .println(encodedMessage)
                .println("the decoded message")
                .println(decodedMessage)
                .println("the length of word decoded by hoffman codes")
                .println(encodedMessage.length())
                .println("the length of word decoded by US ASCII is")
                .println(lengthOfWordAsciiCodingWay)
                .println("the effectivity is")
                .println((double) encodedMessage.length() / lengthOfWordAsciiCodingWay);
    }

    /**
     * Lempel Ziv compress usage blueprint
     *
     * @since 1.1
     */
    public void lempelZivCompressBlueprint(String message, int capacity) {
        LempelZivCompress lempelZivCompress = new LempelZivCompress(stream);

        String encodedMessage = lempelZivCompress.compress(message, capacity);
        String decodedMessage = lempelZivCompress.decompress(encodedMessage, capacity);

        printUtil.println("the message is")
                .println(message)
                .println("the encoded message is")
                .println(encodedMessage)
                .println("the decoded message is")
                .println(decodedMessage);
    }

    /**
     * Block interleaving compress usage blueprint
     *
     * @since 1.1
     */
    public void blockInterleavingSample(String message, int numberOfInformationBytes,
                                        int numberOfRedundantBytes, int numberOfDamagedPackage) {

        BlockInterleaving blockInterleaving = new BlockInterleaving(stream);
        int[] bytes = messageUtil.convertMessageToByteArray(message);
        int codeWordLength = numberOfInformationBytes + numberOfRedundantBytes;

        int[][] informationWordMatrix = blockInterleaving.getInformationBytesMatrix(bytes, numberOfInformationBytes);
        int[][] encodedWordMatrix = blockInterleaving.getEncodedBytesMatrix(informationWordMatrix,
                numberOfInformationBytes, numberOfRedundantBytes);
        int[] interleavedSequence = blockInterleaving.getInterleavedSequence(encodedWordMatrix);

        //sending and receiving
        int[] receivedInterleavedSequence = interleavedSequence.clone();

        int packageLength = bytes.length / numberOfInformationBytes;
        for(int i = 0; i < packageLength; i++){
            receivedInterleavedSequence[numberOfDamagedPackage*packageLength + i] = 1
                    ^ interleavedSequence[numberOfDamagedPackage * packageLength + i];
        }

        int[][] deinterleavedSequenceMatrix = blockInterleaving.deinterleaveSequence(receivedInterleavedSequence, codeWordLength);
        int[][] recoveredMatrix = blockInterleaving.recoverEncodedBytesMatrix(deinterleavedSequenceMatrix,
                numberOfInformationBytes, numberOfRedundantBytes);

        int[][] decodedBytesMatrix = blockInterleaving.decodeBytesMatrix(recoveredMatrix, numberOfInformationBytes);
        int[] gottenMessage = blockInterleaving.getMessageSequence(decodedBytesMatrix);

        printUtil.println("the message is")
                .println(message)
                .println("the bytes of message")
                .printArray(bytes).println()
                .println("the information word matrix is")
                .printMatrix(informationWordMatrix)
                .println("the encoded word matrix is")
                .printMatrix(encodedWordMatrix)
                .println("the interleaved sequence is")
                .printArray(interleavedSequence).println()
                .println("the received interleaved sequence is")
                .printArray(receivedInterleavedSequence).println()
                .println("the deinterleaved sequence matrix is")
                .printMatrix(deinterleavedSequenceMatrix)
                .println("the recovered matrix is")
                .printMatrix(recoveredMatrix)
                .println("the decoded bytes matrix is")
                .printMatrix(decodedBytesMatrix)
                .println("the sent message is")
                .printArray(bytes).println()
                .println("the decoded message is")
                .printArray(gottenMessage).println();
    }
}
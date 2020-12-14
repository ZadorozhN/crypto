package crypto.util;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;

public final class PrintUtil {

    private final PrintStream stream;

    public PrintUtil(PrintStream stream) {
        this.stream = stream;
    }

    public PrintUtil print(String string) {
        stream.print(string);

        return this;
    }

    public PrintUtil print(int number) {
        stream.print(number);

        return this;
    }

    public PrintUtil println(String string) {
        stream.print(string + "\n");

        return this;
    }

    public PrintUtil println(int number) {
        stream.println(number);

        return this;
    }

    public PrintUtil println(double number) {
        stream.println(number);

        return this;
    }

    public PrintUtil println(float number) {
        stream.println(number);

        return this;
    }

    public PrintUtil printArray(int[] array) {
        for (int j : array) {
            stream.print(j);
        }

        return this;
    }

    public PrintUtil printArray(char[] array) {
        for (char c : array) {
            stream.print(c);
        }

        return this;
    }

    public PrintUtil printMatrix(int[][] matrix) {
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                stream.print(anInt);
            }
            stream.println();
        }

        return this;
    }

    public PrintUtil printMatrix(char[][] matrix) {
        for (char[] chars : matrix) {
            for (char aChar : chars) {
                stream.print(aChar);
            }
            stream.println();
        }

        return this;
    }

    public PrintUtil println() {
        stream.println();

        return this;
    }

    public <T, G> PrintUtil printMap(Map<T, G> map) {
        stream.println("Key:Value");
        map.forEach((key, value) -> stream.printf("%s:%s\n", key.toString(), value.toString()));

        return this;
    }
}
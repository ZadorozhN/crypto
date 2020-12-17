package crypto;

import crypto.alphabet.Alphabet;
import crypto.blueprint.Blueprint;
import crypto.entropy.EntropyUtil;

public class Runner {
    public static void main(String[] args) {
        Blueprint blueprint = new Blueprint(System.out);
        EntropyUtil entropyUtil = new EntropyUtil(System.out);
    }
}
package crypto.entropy;

import java.util.concurrent.atomic.AtomicInteger;

class FrequencyOfLetter {
    private final AtomicInteger frequency;

    public FrequencyOfLetter() {
        frequency = new AtomicInteger(0);
    }

    public int getFrequency() {
        return frequency.get();
    }

    public void addFrequency() {
        frequency.incrementAndGet();
    }
}
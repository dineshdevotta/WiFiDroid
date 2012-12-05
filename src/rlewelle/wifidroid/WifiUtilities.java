package rlewelle.wifidroid;

public class WifiUtilities {
    // All in MHz
    private static final int MIN_2GHZ_FREQ = 2412;
    private static final int MAX_2GHZ_FREQ = 2472;
    private static final int CHANNEL_BANDWIDTH = 5;

    /**
     * Given a frequency in the 2.4GHz range, returns the corresponding channel number
     * @param frequency
     * @return -1 if the frequency is outside of the 2.4GHz range, the channel number otherwise
     */
    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= MIN_2GHZ_FREQ && frequency <= MAX_2GHZ_FREQ)
            return (frequency - MIN_2GHZ_FREQ) / CHANNEL_BANDWIDTH + 1;

        return -1;
    }
}

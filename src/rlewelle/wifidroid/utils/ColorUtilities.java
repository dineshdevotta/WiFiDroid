package rlewelle.wifidroid.utils;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

public class ColorUtilities {
    private static Random random = new Random();
    private static ArrayList<Integer> colors = new ArrayList<>();

    public static Integer getColor(int i) {
        if (i <= colors.size()) {
            for (int j=colors.size(); j<=i; ++j) {
                colors.add(getRandomColor());
            }
        }

        return colors.get(i);
    }

    public static int getRandomColor() {
        return Color.rgb(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        );
    }
}

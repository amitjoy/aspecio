package com.amitinside.aspecio.test.component;

import com.amitinside.aspecio.test.api.Randomizer;

public class RandomizerImpl implements Randomizer {

    @Override
    public int randomInt(final int max) {
        // very random!
        return Math.max(max, 42);
    }

}
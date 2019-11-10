package com.amitinside.aspecio.util;

public final class Exceptions {

    private Exceptions() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    public static RuntimeException duck(final Throwable t) {
        Exceptions.throwsUnchecked(t);
        throw new AssertionError("unreachable");
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwsUnchecked(final Throwable throwable) throws E {
        throw (E) throwable;
    }

}
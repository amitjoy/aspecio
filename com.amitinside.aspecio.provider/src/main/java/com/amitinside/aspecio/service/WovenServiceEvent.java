package com.amitinside.aspecio.service;

import java.util.EnumSet;
import java.util.Set;

public final class WovenServiceEvent {

    enum EventKind {
        SERVICE_ARRIVAL,
        SERVICE_UPDATE,
        SERVICE_DEPARTURE
    }

    enum ChangeEvent {
        REQUIRED_ASPECT_CHANGE,
        OPTIONAL_ASPECT_CHANGE,
        SERVICE_PROPERTIES_CHANGE
    }

    public static final WovenServiceEvent SERVICE_REGISTRATION = new WovenServiceEvent(EventKind.SERVICE_ARRIVAL,
            EnumSet.noneOf(ChangeEvent.class));
    public static final WovenServiceEvent SERVICE_DEPARTURE    = new WovenServiceEvent(EventKind.SERVICE_DEPARTURE,
            EnumSet.noneOf(ChangeEvent.class));

    public final EventKind         kind;
    private final Set<ChangeEvent> changeEvents;

    public WovenServiceEvent(final EventKind kind, final Set<ChangeEvent> changeEvents) {
        this.kind         = kind;
        this.changeEvents = changeEvents;
    }

    public boolean matchesCause(final ChangeEvent changeEvent) {
        return changeEvents.contains(changeEvent);
    }

}

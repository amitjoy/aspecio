/*******************************************************************************
 * Copyright 2021 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
    public static final WovenServiceEvent SERVICE_DEPARTURE = new WovenServiceEvent(EventKind.SERVICE_DEPARTURE,
            EnumSet.noneOf(ChangeEvent.class));

    public final EventKind kind;
    private final Set<ChangeEvent> changeEvents;

    public WovenServiceEvent(final EventKind kind, final Set<ChangeEvent> changeEvents) {
        this.kind = kind;
        this.changeEvents = changeEvents;
    }

    public boolean matchesCause(final ChangeEvent changeEvent) {
        return changeEvents.contains(changeEvent);
    }

}

package com.vahid.appconn.domain;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/**
 * Holds the in memory state of the {@link MobileAppActor}.
 * The state simply consists of the unique IP addresses the app has communicated with and the total number
 * of events the app has sent to the server.
 * This state is rebuilt by the actor in case of a failure.
 */
public class MobileAppState {

    public static final MobileAppState EMPTY = new MobileAppState(HashSet.empty(), 0);

    private final Set<Long> ips;
    private final int eventCount;

    public MobileAppState(Set<Long> ips, int eventCount) {
        this.ips = ips;
        this.eventCount = eventCount;
    }

    public MobileAppState withNewIp(long newIp) {
        return new MobileAppState(ips.add(newIp), eventCount + 1);
    }

    public Set<Long> getIps() {
        return ips;
    }

    public int getEventCount() {
        return eventCount;
    }
}

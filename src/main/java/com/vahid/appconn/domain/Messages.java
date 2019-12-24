package com.vahid.appconn.domain;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Messages {
    /**
     * A wrapper for commands that an {@link MobileAppActor} can handle.
     * The wrapper is used by the sharding to determine to which actor the command should be sent.
     */
    public static class CommandEnvelope {
        private final String appId;
        private final Object command;

        public CommandEnvelope(String appId, Object command) {
            this.appId = appId;
            this.command = command;
        }

        public String getAppId() {
            return appId;
        }

        public Object getCommand() {
            return command;
        }
    }

    /**
     * Sent to {@link MobileAppActor} to query for status of good/bad IPs.
     */
    public static class AppReportCommand {}

    /**
     * Sent to actors to reset their states (between tests)
     */
    public static class Reset {
    }

    /**
     * Sent back to the client as a response to /events/:appId request
     */
    public static class ReportResponse {
        private final int eventCount;
        private final String[] goodIps;
        private final String[] badIps;

        public ReportResponse(int eventCount, String[] goodIps, String[] badIps) {
            this.eventCount = eventCount;
            this.goodIps = goodIps;
            this.badIps = badIps;
        }

        @JsonProperty("count")
        public int getEventCount() {
            return eventCount;
        }

        @JsonProperty("good_ips")
        public String[] getGoodIps() {
            return goodIps;
        }

        @JsonProperty("bad_ips")
        public String[] getBadIps() {
            return badIps;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReportResponse that = (ReportResponse) o;
            return eventCount == that.eventCount &&
                Arrays.equals(goodIps, that.goodIps) &&
                Arrays.equals(badIps, that.badIps);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(eventCount);
            result = 31 * result + Arrays.hashCode(goodIps);
            result = 31 * result + Arrays.hashCode(badIps);
            return result;
        }

        @Override
        public String toString() {
            return "ReportResponse{" +
                "eventCount=" + eventCount +
                ", goodIps=" + Arrays.toString(goodIps) +
                ", badIps=" + Arrays.toString(badIps) +
                '}';
        }
    }
}

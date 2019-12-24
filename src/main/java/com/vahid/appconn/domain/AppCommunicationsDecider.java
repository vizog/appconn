package com.vahid.appconn.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vahid.appconn.domain.Messages.ReportResponse;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;

/**
 * Provides a report that separates the good and bad IPs the app has communicated with.
 */
public class AppCommunicationsDecider {

    private static Logger log = LoggerFactory.getLogger(AppCommunicationsDecider.class);

    /**
     * Decides which IPs are good and which ones are bad for each app.
     * The decision is made based on the history of IPs an app has communicated with.
     * The biggest group of a /28 netblock are considered to be good IPs and the rest
     * of the IPs that the app has used are considered bad ones.
     */
    public static ReportResponse createReport(MobileAppState state) {
        Seq<Set<String>> sortedIpGroups = state.getIps()
            .groupBy(AppCommunicationsDecider::get28bitPrefix)
            .values()
            .map(commonIps -> commonIps.map(AppCommunicationsDecider::longToIp))
            .sorted((a, b) -> b.size() - a.size());

        if (sortedIpGroups.isEmpty()) {
            return new ReportResponse(0, new String[0], new String[0]);
        }
        Set<String> goodIpSet = sortedIpGroups.get(0);

        Set<String> badIpSet = sortedIpGroups.drop(1) // remove the good IPs
            .fold(HashSet.empty(), Set::addAll);

        return new ReportResponse(state.getEventCount(), goodIpSet.toJavaArray(String[]::new),
            badIpSet.toJavaArray(String[]::new));
    }

    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "."
            + ((ip >> 16) & 0xFF) + "."
            + ((ip >> 8) & 0xFF) + "."
            + (ip & 0xFF);
    }

    public static long get28bitPrefix(long ip) {
        return ip & 0xFFFFFFF0;
    }
}

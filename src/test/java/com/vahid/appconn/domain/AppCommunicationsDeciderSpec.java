package com.vahid.appconn.domain;

import org.forgerock.cuppa.junit.CuppaRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;

import org.junit.runner.RunWith;

import com.vahid.appconn.domain.Messages.ReportResponse;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

@RunWith(CuppaRunner.class)
public class AppCommunicationsDeciderSpec {
    {
        describe("AppCommunicationsDecider", () -> {
            Set<String> goodIps = HashSet.of(
                "87.98.6.70", "87.98.6.71", "87.98.6.72", "87.98.6.73", "87.98.6.74",
                "87.98.6.75", "87.98.6.65", "87.98.6.76", "87.98.6.66", "87.98.6.77",
                "87.98.6.67", "87.98.6.78", "87.98.6.68", "87.98.6.69"
            );
            when("there is one group of /28 IPs", () ->
                it("should detect good and bad IPs", () -> {
                    Set<String> badIps = HashSet.of("8.165.144.212", "79.121.52.67", "99.149.86.43", "144.137.240.54");
                    MobileAppState state = goodIps.addAll(badIps)
                        .foldLeft(MobileAppState.EMPTY, (midState, ip) -> midState.withNewIp(IpUtil.ipToLong(ip)));
                    ReportResponse report = AppCommunicationsDecider.createReport(state);
                    assertThat(report.getGoodIps()).containsExactlyElementsOf(goodIps);
                    assertThat(report.getBadIps()).containsExactlyElementsOf(badIps);
                    assertThat(report.getEventCount()).isEqualTo(goodIps.size() + badIps.size());
                })
            );

            when("there are multiple groups of /28 IPs", () ->
                it("should detect the largest group as good IPs and the rest as bad IPs", () -> {
                    Set<String> badIps = HashSet.of(
                        "162.118.204.110", "162.118.204.100", "162.118.204.101", //3 IPs in a common /28 block
                        "8.165.144.212", "79.121.52.67", "99.149.86.43", "144.137.240.54"
                    );
                    MobileAppState state = goodIps.addAll(badIps)
                        .foldLeft(MobileAppState.EMPTY, (midState, ip) -> midState.withNewIp(IpUtil.ipToLong(ip)));
                    ReportResponse report = AppCommunicationsDecider.createReport(state);
                    assertThat(report.getGoodIps()).containsExactlyElementsOf(goodIps);
                    assertThat(report.getBadIps()).containsExactlyElementsOf(badIps);
                    assertThat(report.getEventCount()).isEqualTo(goodIps.size() + badIps.size());
                })
            );

            when("state is empty", () ->
                it("should return an empty report", () -> {
                    ReportResponse report = AppCommunicationsDecider.createReport(MobileAppState.EMPTY);
                    assertThat(report.getGoodIps()).isEmpty();
                    assertThat(report.getBadIps()).isEmpty();
                    assertThat(report.getEventCount()).isZero();
                })
            );
        });
    }
}
package com.vahid.appconn.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;

@RunWith(CuppaRunner.class)
public class MobileAppStateSpec {
    {
        describe("MobileAppState", () -> {
            List<Long> ips = List.of(1L, 1L, 1L, 2L, 3L);
            it("should keep unique ips in memory", () -> {
                MobileAppState state = ips.foldLeft(MobileAppState.EMPTY, MobileAppState::withNewIp);
                assertThat(state.getIps()).containsExactlyElementsOf(HashSet.of(1L, 2L, 3L));
                assertThat(state.getEventCount()).isEqualTo(5);
            });
        });
    }
}

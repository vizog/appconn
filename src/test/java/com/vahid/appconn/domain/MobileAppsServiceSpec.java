package com.vahid.appconn.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.cuppa.Cuppa.afterEach;
import static org.forgerock.cuppa.Cuppa.beforeEach;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;

import java.util.concurrent.CompletableFuture;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vahid.appconn.domain.Messages.AppReportCommand;
import com.vahid.appconn.domain.Messages.CommandEnvelope;
import com.vahid.appconn.domain.Messages.ReportResponse;
import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.vahid.appcon.data.IpEventOuterClass.IpEvent;

@RunWith(CuppaRunner.class)
public class MobileAppsServiceSpec {
    private ActorSystem system;

    {
        describe("MobileAppsService", () -> {
            Config config = ConfigFactory.parseResources("test.conf");
            beforeEach(() -> system = ActorSystem.create("test", config.withFallback(ConfigFactory.load())));
            afterEach(() -> TestKit.shutdownActorSystem(system));

            when("handleNewIp() is called", () ->
                it("should send the shard actor the right messages and serve the response after getting the reply", () -> {
                    TestKit shardRegion = new TestKit(system);
                    MobileAppsService service = new MobileAppsService(shardRegion.getRef(), system);
                    IpEvent ipEvent = IpEvent.newBuilder()
                        .setAppSha256("app-id")
                        .setIp(123L)
                        .build();
                    CompletableFuture<Done> future = service.handleNewIp(ipEvent).toCompletableFuture();

                    CommandEnvelope commandEnvelope = shardRegion.expectMsgClass(CommandEnvelope.class);
                    assertThat(commandEnvelope.getAppId()).isEqualTo("app-id");
                    assertThat(commandEnvelope.getCommand()).isEqualTo(ipEvent);
                    shardRegion.reply(Done.getInstance());
                    assertThat(future.get()).isEqualTo(Done.getInstance());
                })
            );

            when("getAppReport() is called", () ->
                it("should send the shard actor the right message and serve the response after getting the reply", () -> {
                    TestKit shardRegion = new TestKit(system);
                    MobileAppsService service = new MobileAppsService(shardRegion.getRef(), system);
                    CompletableFuture<ReportResponse> future = service.getAppReport("app-id").toCompletableFuture();

                    CommandEnvelope commandEnvelope = shardRegion.expectMsgClass(CommandEnvelope.class);
                    assertThat(commandEnvelope.getAppId()).isEqualTo("app-id");
                    assertThat(commandEnvelope.getCommand()).isInstanceOf(AppReportCommand.class);
                    ReportResponse result = new ReportResponse(10, new String[] { "1", "2" }, new String[] { "3", "4" });
                    shardRegion.reply(result);
                    assertThat(future.get()).isEqualTo(result);
                })
            );
        });
    }
}
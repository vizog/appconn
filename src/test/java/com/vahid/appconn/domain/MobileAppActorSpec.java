package com.vahid.appconn.domain;

import static org.forgerock.cuppa.Cuppa.afterEach;
import static org.forgerock.cuppa.Cuppa.beforeEach;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vahid.appconn.domain.Messages.AppReportCommand;
import com.vahid.appconn.domain.Messages.ReportResponse;
import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.vahid.appcon.data.IpEventOuterClass.IpEvent;

@RunWith(CuppaRunner.class)
public class MobileAppActorSpec {
    private ActorSystem system;

    {
        describe("MobileAppActorSpec", () -> {
            beforeEach(() -> {
                Config config = ConfigFactory.parseResources("test.conf");
                system = ActorSystem.create("test", config.withFallback(ConfigFactory.load()));
            });
            afterEach(() -> TestKit.shutdownActorSystem(system));

            it("should handle IpEvent messages", () -> {
                TestKit probe = new TestKit(system);
                ActorRef appActor = system.actorOf(Props.create(MobileAppActor.class, MobileAppActor::new));
                IpEvent ipEvent = IpEvent.newBuilder()
                            .setAppSha256("app-id")
                            .setIp(123L)
                            .build();
                appActor.tell(ipEvent, probe.getRef());
                probe.expectMsg(Done.getInstance());
            });

            it("should handle AppReportCommand messages", () -> {
                TestKit probe = new TestKit(system);
                ActorRef appActor = system.actorOf(Props.create(MobileAppActor.class, MobileAppActor::new));
                appActor.tell(new AppReportCommand(), probe.getRef());
                probe.expectMsgClass(ReportResponse.class);
            });
        });
    }
}
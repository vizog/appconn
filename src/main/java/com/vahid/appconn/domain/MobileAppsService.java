package com.vahid.appconn.domain;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.vahid.appconn.domain.Messages.CommandEnvelope;
import com.vahid.appconn.domain.Messages.ReportResponse;
import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.pattern.Patterns;
import com.vahid.appcon.data.IpEventOuterClass.IpEvent;

/**
 * A service layer in front of {@link MobileAppActor} actors.
 * All the communication with actors are asynchronous and non-blocking.
 */
public class MobileAppsService {
    private final ActorRef appConnectionsShardRegion;
    private final ActorRef pubSubMediator;


    public MobileAppsService(ActorRef appConnectionsShardRegion, ActorSystem system) {
        this.appConnectionsShardRegion = appConnectionsShardRegion;
        this.pubSubMediator = DistributedPubSub.get(system).mediator();
    }

    /**
     * Asynchronously processes the new IpEvent by passing it to the correct app and returning
     * with a future which completes when the actor has successfully persisted the event and updated its state.
     */
    public CompletionStage<Done> handleNewIp(IpEvent ipEvent) {
        return Patterns.ask(appConnectionsShardRegion, new CommandEnvelope(ipEvent.getAppSha256(), ipEvent), Duration.ofSeconds(5))
            .thenApply(result -> Done.getInstance());
    }

    /**
     * Asynchronously asks the correct actor to create its report.
     * Returns with a future which completes with a {@link ReportResponse}.
     */
    public CompletionStage<ReportResponse> getAppReport(String appId) {
        return Patterns.ask(appConnectionsShardRegion, new CommandEnvelope(appId, new Messages.AppReportCommand()), Duration.ofSeconds(10))
            .thenApply(ReportResponse.class::cast);
    }

    /**
     * Notify all actors to reset their states.
     * This is only provided for test purposes.
     * Resetting is implemented by an akka distributed pub/sub. All actors are subscribed to "reset" topic and
     * they will reset their states (by deleting the persistent events and clearing the in-memory state).
     * The call will always be handled by a timeout because there's no way to know the reset is finished for all the actors.
     * This is ok because /reset should never be called in real production scenarios.
     */
    public CompletionStage<Done> resetStates() {
        return Patterns.ask(pubSubMediator, new DistributedPubSubMediator.Publish("reset", new Messages.Reset()), Duration.ofSeconds(5))
            .handle((result, error) -> Done.getInstance());
    }
}

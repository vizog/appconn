package com.vahid.appconn.domain;

import java.time.Duration;

import com.vahid.appconn.domain.Messages.AppReportCommand;
import com.vahid.appconn.domain.Messages.Reset;
import akka.Done;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.DeleteMessagesFailure;
import akka.persistence.DeleteMessagesSuccess;
import com.vahid.appcon.data.IpEventOuterClass.IpEvent;

/**
 * Reacts to the events sent to an app and holds its state.
 * This is a sharded and persistent actor, meaning it can be running on any node in the cluster and
 * it keeps its state even if it crashes or it is stopped or migrated to another node.
 */
public class MobileAppActor extends AbstractPersistentActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private MobileAppState state = MobileAppState.EMPTY;

    public MobileAppActor() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "reset"
        mediator.tell(new DistributedPubSubMediator.Subscribe("reset", getSelf()), getSelf());
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
            .match(IpEvent.class, newIp -> state = state.withNewIp(newIp.getIp()))
            .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log.info("subscribed to 'reset' topic"))
            .match(DeleteMessagesFailure.class, msg -> {
                log.error("failed to reset the state");
                state = MobileAppState.EMPTY;
                passivate();
            })
            .match(DeleteMessagesSuccess.class, msg -> {
                log.debug("events deleted, now clearing the memory state.");
                state = MobileAppState.EMPTY;
                passivate();
            })
            .match(IpEvent.class, this::handleNewIp)
            .match(AppReportCommand.class, report -> handleReport())
            .match(Reset.class, reset -> handleReset())
            .matchEquals(ReceiveTimeout.getInstance(), msg -> passivate())
            .matchAny(any -> log.warning("received other:{}", any))
            .build();
    }

    private void handleReset() {
        deleteMessages(Long.MAX_VALUE);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        //Passivate if there is no communication in 60 seconds . Lowers the memory consumption of the server.
        getContext().setReceiveTimeout(Duration.ofSeconds(60));
    }

    private void handleNewIp(IpEvent newIp) {
        persist(newIp, e -> {
            state = state.withNewIp(newIp.getIp());
            sender().tell(Done.getInstance(), self());
        });
    }

    private void handleReport() {
        sender().tell(AppCommunicationsDecider.createReport(state), self());
    }

    @Override
    public String persistenceId() {
        return getSelf().path().name();
    }

    private void passivate() {
        getContext().getParent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
    }
}

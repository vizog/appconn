package com.vahid.appconn.api;

import java.net.InetSocketAddress;

import com.vahid.appconn.domain.MobileAppsService;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.io.Udp;
import akka.io.UdpMessage;
import com.vahid.appcon.data.IpEventOuterClass.IpEvent;

/**
 * Starts a UDP server on port 9999.
 * Upon receiving a message, deserializes it to IpEvent and passes it to  {@link MobileAppsService#handleNewIp(IpEvent)}
 */
public class UdpServer extends AbstractActor {

    private final MobileAppsService deviceAppsService;

    public UdpServer(MobileAppsService deviceAppsService) {
        this.deviceAppsService = deviceAppsService;

        // request creation of a bound listen socket
        final ActorRef mgr = Udp.get(getContext().getSystem()).getManager();
        mgr.tell(
            UdpMessage.bind(getSelf(), new InetSocketAddress("localhost", 9999)),
            getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(
                Udp.Bound.class,
                bound -> getContext().become(ready(getSender())))
            .build();
    }

    private Receive ready(final ActorRef socket) {
        return receiveBuilder()
            .match(
                Udp.Received.class,
                r -> {
                    IpEvent ipEvent = IpEvent.parseFrom(r.data().toArray());
                    deviceAppsService.handleNewIp(ipEvent);
                })
            .matchEquals(
                UdpMessage.unbind(),
                message -> socket.tell(message, getSelf()))
            .match(
                Udp.Unbound.class,
                message -> getContext().stop(getSelf()))
            .build();
    }
}
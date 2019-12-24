package com.vahid.appconn;

import com.vahid.appconn.domain.Messages.CommandEnvelope;
import com.vahid.appconn.domain.MobileAppActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;

/**
 * Implements a simple sharding for {@link MobileAppActor}s.
 * See https://doc.akka.io/docs/akka/2.5.26/cluster-sharding.html for more details.
 */
public class ActorSharding {

    public static ActorRef getShardRegion(ActorSystem system) {
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        ShardRegion.MessageExtractor messageExtractor =
            new ShardRegion.MessageExtractor() {

                @Override
                public String entityId(Object message) {
                    if (message instanceof CommandEnvelope) {
                        return String.valueOf(((CommandEnvelope) message).getAppId());
                    } else return null;
                }

                @Override
                public Object entityMessage(Object message) {
                    if (message instanceof CommandEnvelope) {
                        return ((CommandEnvelope) message).getCommand();
                    } else return message;
                }

                @Override
                public String shardId(Object message) {
                    int numberOfShards = 100; //hardcoded but should be a config for production environment
                    if (message instanceof CommandEnvelope) {
                        return String.valueOf(((CommandEnvelope) message).getAppId().hashCode() % numberOfShards);
                    } else {
                        return null;
                    }
                }
            };

        return ClusterSharding.get(system)
            .start("AppActor", Props.create(MobileAppActor.class), settings, messageExtractor);
    }
}

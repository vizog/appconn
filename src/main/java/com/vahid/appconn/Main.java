package com.vahid.appconn;

import static scala.compat.java8.FutureConverters.toJava;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vahid.appconn.api.RestRoute;
import com.vahid.appconn.api.UdpServer;
import com.vahid.appconn.domain.MobileAppsService;
import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;

public class Main {

    public final ActorSystem system;
    public final ActorMaterializer materializer;

    public static void main(String[] args) {
        new Main(ConfigFactory.load());
    }

    public Main(Config config) {
        this.system = ActorSystem.create(config.getString("clustering.name"), config);
        try {
            materializer = ActorMaterializer.create(system);
            MobileAppsService deviceAppsService = new MobileAppsService(ActorSharding.getShardRegion(system), system);
            //start the udp server:
            system.actorOf(Props.create(UdpServer.class, deviceAppsService));
            RestRoute appConnectionsRoute = new RestRoute(deviceAppsService);
            // Wait on the server binding result, to allow any binding exceptions to fail server start-up and cause exit.
            bindServers(system, appConnectionsRoute).get(60, TimeUnit.SECONDS);
        } catch (Exception x) {
            System.err.println("Error during startup. Terminating actor system.");
            try {
                toJava(system.terminate()).toCompletableFuture().get(2, TimeUnit.SECONDS);
            } catch (Exception y) {
                System.err.println("Error terminating actor system after startup failure: ");
                y.printStackTrace(System.err);
            }
            throw new RuntimeException(x);
        }
    }

     private CompletableFuture<Done> bindServers(ActorSystem system, RestRoute appConnectionsRoute) {
        Config config = system.settings().config();
        CompletableFuture<Done> ready = new CompletableFuture<>();

        Cluster.get(system).registerOnMemberUp(() -> {
            CompletionStage<ServerBinding> http = Http.get(system).bindAndHandle(
                appConnectionsRoute.getRoute().flow(system, materializer),
                ConnectHttp.toHost(config.getString("http.ip"), config.getInt("http.port")),
                materializer);

            http.thenRun(() -> ready.complete(Done.getInstance()));
        });
        return ready;
    }
}

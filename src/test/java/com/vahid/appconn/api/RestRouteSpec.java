package com.vahid.appconn.api;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vahid.appconn.domain.Messages.ReportResponse;
import com.vahid.appconn.domain.MobileAppsService;
import akka.Done;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.http.scaladsl.model.HttpRequest;

public class RestRouteSpec extends JUnitRouteTest {

    private final MobileAppsService mobileAppsService = mock(MobileAppsService.class);
    private final TestRoute route = testRoute(new RestRoute(mobileAppsService).getRoute());

    @Override
    public Config additionalConfig() {
        return ConfigFactory.parseResources("test.conf");
    }

    @Test
    public void testReportWithoutAcceptHeader() {
        when(mobileAppsService.getAppReport("appId"))
            .thenReturn(completedFuture(new ReportResponse(1, new String[] { "1.2.3.4" }, new String[] { "2.3.4.5" })));

        route.run(HttpRequest.GET("/events/appId"))
            .assertStatusCode(200)
            .assertEntity("{\"bad_ips\":[\"2.3.4.5\"],\"count\":1,\"good_ips\":[\"1.2.3.4\"]}");
    }

    @Test
    public void testReportWithoutJsonAcceptHeader() {
        when(mobileAppsService.getAppReport("appId"))
            .thenReturn(completedFuture(new ReportResponse(1, new String[] { "1.2.3.4" }, new String[] { "2.3.4.5" })));

        route.run(HttpRequest.GET("/events/appId").addHeader(HttpHeader.parse("accept", "application/json")))
            .assertStatusCode(200)
            .assertEntity("{\"bad_ips\":[\"2.3.4.5\"],\"count\":1,\"good_ips\":[\"1.2.3.4\"]}");
    }

    @Test
    public void testReportWithoutTextAcceptHeader() {
        when(mobileAppsService.getAppReport("appId"))
            .thenReturn(completedFuture(new ReportResponse(1, new String[] { "1.2.3.4" }, new String[] { "2.3.4.5" })));

        route.run(HttpRequest.GET("/events/appId").addHeader(HttpHeader.parse("accept", "text/plain")))
            .assertStatusCode(404)
            .assertEntity("Content type not available");
    }

    @Test
    public void testReset() {
        when(mobileAppsService.resetStates())
            .thenReturn(completedFuture(Done.getInstance()));

        route.run(HttpRequest.DELETE("/events"))
            .assertStatusCode(200);
    }
}
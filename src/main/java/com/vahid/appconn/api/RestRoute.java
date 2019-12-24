package com.vahid.appconn.api;

import static akka.http.javadsl.model.StatusCodes.BAD_REQUEST;
import static akka.http.javadsl.model.StatusCodes.INTERNAL_SERVER_ERROR;
import static akka.http.javadsl.model.StatusCodes.NOT_FOUND;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.delete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.optionalHeaderValueByType;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vahid.appconn.domain.MobileAppsService;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.headers.Accept;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;
import akka.http.scaladsl.model.StatusCodes;
import akka.japi.pf.PFBuilder;
import io.vavr.collection.Vector;

/**
 * Implements the REST APIs for /events
 */
public class RestRoute {

    private static final Logger log = LoggerFactory.getLogger(RestRoute.class);
    private final MobileAppsService appsService;

    public RestRoute(MobileAppsService appsService) {
        this.appsService = appsService;
    }

    public static final ExceptionHandler exceptionHandler = ExceptionHandler.of(
        new PFBuilder<Throwable, Route>()
            .match(IllegalArgumentException.class, x ->
                complete(HttpResponse.create()
                    .withStatus(BAD_REQUEST)
                    .withEntity(x.getMessage()))
            )
            .matchAny(x -> {
                log.error("Unhandled exception", x);
                return complete(HttpResponse.create()
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .withEntity("An internal error has occurred. Please contact technical support. " + x.getClass().getName()));
            })
            .build());


    public Route getRoute() {
        return handleExceptions(exceptionHandler, () ->
            pathPrefix("events", () ->
                concat(
                    path(appId ->
                        get(() ->
                            optionalHeaderValueByType(Accept.class, headerOption ->
                                acceptsJson(headerOption)
                                    ? onSuccess(() -> appsService.getAppReport(appId), resp -> complete(StatusCodes.OK(), resp, Jackson.marshaller()))
                                    : complete(HttpResponse.create().withStatus(NOT_FOUND).withEntity("Content type not available"))
                            )
                        )
                    ),
                    delete(() -> onSuccess(appsService.resetStates(), done -> complete(HttpResponse.create())))
                )
            )
        );
    }

    /**
     * returns true if `accepts` header is empty or contains `application/json`, otherwise false.
     */
    private boolean acceptsJson(Optional<Accept> headerOption) {
        return headerOption.map(accept -> Vector.ofAll(accept.getMediaRanges())
            .exists(typeRange -> typeRange.matches(MediaTypes.APPLICATION_JSON))).orElse(true);
    }
}
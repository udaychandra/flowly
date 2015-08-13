/*
 * Copyright (c) 2015 The original author or authors.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Apache License v2.0
 *  which accompanies this distribution.
 *
 *  The Apache License v2.0 is available at
 *  http://opensource.org/licenses/Apache-2.0
 *
 *  You may elect to redistribute this code under this license.
 */

package io.flowly.engine.verticles;

import io.flowly.core.Failure;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowMetadata;
import io.flowly.core.verticles.ConsumerRegistration;
import io.flowly.core.verticles.VerticleUtils;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceStep;
import io.flowly.core.codecs.FlowInstanceCodec;
import io.flowly.core.codecs.FlowInstanceMetadataCodec;
import io.flowly.core.codecs.FlowMetadataCodec;
import io.flowly.engine.data.FlowInstanceWrapper;
import io.flowly.engine.data.manager.FlowInstanceReadManager;
import io.flowly.engine.router.Route;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Verticle that manages all aspects of flow instances.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Engine extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private EventBus eventBus;

    // If configured to true, a flow's start, complete and fail events are broadcasted.
    private boolean publishFlowLifeCycleEvents;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        eventBus = vertx.eventBus();

        publishFlowLifeCycleEvents = config().getBoolean(JsonKeys.PUBLISH_FLOW_LIFE_CYCLE_EVENTS, false);

        // Register message handlers.
        VerticleUtils.registerHandlers(vertx.eventBus(), logger, createMessageHandlers(), h -> {
            if (h.succeeded()) {
                logger.info("Deployed engine verticle.");
                startFuture.complete();
            }
            else {
                startFuture.fail(h.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
        logger.info("Undeployed engine verticle.");
    }

    /**
     * Create message handlers for flow instance requests.
     *
     * @return queue of consumer registrations.
     */
    private Queue<ConsumerRegistration> createMessageHandlers() {
        Queue<ConsumerRegistration> registrations = new LinkedList<>();
        registrations.add(new ConsumerRegistration(EngineAddresses.START_FLOW_INSTANCE,
                startInstanceHandler()));

        registrations.add(new ConsumerRegistration(EngineAddresses.HOP_FLOW_INSTANCE,
                hopInstanceHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.HOP_INTO_FLOW_INSTANCE,
                hopIntoInstanceHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.FAIL_FLOW_INSTANCE,
                failInstanceHandler(), true));

        registrations.add(new ConsumerRegistration(EngineAddresses.AWAIT_USER_INTERACTION,
                awaitUserInteractionHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.START_FLOW_INSTANCE_TASK,
                startUserInteractionHandler()));

        registrations.add(new ConsumerRegistration(EngineAddresses.START_USER_INTERACTION_VIEW,
                startViewHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.SAVE_FLOW_INSTANCE_TASK,
                saveViewHandler()));
        registrations.add(new ConsumerRegistration(EngineAddresses.COMPLETE_FLOW_INSTANCE_TASK,
                completeViewHandler()));

        return registrations;
    }

    private Handler<Message<Object>> startInstanceHandler() {
        return message -> {
            // Message comes from clustered event bus.
            FlowMetadata flowMetadata = new FlowMetadata(((JsonObject) message.body()).getMap());
            flowMetadata.validate();

            createInstance(flowMetadata, resultHandler -> {
                FlowInstance instance = resultHandler.result();

                if (instance != null) {
                    Long instanceId = instance.getMetadata().getInstanceId();
                    message.reply(instanceId);

                    if (logger.isInfoEnabled()) {
                        logger.info("Started instance: " + instanceId + ", flow: " + flowMetadata.getFlowId() +
                                ", app: " + flowMetadata.getAppId());
                    }

                    // Begin the flow.
                    startToken(instance);

                    // Broadcast event.
                    broadcastFlowLifecycleEvent(JsonKeys.FLOW_START_EVENT, instance.getMetadata(),
                            publishFlowLifeCycleEvents);
                }
                else {
                    Failure failure = new Failure(3000,
                            "Unable to start instance for flow: " + flowMetadata.getFlowId() + ", app: " +
                                    flowMetadata.getAppId());
                    logger.error(failure.getError());
                    message.fail(failure.getCode(), failure.getMessage());
                }
            });
        };
    }

    private Handler<Message<Object>> hopInstanceHandler() {
        return message -> {
            // Get the next flow object for the flow instance.
            moveToken(new FlowInstance(((JsonObject) message.body()).getMap()));
        };
    }

    private Handler<Message<Object>> hopIntoInstanceHandler() {
        return message -> {
            // Start sub-flow.
            startToken(new FlowInstance(((JsonObject) message.body()).getMap()));
        };
    }

    private Handler<Message<Object>> failInstanceHandler() {
        return message -> {
            FlowInstance instance = new FlowInstance(((JsonObject) message.body()).getMap());
            failInstance(instance, null);
        };
    }

    private Handler<Message<Object>> awaitUserInteractionHandler() {
        return message -> {
            FlowInstance instance = new FlowInstance(((JsonObject) message.body()).getMap());
            FlowInstanceMetadata metadata = instance.getMetadata();

            // TODO: Assign user based on load balancer output.
            saveInstance(instance, FlowInstanceReadManager.STATUS_NEW, true, resultHandler -> {
                if (resultHandler.result()) {
                    assignUserInteraction(metadata);
                } else {
                    Failure failure = new Failure(3001,
                            "Failed to pause flow instance for user interaction: " + metadata);
                    failInstance(instance, failure);
                }
            });
        };
    }

    private void assignUserInteraction(FlowInstanceMetadata metadata) {
        JsonObject args = new JsonObject().
                put(JsonKeys.SUBJECT_ID, JsonKeys.ADMIN_USER_ID).
                put(JsonKeys.TASK_ID, metadata.getCurrentStep().getFlowObjectInstanceId());

        eventBus.send(EngineAddresses.REPO_ASSIGN_TASK, args, reply -> {
            if ((Boolean) reply.result().body()) {
                broadcastFlowLifecycleEvent(JsonKeys.FLOW_WAIT_UI_EVENT, metadata, true);
                logger.info("Flow instance awaiting user interaction: " + metadata);
            }
        });
    }

    private Handler<Message<Object>> startUserInteractionHandler() {
        return message -> {
            // TODO: Add validation to ensure that status is "New"
            Long flowObjectInstanceId = (Long) message.body();
            getInstance(flowObjectInstanceId, resultHandler -> {
                FlowInstance instance = resultHandler.result();

                // Start interactive service.
                startToken(instance);

                // Update task status.
                updateTask(instance.getMetadata().getCurrentStep().getFlowObjectInstanceId(),
                        FlowInstanceReadManager.STATUS_IN_PROGRESS);
            });
        };
    }

    private void updateTask(Long taskId, String status) {
        JsonObject args = new JsonObject().
                put(JsonKeys.STATUS, status).
                put(JsonKeys.TASK_ID, taskId);

        eventBus.send(EngineAddresses.REPO_UPDATE_TASK, args, reply -> {
            // TODO: If unable to update the task, fail the instance?
            if ((Boolean) reply.result().body()) {
                logger.info("Flow instance user interaction assignment status updated: " + taskId);
            }
        });
    }

    private Handler<Message<Object>> startViewHandler() {
        return message -> {
            FlowInstance instance = new FlowInstance(((JsonObject) message.body()).getMap());
            updateView(instance, FlowInstanceReadManager.STATUS_USER_INTERACTING, JsonKeys.FLOW_START_UI_EVENT, false);
        };
    }

    private Handler<Message<Object>> saveViewHandler() {
        // TODO: update signature to support async client-side save.
        return viewHandler(FlowInstanceReadManager.STATUS_USER_INTERACTING, JsonKeys.FLOW_SAVE_UI_EVENT, false);
    }

    private Handler<Message<Object>> completeViewHandler() {
        return viewHandler(FlowInstanceReadManager.STATUS_COMPLETED, JsonKeys.FLOW_COMPLETE_UI_EVENT, true);
    }

    private Handler<Message<Object>> viewHandler(String viewStatus, String broadcastEventType, boolean moveToken) {
        return message -> {
            // TODO: Add validation.
            // TODO: Add viewData to FlowObjectInstance.
            FlowInstance viewInstance = new FlowInstance(((JsonObject) message.body()).getMap());

            Long flowObjectInstanceId = viewInstance.getMetadata().getCurrentStep().getFlowObjectInstanceId();
            getInstance(flowObjectInstanceId, resultHandler -> {
                FlowInstance instance = resultHandler.result();
                instance.setData(viewInstance.getData());
                updateView(instance, viewStatus, broadcastEventType, moveToken);
            });
        };
    }

    private void updateView(FlowInstance instance, String status, String broadcastEventType, boolean moveToken) {
        FlowInstanceMetadata metadata = instance.getMetadata();

        saveInstance(instance, status, true, resultHandler -> {
            if (resultHandler.result()) {
                // Get the next flow object for the flow instance.
                if (moveToken) {
                    moveToken(instance);
                }

                broadcastFlowLifecycleEvent(broadcastEventType, instance.getMetadata(), true);

                // TODO: Send message to socket.io handler.
                logger.info("Flow instance user interaction view updated: " + metadata);
            }
            else {
                Failure failure = new Failure(3002,
                        "Flow instance user interaction view failure: " + metadata + ", status: " + status);
                failInstance(instance, failure);
            }
        });
    }

    /**
     * Start a given flow instance by creating a start token and running the step (flow object).
     * The flow router is consulted to get the start flow object.
     *
     * @param instance represents an instance of a given flow.
     */
    private void startToken(FlowInstance instance) {
        // TODO: Add validation.
        FlowInstanceMetadata metadata = instance.getMetadata();
        FlowInstanceStep currentStep = metadata.getCurrentStep();

        // See if we are going to start a flow or a sub-flow.
        String flowId = currentStep != null ? currentStep.getSubFlowId() : metadata.getFlowId();

        FlowInstanceMetadata routeMetadata = new FlowInstanceMetadata();
        routeMetadata.setFlowId(flowId);
        routeMetadata.setCurrentStep(new FlowInstanceStep());
        routeMetadata.getCurrentStep().setFlowObjectId("0");

        getNextRoute(routeMetadata, resultHandler -> {
            Route route = resultHandler.result();

            if (validateInstanceRoute(instance, route)) {
                prepareAndRunStep(instance, route.getNext(), 0, true);
            }
        });
    }

    /**
     * End the flow's current token, consult the router, create a new token and run the step (flow object).
     *
     * @param instance represents an instance of a given flow.
     */
    private void moveToken(FlowInstance instance) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        getNextRoute(metadata, resultHandler -> {
            Route route = resultHandler.result();

            if (!validateInstanceRoute(instance, route)) {
                return;
            }

            if (!route.isEnd()) {
                if (route.isSplit()) {
                    List<Route.Next> nextList = route.getNextList();

                    for (int i = 0; i < nextList.size(); i++) {
                        prepareAndRunStep(instance, nextList.get(i), i, false);
                    }
                } else {
                    prepareAndRunStep(instance, route.getNext(), 0, false);
                }
            } else {
                completeInstance(instance);
            }
        });
    }

    private void getNextRoute(FlowInstanceMetadata routeMetadata, Handler<AsyncResult<Route>> resultHandler) {
        Future<Route> future = Future.future();
        future.setHandler(resultHandler);

        DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowInstanceMetadataCodec.NAME);
        eventBus.send(EngineAddresses.REPO_FLOW_NEXT_ROUTE, routeMetadata, options, reply -> {
            future.complete((Route) reply.result().body());
        });
    }

    /**
     * If flow's persistence is enabled, save current graph and create a new vertex (represents a new token)
     * and run the new step (flow object).
     *
     * @param instance represents an instance of a given flow.
     * @param next the next flow object in the flow on which a token is created.
     * @param stepIndex represents the index in a split or loop.
     * @param isStart indicates if the current token is to be completed (move) or not.
     */
    private void prepareAndRunStep(FlowInstance instance, Route.Next next, int stepIndex, boolean isStart) {
        // Persistence enabled - update instance graph.
        if (instance.getMetadata().getInstanceId() != null) {
            FlowInstanceWrapper wrapper = new FlowInstanceWrapper(instance, isStart, false, null, next);

            eventBus.send(EngineAddresses.REPO_FLOW_CREATE_FLOW_OBJECT_INSTANCE, wrapper, reply -> {
                Long flowObjectInstanceId = (Long) reply.result().body();

                if (flowObjectInstanceId != null) {
                    prepareStep(instance, next, stepIndex, isStart, flowObjectInstanceId);
                    runStep(instance);
                }
                else {
                    // Houston, we got a problem.
                }
            });
        }
        else {
            prepareStep(instance, next, stepIndex, isStart, null);
            runStep(instance);
        }
    }

    private void prepareStep(FlowInstance instance, Route.Next next, int stepIndex, boolean isStart,
                             Long flowObjectInstanceId) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        FlowInstanceStep currentStep = metadata.getCurrentStep();

        if (currentStep != null) {
            // This is a sub-flow (current step is not null) - set the parent reference.
            if (isStart) {
                updateSubFlowMetaData(metadata, currentStep);
                instance.setData(new JsonObject());
            }
        }

        currentStep = new FlowInstanceStep();
        currentStep.setStepIndex(stepIndex);
        currentStep.setFlowObjectId(next.getFlowObjectId());

        if (flowObjectInstanceId != null) {
            currentStep.setFlowObjectInstanceId(flowObjectInstanceId);
        }

        if (next.getSubFlowId() != null) {
            currentStep.setSubFlowId(next.getSubFlowId());
        }

        metadata.setCurrentStep(currentStep);

        if (logger.isInfoEnabled()) {
            logger.info("Flow instance token " + (isStart ? "started: " : "moved: ") + metadata);
        }
    }

    /**
     * Executes the flow instance step (flow object) by calling the corresponding flow verticle.
     *
     * @param instance represents the instance of a given flow that is passed to the verticle.
     */
    private void runStep(FlowInstance instance) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        String sendAddress = EngineAddresses.getFlowObjectBusAddress(metadata.getAppId(),
                metadata.getCurrentStep().getFlowObjectId());

        vertx.eventBus().send(sendAddress, instance);

        if (logger.isInfoEnabled()) {
            logger.info("Flow instance running: " + instance.getMetadata().getInstanceId() +
                    ", flow: " + metadata.getFlowId() +
                    ", app: " + metadata.getAppId() +
                    ", step: " + metadata.getCurrentStep());
        }
    }

    private void createInstance(FlowMetadata flowMetadata, Handler<AsyncResult<FlowInstance>> resultHandler) {
        Future<FlowInstance> future = Future.future();
        future.setHandler(resultHandler);

        if (flowMetadata.persistenceEnabled()) {
            DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowMetadataCodec.NAME);
            eventBus.send(EngineAddresses.REPO_FLOW_CREATE_INSTANCE, flowMetadata, options, reply -> {
                future.complete((FlowInstance) reply.result().body());
            });
        }
        else {
            future.complete(new FlowInstance(null, flowMetadata));
        }
    }

    private void saveInstance(FlowInstance instance, String status, boolean saveMetaData,
                              Handler<AsyncResult<Boolean>> resultHandler) {
        Future<Boolean> future = Future.future();
        future.setHandler(resultHandler);

        FlowInstanceWrapper wrapper = new FlowInstanceWrapper(instance, false, saveMetaData, status, null);
        eventBus.send(EngineAddresses.REPO_FLOW_SAVE_INSTANCE, wrapper, reply -> {
            future.complete((Boolean) reply.result().body());
        });
    }

    private void completeInstance(FlowInstance instance, boolean saved) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        String parentFlowObjectId = metadata.getParentFlowObjectId();

        if (saved) {
            logger.info("Flow instance completed: " + metadata);

            // Broadcast event.
            broadcastFlowLifecycleEvent(JsonKeys.FLOW_COMPLETE_EVENT, metadata, publishFlowLifeCycleEvents);

            // This is a sub flow. Now, flow upwards to parent.
            if (parentFlowObjectId != null) {
                getInstance(metadata.getParentFlowObjectInstanceId(), resultHandler -> {
                    FlowInstance parentInstance = resultHandler.result();

                    if (parentInstance != null) {
                        parentInstance.setOutputData(instance.getOutputData());

                        String sendAddress = EngineAddresses.getFlowObjectBusAddress(metadata.getAppId(),
                                parentFlowObjectId + EngineAddresses.HOP_OUT_FLOW_INSTANCE);
                        eventBus.send(sendAddress, parentInstance);

                        // Update task status.
                        updateTask(parentInstance.getMetadata().getCurrentStep().getFlowObjectInstanceId(),
                                FlowInstanceReadManager.STATUS_COMPLETED);
                    }
                    else {
                        logger.fatal("Parent instance not found. Unable to flow out from instance: " + metadata);
                    }
                });
            }
        }
        else {
            Failure failure = new Failure(3003,
                    "Unable to close flow object or instance vertex: " + metadata.getInstanceId() + ", flow: " +
                            metadata.getFlowId() + ", app: " + metadata.getAppId());
            failInstance(instance, failure);
        }
    }

    private void completeInstance(FlowInstance instance) {
        if (instance.getMetadata().getInstanceId() != null) {
            DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowInstanceCodec.NAME);
            eventBus.send(EngineAddresses.REPO_FLOW_COMPLETE_INSTANCE, instance, options, reply -> {
                completeInstance(instance, (Boolean) reply.result().body());
            });
        }
        else {
            completeInstance(instance, true);
        }
    }

    private void getInstance(Long flowObjectInstanceId, Handler<AsyncResult<FlowInstance>> resultHandler) {
        Future<FlowInstance> future = Future.future();
        future.setHandler(resultHandler);

        if (flowObjectInstanceId != null) {
            eventBus.send(EngineAddresses.GET_FLOW_INSTANCE_TASK, flowObjectInstanceId, reply -> {
                future.complete((FlowInstance) reply.result().body());
            });
        }
        else {
            future.complete(null);
        }
    }

    private void failInstance(FlowInstance instance, Failure failure) {
        FlowInstanceMetadata metadata = instance.getMetadata();

        if (metadata.getInstanceId() != null) {
            DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowInstanceCodec.NAME);
            eventBus.send(EngineAddresses.REPO_FLOW_FAIL_INSTANCE, instance, options, reply -> {
                if ((Boolean) reply.result().body()) {
                    // Broadcast event.
                    broadcastFlowLifecycleEvent(JsonKeys.FLOW_FAIL_EVENT, metadata, publishFlowLifeCycleEvents);
                }
                else {
                    // Should never happen.
                    Failure fatalFailure = new Failure(3004,
                            "Failed to update instance status to 'failed': " + metadata);
                    logger.fatal(fatalFailure.getError());
                }
            });
        }
        else {
            // Broadcast event.
            broadcastFlowLifecycleEvent(JsonKeys.FLOW_FAIL_EVENT, metadata, publishFlowLifeCycleEvents);
        }

        if (failure != null) {
            logger.error(failure.getError(), failure.getCause());
        }
    }

    private boolean validateInstanceRoute(FlowInstance instance, Route route) {
        if (!route.isValid()) {
            FlowInstanceMetadata metadata = instance.getMetadata();
            Failure failure = new Failure(3005, "Invalid route for instance: " + metadata);
            failInstance(instance, failure);
        }

        return route.isValid();
    }

    private void broadcastFlowLifecycleEvent(String eventType, FlowInstanceMetadata metadata, boolean enabled) {
        if (!enabled) {
            return;
        }

        JsonObject event = new JsonObject().put(JsonKeys.FLOW_EVENT_TYPE, eventType);
        Long instanceId = metadata.getInstanceId();
        Long parentFlowObjectInstanceId = metadata.getParentFlowObjectInstanceId();

        if (instanceId != null) {
            event.put(JsonKeys.INSTANCE_ID, instanceId);
        }

        if (parentFlowObjectInstanceId != null) {
            event.put(FlowInstanceMetadata._PARENT_FLOW_OBJECT_INSTANCE_ID, parentFlowObjectInstanceId);
        }

        if (metadata.getCurrentStep() != null) {
            event.put(FlowInstanceStep._FLOW_OBJECT_INSTANCE_ID, metadata.getCurrentStep().getFlowObjectInstanceId());
        }

        eventBus.publish(EngineAddresses.FLOW_LIFECYCLE_EVENT, event);
    }

    private void updateSubFlowMetaData(FlowInstanceMetadata metadata, FlowInstanceStep step) {
        metadata.setParentFlowObjectId(step.getFlowObjectId());
        metadata.setFlowId(step.getSubFlowId());

        if (step.getFlowObjectInstanceId() != null) {
            metadata.setParentFlowObjectInstanceId(step.getFlowObjectInstanceId());
        }
    }
}

package org.ISEWebService.Service;

import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.Enums.ISEAlgorithmType;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Task;
import org.ISEWebService.Model.EventLog.Trace;
import org.ISEWebService.Service.BpmnModel.*;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class BpmnModelService extends BpmnModel{

    private WaitModel waitModel = new WaitModel();
    private CancelModel cancelModel = new CancelModel();
    private RedoModel redoModel = new RedoModel();
    private ChangeModel changeModel = new ChangeModel();
    private ReworkModel reworkModel = new ReworkModel();

    /**
     * Creates an BPMN Model for a given ISE
     * Partially inspired the following sources:
     * https://github.com/camunda/camunda-bpm-examples/tree/master/bpmn-model-api/generate-process-fluent-api
     * https://docs.camunda.org/manual/7.17/user-guide/model-api/bpmn-model-api/create-a-model/?__hstc=218867270.5e02c1f40ddbba41e36f07d247e01b28.1688745673057.1689590138882.1689609907503.5&__hssc=218867270.1.1689609907503&__hsfp=3798296355
     * https://github.com/camunda/camunda-bpmn-model/blob/master/src/main/java/org/camunda/bpm/model/bpmn/Bpmn.java
     * https://forum.camunda.io/t/generating-bpmndi-for-model-api-processes/1889/7
     * @param algorithmType
     * @param iseSingle
     * @return
     */
    public byte[] createIseBpmnModel(ISEAlgorithmType algorithmType, ISESingle iseSingle){
        // Get all processes with their traceInstances of the given ISESingle
        List<LogProcess> logProcessList = this.getProcesses(iseSingle, algorithmType);

        // Create an empty model & definitions
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("ISEWebService");
        modelInstance.setDefinitions(definitions);

        // Create Collaboration and add to definitions
        Collaboration collaboration = modelInstance.newInstance(Collaboration.class);
        definitions.addChildElement(collaboration);

        // Create BpmnDiagram and add to definitions
        BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);
        definitions.addChildElement(bpmnDiagram);

        // Create BpmnPlane, add to BpmnDiagram and link to Collaboration
        BpmnPlane bpmnPlane = modelInstance.newInstance(BpmnPlane.class);
        bpmnDiagram.setBpmnPlane(bpmnPlane);
        bpmnPlane.setBpmnElement(collaboration);

        // Gets size of longest instance
        int countTraces = 0;
        int longestInstanceSize = 0;
        for(LogProcess logProcess : logProcessList){
            for(TraceInstance traceInstance : logProcess.getTraceInstances()){
                countTraces++;
                if(traceInstance.getTasks().size() > longestInstanceSize){
                    longestInstanceSize = traceInstance.getTasks().size();
                }
            }
        }

        // Parameter for the BPMN model
        int processNumber = 0;
        int processX = 0;
        int processY = 0;
        int distanceBetweenProcesses = 100; // 200
        int heightEvents = 40; // 20
        int widthEvents = 40; // 20
        int heightTasks = 90; // 50
        int widthTasks = 120; // 70
        int distanceBetweenElements = 100; // 70
        double horizontalDisplacementInTrace = 0.55; // %
        switch (algorithmType){
            case CANCEL:
                horizontalDisplacementInTrace = 0; // %
                break;
            case REDO:
                horizontalDisplacementInTrace = 0; // %
                longestInstanceSize += (iseSingle.getHandling().size() - 1) / countTraces;
                break;
            case CHANGE, WAIT:
                horizontalDisplacementInTrace = 0.55; // %
                break;
            case REWORK:
                horizontalDisplacementInTrace = -0.45; // %
                break;
        }

        String cancelTaskName = "";
        String waitTaskName = "";
        String changeOldResource = "";
        String changeNewResource = "";
        // Create all Processes and add them to the definitions
        for(LogProcess logProcess : logProcessList){
            // Calculate parameter
            int instanceHeight = ((heightTasks + distanceBetweenElements + heightTasks) * 12)/10;
            int processWidth = 130 + (longestInstanceSize + 2) * distanceBetweenElements + longestInstanceSize * widthTasks + widthEvents * 2;
            int processHeight = instanceHeight * logProcess.getTraceInstances().size();
            int heightDataObject = heightTasks;
            int widthDataObject = (widthTasks * 5) / 4;

            // Process
            Process process = modelInstance.newInstance(Process.class);
            process.setName(logProcess.getNameProcess());
            definitions.addChildElement(process);

            // Participant
            Participant participant = modelInstance.newInstance(Participant.class);
            participant.setName(logProcess.getNameProcess());
            participant.setProcess(process);
            collaboration.addChildElement(participant);
            createBpmnShape(modelInstance, participant, bpmnPlane, processHeight, processWidth, processX, processY, null, null);

            // LaneSet
            LaneSet laneSet = modelInstance.newInstance(LaneSet.class);
            process.addChildElement(laneSet);

            int traceNumber = 0;
            for(TraceInstance traceInstance : logProcess.getTraceInstances()){
                // Initialize parameter of first event
                int previousRightMiddleX = processX + 70;
                int instanceMiddleY =  (int) (processY + instanceHeight/2 * (1 + horizontalDisplacementInTrace) + instanceHeight * traceNumber);

                // Lane
                Lane lane = modelInstance.newInstance(Lane.class);
                lane.setName(traceInstance.getNameTrace());
                laneSet.addChildElement(lane);
                createBpmnShape(modelInstance, lane, bpmnPlane, instanceHeight, processWidth - 30, 30, processY + (instanceHeight * traceNumber), null, null);

                // Start event
                StartEvent startEvent = createProcessElement(modelInstance, StartEvent.class, process, lane, null);
                createBpmnShape(modelInstance, startEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX, instanceMiddleY - heightEvents/2, null, null);
                previousRightMiddleX += widthEvents;

                // Tasks
                int taskNumber = 0;
                boolean flagLasEventCancelHandling = false;
                boolean flagFirstHandlingRework = true;
                boolean flagLastTriggerRework = false;
                boolean flagIsRework = false;
                int countIterationRedo = 0;
                FlowNode previousTaskElement = startEvent;
                Task previousEventLogTask = null;
                for(Task task : traceInstance.getTasks()){
                    // Creates task
                    FlowNode taskElement = createProcessElement(modelInstance, org.camunda.bpm.model.bpmn.instance.Task.class, process, lane, task.getEvents().get(0).getConceptName());

                    // Checks, whether this task is a trigger, a handling or a normal task
                    if(task.isTrigger()){
                        switch (algorithmType) {
                            case WAIT:
                                // Create Boundary non interrupting time event, Data Object, Association and Signal End Event
                                waitModel.createTrigger(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, bpmnPlane, heightEvents, widthEvents, heightDataObject, widthDataObject, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, this.convertTimeToYearDayHourMinute(task.getEvents().get(0).getOriginalLog().getMean(task.getEvents().get(0).getConceptName()), true), this.convertTimeToYearDayHourMinute(task.getDurationTask(), true));

                                // Create Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#F9CA79", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null,previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                break;
                            case CANCEL:
                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#F9CA79", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;

                                // Create intermediate signal event
                                cancelTaskName = task.getEvents().get(0).getConceptName();
                                IntermediateThrowEvent signalEventCancel = cancelModel.createTrigger(modelInstance, process, lane, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightEvents/2, cancelTaskName);

                                // Sequence flow
                                createSequenceFlow(modelInstance, previousTaskElement, signalEventCancel, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = signalEventCancel;
                                previousRightMiddleX += distanceBetweenElements + widthEvents;
                                break;
                            case REDO:
                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#F9CA79", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                countIterationRedo++;
                                previousEventLogTask = task;
                                break;
                            case CHANGE:
                                // Create Boundary non interrupting conditional event, Data Object, Association and Signal End Event
                                changeModel.createTrigger(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, bpmnPlane, heightEvents, widthEvents, heightDataObject, widthDataObject, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, task);

                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#F9CA79", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null,previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                changeOldResource = task.getEvents().get(0).getAttributeMap().get("org:resource").toString();
                                changeNewResource = task.getEvents().get(task.getEvents().size() - 1).getAttributeMap().get("org:resource").toString();
                                break;
                            case REWORK:
                                // Create boundary error event and intermediate signal event
                                IntermediateThrowEvent intermediateThrowEventRework = reworkModel.createTrigger(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, heightEvents, widthEvents, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, bpmnPlane);

                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#F9CA79", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = intermediateThrowEventRework;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                flagLastTriggerRework = true;
                                break;
                        }
                    }else if(task.isHandling()){
                        switch (algorithmType) {
                            case WAIT:
                                // Create Boundary non interrupting time event, Data Object and Association
                                waitModel.createHandling(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, bpmnPlane, heightEvents, widthEvents, heightDataObject, widthDataObject, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, this.convertTimeToYearDayHourMinute(task.getEvents().get(0).getOriginalLog().getMean(task.getEvents().get(0).getConceptName()), true), this.convertTimeToYearDayHourMinute(task.getDurationTask(), true));

                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#BFE746", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                break;
                            case CANCEL:
                                // Create boundary interrupting signal event
                                cancelModel.createHandling(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, cancelTaskName);

                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#BFE746", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null,previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks/2;
                                flagLasEventCancelHandling = true;
                                break;
                            case REDO:
                                // Create Intermediate Signal Event for previous iteration
                                FlowNode signalEventRedo;
                                if(previousEventLogTask.getEvents().get(previousEventLogTask.getEvents().size() - 1).getLifecycle().equals("Completed.Success")){
                                    signalEventRedo = redoModel.createHandlingCompletedSuccess(modelInstance, process, lane, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightEvents/2, countIterationRedo, previousEventLogTask);
                                }else{
                                    signalEventRedo = redoModel.createHandlingCompletedFailed(modelInstance, process, lane, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightEvents/2, countIterationRedo, previousEventLogTask);
                                }

                                // Sequence flow
                                createSequenceFlow(modelInstance, previousTaskElement, signalEventRedo, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = signalEventRedo;
                                previousRightMiddleX += distanceBetweenElements + widthEvents;

                                // Create handling task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#BFE746", null);

                                // Create handling sequence flow
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX-widthEvents/2, instanceMiddleY + heightEvents/2, previousRightMiddleX-widthEvents/2, instanceMiddleY + heightEvents/2 + heightTasks, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightEvents/2 + heightTasks, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightTasks/2);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                countIterationRedo++;
                                previousEventLogTask = task;
                                break;
                            case CHANGE:
                                // Create boundary non interrupting signal event, data object and association
                                changeModel.createHandling(modelInstance, process, lane, (org.camunda.bpm.model.bpmn.instance.Task) taskElement, bpmnPlane, heightEvents, widthEvents, heightDataObject, widthDataObject, previousRightMiddleX, distanceBetweenElements, widthTasks, instanceMiddleY, heightTasks, changeOldResource, changeNewResource, task);

                                // Task
                                createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#BFE746", null);
                                createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);

                                // Update parameter
                                previousTaskElement = taskElement;
                                previousRightMiddleX += distanceBetweenElements + widthTasks;
                                break;
                            case REWORK:
                                if(flagFirstHandlingRework){
                                    if(!flagLastTriggerRework){
                                        // Creates Intermediate Signal Event
                                        IntermediateCatchEvent intermediateCatchEvent = reworkModel.createHandling(modelInstance, process, lane, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX - widthTasks/2 - widthEvents/2, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses - heightEvents/2, "#BFE746");
                                        createSequenceFlow(modelInstance, previousTaskElement, intermediateCatchEvent, process, bpmnPlane, previousRightMiddleX - widthTasks/2, instanceMiddleY + heightTasks/2, null, null, null, null,previousRightMiddleX - widthTasks/2, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses - heightEvents/2);
                                        previousTaskElement = intermediateCatchEvent;
                                    }
                                    flagFirstHandlingRework = false;
                                    // Task
                                    createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX - widthTasks/2 + widthEvents/2 + distanceBetweenElements, instanceMiddleY + heightTasks/2 + distanceBetweenElements - heightTasks/2, "#BFE746", null);
                                    createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX - widthTasks/2 + widthEvents/2, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses, null, null, null, null,previousRightMiddleX - widthTasks/2 + widthEvents/2 + distanceBetweenElements, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses);
                                    previousRightMiddleX += distanceBetweenElements - widthTasks/2 + widthEvents/2 + widthTasks;
                                }else{
                                    // Task
                                    createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY + heightTasks/2 + distanceBetweenElements - heightTasks/2, "#BFE746", null);
                                    createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses, null, null, null, null,previousRightMiddleX + distanceBetweenElements, instanceMiddleY + heightTasks/2 + distanceBetweenProcesses);
                                    previousRightMiddleX += distanceBetweenElements + widthTasks;
                                }

                                // Update parameter
                                previousTaskElement = taskElement;
                                flagIsRework = true;
                                break;
                        }
                    }else{
                        if(flagLasEventCancelHandling){
                            createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + widthTasks/2 + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#FFFFEA", null);
                            createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY + heightTasks/2 + heightEvents/2, previousRightMiddleX, instanceMiddleY + heightTasks/2 + heightEvents/2 + heightEvents, previousRightMiddleX + distanceBetweenElements + widthTasks,instanceMiddleY + heightTasks/2 + heightEvents/2 + heightEvents, previousRightMiddleX + distanceBetweenElements + widthTasks, instanceMiddleY + heightTasks/2);
                            previousTaskElement = taskElement;
                            previousRightMiddleX += distanceBetweenElements + widthTasks + widthTasks/2;
                            flagLasEventCancelHandling = false;
                        }else if(flagIsRework){
                            createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#FFFFEA", null);
                            createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY + heightTasks/2 + distanceBetweenElements, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightTasks/2 + distanceBetweenElements, null, null, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightTasks/2);
                            previousTaskElement = taskElement;
                            previousRightMiddleX += distanceBetweenElements + widthTasks;
                            flagIsRework = false;
                        }else{
                            createBpmnShape(modelInstance, taskElement, bpmnPlane, heightTasks, widthTasks, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2, "#FFFFEA", null);
                            createSequenceFlow(modelInstance, previousTaskElement, taskElement, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null, previousRightMiddleX + distanceBetweenElements, instanceMiddleY);
                            previousTaskElement = taskElement;
                            previousRightMiddleX += distanceBetweenElements + widthTasks;
                        }
                    }
                    taskNumber++;
                    // Reset parameter for later reuse in other ISE-Classes
                    task.setHandling(false);
                    task.setTrigger(false);
                }
                // End event
                EndEvent endEvent = createProcessElement(modelInstance, EndEvent.class, process, lane, null);
                createBpmnShape(modelInstance, endEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightEvents/2, null, null);

                if(flagIsRework){
                    createSequenceFlow(modelInstance, previousTaskElement, endEvent, process, bpmnPlane, previousRightMiddleX, instanceMiddleY + heightTasks/2 + distanceBetweenElements, previousRightMiddleX + distanceBetweenElements + widthEvents/2, instanceMiddleY + heightTasks/2 + distanceBetweenElements, null, null,previousRightMiddleX + distanceBetweenElements + widthEvents/2, instanceMiddleY + heightEvents/2);
                    flagIsRework = false;
                }else{
                    createSequenceFlow(modelInstance, previousTaskElement, endEvent, process, bpmnPlane, previousRightMiddleX, instanceMiddleY, null, null, null, null,previousRightMiddleX + distanceBetweenElements, instanceMiddleY);
                }

                traceNumber++;
            }
            processY += distanceBetweenProcesses + instanceHeight * logProcess.getTraceInstances().size();
            processNumber++;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bpmn.writeModelToStream(outputStream, modelInstance);

        return outputStream.toByteArray();
    }

    /**
     * Gets all processes with their traceInstances of the given ISESingle
     * @param iseSingle ISESingle containing the trigger and handling events
     * @return List of processes with their traceInstances
     */
    private List<LogProcess> getProcesses(ISESingle iseSingle, ISEAlgorithmType algorithmType){
        // Set first all parameters to false
        iseSingle.getTrigger().getTask().setTrigger(false);
        iseSingle.getTrigger().getTask().setHandling(false);
        for(Event e : iseSingle.getHandling()){
            e.getTask().setTrigger(false);
            e.getTask().setHandling(false);
        }
        // Map of all processes with their traceInstances
        Map<Log, List<TraceInstance>> instancesPerProcess = new HashMap<>();

        switch (algorithmType){
            case WAIT, CANCEL, CHANGE:
                // Marks the tasks of the trigger as trigger task
                iseSingle.getTrigger().getTask().setTrigger(true);

                // Creates an instance, marks as traceInstances that contains the trigger task and adds it to the corresponding map
                TraceInstance triggerTraceInstance = new TraceInstance();
                triggerTraceInstance.setHasTriggerTask(true);
                triggerTraceInstance.setNameTrace(iseSingle.getTrigger().getOriginalTrace().getTraceName());
                List<TraceInstance> tempTraceInstances = new ArrayList<>();
                tempTraceInstances.add(triggerTraceInstance);
                instancesPerProcess.put(iseSingle.getTrigger().getOriginalLog(), tempTraceInstances);

                // Adds the trigger task to the instance
                for (Event event : iseSingle.getTrigger().getOriginalTrace().getEvents()) {
                    if(triggerTraceInstance.getTasks().contains(event.getTask())){
                        continue;
                    }
                    triggerTraceInstance.addTask(event.getTask());
                }

                // Creates an instance for each handling event and adds it to the corresponding map
                for (Event handlingEvent : iseSingle.getHandling()) {
                    handlingEvent.getTask().setHandling(true);
                    TraceInstance handlingTraceInstance = new TraceInstance();
                    handlingTraceInstance.setNameTrace(handlingEvent.getOriginalTrace().getTraceName());

                    if(instancesPerProcess.containsKey(handlingEvent.getOriginalLog())){
                        instancesPerProcess.get(handlingEvent.getOriginalLog()).add(handlingTraceInstance);
                    }else {
                        tempTraceInstances = new ArrayList<>();
                        tempTraceInstances.add(handlingTraceInstance);
                        instancesPerProcess.put(handlingEvent.getOriginalLog(), tempTraceInstances);
                    }

                    for(Event event : handlingEvent.getOriginalTrace().getEvents()){
                        if(handlingTraceInstance.getTasks().contains(event.getTask())){
                            continue;
                        }
                        handlingTraceInstance.addTask(event.getTask());
                    }
                }
                break;
            case REDO, REWORK:
                // Put trigger and handling tasks in Map with their trace as key
                Map<Trace, List<Event>> traces = new HashMap<>();

                List<Event> tempEvents = new ArrayList<>();
                tempEvents.add(iseSingle.getTrigger());
                iseSingle.getTrigger().getTask().setTrigger(true);
                traces.put(iseSingle.getTrigger().getOriginalTrace(), tempEvents);

                for(Event e : iseSingle.getHandling()){
                    Trace originalTrace = e.getOriginalTrace();
                    e.getTask().setHandling(true);
                    if(traces.containsKey(originalTrace)){
                        traces.get(originalTrace).add(e);
                    }else{
                        List<Event> tempTraceEvents = new ArrayList<>();
                        tempTraceEvents.add(e);
                        traces.put(originalTrace, tempTraceEvents);
                    }
                }

                for(Trace trace : traces.keySet()){
                    if(algorithmType == ISEAlgorithmType.REDO){
                        // Set first iteration as trigger
                        traces.get(trace).get(0).getTask().setTrigger(true);
                    }

                    TraceInstance traceInstance = new TraceInstance();
                    if(trace.equals(iseSingle.getTrigger().getOriginalTrace())){
                        traceInstance.setHasTriggerTask(true);
                    }else{
                        traceInstance.setHasTriggerTask(false);
                    }

                    traceInstance.setNameTrace(trace.getTraceName());

                    for(Event event : trace.getEvents()){
                        if(traceInstance.getTasks().contains(event.getTask())){
                            continue;
                        }
                        traceInstance.addTask(event.getTask());
                    }

                    if(instancesPerProcess.containsKey(trace.getOriginalLog())){
                        instancesPerProcess.get(trace.getOriginalLog()).add(traceInstance);
                    }else {
                        List<TraceInstance> tempTraceInstanceList = new ArrayList<>();
                        tempTraceInstanceList.add(traceInstance);
                        instancesPerProcess.put(trace.getOriginalLog(), tempTraceInstanceList);
                    }
                }
                break;
        }


        // All Processes with their traceInstances
        List<LogProcess> logProcesses = new ArrayList<>();

        // Creates a process for each process and adds all traceInstances to it
        for(Log log : instancesPerProcess.keySet()) {
            LogProcess logProcess = new LogProcess();
            logProcess.addAllInstance(instancesPerProcess.get(log));
            logProcess.setNameProcess(log.getProcessName());
            logProcesses.add(logProcess);
        }

        // Sorts the list, so that the log with the trigger task always appears first
        for(LogProcess logProcess : logProcesses){
            Collections.sort(logProcess.getTraceInstances(), new Comparator<TraceInstance>() {
                @Override
                public int compare(TraceInstance t1, TraceInstance t2) {
                    return Boolean.compare(t2.getHasTriggerTask(), t1.getHasTriggerTask());
                }
            });
        }

        Collections.sort(logProcesses, new Comparator<LogProcess>() {
            @Override
            public int compare(LogProcess l1, LogProcess l2) {
                boolean hasTrigger = false;
                for(TraceInstance traceInstance : l1.getTraceInstances()){
                    if(traceInstance.hasTriggerTask){
                        hasTrigger = true;
                        break;
                    }
                }
                if(hasTrigger){
                    return -1;
                }
                return 0;
            }
        });

        return logProcesses;
    }

    private class LogProcess {
        private List<TraceInstance> traceInstances;
        private String nameProcess;
        void addAllInstance(List<TraceInstance> traceInstances){
            this.traceInstances = traceInstances;
        }

        List<TraceInstance> getTraceInstances(){
            return this.traceInstances;
        }

        void setNameProcess(String nameProcess) {
            this.nameProcess = nameProcess;
        }

        String getNameProcess() {
            return nameProcess;
        }
    }

    private class TraceInstance {
        private List<Task> tasks = new ArrayList<>();
        private boolean hasTriggerTask;
        private String nameTrace;

        void addTask(Task task){
            tasks.add(task);
        }

        void setHasTriggerTask(boolean hasTriggerTask){
            this.hasTriggerTask = hasTriggerTask;
        }

        boolean getHasTriggerTask(){
            return this.hasTriggerTask;
        }

        List<Task> getTasks(){
            return tasks;
        }

        void setNameTrace(String nameTrace) {
            this.nameTrace = nameTrace;
        }

        String getNameTrace() {
            return nameTrace;
        }
    }

    private String convertTimeToYearDayHourMinute(double timeInMilli, boolean includeMinute){
        int timeYear = (int) Math.round(timeInMilli / (1000 * 60 * 60 * 24 * 365));
        int timeDays = (int) (Math.round(timeInMilli / (1000 * 60 * 60 * 24)) % 365);
        double timeHours = (timeInMilli / (1000 * 60 * 60)) % 24;
        double timeMinute = (timeInMilli / (1000 * 60)) % 60;

        String result = "";
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if(timeYear > 0){
            result = result + timeYear + "y ";
        }
        if(timeDays > 0){
            result = result + timeDays + "d ";
        }
        if(includeMinute){
            if(((int) timeHours) > 0){
                result = result + (int) timeHours + "h ";
            }
            if(timeMinute > 0){
                result = result + decimalFormat.format(timeMinute) + "min";
            }
        }else{
            if(timeHours > 0){
                result = result + decimalFormat.format(timeHours) + "h";
            }
        }

        return result;
    }
}

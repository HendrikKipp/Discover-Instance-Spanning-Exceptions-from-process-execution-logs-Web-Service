package org.ISEWebService.Service.BpmnModel;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;

public class ReworkModel extends BpmnModel{

    public IntermediateThrowEvent createTrigger(BpmnModelInstance modelInstance, Process process, Lane lane, Activity activity, int heightEvents, int widthEvents, int previousRightMiddleX, int distanceBetweenElements, int widthTasks, int instanceMiddleY, int heightTasks, BpmnPlane bpmnPlane){
        // Create boundary error event
        BoundaryEvent boundaryEvent = createProcessElement(modelInstance, BoundaryEvent.class, process, lane, "broken object");
        boundaryEvent.setAttachedTo(activity);
        boundaryEvent.setCancelActivity(true);
        boundaryEvent.setName("broken object");
        ErrorEventDefinition errorEventDefinition = modelInstance.newInstance(ErrorEventDefinition.class);
        boundaryEvent.addChildElement(errorEventDefinition);
        createBpmnShape(modelInstance, boundaryEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks/2 - widthEvents/2, instanceMiddleY + heightTasks/2 - heightEvents/2, "#F9CA79", null);

        // Create intermediate signal event
        IntermediateThrowEvent intermediateThrowEvent = createProcessElement(modelInstance, IntermediateThrowEvent.class, process, lane, "broken object");
        intermediateThrowEvent.setName("broken object");
        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        intermediateThrowEvent.addChildElement(signalEventDefinition);
        createBpmnShape(modelInstance, intermediateThrowEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks/2 - widthEvents/2, instanceMiddleY + heightTasks/2 + distanceBetweenElements - heightEvents/2 , "#F9CA79", null);

        // Sequence flow
        createSequenceFlow(modelInstance, boundaryEvent, intermediateThrowEvent, process, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightTasks/2 + heightEvents/2, null, null, null, null, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY + heightTasks/2 + distanceBetweenElements - heightEvents/2);

        return intermediateThrowEvent;
    }

    public IntermediateCatchEvent createHandling(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, String color){
        IntermediateCatchEvent intermediateCatchEvent = createProcessElement(modelInstance, IntermediateCatchEvent.class, process, lane, "broken object");
        intermediateCatchEvent.setName("broken object");

        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        intermediateCatchEvent.addChildElement(signalEventDefinition);

        createBpmnShape(modelInstance, intermediateCatchEvent, bpmnPlane, height, width, x, y, color, null);

        return intermediateCatchEvent;
    }
}

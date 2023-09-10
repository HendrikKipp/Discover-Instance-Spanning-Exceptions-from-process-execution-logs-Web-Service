package org.ISEWebService.Service.BpmnModel;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;

public class CancelModel extends BpmnModel{
    public IntermediateThrowEvent createTrigger(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, String taskName){
        return createIntermediateSignalEvent(modelInstance, process, lane, bpmnPlane, height, width, x, y, "Task '" + taskName +"' completed", "#F9CA79");
    }

    public void createHandling(BpmnModelInstance modelInstance, Process process, Lane lane, Activity activity, BpmnPlane bpmnPlane, int heightEvents, int widthEvents, int previousRightMiddleX, int distanceBetweenElements, int widthTasks, int instanceMiddleY, int heightTasks, String cancelTaskName){
        // Create boundary signal event
        BoundaryEvent boundaryEvent = createProcessElement(modelInstance, BoundaryEvent.class, process, lane, "Task '" + cancelTaskName + "' completed");
        boundaryEvent.setAttachedTo(activity);
        boundaryEvent.setName("Task '" + cancelTaskName +"' completed");
        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        boundaryEvent.addChildElement(signalEventDefinition);
        BpmnLabel bpmnLabelBoundaryEvent = createBpmnLabel(modelInstance, (int) (heightEvents * 1), (int) (widthEvents * 1), previousRightMiddleX + distanceBetweenElements + widthTasks/2 + widthEvents/2, instanceMiddleY + heightTasks/2 + heightEvents/2);
        createBpmnShape(modelInstance, boundaryEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks/2 - widthEvents/2, instanceMiddleY + heightTasks/2 - heightEvents/2, "#BFE746", bpmnLabelBoundaryEvent);
    }
}

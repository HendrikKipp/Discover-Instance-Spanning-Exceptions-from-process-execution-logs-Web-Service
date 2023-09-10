package org.ISEWebService.Service.BpmnModel;

import org.ISEWebService.Model.EventLog.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;

public class RedoModel extends BpmnModel{

    public IntermediateThrowEvent createHandlingCompletedFailed(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, int iteration, Task task){
        String color = "#BFE746";
        if(iteration==1){
            color = "#F9CA79";
        }
        return createIntermediateSignalEvent(modelInstance, process, lane, bpmnPlane, height, width, x, y, "Iteration " + iteration + "\n" + task.getEvents().get(task.getEvents().size()-1).getLifecycle().toString(), color);
    }

    public IntermediateCatchEvent createHandlingCompletedSuccess(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, int iteration, Task task){
        String color = "#BFE746";
        if(iteration==1){
            color = "#F9CA79";
        }
        return createIntermediateCatchSignalEvent(modelInstance, process, lane, bpmnPlane, height, width, x, y, "Iteration " + iteration + "\n" + task.getEvents().get(task.getEvents().size()-1).getLifecycle().toString(), color);
    }
}

package org.ISEWebService.Service.BpmnModel;

import org.ISEWebService.Model.EventLog.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;

public class ChangeModel extends BpmnModel{

    public void createTrigger(BpmnModelInstance modelInstance, Process process, Lane lane, Activity
        activity, BpmnPlane bpmnPlane, int heightEvents, int widthEvents, int heightDataObject, int widthDataObject, int previousRightMiddleX, int distanceBetweenElements, int widthTasks, int instanceMiddleY, int heightTasks, Task task){
        // Create Boundary non interrupting time event
        BoundaryEvent boundaryEvent = createProcessElement(modelInstance, BoundaryEvent.class, process, lane, "Reassign of resource");
        boundaryEvent.setAttachedTo(activity);
        boundaryEvent.setCancelActivity(false);
        boundaryEvent.setName("Reassign of resource");
        ConditionalEventDefinition conditionalEventDefinition = modelInstance.newInstance(ConditionalEventDefinition.class);
        Condition condition = modelInstance.newInstance(Condition.class);
        conditionalEventDefinition.setCondition(condition);
        boundaryEvent.addChildElement(conditionalEventDefinition);
        BpmnLabel bpmnLabelBoundaryEvent = createBpmnLabel(modelInstance, (int) (heightEvents * 0.5), (int) (widthEvents * 1.25), (int) ((previousRightMiddleX + distanceBetweenElements + widthTasks - widthEvents/2) + heightEvents), (int) ((instanceMiddleY - heightTasks/2 - heightEvents/2) + widthEvents));
        createBpmnShape(modelInstance, boundaryEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks - widthEvents/2, instanceMiddleY - heightTasks/2 - heightEvents/2, "#F9CA79", bpmnLabelBoundaryEvent);

        // Data Object
        String oR = task.getEvents().get(0).getAttributeMap().get("org:resource").toString();
        String nR = task.getEvents().get(task.getEvents().size() - 1).getAttributeMap().get("org:resource").toString();
        String descriptionData = "Resource: " + oR + "\n" + "Resource': " + nR;
        DataObject dataObject = modelInstance.newInstance(DataObject.class);
        dataObject.setName(descriptionData);
        process.addChildElement(dataObject);
        DataObjectReference dataObjectReference = modelInstance.newInstance(DataObjectReference.class);
        dataObjectReference.setDataObject(dataObject);
        dataObjectReference.setName(descriptionData);
        process.addChildElement(dataObjectReference);
        BpmnLabel bpmnLabelDataObject = createBpmnLabel(modelInstance, (int) (heightDataObject * 0.6), (int) (widthDataObject * 0.9), (int) ((previousRightMiddleX + distanceBetweenElements) + widthDataObject * 0.05), (int) ((instanceMiddleY - heightTasks/2 - distanceBetweenElements - heightDataObject) + heightDataObject * 0.3));
        createBpmnShape(modelInstance, dataObjectReference, bpmnPlane, heightDataObject, widthDataObject, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2 - distanceBetweenElements - heightDataObject, null, bpmnLabelDataObject);

        // Create Association
        createAssociation(modelInstance, process, activity, dataObjectReference, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY - heightTasks/2, null, null, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY - heightTasks/2 - distanceBetweenElements);

        // Create Association
        createAssociation(modelInstance, process, boundaryEvent, dataObjectReference, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks, instanceMiddleY - heightTasks/2 - heightEvents/2, null, null,previousRightMiddleX + distanceBetweenElements + widthTasks, instanceMiddleY - heightTasks/2 - distanceBetweenElements);

        // Create Signal End Event
        EndEvent signalEndEvent = createProcessElement(modelInstance, EndEvent.class, process, lane, null);
        signalEndEvent.setName("Reassigned " + oR + " to "  + nR);
        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        signalEndEvent.addChildElement(signalEventDefinition);
        BpmnLabel bpmnLabelSignal = createBpmnLabel(modelInstance, (int) (heightEvents * 1), (int) (widthEvents * 1), previousRightMiddleX + distanceBetweenElements + widthTasks + distanceBetweenElements/2 + (widthEvents * 3)/2, instanceMiddleY - heightTasks/2 - distanceBetweenElements/2 - heightEvents);
        createBpmnShape(modelInstance, signalEndEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks + distanceBetweenElements/2, instanceMiddleY - heightTasks/2 - distanceBetweenElements/2 - heightEvents, "#F9CA79", bpmnLabelSignal);
        createSequenceFlow(modelInstance, boundaryEvent, signalEndEvent, process, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks + widthEvents/2, instanceMiddleY - heightTasks/2, previousRightMiddleX + distanceBetweenElements + widthTasks + distanceBetweenElements/2 + widthEvents/2, instanceMiddleY - heightTasks/2, null, null,previousRightMiddleX + distanceBetweenElements + widthTasks + distanceBetweenElements/2 + widthEvents/2, instanceMiddleY - heightTasks/2 - distanceBetweenElements/2);
    }

    public void createHandling(BpmnModelInstance modelInstance, Process process, Lane lane, Activity activity, BpmnPlane bpmnPlane, int heightEvents, int widthEvents, int heightDataObject, int widthDataObject, int previousRightMiddleX, int distanceBetweenElements, int widthTasks, int instanceMiddleY, int heightTasks, String oldResource, String newResource, Task task){
        // Create boundary signal event
        BoundaryEvent boundaryEvent = createProcessElement(modelInstance, BoundaryEvent.class, process, lane, "Task took longer than expected");
        boundaryEvent.setAttachedTo(activity);
        boundaryEvent.setCancelActivity(false);
        boundaryEvent.setName("Reassigned " + oldResource + " to "  + newResource);
        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        boundaryEvent.addChildElement(signalEventDefinition);
        BpmnLabel bpmnLabelBoundaryEvent = createBpmnLabel(modelInstance, (int) (heightEvents * 1), (int) (widthEvents * 1), previousRightMiddleX + distanceBetweenElements + widthTasks - widthEvents/2 + (widthEvents * 3)/2, instanceMiddleY - heightTasks/2 - heightEvents/2);
        createBpmnShape(modelInstance, boundaryEvent, bpmnPlane, heightEvents, widthEvents, previousRightMiddleX + distanceBetweenElements + widthTasks - widthEvents/2, instanceMiddleY - heightTasks/2 - heightEvents/2, "#BFE746", bpmnLabelBoundaryEvent);

        // Create Data object
        String nR = task.getEvents().get(task.getEvents().size() - 1).getAttributeMap().get("org:resource").toString();
        String descriptionData = "Resource: " + oldResource + "\n" + "Resource': " + nR;
        DataObject dataObject = modelInstance.newInstance(DataObject.class);
        dataObject.setName(descriptionData);
        process.addChildElement(dataObject);
        DataObjectReference dataObjectReference = modelInstance.newInstance(DataObjectReference.class);
        dataObjectReference.setDataObject(dataObject);
        dataObjectReference.setName(descriptionData);
        process.addChildElement(dataObjectReference);
        BpmnLabel bpmnLabelDataObject = createBpmnLabel(modelInstance, (int) (heightDataObject * 0.6), (int) (widthDataObject * 0.9), (int) ((previousRightMiddleX + distanceBetweenElements) + widthDataObject * 0.05), (int) ((instanceMiddleY - heightTasks/2 - distanceBetweenElements - heightDataObject) + heightDataObject * 0.3));
        createBpmnShape(modelInstance, dataObjectReference, bpmnPlane, heightDataObject, widthDataObject, previousRightMiddleX + distanceBetweenElements, instanceMiddleY - heightTasks/2 - distanceBetweenElements - heightDataObject, null, bpmnLabelDataObject);

        // Create Association
        createAssociation(modelInstance, process, activity, dataObjectReference, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY - heightTasks/2, null, null, previousRightMiddleX + distanceBetweenElements + widthTasks/2, instanceMiddleY - heightTasks/2 - distanceBetweenElements);

        // Create Association
        createAssociation(modelInstance, process, boundaryEvent, dataObjectReference, bpmnPlane, previousRightMiddleX + distanceBetweenElements + widthTasks, instanceMiddleY - heightTasks/2 - heightEvents/2, null, null,previousRightMiddleX + distanceBetweenElements + widthTasks, instanceMiddleY - heightTasks/2 - distanceBetweenElements);
    }
}

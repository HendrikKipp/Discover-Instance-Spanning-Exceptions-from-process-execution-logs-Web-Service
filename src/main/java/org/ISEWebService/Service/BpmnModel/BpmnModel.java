package org.ISEWebService.Service.BpmnModel;

import org.camunda.bpm.model.bpmn.AssociationDirection;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.FlowNodeRef;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;

public abstract class BpmnModel {

    /**
     * Create a intermediate signal event with a bpmn shape
     * @param modelInstance
     * @param process
     * @param lane
     * @param bpmnPlane
     * @param height
     * @param width
     * @param x
     * @param y
     * @return
     */
    protected IntermediateThrowEvent createIntermediateSignalEvent(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, String name, String color){
        IntermediateThrowEvent intermediateThrowEvent = createProcessElement(modelInstance, IntermediateThrowEvent.class, process, lane, name);
        intermediateThrowEvent.setName(name);

        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        intermediateThrowEvent.addChildElement(signalEventDefinition);

        createBpmnShape(modelInstance, intermediateThrowEvent, bpmnPlane, height, width, x, y, color, null);

        return intermediateThrowEvent;
    }

    protected IntermediateCatchEvent createIntermediateCatchSignalEvent(BpmnModelInstance modelInstance, Process process, Lane lane, BpmnPlane bpmnPlane, int height, int width, int x, int y, String name, String color){
        IntermediateCatchEvent intermediateCatchEvent = createProcessElement(modelInstance, IntermediateCatchEvent.class, process, lane, name);
        intermediateCatchEvent.setName(name);

        SignalEventDefinition signalEventDefinition = modelInstance.newInstance(SignalEventDefinition.class);
        intermediateCatchEvent.addChildElement(signalEventDefinition);

        createBpmnShape(modelInstance, intermediateCatchEvent, bpmnPlane, height, width, x, y, color, null);

        return intermediateCatchEvent;
    }

    /**
     * Create a association with a bpmn edge
     * @param modelInstance
     * @param process
     * @param sourceElement
     * @param dataObjectReference
     * @param bpmnPlane
     * @param sourceX
     * @param sourceY
     * @param additionalX
     * @param additionalY
     * @param targetX
     * @param targetY
     * @return
     */

    protected Association createAssociation(BpmnModelInstance modelInstance, Process process, BaseElement sourceElement, DataObjectReference dataObjectReference, BpmnPlane bpmnPlane, int sourceX, int sourceY, Integer additionalX, Integer additionalY, int targetX, int targetY){
        Association association = modelInstance.newInstance(Association.class);
        process.addChildElement(association);
        association.setSource(sourceElement);
        association.setTarget(dataObjectReference);
        association.setAssociationDirection(AssociationDirection.valueOf("One"));

        createBpmnEdge(modelInstance, association, bpmnPlane, sourceX, sourceY, additionalX, additionalY, null, null, targetX, targetY);

        return association;
    }

    /**
     * Create a sequence flow with a bpmn edge
     * @param modelInstance
     * @param from
     * @param to
     * @param process
     * @param bpmnPlane
     * @param sourceX
     * @param sourceY
     * @param additionalX
     * @param additionalY
     * @param additionalX2
     * @param additionalY2
     * @param targetX
     * @param targetY
     * @return
     */

    protected SequenceFlow createSequenceFlow(BpmnModelInstance modelInstance, FlowNode from, FlowNode to, Process process, BpmnPlane bpmnPlane, int sourceX, int sourceY, Integer additionalX, Integer additionalY, Integer additionalX2, Integer additionalY2, int targetX, int targetY){
        SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
        sequenceFlow.setSource(from);
        sequenceFlow.setTarget(to);
        process.addChildElement(sequenceFlow);
        from.getOutgoing().add(sequenceFlow);
        to.getIncoming().add(sequenceFlow);

        createBpmnEdge(modelInstance, sequenceFlow, bpmnPlane, sourceX, sourceY, additionalX, additionalY, additionalX2, additionalY2, targetX, targetY);

        return sequenceFlow;
    }

    /**
     * Create a BpmnEdge
     * @param modelInstance
     * @param element
     * @param bpmnPlane
     * @param sourceX
     * @param sourceY
     * @param additionalX
     * @param additionalY
     * @param additionalX2
     * @param additionalY2
     * @param targetX
     * @param targetY
     * @return
     */
    private BpmnEdge createBpmnEdge(BpmnModelInstance modelInstance, BaseElement element, BpmnPlane bpmnPlane, int sourceX, int sourceY, Integer additionalX, Integer additionalY, Integer additionalX2, Integer additionalY2, int targetX, int targetY){
        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        bpmnEdge.setBpmnElement(element);
        bpmnPlane.addChildElement(bpmnEdge);

        Waypoint waypointSource = modelInstance.newInstance(Waypoint.class);
        waypointSource.setX(sourceX);
        waypointSource.setY(sourceY);
        bpmnEdge.addChildElement(waypointSource);

        if(additionalX != null && additionalY != null){
            Waypoint waypointAdditional = modelInstance.newInstance(Waypoint.class);
            waypointAdditional.setX(additionalX);
            waypointAdditional.setY(additionalY);
            bpmnEdge.addChildElement(waypointAdditional);
        }

        if(additionalX2 != null && additionalY2 != null){
            Waypoint waypointAdditional = modelInstance.newInstance(Waypoint.class);
            waypointAdditional.setX(additionalX2);
            waypointAdditional.setY(additionalY2);
            bpmnEdge.addChildElement(waypointAdditional);
        }

        Waypoint waypointTarget = modelInstance.newInstance(Waypoint.class);
        waypointTarget.setX(targetX);
        waypointTarget.setY(targetY);
        bpmnEdge.addChildElement(waypointTarget);

        return bpmnEdge;
    }

    /**
     * Create a process element
     * @param modelInstance
     * @param elementClass
     * @param process
     * @param lane
     * @param name
     * @return
     * @param <T>
     */

    protected <T extends BaseElement> T createProcessElement(BpmnModelInstance modelInstance, Class<T> elementClass, Process process, Lane lane, String name){
        T element = modelInstance.newInstance(elementClass);
        process.addChildElement(element);

        if(elementClass.equals(org.camunda.bpm.model.bpmn.instance.Task.class)){
            ((org.camunda.bpm.model.bpmn.instance.Task) element).setName(name);
        }

        FlowNodeRef flowNodeRef = modelInstance.newInstance(FlowNodeRef.class);
        flowNodeRef.setTextContent(element.getId());
        lane.addChildElement(flowNodeRef);

        return element;
    }

    /**
     * Create a BpmnShape
     * @param modelInstance
     * @param baseElement
     * @param bpmnPlane
     * @param height
     * @param width
     * @param x
     * @param y
     * @param hexColor
     * @param bpmnLabel
     * @return
     */
    protected BpmnShape createBpmnShape(BpmnModelInstance modelInstance, BaseElement baseElement, BpmnPlane bpmnPlane, int height, int width, int x, int y, String hexColor, BpmnLabel bpmnLabel){
        BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);
        bpmnShape.setBpmnElement(baseElement);
        bpmnPlane.addChildElement(bpmnShape);

        Bounds bounds = modelInstance.newInstance(Bounds.class);
        bounds.setHeight(height);
        bounds.setWidth(width);
        bounds.setX(x);
        bounds.setY(y);
        bpmnShape.setBounds(bounds);

        if(hexColor != null){
            bpmnShape.setAttributeValueNs("http://bpmn.io/schema/bpmn/biocolor/1.0", "fill",hexColor,true);
        }

        if(bpmnLabel != null){
            bpmnShape.setBpmnLabel(bpmnLabel);
        }

        return bpmnShape;
    }

    /**
     * Create a BpmnLabel
     * @param modelInstance
     * @param height
     * @param width
     * @param x
     * @param y
     * @return
     */
    protected BpmnLabel createBpmnLabel(BpmnModelInstance modelInstance, int height, int width, int x, int y){
        BpmnLabel bpmnLabel = modelInstance.newInstance(BpmnLabel.class);

        Bounds boundsLabel = modelInstance.newInstance(Bounds.class);
        boundsLabel.setHeight(height);
        boundsLabel.setWidth(width);
        boundsLabel.setX(x);
        boundsLabel.setY(y);
        bpmnLabel.setBounds(boundsLabel);

        return bpmnLabel;
    }
}

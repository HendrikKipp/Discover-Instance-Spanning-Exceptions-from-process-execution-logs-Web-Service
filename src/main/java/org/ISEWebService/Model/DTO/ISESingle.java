package org.ISEWebService.Model.DTO;

import org.ISEWebService.Model.EventLog.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ISESingle {
    private Event trigger = null;
    private List<Event> handling = null;

    private byte[] bpmnModel;

    private List<String> ppi = new ArrayList<>();
    private List<String> ppiNames = new ArrayList<>();
    private double SumOfDeviationOfThroughputTime;

    public ISESingle(Event trigger, List<Event> handling){
        this.trigger = trigger;
        this.handling = handling;
    }

    public Event getTrigger(){
        return trigger;
    }

    public void setTrigger(Event trigger) {
        this.trigger = trigger;
    }

    public List<Event> getHandling() {
        return handling;
    }

    public void setHandling(List<Event> handling) {
        this.handling = handling;
    }

    public byte[] getBpmnModel() {
        return bpmnModel;
    }

    public void setBpmnModel(byte[] bpmnModel) {
        this.bpmnModel = bpmnModel;
    }

    public void addPPI(String ppiValue){
        ppi.add(ppiValue);
    }

    public void addPPIName(String ppiName){
        ppiNames.add(ppiName);
    }

    public String getPPIById(int index){
        return this.ppi.get(index);
    }

    public List<String> getPpi() {
        return ppi;
    }

    public List<String> getPpiNames() {
        return ppiNames;
    }

    public String toString(){
        return "Trigger: " + getTrigger().toString() + ", Handling: " + getHandling().toString();
    }

    public double getSumOfDeviationOfThroughputTime() {
        return SumOfDeviationOfThroughputTime;
    }

    public void setSumOfDeviationOfThroughputTime(double sumOfDeviationOfThroughputTime) {
        SumOfDeviationOfThroughputTime = sumOfDeviationOfThroughputTime;
    }
}

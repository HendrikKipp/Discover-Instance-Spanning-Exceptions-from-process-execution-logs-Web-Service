package org.ISEWebService.Model.EventLog;

import org.ISEWebService.Model.EventLog.Event;

import java.util.List;

public class Task {
    private List<Event> events;
    // Duration of the event in milliseconds
    private double durationTask;

    private boolean isTrigger = false;
    private boolean isHandling = false;

    public Task(List<Event> events){
        this.events = events;
    }

    public List<Event> getEvents(){
        return this.events;
    }

    public boolean isTrigger() {
        return isTrigger;
    }

    public void setTrigger(boolean trigger) {
        isTrigger = trigger;
    }

    public boolean isHandling() {
        return isHandling;
    }

    public void setHandling(boolean handling) {
        isHandling = handling;
    }

    public int getAmoutEvents(){
        return events.size();
    }

    public double getDurationTask(){
        return durationTask;
    }

    public void calculateDuration(){
        if(events.size() > 1){
            this.durationTask = events.get(events.size() - 1).getTimestamp().getTime() - events.get(0).getTimestamp().getTime();
        }else if(events.get(0).getAttributeMap().containsKey("duration")){
            this.durationTask = Double.parseDouble(events.get(0).getAttributeMap().get("duration").toString());
        }
    }
}

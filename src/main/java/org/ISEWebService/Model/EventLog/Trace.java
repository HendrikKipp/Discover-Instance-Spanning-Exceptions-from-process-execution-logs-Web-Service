package org.ISEWebService.Model.EventLog;

import java.util.*;

public class Trace {
    private List<Event> events;
    private Log originalLog;
    private Map<String, String> attributeMap;
    private String traceName;

    public Trace(List<Event> events, Map<String, String> attributeMap, String traceName){
        this.events = events;
        this.attributeMap = attributeMap;
        this.traceName = traceName;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public Log getOriginalLog() {
        return originalLog;
    }

    public void setOriginalLog(Log originalLog) {
        this.originalLog = originalLog;
    }
    public void insertOrdered(Trace trace){
        events.addAll(trace.getEvents());
        Collections.sort(events);
    }

    public int getLength(){
        return events.size();
    }

    public List<String> getAllEventConceptNames(){
        List<String> allEventConceptNames = new ArrayList<>();
        for(Event event : events){
            if(!allEventConceptNames.contains(event.getConceptName())){
                allEventConceptNames.add(event.getConceptName());
            }
        }
        return allEventConceptNames;
    }


    public List<Event> hasAdditionalEvents(Event trigger){
        List<Trace> allTraces = originalLog.getTraces();

        int countAllEvents = 0;
        for(Trace trace : allTraces){
            if(trace != this && trace != trigger.getOriginalTrace()){
                countAllEvents += trace.getEvents().size();
            }
        }

        double averageEventsPerTrace = ((double) countAllEvents)/((double) allTraces.size() - 2);

        if(((double) this.events.size()) > averageEventsPerTrace){
            List<Event> additionalEvents = new ArrayList<>();
            for(Event event : events){
                for(Trace trace : allTraces){
                    if(trace != this && trace != trigger.getOriginalTrace() && !trace.getAllEventConceptNames().contains(event.getConceptName()) && !additionalEvents.contains(event)){
                        additionalEvents.add(event);
                    }
                }
            }
            return additionalEvents;
        }else{
            return new ArrayList<>();
        }
    }

    public long getThroughputTime(){
        return events.get(events.size() - 1).getTimestamp().getTime() - events.get(0).getTimestamp().getTime();
    }

    public String getTraceName() {
        return traceName;
    }
}

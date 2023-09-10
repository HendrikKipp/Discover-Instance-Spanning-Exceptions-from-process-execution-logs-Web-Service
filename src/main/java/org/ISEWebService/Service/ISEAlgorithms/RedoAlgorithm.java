package org.ISEWebService.Service.ISEAlgorithms;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RedoAlgorithm {

    /**
     * Discovers ISE of type REDO
     * @param mergedTraces
     * @return
     */
    public ISECollective discoverRedo(List<Trace> mergedTraces){
        ISECollective iseCollective = new ISECollective(ISECollective.ISEClass.REDO);
        for(Trace trace : mergedTraces){
            for(Event event : trace.getEvents()){
                Event trigger = null;
                List<Event> handling = new ArrayList<>();
                if(event.getLifecycle().equals("Completed.Failed")){
                    trigger = event;
                    Map<Log, List<Event>> iterations = this.collect_iterations(event, trace);
                    boolean completedFailed = false;
                    for(Log log : iterations.keySet()){
                        List<Event> tempList = iterations.get(log);
                        if(tempList.get(tempList.size() - 1).getLifecycle().equals("Completed.Failed")){
                            completedFailed = true;
                        }
                    }
                    if(!completedFailed){
                        for(Log log : iterations.keySet()){
                            for(Event e : iterations.get(log)){
                                handling.add(e);
                            }
                        }
                        iseCollective.addISE(new ISESingle(trigger, handling));
                        break;
                    }
                }
            }
        }
        return iseCollective;
    }

    private Map<Log, List<Event>> collect_iterations(Event event, Trace currentTrace){
        Map<Log, List<Event>> iterations = new HashMap<>();
        int start = currentTrace.getEvents().indexOf(event) + 1;
        int end = currentTrace.getEvents().size();
        for(int i = start; i < end; i++){
            Event tempEvent = currentTrace.getEvents().get(i);
            if(!tempEvent.getConceptName().equals(event.getConceptName())){
               continue;
            }
            if(!(tempEvent.getLifecycle().equals("Completed.Failed") || tempEvent.getLifecycle().equals("Completed.Success"))){
                continue;
            }
            Log originalLog = tempEvent.getOriginalLog();
            if(iterations.containsKey(originalLog)){
                iterations.get(originalLog).add(tempEvent);
            }else{
                List<Event> tempList = new ArrayList<>();
                tempList.add(tempEvent);
                iterations.put(originalLog, tempList);
            }
        }
        return iterations;
    }
}

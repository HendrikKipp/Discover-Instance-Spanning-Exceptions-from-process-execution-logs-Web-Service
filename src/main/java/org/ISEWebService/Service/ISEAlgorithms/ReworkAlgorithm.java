package org.ISEWebService.Service.ISEAlgorithms;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReworkAlgorithm {

    /**
     * Discovers ISE of type Rework
     * @param mergedTraces
     * @return
     */
    public ISECollective discoverRework(List<Trace> mergedTraces){
        ISECollective iseCollective = new ISECollective(ISECollective.ISEClass.REWORK);
        nextTrace: for(Trace trace : mergedTraces){
            List<Event> tempPreviousComputedEvents = new ArrayList<>();
            for(Event event : trace.getEvents()){
                Event trigger = null;
                List<Event> handling = new ArrayList<>();
                if(event.getLifecycle().equals("pi_abort")){
                    trigger = event;
                    Set<Trace> previousInstancesList = previousInstances(tempPreviousComputedEvents);
                    for(Trace previousInstance : previousInstancesList){
                        List<Event> additionalEvents = previousInstance.hasAdditionalEvents(trigger);
                        if(!additionalEvents.isEmpty()){
                            for(Event additionalEvent : additionalEvents){
                                if(tempPreviousComputedEvents.contains(additionalEvent) || additionalEvent.getOriginalTrace().getEvents().contains(trigger)){
                                    handling.add(additionalEvent);
                                }
                            }
                        }
                    }
                    if(!handling.isEmpty()){
                        iseCollective.addISE(new ISESingle(trigger, handling));
                        continue nextTrace;
                    }
                }
                tempPreviousComputedEvents.add(event);
            }
        }

        return iseCollective;
    }

    private Set<Trace> previousInstances(List<Event> tempPreviousComputedEvents){
        Set<Trace> result = new HashSet<>();
        for(Event event : tempPreviousComputedEvents){
            if(!result.contains(event.getOriginalTrace())){
                result.add(event.getOriginalTrace());
            }
        }
        return result;
    }
}

package org.ISEWebService.Service.ISEAlgorithms;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CancelAlgorithm {

    /**
     * Discovers ISE of type CANCEL
     * @param mergedTraces
     * @return
     */
    public ISECollective discoverCancel(List<Trace> mergedTraces){
        ISECollective iseCollective = new ISECollective(ISECollective.ISEClass.CANCEL);
        for(Trace trace : mergedTraces){
            for(Event event : trace.getEvents()){
                if(event.getLifecycle().equals("pi_abort")){
                    Event trigger = null;
                    List<Event> handling = new ArrayList<>();
                    List<Event> potentials = collect_events(event.getConceptName(), trace);
                    for(Event potential : potentials){
                        if(potential.getLifecycle().equals("complete")){
                            trigger = potential;
                        }else if(potential.getLifecycle().equals("pi_abort")){
                            handling.add(potential);
                        }
                    }
                    if(trigger!=null && !handling.isEmpty()){
                        iseCollective.addISE(new ISESingle(trigger, handling));
                        break;
                    }
                }
            }
        }

        return iseCollective;
    }

    private List<Event> collect_events(String conceptName, Trace currentTrace){
        List<Event> result = new ArrayList<>();
        for(Event event : currentTrace.getEvents()){
            if(event.getConceptName().equals(conceptName)){
                result.add(event);
            }
        }

        return result;
    }
}

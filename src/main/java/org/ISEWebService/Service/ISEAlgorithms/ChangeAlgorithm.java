package org.ISEWebService.Service.ISEAlgorithms;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChangeAlgorithm {

    /**
     * Discovers ISE of type Change
     * @param mergedTraces
     * @return
     */
    public ISECollective discoverChange(List<Trace> mergedTraces){
        ISECollective iseCollective = new ISECollective(ISECollective.ISEClass.CHANGE);

        for(Trace trace : mergedTraces){
            String o_res = null;
            Event trigger = null;
            List<Event> handling = new ArrayList<>();
            for(Event event : trace.getEvents()){
                if(event.getLifecycle().equals("reassign")){
                    o_res = event.getAttributeMap().get("org:resource");
                    String n_res = find_start_resource(event);
                    if(!o_res.equals(n_res)){
                        trigger = event;
                        continue;
                    }
                }
                if(trigger != null && event.getLifecycle().equals("start") && event.getAttributeMap().get("org:resource").equals(o_res)){
                    handling.add(event);
                }
            }
            if(!handling.isEmpty()){
                iseCollective.addISE(new ISESingle(trigger, handling));
            }
        }
        return iseCollective;
    }

    private String find_start_resource(Event event){
        return event.getTask().getEvents().get(0).getAttributeMap().get("org:resource");
    }
}

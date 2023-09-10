package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Trace;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PPIRework extends PPICalculation{
    /**
     * Average number of newly created handling tasks
     * @param iseSingle
     * @return
     */
    public String reworkPPI1(ISESingle iseSingle){
        Set<Trace> traces = new HashSet<>();
        for(Event event : iseSingle.getHandling()){
            Trace trace = event.getOriginalTrace();
            if(!traces.contains(trace)){
                traces.add(trace);
            }
        }

        return "" + iseSingle.getHandling().size() / 2 / traces.size();
    }

    /**
     * Duration between occurrence and discovery of broken resource
     * @param iseSingle
     * @return
     */

    public String reworkPPI2(ISESingle iseSingle){
        Date abortTriggerTask = new Date(0);
        for(Event event : iseSingle.getTrigger().getTask().getEvents()){
            if(event.getLifecycle().equals("pi_abort")){
                abortTriggerTask = event.getTimestamp();
                break;
            }
        }

        Date firstTimeStamp = new Date(Long.MAX_VALUE);
        for(Event event : iseSingle.getHandling()){
            int positionElementPriorHandling = event.getOriginalTrace().getEvents().indexOf(event) - 1;
            Event elementPriorHandling = event.getOriginalTrace().getEvents().get(positionElementPriorHandling);
            if(elementPriorHandling.getTimestamp().getTime() <= firstTimeStamp.getTime()){
                firstTimeStamp = elementPriorHandling.getTimestamp();
            }

        }

        double duration = abortTriggerTask.getTime() - firstTimeStamp.getTime();
        if(duration == 0){
            return "0min";
        }else{
            return convertTimeToYearDayHourMinute(duration, true);
        }
    }

    /**
     * Average deviation of throughput time
     * @param iseSingle
     * @return
     */
    public String reworkPPI3(ISESingle iseSingle){
        return isePPI3(iseSingle);
    }
}

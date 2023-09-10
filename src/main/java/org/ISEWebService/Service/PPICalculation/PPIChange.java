package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Task;

import java.util.Date;

public class PPIChange extends PPICalculation{

    /**
     * Duration of resource being broken
     * @param iseSingle
     * @return
     */
    public String changePPI1(ISESingle iseSingle){
        Date reassignResource = new Date(0);
        Task taskTrigger = iseSingle.getTrigger().getTask();
        for(Event event : taskTrigger.getEvents()){
            if(event.getLifecycle().equals("reassign")){
                reassignResource = event.getTimestamp();
                break;
            }
        }

        Date lastUseNewResource = new Date(0);
        for(Event event : iseSingle.getHandling()){
            if(event.getTimestamp().getTime() >= lastUseNewResource.getTime()){
                lastUseNewResource = event.getTimestamp();
            }
        }

        return convertTimeToYearDayHourMinute(lastUseNewResource.getTime() - reassignResource.getTime(), true);
    }

    /**
     * Duration of trigger task until reassign
     * @param iseSingle
     * @return
     */
    public String changePPI2(ISESingle iseSingle){
        Date reassignResource = new Date(0);
        Task taskTrigger = iseSingle.getTrigger().getTask();
        for(Event event : taskTrigger.getEvents()){
            if(event.getLifecycle().equals("reassign")){
                reassignResource = event.getTimestamp();
                break;
            }
        }

        Date startTriggerTask = iseSingle.getTrigger().getTask().getEvents().get(0).getTimestamp();

        double durationOfTriggerTaskUntilReassign = reassignResource.getTime() - startTriggerTask.getTime();
        if(durationOfTriggerTaskUntilReassign == 0){
            return "0 min";
        }

        return convertTimeToYearDayHourMinute(durationOfTriggerTaskUntilReassign, true);
    }

    /**
     * Average deviation of throughput time
     * @param iseSingle
     * @return
     */
    public String changePPI3(ISESingle iseSingle){
        return this.isePPI3(iseSingle);
    }
}

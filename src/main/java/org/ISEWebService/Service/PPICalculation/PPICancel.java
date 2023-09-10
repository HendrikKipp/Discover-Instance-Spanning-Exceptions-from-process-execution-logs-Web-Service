package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;

import java.util.Date;

public class PPICancel extends PPICalculation{

    /**
     * Duration of cancelling task
     * @param iseSingle
     * @return
     */
    public String cancelPPI1(ISESingle iseSingle){
        return convertTimeToYearDayHourMinute(iseSingle.getTrigger().getTask().getDurationTask(), true);
    }

    /**
     * Duration of propagation of cancellation of the task
     * @param iseSingle
     * @return
     */
    public String cancelPPI2(ISESingle iseSingle){
        Date completionTriggerTask = iseSingle.getTrigger().getTimestamp();
        Date lasAbortHandlingTask = new Date(0);
        for(Event event : iseSingle.getHandling()){
            if(event.getTimestamp().getTime() >= lasAbortHandlingTask.getTime()){
                lasAbortHandlingTask = event.getTimestamp();
            }
        }

        if(lasAbortHandlingTask.getTime() - completionTriggerTask.getTime() == 0){
            return "0min";
        }else if(lasAbortHandlingTask.getTime() - completionTriggerTask.getTime() < 0){
            return "-" + convertTimeToYearDayHourMinute((-1) * (lasAbortHandlingTask.getTime() - completionTriggerTask.getTime()), true);
        }

        return convertTimeToYearDayHourMinute(lasAbortHandlingTask.getTime() - completionTriggerTask.getTime(), true);
    }

    /**
     * Average deviation of throughput time
     * @param iseSingle
     * @return
     */
    public String cancelPPI3(ISESingle iseSingle){
        return isePPI3(iseSingle);
    }
}

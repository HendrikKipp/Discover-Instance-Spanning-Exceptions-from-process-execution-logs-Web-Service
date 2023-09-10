package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;

import java.util.HashSet;
import java.util.Set;

public class PPIWait extends PPICalculation{

    /**
     * Wait-PPI1: Deviation of duration of trigger task
     * Explanation: The deviation of the duration of the trigger task from the average duration of the same task in the
     * other process instances of the respective process is calculated.
     * @param iseSingle
     * @return
     */
    public String waitPPI1(ISESingle iseSingle){
        String conceptName = iseSingle.getTrigger().getConceptName();
        double meanDuration = iseSingle.getTrigger().getOriginalLog().getMean(conceptName);
        double triggerDuration = iseSingle.getTrigger().getTask().getDurationTask();
        double changeInDurationOfTriggerTask = triggerDuration - meanDuration;

        return convertTimeToYearDayHourMinute(changeInDurationOfTriggerTask, true);
    }

    /**
     * Wait-PPI2: Sum of deviation of duration of handling task
     * Explanation: The average deviation of the respective durations of the handling task to the average duration of
     * the same task in the other process instances of the respective process is calculated.
     * @param iseSingle
     * @return
     */
    public String waitPPI2(ISESingle iseSingle){
        double totalChangeInDurationOfHandlingTask = 0;
        for(Event handlingEvent : iseSingle.getHandling()){
            String conceptName = handlingEvent.getConceptName();
            double meanDuration = handlingEvent.getOriginalLog().getMean(conceptName);
            double handlingDuration = handlingEvent.getTask().getDurationTask();
            totalChangeInDurationOfHandlingTask += handlingDuration - meanDuration;
        }

        return convertTimeToYearDayHourMinute(totalChangeInDurationOfHandlingTask, true);
    }

    /**
     * Wait-PPI3: Average of deviations of throughput time
     * Explanation: For each process instance involved in the ISE, the deviation of the respective throughput time from
     * the average throughput time of the respective process is determined and then the deviations are added up.
     * @param iseSingle
     * @return
     */
    public String waitPPI3(ISESingle iseSingle){
        return isePPI3(iseSingle);
    }
}

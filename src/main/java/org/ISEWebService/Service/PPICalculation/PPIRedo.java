package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Task;
import org.ISEWebService.Model.EventLog.Trace;

import java.util.*;

public class PPIRedo extends PPICalculation{

    /**
     * Iterations
     * Average deviation of throughput time
     * @param iseSingle
     * @return
     */
    public String[] redoPPI1PPI2(ISESingle iseSingle){
        Map<Trace, List<Event>> traceWithTasks = new HashMap<>();

        // Trigger
        List<Event> tempTriggerList = new ArrayList<>();
        tempTriggerList.add(iseSingle.getTrigger());
        traceWithTasks.put(iseSingle.getTrigger().getOriginalTrace(), tempTriggerList);

        // Handling
        for(Event event : iseSingle.getHandling()){
            Trace trace = event.getOriginalTrace();
            if(traceWithTasks.containsKey(trace)){
                traceWithTasks.get(trace).add(event);
            }else{
                List<Event> tempHandlingList = new ArrayList<>();
                tempHandlingList.add(event);
                traceWithTasks.put(trace, tempHandlingList);
            }
        }

        // Iterations
        Set<Task> tasks = new HashSet<>();
        for(Event event : traceWithTasks.get(iseSingle.getTrigger().getOriginalTrace())){
            if(tasks.contains(event.getTask())){
                continue;
            }
            tasks.add(event.getTask());
        }

        String iterations = "" + tasks.size();

        // Duration of additional iterations
        double durationOfAdditionalIterations = 0;
        for(List<Event> eventList : traceWithTasks.values()){
            Date lastDate = new Date(0);
            Date firstDate = new Date(Long.MAX_VALUE);

            // Skip first iteration
            int count = 0;
            for(Event e : eventList){
                if(count == 0){
                    count++;
                    continue;
                }
                count++;
                if(e.getTimestamp().getTime() >= lastDate.getTime()){
                    lastDate = e.getTimestamp();
                }
                if(e.getTimestamp().getTime() <= firstDate.getTime()){
                    firstDate = e.getTimestamp();
                }
            }

            durationOfAdditionalIterations += lastDate.getTime() - firstDate.getTime();

        }

        String durationOfAdditionalItearationsString;
        if(durationOfAdditionalIterations == 0){
            durationOfAdditionalItearationsString = "0 min";
        }else if(durationOfAdditionalIterations < 0){
            durationOfAdditionalItearationsString = convertTimeToYearDayHourMinute((-1) * durationOfAdditionalIterations, true);
        }else{
            durationOfAdditionalItearationsString = convertTimeToYearDayHourMinute(durationOfAdditionalIterations, true);
        }

        return new String[]{iterations, durationOfAdditionalItearationsString};
    }

    /**
     * Average deviation of throughput time
     * @param iseSingle
     * @return
     */
    public String redoPPI3(ISESingle iseSingle){
        return this.isePPI3(iseSingle);
    }
}

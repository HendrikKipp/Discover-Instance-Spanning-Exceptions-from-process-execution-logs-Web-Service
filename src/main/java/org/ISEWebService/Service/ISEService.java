package org.ISEWebService.Service;

import org.ISEWebService.Model.DTO.*;
import org.ISEWebService.Model.Enums.ISEAlgorithmStatus;
import org.ISEWebService.Model.Enums.ISEAlgorithmType;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Task;
import org.ISEWebService.Model.EventLog.Trace;
import org.ISEWebService.Service.ISEAlgorithms.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ISEService {

    public void checkApplicabilityISEAlgorithms(List<Log> parsedEventLogs, Result result){
        Map<String, Boolean> presentRequirements = new HashMap<>();
        presentRequirements.put("concept:name", true);
        presentRequirements.put("lifecycle:transition", true);
        presentRequirements.put("lifecycle:transition_Start", true);
        presentRequirements.put("lifecycle:transition_Complete", true);
        presentRequirements.put("lifecycle:transition_Completed.Success", true);
        presentRequirements.put("org:resource", true);
        presentRequirements.put("time:timestamp", true);

        for(Log log : parsedEventLogs){
            for(Trace trace : log.getTraces()){
                // Gets all tasks per trace
                Set<Task> tasks = new HashSet<>();
                for(Event event : trace.getEvents()){
                    if(!tasks.contains(event.getTask())){
                        tasks.add(event.getTask());
                    }
                }

                // Checks tasks for requirements
                for(Task task : tasks){
                    boolean hasStart = false;
                    boolean hasComplete = false;
                    boolean hasCompletedSuccess = false;
                    for(Event event : task.getEvents()){
                        if(!event.getAttributeMap().containsKey("concept:name")){
                            presentRequirements.put("concept:name", false);
                        }
                        if(!event.getAttributeMap().containsKey("lifecycle:transition")){
                            presentRequirements.put("lifecycle:transition", false);
                            presentRequirements.put("lifecycle:transition_Start", false);
                            presentRequirements.put("lifecycle:transition_Complete", false);
                            presentRequirements.put("lifecycle:transition_Completed.Success", false);
                        }else{
                            if(event.getLifecycle().equals("start")){
                                hasStart = true;
                            }
                            if(event.getLifecycle().equals("complete") || event.getLifecycle().equals("pi_abort")){
                                hasComplete = true;
                            }
                            if(event.getLifecycle().equals("Completed.Success") || event.getLifecycle().equals("Completed.Failed") || event.getLifecycle().equals("complete")){
                                hasCompletedSuccess = true;
                            }
                        }
                        if(!event.getAttributeMap().containsKey("org:resource")){
                            presentRequirements.put("org:resource", false);
                        }
                        if(!event.getAttributeMap().containsKey("time:timestamp")){
                            presentRequirements.put("time:timestamp", false);
                        }
                    }
                    if(!hasStart){
                        presentRequirements.put("lifecycle:transition_Start", false);
                    }
                    if(!hasComplete){
                        presentRequirements.put("lifecycle:transition_Complete", false);
                    }
                    if(!hasCompletedSuccess){
                        presentRequirements.put("lifecycle:transition_Completed.Success", false);
                    }
                }
            }
        }

        Map<ISEAlgorithmType, ISEAlgorithmStatus> iseAlgorithmStatusMap = result.getIseAlgorithmStatusMap();
        for(ISEAlgorithmType iseAlgorithmType : iseAlgorithmStatusMap.keySet()){
            if(iseAlgorithmStatusMap.get(iseAlgorithmType) == ISEAlgorithmStatus.REQUESTED){
                switch (iseAlgorithmType){
                    case WAIT, CANCEL, REWORK:
                        // name, start, complete, timestamp
                        if(presentRequirements.get("concept:name") && presentRequirements.get("lifecycle:transition") && presentRequirements.get("lifecycle:transition_Start") && presentRequirements.get("lifecycle:transition_Complete") && presentRequirements.get("time:timestamp")){
                            result.setISEAlgorithmStatus(iseAlgorithmType, ISEAlgorithmStatus.APPLIED);
                        }
                        break;
                    case REDO:
                        // name, completed.success, timestamp
                        if(presentRequirements.get("concept:name") && presentRequirements.get("lifecycle:transition") && presentRequirements.get("lifecycle:transition_Completed.Success") && presentRequirements.get("time:timestamp")){
                            result.setISEAlgorithmStatus(iseAlgorithmType, ISEAlgorithmStatus.APPLIED);
                        }
                        break;
                    case CHANGE:
                        // name, complete, org:resource, timestamp
                        if(presentRequirements.get("concept:name") && presentRequirements.get("lifecycle:transition") && presentRequirements.get("lifecycle:transition_Complete") && presentRequirements.get("org:resource") && presentRequirements.get("time:timestamp")){
                            result.setISEAlgorithmStatus(iseAlgorithmType, ISEAlgorithmStatus.APPLIED);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Discovers wait exceptions in the given event logs. The waitParameter defines the threshold for the wait
     * @param originalLogs List of original logs
     * @param mergedTraces List of merged traces
     * @param waitParameter Threshold for wait
     * @return ISECollective with list of ISESingle representing the wait exceptions
     */
    public ISECollective discoverWait(List<Log> originalLogs, List<Trace> mergedTraces, double waitParameter){
        WaitAlgorithm waitAlgorithm= new WaitAlgorithm();
        return waitAlgorithm.discoverWait(mergedTraces, originalLogs, waitParameter);
    }

    public ISECollective discoverCancel(List<Trace> mergedTraces){
        CancelAlgorithm cancelAlgorithm = new CancelAlgorithm();
        return cancelAlgorithm.discoverCancel(mergedTraces);
    }
    public ISECollective discoverRedo(List<Trace> mergedTraces){
        RedoAlgorithm redoAlgorithm = new RedoAlgorithm();
        return redoAlgorithm.discoverRedo(mergedTraces);
    }
    public ISECollective discoverChange(List<Trace> mergedTraces){
        ChangeAlgorithm changeAlgorithm = new ChangeAlgorithm();
        return changeAlgorithm.discoverChange(mergedTraces);
    }
    public ISECollective discoverRework(List<Trace> mergedTraces){
        ReworkAlgorithm reworkAlgorithm = new ReworkAlgorithm();
        return reworkAlgorithm.discoverRework(mergedTraces);
    }

    /**
     * Merges traces of different logs into one trace list. The traces are merged by the given mergeAttribute. The
     * mergeAttribute must be present on trace level.
     * @param logs List of logs
     * @param mergeAttribute Attribute to merge traces
     * @return List of merged traces
     */
    public List<Trace> mergeTraces(List<Log> logs, String mergeAttribute) {
        // HashMap connects mergeAttribute and trace
        HashMap<String, Trace> tempHashMap = new LinkedHashMap<>();
        for (Log log : logs) {
            for (Trace trace : log.getTraces()) {
                Map<String, String> attributeMap = trace.getAttributeMap();
                if (attributeMap.containsKey(mergeAttribute)) {
                    if(!tempHashMap.containsKey(attributeMap.get(mergeAttribute).toString())) {
                        // Creates a new event and trace attribute list (copy of the old list) to avoid manipulating the original list
                        tempHashMap.put(attributeMap.get(mergeAttribute).toString(), new Trace(new ArrayList<>(trace.getEvents()), new HashMap<>(trace.getAttributeMap()), trace.getTraceName()));
                    }else{
                        tempHashMap.get(attributeMap.get(mergeAttribute)).insertOrdered(trace);
                    }
                }else{
                    throw new RuntimeException("Missing merge attribute in at least on trace. Merge attribute must be present on trace level.");
                }
            }
        }

        // Merge traces with same mergeAttribute
        List<Trace> mergedTraces = new ArrayList<>();
        for(String key : tempHashMap.keySet()){
            mergedTraces.add(tempHashMap.get(key));
        }

        return mergedTraces;
    }
}

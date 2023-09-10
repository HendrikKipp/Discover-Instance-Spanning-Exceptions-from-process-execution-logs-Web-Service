package org.ISEWebService.Service;

import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Task;
import org.ISEWebService.Model.EventLog.Trace;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class XESParserService {

    /**
     * Parses the multipart files into input streams and creates a list of process logs using a helper method
     *
     * @param multipartFiles Files uploaded by the user
     * @return List of process logs
     */
    public List<Log> parseXESFiles(MultipartFile[] multipartFiles) {
        List<InputStream> filesAsInputStreams = new ArrayList<>();
        List<String> filesNames = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                filesNames.add(multipartFile.getOriginalFilename());
                filesAsInputStreams.add(new ByteArrayInputStream(multipartFile.getBytes()));
            }
        } catch (IOException ioException) {
            throw new RuntimeException("Exception occurred parsing an XES file", ioException);
        }

        return this.parseXESFiles(filesAsInputStreams, filesNames);
    }

    /**
     * Parses a list of input streams into a list of process logs using a xes parser
     *
     * @param filesAsInputStreams Input streams representing the uploaded files by the user
     * @return List of process logs
     */
    public List<Log> parseXESFiles(List<InputStream> filesAsInputStreams, List<String> fileNames) {
        // Parses the input streams into a list of XLog objects using a xes xml parser
        List<XLog> xLogList = new ArrayList<>();
        try {
            XParser xParser = new XesXmlParser();
            for (InputStream inputStream : filesAsInputStreams) {
                xLogList.addAll(xParser.parse(inputStream));
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred parsing an XES file", e);
        }

        // Throws error, if an event log file has more than one log stored
        if(xLogList.size() != fileNames.size()){
            throw new RuntimeException("Exception occurred parsing an XES file: In at least one event log file there was more than one event log.");
        }

        // Transfer the list of XLog objects into your own event logs data structure
        List<Log> logs = new ArrayList<>();
        int numberLog = 0;
        for (XLog xLog : xLogList) {
            List<Trace> traces = new ArrayList<>();
            int numberTrace = 0;
            for (XTrace xTrace : xLog) {
                List<Event> events = new ArrayList<>();
                HashMap<String, List<Event>> conceptNameToEvent = new HashMap<>();
                for (XEvent xEvent : xTrace) {
                    Map<String, String> attributeMap = new HashMap<>();
                    for (String keyAttribute : xEvent.getAttributes().keySet()) {
                        attributeMap.put(keyAttribute, xEvent.getAttributes().get(keyAttribute).toString());
                    }
                    Event event = new Event(attributeMap);
                    events.add(event);

                    // In order to combine the following activities of a trace with the same name into one task,
                    // the individual names are collected here
                    String conceptName = attributeMap.get("concept:name");
                    if (conceptNameToEvent.containsKey(conceptName)) {
                        conceptNameToEvent.get(conceptName).add(event);
                    } else {
                        List<Event> tasks = new ArrayList<>();
                        tasks.add(event);
                        conceptNameToEvent.put(attributeMap.get("concept:name"), tasks);
                    }

                }
                Map<String, String> traceAttributeMap = new HashMap<>();
                for (String keyAttribute : xTrace.getAttributes().keySet()) {
                    traceAttributeMap.put(keyAttribute, xTrace.getAttributes().get(keyAttribute).toString());
                }
                traces.add(new Trace(events, traceAttributeMap, "instance_" + numberTrace++));

                // Creates a task per name and lifecycle and adds it to each event
                for (String s : conceptNameToEvent.keySet()) {
                    List<Event> tempEvents = conceptNameToEvent.get(s);
                    Collections.sort(tempEvents, new Comparator<Event>() {
                        @Override
                        public int compare(Event e1, Event e2) {
                            return e1.getTimestamp().compareTo(e2.getTimestamp());
                        }
                    });

                    List<Event> eventsPerTask = new ArrayList<>();
                    for(Event e : tempEvents){
                        eventsPerTask.add(e);

                        if(e.getLifecycle().equals("pi_abort") || e.getLifecycle().equals("complete") || e.getLifecycle().equals("Completed.Success") || e.getLifecycle().equals("Completed.Failed")){
                            Task tempTask = new Task(eventsPerTask);
                            tempTask.calculateDuration();
                            for(Event e1 : eventsPerTask){
                                e1.setTask(tempTask);
                            }
                            eventsPerTask = new ArrayList<>();
                        }
                    }
                }
            }

            // Creates a log and adds to each trace its original log and to each event its original trace
            Log log = new Log(traces);
            log.setProcessName(fileNames.get(numberLog++));
            logs.add(log);
            for (Trace trace : traces) {
                trace.setOriginalLog(log);
                for (Event event : trace.getEvents()) {
                    event.setOriginalTrace(trace);
                }
            }
        }

        return logs;
    }
}

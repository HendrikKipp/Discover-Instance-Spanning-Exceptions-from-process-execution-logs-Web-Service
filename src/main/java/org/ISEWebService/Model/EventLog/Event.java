package org.ISEWebService.Model.EventLog;

import org.springframework.format.datetime.standard.DateTimeFormatterFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

public class Event implements Comparable<Event>{
    private Map<String, String> attributeMap;
    private Task task;
    private Trace originalTrace;

    public Event(Map<String, String> attributeMap){
        this.attributeMap = attributeMap;
    }

    public Log getOriginalLog() {
        return this.originalTrace.getOriginalLog();
    }

    public Trace getOriginalTrace() {
        return originalTrace;
    }

    public void setOriginalTrace(Trace originalTrace) {
        this.originalTrace = originalTrace;
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public Task getTask(){
        return task;
    }

    public void setTask(Task task){
        this.task = task;
    }

    public Date getTimestamp(){
        // Checks for the following Date formats:
        // ISO_LOCAL_DATE_TIME: yyyy-MM-dd'T'HH:mm:ss
        // ISO_OFFSET_DATE_TIME: yyyy-MM-dd'T'HH:mm:ssXXX
        // ISO_ZONED_DATE_TIME: yyyy-MM-dd'T'HH:mm:ssXXX VV
        // ISO_DATE_TIME: yyyy-MM-dd'T'HH:mm:ss.SSSXXX

        for(String s : attributeMap.keySet()){
            if(s.equals("time:timestamp")){
                String time = attributeMap.get(s).toString();
                // ISO_LOCAL_DATE_TIME: yyyy-MM-dd'T'HH:mm:ss
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try{
                    return format.parse(time);
                }catch(ParseException p){}
                // ISO_OFFSET_DATE_TIME: yyyy-MM-dd'T'HH:mm:ssXXX
                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                try{
                    return format.parse(time);
                }catch(ParseException p){}
                // ISO_ZONED_DATE_TIME: yyyy-MM-dd'T'HH:mm:ssXXX VV
                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX VV");
                try{
                    return format.parse(time);
                }catch(ParseException p){}
                // ISO_DATE_TIME: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                try{
                    return format.parse(time);
                }catch(ParseException p){}

                throw new RuntimeException("Wrong Dateformat found in XES-File. Allowed formats: " +
                        "ISO_LOCAL_DATE_TIME, ISO_OFFSET_DATE_TIME, ISO_ZONED_DATE_TIME or ISO_DATE_TIME");
            }
        }
        throw new RuntimeException("No Timestamp found in at least one event of an XES-File");
    }

    public String getLifecycle(){
        return attributeMap.get("lifecycle:transition");
    }

    public String getConceptName(){
        return attributeMap.get("concept:name");
    }

    @Override
    public int compareTo(Event otherEvent) {
        try{
            Date timeStamp = this.getTimestamp();
            Date timeStampOther = otherEvent.getTimestamp();
            int compare = timeStamp.compareTo(timeStampOther);
            if(compare < 0){
                return -1;
            }
            if(compare > 0){
                return 1;
            }
            if(this.getTask().getDurationTask() > otherEvent.getTask().getDurationTask()){
                return -1;
            }
            if(this.getTask().getDurationTask() < otherEvent.getTask().getDurationTask()){
                return 1;
            }
            return 0;
        }catch (Exception e){
            throw new RuntimeException();
        }
    }
}

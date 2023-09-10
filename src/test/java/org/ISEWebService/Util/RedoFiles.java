package org.ISEWebService.Util;

import org.deckfour.xes.model.XLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RedoFiles extends XesFile{
    @Override
    public List<ByteArrayOutputStream> create() throws IOException, ParseException {
        // Create Process Templates
        List<ProcessTemplate> processTemplates = new ArrayList<>();
        processTemplates.add(createPlayInstrument());
        processTemplates.add(createRecordMusic());

        // Creates Exceptions
        ExceptionCollection exceptionCollection = this.createExceptions();

        // Create XLogs with Exceptions
        List<XLog> xLogList = this.createXLogListWithExceptions(processTemplates, exceptionCollection);

        // Parse XLogList to Files
        // this.serializeXLogToFile(xLogList, "redo/redoISE");

        // Parse XLogList to OutputSteam
        return this.serializeXLogToOutputStream(xLogList, "redoISE");
    }

    private ProcessTemplate createPlayInstrument() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Prepare instrument", "Produce song", "Dismantle instrument"};
        // Amount of Traces of the Log
        int amountTraces = 6;
        // Duration of a Task
        long durationTaskInMinute = 60;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 10;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60 * 24 * 2; // Every 2 d
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2022-04-15T11:35:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "song";
        // Different merge attribute values
        String[] mergeAttributeValues = {"ShapeOfYou", "SeeYouAgain", "Sugar", "Sugar", "CountingStars", "Sunflower"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ProcessTemplate createRecordMusic() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Setup recording", "Produce song", "Postprocess recording"};
        // Amount of Traces of the Log
        int amountTraces = 5;
        // Duration of a Task
        long durationTaskInMinute = 60;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 10;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60 * 24 * 2; // Every 2 d
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2022-04-15T11:35:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "song";
        // Different merge attribute values
        String[] mergeAttributeValues = {"ShapeOfYou", "SeeYouAgain", "Sugar", "CountingStars", "Sunflower"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ExceptionCollection createExceptions(){
        // Redo-Exceptions
        List<ExceptionDefinition> redoExceptions = new ArrayList<>();

        // Redo-Exception 1
        redoExceptions.add(new RedoException(new int[][]{{0, 0, 1},{1, 0, 1}}, new boolean[][]{{false, true, false, false, true}, {false, false, false, true, true}}, 5));

        // Redo-Exception 2
        redoExceptions.add(new RedoException(new int[][]{{0, 2, 1}, {1, 2, 1}}, new boolean[][]{{false, false, true}, {false, false, true}}, 3));

        return new ExceptionCollection(ExceptionType.REDO_EXCEPTION, redoExceptions);
    }
}

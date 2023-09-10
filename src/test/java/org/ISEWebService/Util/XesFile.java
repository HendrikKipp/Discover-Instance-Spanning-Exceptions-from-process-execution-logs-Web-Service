package org.ISEWebService.Util;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;

public abstract class XesFile {

    public abstract List<ByteArrayOutputStream> create() throws IOException, ParseException;

    protected List<XLog> createXLogListWithExceptions(List<ProcessTemplate> processTemplates, ExceptionCollection exceptionCollection) {
        // Initialize factory
        XFactory xFactory = new XFactoryNaiveImpl();

        // Initialize list of logs
        List<XLog> xLogList = new ArrayList<>();

        // Creates XLog for each process template
        int countLog = 0;
        for (ProcessTemplate processTemplate : processTemplates) {
            XLog xLog = xFactory.createLog();
            xLogList.add(xLog);

            // Creates Traces for XLog
            Date startTimeTrace = new Date(processTemplate.getFirstTimestamp().getTime());
            for (int i = 0; i < processTemplate.getAmountTraces(); i++) {
                XTrace xTrace = createXTrace(xFactory, processTemplate.getMergeAttribute(), processTemplate.getMergeValue(i, processTemplate.getAmountTraces()));
                xLog.add(xTrace);

                // Creates Tasks for Trace
                Date startTimeTask = new Date(startTimeTrace.getTime());
                for (int j = 0; j < processTemplate.getAmountTasks(); j++) {
                    // Checks for trigger or handling position
                    ExceptionDefinition exceptionDefinition = exceptionCollection.getExceptionByPosition(countLog, i, j);
                    if(exceptionDefinition != null){
                        switch (exceptionCollection.getExceptionType()){
                            case WAIT_EXCEPTION:
                                // Casts the ExceptionDefinition to WaitException
                                WaitException waitException = (WaitException) exceptionDefinition;

                                // Creates trigger or handling task
                                Date endTimeTaskWait = new Date((long) (startTimeTask.getTime() + processTemplate.getDurationTaskInMilli() * waitException.getTaskDelayAsFactor(countLog, i, j)));
                                xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTaskWait, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                // Set start time for next task
                                startTimeTask = new Date(endTimeTaskWait.getTime() + processTemplate.getDurationBetweenTasksInMilli());

                                // Sets trace start after last task
                                startTimeTrace = new Date((long) (startTimeTrace.getTime() + processTemplate.getDurationBetweenTraceStartsInMilli() * waitException.getNextTraceDelayAsFactor(countLog, i, j) - processTemplate.getDurationBetweenTraceStartsInMilli()));
                                break;
                            case CANCEL_EXCEPTION:
                                // Casts the ExceptionDefinition to CancelException
                                CancelException cancelException = (CancelException) exceptionDefinition;

                                // Creates trigger task
                                if(cancelException.isTrigger(countLog, i, j)){
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    Date endTimeTaskCancel = new Date((long) (startTimeTask.getTime() + processTemplate.getDurationTaskInMilli() * cancelException.getDurationUntilCancellationAsPercentage()));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTaskCancel, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTaskCancel.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                }
                                // Creates handling tasks
                                else{
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    Date endTimeTaskCancel = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTaskCancel, "pi_abort", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTaskCancel.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                }
                                break;
                            case REDO_EXCEPTION:
                                // Casts the ExceptionDefinition to RedoException
                                RedoException redoException = (RedoException) exceptionDefinition;

                                // Create one task for each iteration
                                int iterationStartPosition = redoException.getIterationStartPosition(countLog, i, j);
                                for(int x=0; x<redoException.getNumberOfIterations(); x++){
                                    Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    if(redoException.isCompletedSuccess(iterationStartPosition, x)){
                                        xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "Completed.Success", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    }else{
                                        xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "Completed.Failed", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    }

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                }
                                break;
                            case CHANGE_EXCEPTION:
                                // Casts the ExceptionDefinition to ChangeException
                                ChangeException changeException = (ChangeException) exceptionDefinition;

                                // Create trigger task
                                if(changeException.isTrigger(countLog, i, j)){
                                    Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "reassign", changeException.getNewResource()));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "complete", changeException.getNewResource()));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                }else{
                                    Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", changeException.getNewResource()));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "complete", changeException.getNewResource()));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                }
                                // Sets trace start after last task
                                startTimeTrace = new Date((long) (startTimeTrace.getTime() + processTemplate.getDurationBetweenTraceStartsInMilli() * changeException.getNextTraceDelayAsFactor(countLog, i, j) - processTemplate.getDurationBetweenTraceStartsInMilli()));
                                break;
                            case REWORK_EXCEPTION:
                                // Casts the ExceptionDefinition to ReworkException
                                ReworkException reworkException = (ReworkException) exceptionDefinition;

                                // Create trigger task with handling task for this trace
                                if(reworkException.isTrigger(countLog, i, j)){
                                    Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "pi_abort", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());

                                    for(String taskName : reworkException.getAdditionalTaskNames()){
                                        endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                        xTrace.add(createEvent(xFactory, taskName, startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                        xTrace.add(createEvent(xFactory, taskName, endTimeTask, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                        // Set start time for next task
                                        startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                    }
                                }else{
                                    Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                    xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                    // Set start time for next task
                                    startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());

                                    for(String taskName : reworkException.getAdditionalTaskNames()){
                                        endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                                        xTrace.add(createEvent(xFactory, taskName, startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                                        xTrace.add(createEvent(xFactory, taskName, endTimeTask, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                                        // Set start time for next task
                                        startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                                    }
                                }

                                break;
                        }
                    }else{
                        //Create regular task
                        Date endTimeTask = new Date(startTimeTask.getTime() + processTemplate.getDurationTaskInMilli());
                        xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], startTimeTask, "start", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));
                        xTrace.add(createEvent(xFactory, processTemplate.getTraceTemplate()[j], endTimeTask, "complete", processTemplate.getMergeValue(i, processTemplate.getAmountTraces())));

                        // Set start time for next task
                        startTimeTask = new Date(endTimeTask.getTime() + processTemplate.getDurationBetweenTasksInMilli());
                    }
                }
                // Set start time for next trace
                startTimeTrace = new Date(startTimeTrace.getTime() + processTemplate.getDurationBetweenTraceStartsInMilli());
            }
            countLog++;
        }

        // Serialize logs and return them
        return xLogList;
    }

    private XTrace createXTrace(XFactory xFactory, String mergeAttribute, String mergeAttributeValue){
        XAttributeMap xAttributeMap = xFactory.createAttributeMap();
        XAttribute xAttribute = xFactory.createAttributeLiteral(mergeAttribute, mergeAttributeValue, null);
        xAttributeMap.put(mergeAttribute, xAttribute);
        XTrace xTrace = xFactory.createTrace(xAttributeMap);

        return xTrace;
    }

    private XEvent createEvent(XFactory xFactory, String name, Date date, String lifecycle, String resource){
        XEvent xEvent = xFactory.createEvent();

        XAttributeMap xAttributeMap = xFactory.createAttributeMap();
        xEvent.setAttributes(xAttributeMap);

        XAttribute xAttributeConceptName = xFactory.createAttributeLiteral("concept:name", name, null);
        xAttributeMap.put("conceptName", xAttributeConceptName);

        XAttribute xAttributeTimestamp = xFactory.createAttributeTimestamp("time:timestamp", date, null);
        xAttributeMap.put("timeStamp", xAttributeTimestamp);

        XAttribute xAttributeLifecycle = xFactory.createAttributeLiteral("lifecycle:transition", lifecycle, null);
        xAttributeMap.put("lifecycleTransition", xAttributeLifecycle);

        if(resource != null){
            XAttribute xAttributeResource = xFactory.createAttributeLiteral("org:resource", resource, null);
            xAttributeMap.put("orgResource", xAttributeResource);
        }

        return xEvent;
    }

    protected void serializeXLogToFile(List<XLog> xLogList, String namePrefix) throws IOException {
        XesXmlSerializer xesXmlSerializer = new XesXmlSerializer();

        int x=0;
        for(XLog xlog : xLogList){
            OutputStream outputStream = new FileOutputStream("src/test/resources/logs/" + namePrefix + ++x + ".xes");
            xesXmlSerializer.serialize(xlog, outputStream);
        }
    }

    protected List<ByteArrayOutputStream> serializeXLogToOutputStream(List<XLog> xLogList, String namePrefix) throws IOException {
        XesXmlSerializer xesXmlSerializer = new XesXmlSerializer();
        List<ByteArrayOutputStream> result = new ArrayList<>();

        for(XLog xlog : xLogList){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            xesXmlSerializer.serialize(xlog, outputStream);
            result.add(outputStream);
        }

        return result;
    }

    protected class ProcessTemplate{
        private final String[] traceTemplate;
        private final int amountTraces;
        private final long durationTaskInMinute;
        private final long durationBetweenTasksInMinute;
        private final long durationBetweenTraceStarts;
        private final Date firstTimestamp;
        private final String mergeAttribute;
        private final String[] mergeAttributeValues;

        ProcessTemplate(String[] traceTemplate, int amountTraces, long durationTaskInMinute, long durationBetweenTasksInMinute, long durationBetweenTraceStarts, Date firstTimestamp, String mergeAttribute, String[] mergeAttributeValues) {
            this.traceTemplate = traceTemplate;
            this.amountTraces = amountTraces;
            this.durationTaskInMinute = durationTaskInMinute;
            this.durationBetweenTasksInMinute = durationBetweenTasksInMinute;
            this.durationBetweenTraceStarts = durationBetweenTraceStarts;
            this.firstTimestamp = firstTimestamp;
            this.mergeAttribute = mergeAttribute;
            this.mergeAttributeValues = mergeAttributeValues;
        }

        public String[] getTraceTemplate() {
            return traceTemplate;
        }

        public int getAmountTraces() {
            return amountTraces;
        }

        public long getDurationTaskInMilli() {
            return durationTaskInMinute * 60 * 1000;
        }

        public long getDurationBetweenTasksInMilli() {
            return durationBetweenTasksInMinute * 60 * 1000;
        }

        public long getDurationBetweenTraceStartsInMilli() {
            return durationBetweenTraceStarts * 60 * 1000;
        }

        public Date getFirstTimestamp() {
            return firstTimestamp;
        }

        public String getMergeAttribute() {
            return mergeAttribute;
        }

        public String[] getMergeAttributeValues() {
            return mergeAttributeValues;
        }

        public int getAmountTasks(){
            return traceTemplate.length;
        }

        public String getMergeValue(int indexTrace, int lengthTraces) {
            // Divides mergeValue in equal groups
            double relativePosition = ((double) (indexTrace + 1)) / (double) lengthTraces;

            for(int i=0; i<mergeAttributeValues.length; i++){
                if(relativePosition <= ((double) (i + 1) / (double) mergeAttributeValues.length)){
                    return mergeAttributeValues[i];
                }
            }

            return mergeAttributeValues[0];
        }
    }

    protected class ExceptionCollection{
        private ExceptionType exceptionType;
        private List<ExceptionDefinition> exceptionDefinitions;

        public ExceptionCollection(ExceptionType exceptionType, List<ExceptionDefinition> exceptionDefinitions){
            this.exceptionType = exceptionType;
            this.exceptionDefinitions = exceptionDefinitions;
        }

        public ExceptionType getExceptionType() {
            return exceptionType;
        }

        public ExceptionDefinition getExceptionByPosition(int log, int trace, int task){
            for(ExceptionDefinition exceptionDefinition : exceptionDefinitions){
                if(exceptionDefinition.hasPosition(log, trace, task)){
                    return exceptionDefinition;
                }
            }
            return null;
        }
    }

    protected abstract class ExceptionDefinition {
        protected abstract boolean hasPosition(int log, int trace, int task);
    }

    protected enum ExceptionType{
        WAIT_EXCEPTION,
        CANCEL_EXCEPTION,
        REDO_EXCEPTION,
        CHANGE_EXCEPTION,
        REWORK_EXCEPTION
    }

    protected class WaitException extends ExceptionDefinition{
        private double factorDurationTriggerTask;
        private double[] factorDurationHandlingTasks;
        private int[] positionTriggerTask;
        private int[][] positionHandlingTasks;
        private Map<String, Double> nextTraceDelayPerLog;

        public WaitException(double factorDurationTriggerTask, double[] factorDurationHandlingTasks, int[] positionTriggerTask, int[][] positionHandlingTasks, Map<String, Double> nextTraceDelayPerLog){
            this.factorDurationTriggerTask = factorDurationTriggerTask;
            this.factorDurationHandlingTasks = factorDurationHandlingTasks;
            this.positionTriggerTask = positionTriggerTask;
            this.positionHandlingTasks = positionHandlingTasks;
            this.nextTraceDelayPerLog = nextTraceDelayPerLog;
        }

        protected double getTaskDelayAsFactor(int log, int trace, int task){
            // Checks trigger
            if(positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task){
                return factorDurationTriggerTask;
            }

            // Checks handlings
            for(int i=0; i<positionHandlingTasks.length; i++){
                if(positionHandlingTasks[i][0] == log && positionHandlingTasks[i][1] == trace && positionHandlingTasks[i][2] == task){
                    return factorDurationHandlingTasks[i];
                }
            }

            return 1;
        }

        protected double getNextTraceDelayAsFactor(int log, int trace, int task){
            String key = "" + log + ", " + trace + ", " + task;
            if(nextTraceDelayPerLog.containsKey(key)){
                return nextTraceDelayPerLog.get(key);
            }

            return 1;
        }

        protected boolean hasPosition(int log, int trace, int task){
            // Checks trigger
            if(positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task){
                return true;
            }

            // Checks handlings
            for(int i=0; i<positionHandlingTasks.length; i++){
                if(positionHandlingTasks[i][0] == log && positionHandlingTasks[i][1] == trace && positionHandlingTasks[i][2] == task){
                    return true;
                }
            }

            return false;
        }
    }

    protected class CancelException extends ExceptionDefinition{
        private int[] positionTriggerTask;
        private int[][] positionHandlingTasks;
        private double durationUntilCancellationAsPercentage;

        public CancelException(int[] positionTriggerTask, int[][] positionHandlingTasks, double durationUntilCancellationAsPercentage){
            this.positionTriggerTask = positionTriggerTask;
            this.positionHandlingTasks = positionHandlingTasks;
            this.durationUntilCancellationAsPercentage = durationUntilCancellationAsPercentage;
        }

        @Override
        protected boolean hasPosition(int log, int trace, int task) {
            // Checks trigger
            if(positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task){
                return true;
            }

            // Checks handlings
            for(int i=0; i<positionHandlingTasks.length; i++){
                if(positionHandlingTasks[i][0] == log && positionHandlingTasks[i][1] == trace && positionHandlingTasks[i][2] == task){
                    return true;
                }
            }

            return false;
        }

        protected boolean isTrigger(int log, int trace, int task){
            if(positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task){
                return true;
            }
            return false;
        }

        protected double getDurationUntilCancellationAsPercentage(){
            return durationUntilCancellationAsPercentage;
        }
    }

    protected class RedoException extends ExceptionDefinition{
        private int[][] iterationStarts;
        private boolean[][] iterationPattern;
        private int numberOfIterations;

        public RedoException(int[][] iterationStarts, boolean[][] iterationPattern, int numberOfIterations){
            this.iterationStarts = iterationStarts;
            this.iterationPattern = iterationPattern;
            this.numberOfIterations = numberOfIterations;
        }

        @Override
        protected boolean hasPosition(int log, int trace, int task) {
            for(int i=0; i<iterationStarts.length; i++){
                if(iterationStarts[i][0] == log && iterationStarts[i][1] == trace && iterationStarts[i][2] == task){
                    return true;
                }
            }
            return false;
        }

        protected int getNumberOfIterations(){
            return numberOfIterations;
        }

        protected boolean isCompletedSuccess(int iterationStartPosition, int iteration){
            return iterationPattern[iterationStartPosition][iteration];
        }

        protected int getIterationStartPosition(int log, int trace, int task){
            for(int i=0; i<iterationStarts.length; i++){
                if(iterationStarts[i][0] == log && iterationStarts[i][1] == trace && iterationStarts[i][2] == task){
                    return i;
                }
            }
            return 0;
        }
    }

    protected class ChangeException extends ExceptionDefinition {
        private int[] positionTriggerTask;
        private int[][] positionHandlingTasks;
        private Map<String, Double> nextTraceDelayPerLog;
        private String newResource;

        public ChangeException(int[] positionTriggerTask, int[][] positionHandlingTasks, Map<String, Double> nextTraceDelayPerLog, String newResource) {
            this.positionTriggerTask = positionTriggerTask;
            this.positionHandlingTasks = positionHandlingTasks;
            this.nextTraceDelayPerLog = nextTraceDelayPerLog;
            this.newResource = newResource;
        }

        @Override
        protected boolean hasPosition(int log, int trace, int task) {
            // Checks trigger
            if (positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task) {
                return true;
            }

            // Checks handlings
            for (int i = 0; i < positionHandlingTasks.length; i++) {
                if (positionHandlingTasks[i][0] == log && positionHandlingTasks[i][1] == trace && positionHandlingTasks[i][2] == task) {
                    return true;
                }
            }

            return false;
        }

        protected boolean isTrigger(int log, int trace, int task) {
            // Checks trigger
            if (positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task) {
                return true;
            }

            return false;
        }

        protected String getNewResource() {
            return newResource;
        }

        protected double getNextTraceDelayAsFactor(int log, int trace, int task) {
            String key = "" + log + ", " + trace + ", " + task;
            if (nextTraceDelayPerLog.containsKey(key)) {
                return nextTraceDelayPerLog.get(key);
            }

            return 1;
        }
    }

    protected class ReworkException extends ExceptionDefinition{
        private int[] positionTriggerTask;
        private int[][] positionHandlingTasks;
        private String[] additionalTaskNames;

        public ReworkException(int[] positionTriggerTask, int[][] positionHandlingTasks, String[] additionalTaskNames){
            this.positionTriggerTask = positionTriggerTask;
            this.positionHandlingTasks = positionHandlingTasks;
            this.additionalTaskNames = additionalTaskNames;
        }

        @Override
        protected boolean hasPosition(int log, int trace, int task) {
            // Checks trigger
            if (positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task) {
                return true;
            }

            // Checks handlings
            for (int i = 0; i < positionHandlingTasks.length; i++) {
                if (positionHandlingTasks[i][0] == log && positionHandlingTasks[i][1] == trace && positionHandlingTasks[i][2] == task) {
                    return true;
                }
            }

            return false;
        }

        protected boolean isTrigger(int log, int trace, int task) {
            // Checks trigger
            if (positionTriggerTask[0] == log && positionTriggerTask[1] == trace && positionTriggerTask[2] == task) {
                return true;
            }

            return false;
        }

        protected String[] getAdditionalTaskNames(){
            return additionalTaskNames;
        }
    }
}

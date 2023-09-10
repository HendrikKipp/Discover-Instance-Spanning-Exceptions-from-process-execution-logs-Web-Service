package org.ISEWebService.Service;

import org.ISEWebService.Model.DTO.DashboardView;
import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.DTO.Result;
import org.ISEWebService.Model.Enums.ISEAlgorithmStatus;
import org.ISEWebService.Model.Enums.ISEAlgorithmType;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MainService {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private XESParserService xesParserService;

    @Autowired
    private ISEService iseService;

    @Autowired
    private BpmnModelService bpmnModelService;

    @Autowired
    private PPIService ppiService;

    public long upload(Result result, MultipartFile[] files){
        // Store XES-File in database for later use
        databaseService.saveXESFiles(files);

        // Parse XES-File
        List<Log> parsedEventLogs = xesParserService.parseXESFiles(files);

        // Check event logs for applicable ISESingle Algorithms
        iseService.checkApplicabilityISEAlgorithms(parsedEventLogs, result);

        // Create PPI for processes
        ppiService.computeProcessPPI(parsedEventLogs, result);

        // Merge Traces
        List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, result.getMergeAttribute());

        // Apply ISESingle Algorithms and create BPMN models
        if (result.getISEAlgorithmStatus(ISEAlgorithmType.WAIT) == ISEAlgorithmStatus.APPLIED) {
            // Discovers ISEs
            ISECollective iseCollective = iseService.discoverWait(parsedEventLogs, mergedTraces, result.getWaitThreshold());

            // Creates PPI
            ppiService.computeWaitPPI(iseCollective);

            // Creates bpmn Model for every discovered ISE
            for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                iseSingle.setBpmnModel(bpmnModelService.createIseBpmnModel(ISEAlgorithmType.WAIT, iseSingle));
            }

            // Adds ISEs to Result
            result.setISEAlgorithmResult(ISEAlgorithmType.WAIT, iseCollective);
        }

        if (result.getISEAlgorithmStatus(ISEAlgorithmType.CANCEL) == ISEAlgorithmStatus.APPLIED) {
            // Discover ISEs
            ISECollective iseCollective = iseService.discoverCancel(mergedTraces);

            // Create PPI
            ppiService.computeCancelPPI(iseCollective);

            // Creates bpmn Model for every discovered ISE
            for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                iseSingle.setBpmnModel(bpmnModelService.createIseBpmnModel(ISEAlgorithmType.CANCEL, iseSingle));
            }

            // Adds ISEs to Result
            result.setISEAlgorithmResult(ISEAlgorithmType.CANCEL, iseCollective);
        }

        if (result.getISEAlgorithmStatus(ISEAlgorithmType.REDO) == ISEAlgorithmStatus.APPLIED) {
            // Discover ISEs
            ISECollective iseCollective = iseService.discoverRedo(mergedTraces);

            // Create PPI
            ppiService.computeRedoPPI(iseCollective);

            // Creates bpmn Model for every discovered ISE
            for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                iseSingle.setBpmnModel(bpmnModelService.createIseBpmnModel(ISEAlgorithmType.REDO, iseSingle));
            }

            // Adds ISEs to Result
            result.setISEAlgorithmResult(ISEAlgorithmType.REDO, iseCollective);
        }

        if (result.getISEAlgorithmStatus(ISEAlgorithmType.CHANGE) == ISEAlgorithmStatus.APPLIED) {
            // Discover ISEs
            ISECollective iseCollective = iseService.discoverChange(mergedTraces);

            // Create PPI
            ppiService.computeChangePPI(iseCollective);

            // Creates bpmn Model for every discovered ISE
            for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                iseSingle.setBpmnModel(bpmnModelService.createIseBpmnModel(ISEAlgorithmType.CHANGE, iseSingle));
            }

            // Adds ISEs to Result
            result.setISEAlgorithmResult(ISEAlgorithmType.CHANGE, iseCollective);
        }

        if (result.getISEAlgorithmStatus(ISEAlgorithmType.REWORK) == ISEAlgorithmStatus.APPLIED) {
            // Discover ISEs
            ISECollective iseCollective = iseService.discoverRework(mergedTraces);

            // Create PPI
            ppiService.computeReworkPPI(iseCollective);

            // Creates bpmn Model for every discovered ISE
            for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                iseSingle.setBpmnModel(bpmnModelService.createIseBpmnModel(ISEAlgorithmType.REWORK, iseSingle));
            }

            // Adds ISEs to Result
            result.setISEAlgorithmResult(ISEAlgorithmType.REWORK, iseCollective);
        }

        // Add all needed results to database and return id
        return databaseService.saveResults(result);
    }

    public DashboardView getDashboardView(Long id){
        return databaseService.getResultDashboard(id);
    }

    public List<String> getUploadHistory(){
        return databaseService.getUploadHistory();
    }

    public byte[] getBpmnModel(Long id){
        return databaseService.getBpmnModel(id);
    }
}

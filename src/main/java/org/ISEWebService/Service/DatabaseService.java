package org.ISEWebService.Service;

import org.ISEWebService.Model.DTO.DashboardView;
import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.DTO.Result;
import org.ISEWebService.Model.Entity.*;
import org.ISEWebService.Model.Enums.ISEAlgorithmType;
import org.ISEWebService.Repository.BpmnModelRepository;
import org.ISEWebService.Repository.ResultRepository;
import org.ISEWebService.Repository.XESFileListRepository;
import org.ISEWebService.Repository.XESFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    @Autowired
    private XESFileRepository xesFileRepository;

    @Autowired
    private XESFileListRepository xesFileListRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private BpmnModelRepository bpmnModelRepository;

    public long saveResults(Result result){
        ResultEntity resultEntity = new ResultEntity();

        for(ISEAlgorithmType iseAlgorithmType : result.getIseAlgorithmStatusMap().keySet()){
            switch (result.getIseAlgorithmStatusMap().get(iseAlgorithmType)){
                case REQUESTED:
                    resultEntity.addISEClassEntity(new ISEClassEntity(iseAlgorithmType.toString(), null, null, null, true));
                    break;
                case APPLIED:
                    List<ISEEntity> iseEntityList = new ArrayList<>();
                    ISECollective iseCollective = result.getIseAlgorithmResult().get(iseAlgorithmType);

                    // Checks, if results are found
                    if(iseCollective.getSingleIseList().isEmpty()){
                        resultEntity.addISEClassEntity(new ISEClassEntity(iseAlgorithmType.toString(), null, null, null, true));
                    }else{
                        for(ISESingle iseSingle : iseCollective.getSingleIseList()){
                            iseEntityList.add(new ISEEntity(iseSingle.getPpi(), iseSingle.getPpiNames(), new BpmnModelEntity(iseSingle.getBpmnModel())));
                        }
                        resultEntity.addISEClassEntity(new ISEClassEntity(iseAlgorithmType.toString(), iseCollective.getPpi(), iseCollective.getPpiNames(), iseEntityList, false));
                    }
                    break;
            }
        }

        for(String processName : result.getProcessPPI().keySet()){
            resultEntity.addProcessEntity(new ProcessEntity(processName, result.getProcessPPI().get(processName), result.getProcessPPINames().get(processName)));
        }

        resultRepository.save(resultEntity);

        return resultEntity.getId();
    }

    public byte[] getBpmnModel(Long id){
        return bpmnModelRepository.findById(id).get().getBpmnModel();
    }

    public DashboardView getResultDashboard(Long id){
        ResultEntity resultEntity = resultRepository.findById(id).get();

        // Creates DashboardView
        List<DashboardView.ProcessTab> processTabs = new ArrayList<>();
        List<DashboardView.ISETab> iseTabs = new ArrayList<>();
        DashboardView dashboardView = new DashboardView(processTabs, iseTabs);

        // Creates ProcessTabs
        for(ProcessEntity processEntity : resultEntity.getProcessEntityList()){
            processTabs.add(dashboardView.new ProcessTab(processEntity.getProcessName(), processEntity.getPpiWithNames()));
        }

        // Creates ISETabs and ISEs
        for(ISEClassEntity iseClassEntity : resultEntity.getIseClassEntityList()){
            if(iseClassEntity.isRequestedButNotApplied()){
                iseTabs.add(dashboardView.new ISETab(convertString(iseClassEntity.getClassName()), null, null, true));
            }else{
                List<DashboardView.ISE> iseList = new ArrayList<>();
                for(ISEEntity iseEntity : iseClassEntity.getIseEntityList()){
                    iseList.add(dashboardView.new ISE(iseEntity.getPpiWithNames(), iseEntity.getBpmnModel().getId()));
                }
                iseTabs.add(dashboardView.new ISETab(convertString(iseClassEntity.getClassName()), iseClassEntity.getPpiWithNames(), iseList, false));
            }
        }

        List<String> classOrder = List.of("Wait-Class", "Cancel-Class", "Redo-Class", "Change-Class", "Rework-Class");
        Collections.sort(iseTabs, new Comparator<DashboardView.ISETab>() {
            @Override
            public int compare(DashboardView.ISETab iseTab1, DashboardView.ISETab iseTab2) {
                return Integer.compare(classOrder.indexOf(iseTab1.getIseType()), classOrder.indexOf(iseTab2.getIseType()));
            }
        });

        Collections.sort(iseTabs, new Comparator<DashboardView.ISETab>() {
            @Override
            public int compare(DashboardView.ISETab d1, DashboardView.ISETab d2) {
                return Boolean.compare(d1.isRequestedButNotApplied(), d2.isRequestedButNotApplied());
            }
        });

        return dashboardView;
    }

    private String convertString(String s){
        if(s.length() > 1){
            String firstPart = s.substring(0, 1);
            String secondPart = s.substring(1).toLowerCase();
            return firstPart + secondPart + "-Class";
        }
        return s + "-Class";
    }

    public Long saveXESFiles(MultipartFile[] multipartFiles) {
        List<XESFileEntity> xesFileEntityList = new ArrayList<XESFileEntity>();
        try {
            for(MultipartFile multipartFile : multipartFiles){
                XESFileEntity xesFileEntity = new XESFileEntity();
                xesFileEntity.setFileName(multipartFile.getOriginalFilename());
                xesFileEntity.setXESFile(multipartFile.getBytes());
                xesFileEntityList.add(xesFileEntity);
                xesFileRepository.save(xesFileEntity);
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred saving an XES file", e);
        }
        XESFileListEntity xesFileListEntity = new XESFileListEntity();
        xesFileListEntity.setXesFileEntities(xesFileEntityList);
        xesFileListRepository.save(xesFileListEntity);

        return xesFileListEntity.getId();
    }

    public List<InputStream> getXESFiles(List<Long> listIDs){
        List<XESFileEntity> xesFileEntityList = xesFileRepository.findAllById(listIDs);
        List<InputStream> inputStreamList = new ArrayList<InputStream>();
        for(XESFileEntity xesFileEntity:xesFileEntityList){
            inputStreamList.add(new ByteArrayInputStream(xesFileEntity.getXesFile()));
        }

        return inputStreamList;
    }

    public List<String> getUploadHistory(){
        List<String> fileNameList = new ArrayList<String>();
        List<XESFileListEntity> xesFileEntityList = xesFileListRepository.findAll();
        return xesFileEntityList.stream().sorted(Comparator.comparing(XESFileListEntity::getCreateDateTime).reversed()).map(x -> x.getXesFileEntities().stream().map(i -> i.getFileName().toString()).collect(Collectors.joining("; "))).collect(Collectors.toList());
    }
}

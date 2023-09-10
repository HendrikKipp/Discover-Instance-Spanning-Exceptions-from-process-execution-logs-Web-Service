package org.ISEWebService.Controller;

import org.ISEWebService.Model.DTO.Result;
import org.ISEWebService.Service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MainController {

    @Autowired
    private MainService mainService;

    /**
     * Creates and returns start page
     * @return
     */
    @GetMapping
    public String getHome(){
        return "index";
    }

    /**
     * Creates and returns upload page
     * @return
     */
    @GetMapping("upload")
    public String getUpload(){
        return "upload";
    }

    /**
     * Handles the uploaded form and redirects to dashboard with the corresponding resultID
     * Partially inspired by https://spring.io/guides/gs/uploading-files/
     * @param files
     * @param mergeAttribute
     * @param algorithmWait
     * @param algorithmCancel
     * @param algorithmRedo
     * @param algorithmChange
     * @param algorithmRework
     * @param waitThreshold
     * @return
     */
    @PostMapping("upload")
    public String upload(@RequestParam("files") MultipartFile[] files,
                         @RequestParam("mergeAttribute") String mergeAttribute,
                         @RequestParam(value = "algorithmWait", required = false) boolean algorithmWait,
                         @RequestParam(value = "algorithmCancel", required = false) boolean algorithmCancel,
                         @RequestParam(value = "algorithmRedo", required = false) boolean algorithmRedo,
                         @RequestParam(value = "algorithmChange", required = false) boolean algorithmChange,
                         @RequestParam(value = "algorithmRework", required = false) boolean algorithmRework,
                         @RequestParam(value = "waitParameter", required = false) double waitThreshold
    ){
        Result result = new Result(mergeAttribute, algorithmWait, algorithmCancel, algorithmRedo, algorithmChange, algorithmRework, waitThreshold);
        long resultID = mainService.upload(result, files);

        return "redirect:dashboard?" + "resultID=" + resultID;
    }

    /**
     * Creates and returns dashboard
     * @param model
     * @param resultID
     * @return
     */
    @GetMapping("dashboard")
    public String getDashboard(Model model, @RequestParam(value="resultID", required = false) String resultID){
        if(resultID != null){
            model.addAttribute("dashboardView", mainService.getDashboardView(Long.parseLong(resultID)));
        }
        return "dashboard";
    }

    /**
     * Handles api request from dashboard for results
     * @param modelID
     * @return
     */
    @GetMapping(value = "api/bpmnModel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody Resource getBpmnModel(@RequestParam(value = "modelID", required = false) String modelID){
        if(modelID != null){
            ByteArrayResource byteArrayResource = new ByteArrayResource(mainService.getBpmnModel(Long.parseLong(modelID)));
            return byteArrayResource;
        }
        return null;
    }
}

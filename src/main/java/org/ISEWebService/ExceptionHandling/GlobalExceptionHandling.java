package org.ISEWebService.ExceptionHandling;

import org.ISEWebService.Service.ISEAlgorithms.WaitAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandling {

    /**
     * Global Exception Handler
     * @param model
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Model model, Exception e){
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }
}

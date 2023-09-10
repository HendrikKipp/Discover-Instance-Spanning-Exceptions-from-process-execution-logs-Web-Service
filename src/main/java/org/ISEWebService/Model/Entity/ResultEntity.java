package org.ISEWebService.Model.Entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ResultEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessEntity> processEntityList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ISEClassEntity> iseClassEntityList;

    public ResultEntity(){
        processEntityList = new ArrayList<>();
        iseClassEntityList = new ArrayList<>();
    }

    public void addProcessEntity(ProcessEntity processEntity){
        this.processEntityList.add(processEntity);
    }

    public void addISEClassEntity(ISEClassEntity iseClassEntity){
        this.iseClassEntityList.add(iseClassEntity);
    }

    public Long getId() {
        return id;
    }

    public List<ProcessEntity> getProcessEntityList() {
        return processEntityList;
    }

    public List<ISEClassEntity> getIseClassEntityList() {
        return iseClassEntityList;
    }
}

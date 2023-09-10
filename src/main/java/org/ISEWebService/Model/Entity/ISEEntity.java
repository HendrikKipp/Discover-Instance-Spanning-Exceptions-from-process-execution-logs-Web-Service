package org.ISEWebService.Model.Entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ISEEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    private List<String> ppi;

    @ElementCollection
    private List<String> ppiNames;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private BpmnModelEntity bpmnModel;

    public ISEEntity(List<String> ppi, List<String> ppiNames, BpmnModelEntity bpmnModel){
        this.ppi = ppi;
        this.ppiNames = ppiNames;
        this.bpmnModel = bpmnModel;
    }

    public ISEEntity() {

    }

    public List<String[]> getPpiWithNames() {
        List<String[]> result = new ArrayList<>();

        for(int i=0; i<ppi.size(); i++){
            result.add(new String[]{ppi.get(i), ppiNames.get(i)});
        }

        return result;
    }

    public BpmnModelEntity getBpmnModel() {
        return bpmnModel;
    }
}

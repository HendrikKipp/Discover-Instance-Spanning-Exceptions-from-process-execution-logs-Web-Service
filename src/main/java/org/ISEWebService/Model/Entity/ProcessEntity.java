package org.ISEWebService.Model.Entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ProcessEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String processName;

    @ElementCollection
    private List<String> ppi;

    @ElementCollection
    private List<String> ppiNames;

    public ProcessEntity(String processName, List<String> ppi, List<String> ppiNames){
        this.processName = processName;
        this.ppiNames = ppiNames;
        this.ppi = ppi;
    }

    public ProcessEntity() {

    }

    public String getProcessName() {
        return processName;
    }

    public List<String[]> getPpiWithNames() {
        List<String[]> result = new ArrayList<>();

        for(int i=0; i<ppi.size(); i++){
            result.add(new String[]{ppi.get(i), ppiNames.get(i)});
        }

        return result;
    }
}

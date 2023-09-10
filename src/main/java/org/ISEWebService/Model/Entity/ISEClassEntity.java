package org.ISEWebService.Model.Entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ISEClassEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String className;

    @ElementCollection
    private List<String> ppi;

    @ElementCollection
    private List<String> ppiNames;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ISEEntity> iseEntityList;

    private boolean requestedButNotApplied;

    public ISEClassEntity(String className, List<String> ppi, List<String> ppiNames, List<ISEEntity> iseEntityList, boolean requestedButNotApplied){
        this.className = className;
        this.ppi = ppi;
        this.ppiNames = ppiNames;
        this.iseEntityList = iseEntityList;
        this.requestedButNotApplied = requestedButNotApplied;
    }

    public ISEClassEntity() {

    }

    public List<ISEEntity> getIseEntityList() {
        return iseEntityList;
    }

    public String getClassName() {
        return className;
    }

    public List<String[]> getPpiWithNames() {
        List<String[]> result = new ArrayList<>();

        for(int i=0; i<ppi.size(); i++){
            result.add(new String[]{ppi.get(i), ppiNames.get(i)});
        }

        return result;
    }

    public boolean isRequestedButNotApplied() {
        return requestedButNotApplied;
    }
}

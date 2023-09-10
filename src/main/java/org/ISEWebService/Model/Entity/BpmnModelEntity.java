package org.ISEWebService.Model.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class BpmnModelEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    private byte[] bpmnModel;

    public BpmnModelEntity(byte[] bpmnModel){
        this.bpmnModel = bpmnModel;
    }

    public BpmnModelEntity() {

    }

    public byte[] getBpmnModel() {
        return bpmnModel;
    }

    public Long getId() {
        return id;
    }
}

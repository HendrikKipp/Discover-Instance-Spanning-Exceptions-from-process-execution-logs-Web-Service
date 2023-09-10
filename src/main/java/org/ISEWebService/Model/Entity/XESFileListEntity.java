package org.ISEWebService.Model.Entity;

import jakarta.persistence.*;
import org.ISEWebService.Model.Entity.XESFileEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class XESFileListEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    @JoinColumn(name = "xesFileEntityList_id")
    private List<XESFileEntity> xesFileEntities;

    private final LocalDateTime createDateTime = LocalDateTime.now();

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<XESFileEntity> getXesFileEntities() {
        return xesFileEntities;
    }

    public void setXesFileEntities(List<XESFileEntity> xesFileEntities) {
        this.xesFileEntities = xesFileEntities;
    }
}

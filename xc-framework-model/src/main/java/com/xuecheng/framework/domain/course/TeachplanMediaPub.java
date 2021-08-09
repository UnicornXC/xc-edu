package com.xuecheng.framework.domain.course;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name="teachplan_media_pub")
@GenericGenerator(name="jpa-assigned",strategy = "assigned")
public class TeachplanMediaPub implements Serializable {

    private static final long serialVersionUUID = -916357110051689485L;

    @Id
    @GeneratedValue(generator="jpa-assigned")
    @Column(name = "teachplan_id")
    private String teachplanId;

    @Column(name="media_id")
    private String mediaId;

    @Column(name="media_fileoriginalname")
    private String mediaFileOriginalName;

    @Column(name="media_url")
    private String mediaUrl;

    @Column(name="courseid")
    private String courseId;

    // 时间戳
    @Column(name="timestamp")
    private Date timestamp;

}

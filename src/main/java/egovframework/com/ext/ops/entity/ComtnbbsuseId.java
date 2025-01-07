package egovframework.com.ext.ops.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class ComtnbbsuseId implements Serializable {
	
	private static final long serialVersionUID = -5635461025069353481L;

	@Column(name = "BBS_ID")
    private String bbsId;

    @Column(name = "TRGET_ID")
    private String trgetId;

}

package egovframework.com.ext.ops.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class ComtnbbsId implements Serializable {

	private static final long serialVersionUID = -9190253886780052509L;

	@Column(name = "NTT_ID")
    private Long nttId;

    @Column(name = "BBS_ID")
    private String bbsId;
}

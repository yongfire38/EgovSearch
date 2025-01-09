package egovframework.com.ext.ops.event;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardEvent {
    private BoardEventType eventType;
    private Long nttId;
    private String bbsId;
    private String nttSj;
    private String nttCn;
    private Date eventDateTime;
}

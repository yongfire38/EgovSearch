package egovframework.com.ext.ops.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Data
@Immutable
@Table(name = "COMVNUSERMASTER")
public class Comvnusermaster {

	@Id
    @Column(name = "ESNTL_ID")
    private String esntlId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "USER_NM")
    private String userNm;

    @Column(name = "USER_ZIP")
    private String userZip;

    @Column(name = "USER_ADRES")
    private String userAdres;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "GROUP_ID")
    private String groupId;

    @Column(name = "USER_SE")
    private String userSe;

    @Column(name = "ORGNZT_ID")
    private String orgnztId;

    @OneToMany(mappedBy = "userList")
    private List<Comtnbbs> comtnQustnrIems;
}

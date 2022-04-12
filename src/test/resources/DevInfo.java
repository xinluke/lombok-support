package cn.jpush.portal.bean.dev;

import java.util.Date;

public class DevInfo {
    private Long id;

    //用户注册名
    private String username;

    private String email;

    private String contacter;

    private String qq;

    private String mobile;

    //注册时的公司名
    private String companyName;

    private String devKey;

    private String devSecret;

    private Long parentId;

    private Date regDate; //注册时间

    private boolean isPushVip;  // 是否是推送VIP

    private int iAppVip = 0;  // iApp VIP信息，0 - 免费用户，1 - 过期用户，2 - 付费用户

    private int antiFraudVip = 0; // iApp VIP信息，0 - 免费用户，1 - 过期用户，2 - 付费用户

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean isPushVip() {
        return isPushVip;
    }

    public void setPushVip(boolean pushVip) {
        isPushVip = pushVip;
    }

    public int getiAppVip() {
        return iAppVip;
    }

    public void setiAppVip(int iAppVip) {
        this.iAppVip = iAppVip;
    }

    public int getAntiFraudVip() {
        return antiFraudVip;
    }

    public void setAntiFraudVip(int antiFraudVip) {
        this.antiFraudVip = antiFraudVip;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContacter() {
        return contacter;
    }

    public void setContacter(String contacter) {
        this.contacter = contacter;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDevKey() {
        return devKey;
    }

    public void setDevKey(String devKey) {
        this.devKey = devKey;
    }

    public String getDevSecret() {
        return devSecret;
    }

    public void setDevSecret(String devSecret) {
        this.devSecret = devSecret;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }
}

package com.link.cloud.network.bean;

/**
 * 作者：qianlu on 2018/11/5 17:21
 * 邮箱：zar.l@qq.com
 */
public class DeviceInfoBean {

    /**
     * id : 5
     * code : EA:07:68:94:BF:01
     * deviceModelId : 1
     * deviceTypeId : 1003
     * deviceVersionId : 1
     * branchId : 1
     * status : 1
     * networkingStatus : 2
     * createTime : 2018-10-31 15:15:54
     * merchantId : 1
     * bind : true
     * endTime : 2023-11-30 00:00:00
     * startTime : 2018-10-01 00:00:00
     * bindType : 1
     * remark : 柜子测试
     * merchantCode : KHJJD653
     * merchantName : 金证系统指静脉对接
     * branchCode : FDBM1236
     * branchName : 分店B
     * deviceType : 临时柜
     * deviceModel : SX1527
     * deviceVersion : 1.0.0
     * pw : null
     */

    private int id;
    private String code;
    private int deviceModelId;
    private int deviceTypeId;
    private int deviceVersionId;
    private int branchId;
    private int status;
    private int networkingStatus;
    private String createTime;
    private int merchantId;
    private boolean bind;
    private String endTime;
    private String startTime;
    private int bindType;
    private String remark;
    private String merchantCode;
    private String merchantName;
    private String branchCode;
    private String branchName;
    private String deviceType;
    private String deviceModel;
    private String deviceVersion;
    private Object pw;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDeviceModelId() {
        return deviceModelId;
    }

    public void setDeviceModelId(int deviceModelId) {
        this.deviceModelId = deviceModelId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public int getDeviceVersionId() {
        return deviceVersionId;
    }

    public void setDeviceVersionId(int deviceVersionId) {
        this.deviceVersionId = deviceVersionId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNetworkingStatus() {
        return networkingStatus;
    }

    public void setNetworkingStatus(int networkingStatus) {
        this.networkingStatus = networkingStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isBind() {
        return bind;
    }

    public void setBind(boolean bind) {
        this.bind = bind;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getBindType() {
        return bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public Object getPw() {
        return pw;
    }

    public void setPw(Object pw) {
        this.pw = pw;
    }
}

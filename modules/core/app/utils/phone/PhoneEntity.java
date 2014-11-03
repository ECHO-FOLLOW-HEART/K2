package utils.phone;

/**
 * 表示一个电话号码的实体
 * <p/>
 * @author Zephyre
 */
public class PhoneEntity {
    /**
     * 国家代码
     */
    private int dialCode;

    public int getDialCode() {
        return dialCode;
    }

    void setDialCode(int val) {
        dialCode = val;
    }

    /**
     * 区号
     */
    private String regionNumber;

    public String getRegionNumber() {
        return regionNumber;
    }

    void setRegionNumber(String val) {
        regionNumber = val;
    }

    /**
     * 电话号码
     */
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    void setPhoneNumber(String val) {
        phoneNumber = val;
    }

    /**
     * 分机号
     */
    private String extension;

    public String getExtension() {
        return extension;
    }

    void setExtension(String val) {
        extension = val;
    }

    /**
     * 是否为手机
     */
    private Boolean cellPhone;

    public Boolean getCellPhone() {
        return cellPhone;
    }

    void setCellPhone(Boolean val) {
        cellPhone = val;
    }
}

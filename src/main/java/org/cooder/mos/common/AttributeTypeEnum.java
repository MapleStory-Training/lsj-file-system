package org.cooder.mos.common;

/**
 * @author lishujiang
 */
public enum AttributeTypeEnum {

    /**
     * 文件夹
     */
    folder(1, "文件夹"),
    /**
     * 文件
     */
    file(2, "文件");

    private int code;
    private String desc;

    AttributeTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

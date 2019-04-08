package com.zndbl.rpc.dto;

import java.io.Serializable;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
public class StudentDto implements Serializable {

    private static final long serialVersionUID = 42L;

    private Integer age;
    private String name;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
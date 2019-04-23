package com.zndbl.rpc.service;

import com.zndbl.rpc.dto.StudentDto;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
public interface StudentService {

    StudentDto getStuInfo(Integer age, String name);
}
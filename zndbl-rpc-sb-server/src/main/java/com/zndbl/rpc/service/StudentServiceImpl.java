package com.zndbl.rpc.service;

import org.springframework.stereotype.Service;

import com.zndbl.rpc.dto.StudentDto;
import com.zndbl.rpc.provider.annotation.ZndblRpcService;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
@ZndblRpcService(group = "")
@Service
public class StudentServiceImpl implements StudentService {

    @Override
    public StudentDto getStuInfo(Integer age, String name) {
        age = age + 10;
        name = name + "返回";
        StudentDto studentDto = new StudentDto();
        studentDto.setName(name);
        studentDto.setAge(age);
        return studentDto;
    }
}
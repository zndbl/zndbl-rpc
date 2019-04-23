package com.zndbl.rpc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zndbl.rpc.dto.StudentDto;
import com.zndbl.rpc.invoker.annotation.ZndblRpcRefrence;
import com.zndbl.rpc.service.StudentService;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
@RestController
public class StudentController {

    @ZndblRpcRefrence
    private StudentService studentService;

    @GetMapping("/test")
    public StudentDto http() {
        String name = "zndbl";
        Integer age = 12;
        return studentService.getStuInfo(age, name);
    }
}
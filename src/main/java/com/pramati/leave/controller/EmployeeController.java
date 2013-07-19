package com.pramati.leave.controller;


import com.pramati.leave.service.EmployeeLeaveService;
import com.pramati.leave.service.impl.EmployeeDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 13/06/13
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/employee")
public class EmployeeController {

    @Autowired
    private EmployeeLeaveService employeeLeaveService;

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public @ResponseBody EmployeeDetails getLeaves(HttpServletRequest httpServletRequest) {
        String emailId = httpServletRequest.getParameter("emailId");
        return employeeLeaveService.getDetails(emailId);
    }


}

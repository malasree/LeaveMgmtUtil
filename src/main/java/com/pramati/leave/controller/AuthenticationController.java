package com.pramati.leave.controller;

import com.pramati.leave.service.EmployeeAuthenticationService;
import com.pramati.leave.service.impl.EmployeeDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 18/06/13
 * Time: 6:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/user")
public class AuthenticationController {
    @Autowired
    private EmployeeAuthenticationService employeeAuthService;

    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    public @ResponseBody String getEmailID(HttpServletRequest httpServletRequest) {
        String emailId = httpServletRequest.getParameter("username");
        String pwd =  httpServletRequest.getParameter("password");
        return employeeAuthService.getUserMailID(emailId,pwd);
    }
}

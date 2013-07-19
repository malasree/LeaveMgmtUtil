package com.pramati.leave.service;

import com.pramati.leave.service.impl.EmployeeDetails;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 13/06/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EmployeeLeaveService {

    EmployeeDetails getDetails(String emailID);
}

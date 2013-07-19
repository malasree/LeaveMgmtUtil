package com.pramati.leave.service.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 13/06/13
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */

@XmlRootElement
public class EmployeeDetails
{
    private String emailID;
    private Date datOfJoining;
    private int totalLeaves;
    QuarterDetails quarterDetails[];

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public Date getDatOfJoining() {
        return datOfJoining;
    }

    public void setDatOfJoining(Date datOfJoining) {
        this.datOfJoining = datOfJoining;
    }

    public int getTotalLeaves() {
        return totalLeaves;
    }

    public void setTotalLeaves(int totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    public QuarterDetails[] getQuarterDetails() {
        return quarterDetails;
    }

    public void setQuarterDetails(QuarterDetails[] quarterDetails) {
        this.quarterDetails = quarterDetails;
    }
}

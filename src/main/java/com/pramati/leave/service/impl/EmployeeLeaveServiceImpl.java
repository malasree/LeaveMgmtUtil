package com.pramati.leave.service.impl;

import com.pramati.leave.parser.ILeaveParser;
import com.pramati.leave.parser.LeaveParserImpl;
import com.pramati.leave.service.EmployeeLeaveService;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.StringBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 13/06/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */

@Service(value = "employeeLeaveService")
public class EmployeeLeaveServiceImpl implements EmployeeLeaveService {

    private final String QUICKBASE_URL = "https://pramati.quickbase.com/db/";
    private final String LEAVES_DBID = "bh7sdi38s";
    private final String LEAVE_APPLICATION_DBID = "bh7sdi37c";

    public enum Quarter {
        FIRST_QUARTER(1), SECOND_QUARTER(2), THIRD_QUARTER(3), FORTH_QUARTER(4);
        private int quarter;
        private Quarter(int q) {
            this.quarter = q;
        }
    }

    public enum Month {
        JAN(0), FEB(1), MAR(2), APR(3), MAY(4), JUN(5), JUL(6), AUG(7), SEP(8), OCT(9), NOV(10), DEC(11);
        private int month;
        private Month(int m) {
            this.month = m;
        }
    }

    static Quarter currentQuarter;

    @Override
    public EmployeeDetails getDetails(String emailID)  {
        EmployeeDetails employee = new EmployeeDetails();
        currentQuarter = getRunningQuarter();
        Leave leave = null;
        try
        {
            Date doj = getEmployeeDOJ(emailID);
            employee.setEmailID(emailID);
            employee.setDatOfJoining(doj);
            employee.setTotalLeaves(calculateTotalLeaves(doj));

            String leavesList = getLeavesListOfPresentYear(emailID);

            ILeaveParser lp = new LeaveParserImpl();
            leave = lp.getLeave(new InputSource(new StringReader(leavesList)));
            employee.setQuarterDetails(getQuarterlyDetails(leave, doj, employee.getTotalLeaves()));
        }
        catch (Exception e)
        {

        }
        return employee;
    }

    private int getCarryForwordLeaves(Quarter q, int prevBalLeaves)
    {
        if(q.quarter > currentQuarter.quarter)
            return 0;
        return prevBalLeaves;
    }

    private QuarterDetails[] getQuarterlyDetails(Leave leave, Date dateOfJoining, int totalLeaves)
    {

        int availedQ1 = leave.getQ1UtilizedLeaves();
        int accruedQ1 = getAccruedLeaves(dateOfJoining, Quarter.FIRST_QUARTER);
        int balanceQ1 = balanceQ1 = totalLeaves<5? totalLeaves:5 - leave.getQ1UtilizedLeaves();
        int carryForwardQ1 = 0; //Yearly carry forword leaves are disabled.
        QuarterDetails q1 = new QuarterDetails("APR-JUN",leave.getQ1AppliedLeaves(),leave.getQ1ApprovedLeaves(),carryForwardQ1,availedQ1,balanceQ1,accruedQ1,leave.getQ1CanceledLeaves());

        int availedQ2 = leave.getQ2UtilizedLeaves();
        int accruedQ2 = getAccruedLeaves(dateOfJoining, Quarter.SECOND_QUARTER);
        int balanceQ2 = 0;
        if(currentQuarter.quarter >= Quarter.SECOND_QUARTER.quarter)
            balanceQ2 =  totalLeaves<5? totalLeaves:5 - leave.getQ2UtilizedLeaves() + balanceQ1;
        int carryForwardQ2 = getCarryForwordLeaves(Quarter.SECOND_QUARTER, balanceQ1);
        QuarterDetails q2 = new QuarterDetails("JUL-SEP",leave.getQ2AppliedLeaves(),leave.getQ2ApprovedLeaves(),carryForwardQ2,availedQ2,balanceQ2,accruedQ2,leave.getQ2CanceledLeaves());

        int availedQ3 = leave.getQ3UtilizedLeaves();
        int accruedQ3 = getAccruedLeaves(dateOfJoining, Quarter.THIRD_QUARTER);
        int balanceQ3 = 0;
        if(currentQuarter.quarter >= Quarter.THIRD_QUARTER.quarter)
            balanceQ3 = totalLeaves<5? totalLeaves:5 - leave.getQ3UtilizedLeaves() + balanceQ2;
        int carryForwardQ3 = getCarryForwordLeaves(Quarter.THIRD_QUARTER, balanceQ2);
        QuarterDetails q3 = new QuarterDetails("OCT-DEC",leave.getQ3AppliedLeaves(),leave.getQ3ApprovedLeaves(),carryForwardQ3,availedQ3,balanceQ3,accruedQ3,leave.getQ3CanceledLeaves());

        int availedQ4 = leave.getQ4UtilizedLeaves();
        int accruedQ4 = getAccruedLeaves(dateOfJoining, Quarter.FORTH_QUARTER);
        int balanceQ4 = 0;
        if(currentQuarter.quarter >= Quarter.FORTH_QUARTER.quarter)
            balanceQ4 = totalLeaves<5? totalLeaves:5 - leave.getQ4UtilizedLeaves() + balanceQ3;
        int carryForwardQ4 = getCarryForwordLeaves(Quarter.FORTH_QUARTER, balanceQ3);
        QuarterDetails q4 = new QuarterDetails("JAN-MAR",leave.getQ4AppliedLeaves(),leave.getQ4ApprovedLeaves(),carryForwardQ4,availedQ4,balanceQ4,accruedQ4,leave.getQ4CanceledLeaves());

        return new QuarterDetails[]{q1,q2,q3,q4};
    }

    private int getAccruedLeaves(Date datOfJoining, Quarter q)
    {
        if(q.quarter > currentQuarter.quarter)
            return 0;

        int accruedLeaves = 0;
        Calendar dojCal = Calendar.getInstance();
        dojCal.setTime(datOfJoining);
        int dojMonth = dojCal.get(Calendar.MONTH);

        Calendar currCal = Calendar.getInstance();
        Date currentDate = new Date();
        currCal.setTime(currentDate);
        int currMonth = currCal.get(Calendar.MONTH);

        Quarter dojQuarter = getDojQuarter(datOfJoining);
        Quarter currentQuarter = getRunningQuarter();

        int yearDiff = currCal.get(Calendar.YEAR)-dojCal.get(Calendar.YEAR);
        int monthDiff = currCal.get(Calendar.MONTH)-dojCal.get(Calendar.MONTH);

        if (yearDiff >= 1)
        {
            accruedLeaves = 5;
        }
        else if(yearDiff == 0)
        {
            if(monthDiff < 3)
            {
                accruedLeaves = 0;
            }
            else if(monthDiff >= 3 && q.quarter <= currentQuarter.quarter)
            {
                accruedLeaves = 5;
            }
            else if(q.quarter > currentQuarter.quarter)
            {
                accruedLeaves = 0;
            }
        }
        return accruedLeaves;
    }

    private Quarter getDojQuarter(Date datOfJoining)
    {
        int accruedLeaves = 0;
        Calendar dojCal = Calendar.getInstance();
        dojCal.setTime(datOfJoining);
        int dojMonth = dojCal.get(Calendar.MONTH);

        Quarter dojQuarter;
        if(dojMonth >= Month.APR.month && dojMonth <= Month.JUN.month) dojQuarter = Quarter.FIRST_QUARTER;
        else if(dojMonth >= Month.JUL.month && dojMonth <= Month.SEP.month) dojQuarter = Quarter.SECOND_QUARTER;
        else if(dojMonth >= Month.OCT.month && dojMonth <= Month.DEC.month) dojQuarter = Quarter.THIRD_QUARTER;
        else dojQuarter = Quarter.FORTH_QUARTER;
        return  dojQuarter;
    }

    private String getTicket()
    {
        String ticket = "";
        try
        {
            String a = getRequest("https://pramati.quickbase.com/db/main?act=API_Authenticate&username=appsportal.user@pramati.com&password=pr1m1ti1");
            String pattern = "<ticket>(.*?)</ticket>";
            Pattern r = Pattern.compile(pattern);

            Matcher m = r.matcher(a);
            if (m.find( )) {
                ticket = m.group(1);
            }
        }
        catch (Exception e)
        {

        }
        return ticket;
    }

    private Date getEmployeeDOJ(String emailID)
    {
        Date doj = null;
        try
        {
            String ticket = getTicket();

            StringBuffer request = new StringBuffer(QUICKBASE_URL + LEAVES_DBID +"?act=API_DoQuery&includeRids=1&ticket=");
            request.append(ticket);
            request.append("&query={'36'.EX.'");
            request.append(emailID);
            request.append("'}&clist=37&slist=3&format=structured");
            String response = getRequest(request.toString());

            String dojPattern = "<date_of_joining>(.*?)</date_of_joining>";
            Pattern pattern1 = Pattern.compile(dojPattern);

            Matcher match = pattern1.matcher(response);
            System.out.println(response);
            if (match.find( ))
            {
                long l = Long.parseLong(match.group(1));
                doj = new Date(l);
            }
        }
        catch (Exception e)
        {

        }
        return doj;
    }

    private String getLeavesListOfPresentYear(String emailID)
    {
        String response = null;
        try
        {
            String ticket = getTicket();

            StringBuffer request = new StringBuffer(QUICKBASE_URL + LEAVE_APPLICATION_DBID + "?act=API_DoQuery&includeRids=1&ticket=");
            request.append(ticket);
            request.append("&query={'125'.EX.'");
            request.append(emailID);

            request.append("'}AND{'148'.EX.'");
            request.append(getPresentYear());
            request.append("'}&clist=3.7.8.9.10.51&slist=3&format=structured");
            response = getRequest(request.toString());
        }
        catch (Exception e)
        {

        }
        return response;
    }

    private String getPresentYear()
    {
        String yearStr;
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);
        int preYear = currentYear - 1;
        int nextYear = currentYear + 1;

        if(currentMonth <= Calendar.MARCH)
        {
            yearStr = Integer.toString(preYear) + "-" +Integer.toString(currentYear);
        }
        else
        {
            yearStr = Integer.toString(currentYear) + "-" +Integer.toString(nextYear);
        }
        return yearStr;
    }

    private Quarter getRunningQuarter()
    {
        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date();
        cal.setTime(currentDate);
        int currentMonth = cal.get(Calendar.MONTH);

        if(currentMonth >= Month.APR.month && currentMonth <= Month.JUN.month) return Quarter.FIRST_QUARTER;
        else if(currentMonth >= Month.JUL.month && currentMonth <= Month.SEP.month) return Quarter.SECOND_QUARTER;
        else if(currentMonth >= Month.OCT.month && currentMonth <= Month.DEC.month) return Quarter.THIRD_QUARTER;
        else return Quarter.FORTH_QUARTER;
    }

    private int calculateTotalLeaves(Date datOfJoining )
    {
        int totalLeaves = 0;

        Calendar dojCal = Calendar.getInstance();
        dojCal.setTime(datOfJoining);
        int dojMonth = dojCal.get(Calendar.MONTH);

        Calendar currCal = Calendar.getInstance();
        Date currentDate = new Date();
        currCal.setTime(currentDate);
        int currMonth = currCal.get(Calendar.MONTH);

        Quarter dojQuarter = getDojQuarter(datOfJoining);

        int yearDiff = currCal.get(Calendar.YEAR)-dojCal.get(Calendar.YEAR);
        int monthDiff = currCal.get(Calendar.MONTH)-dojCal.get(Calendar.MONTH);
        if (yearDiff >= 2)
            totalLeaves = 5 * currentQuarter.quarter;
        else if(yearDiff == 0)
        {
            if(monthDiff < 3)
            {
                totalLeaves = 0;
            }
            else
            {
                if(monthDiff >= 3 && monthDiff <= 6)
                {
                    if(monthDiff == 3)
                    {
                        if (currCal.get(Calendar.DAY_OF_MONTH) < dojCal.get(Calendar.DAY_OF_MONTH))
                        {
                            totalLeaves = 0;
                        }
                        else{
                            totalLeaves = getNumberOfLeaves(datOfJoining) + 5;
                        }
                    }
                    else{
                        totalLeaves = getNumberOfLeaves(datOfJoining) + 5;
                    }
                }
                else if(monthDiff > 6 && monthDiff <= 9)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5 * 2;
                }
                else if(monthDiff > 9 && monthDiff <= 12)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5 * 3;
                }
            }
            if(dojQuarter == Quarter.FORTH_QUARTER)
            {
                totalLeaves = 5 * currentQuarter.quarter;
            }
        }
        else if(yearDiff == 1)
        {
            //If yearly carry forward is required. Un comment this
            /*
            if(dojQuarter == Quarter.THIRD_QUARTER)
            {
                if(currMonth == Calendar.JANUARY || currMonth == Calendar.FEBRUARY || currMonth == Calendar.MARCH)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5;
                }
                else if(currMonth == Calendar.APRIL || currMonth == Calendar.MAY || currMonth == Calendar.JUNE)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5 * 2;
                }
                else if(currMonth == Calendar.JULY || currMonth == Calendar.AUGUST || currMonth == Calendar.SEPTEMBER)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5 * 3;
                }
                else if(currMonth == Calendar.OCTOBER || currMonth == Calendar.NOVEMBER || currMonth == Calendar.DECEMBER)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5 * 4;
                }
            }
            */


            if(dojQuarter != Quarter.FORTH_QUARTER)
            {
                if(currMonth == Calendar.JANUARY || currMonth == Calendar.FEBRUARY || currMonth == Calendar.MARCH)
                {
                    totalLeaves = getNumberOfLeaves(datOfJoining) + 5;
                }
                else if(currMonth == Calendar.APRIL || currMonth == Calendar.MAY || currMonth == Calendar.JUNE)
                {
                    totalLeaves = 5;
                }
                else if(currMonth == Calendar.JULY || currMonth == Calendar.AUGUST || currMonth == Calendar.SEPTEMBER)
                {
                    totalLeaves = 5 * 2;
                }
                else if(currMonth == Calendar.OCTOBER || currMonth == Calendar.NOVEMBER || currMonth == Calendar.DECEMBER)
                {
                    totalLeaves = 5 * 3;
                }
            }
            else
            {
                totalLeaves = 5 * currentQuarter.quarter;
            }
        }
        return totalLeaves;
    }


    /*
    Calculating number of leaves based on the date of joining
     */
    private int getNumberOfLeaves(Date date)
    {
        Calendar currCal = Calendar.getInstance();
        currCal.setTime(date);
        int month = currCal.get(Calendar.MONTH);
        int day = currCal.get(Calendar.DAY_OF_MONTH);
        int workingDays = 31 - day;

        if (month == Month.JAN.month || month == Month.APR.month || month == Month.JUL.month || month == Month.OCT.month)
        {
            workingDays += 60;
        }
        else if(month == Month.FEB.month || month == Month.MAY.month || month == Month.AUG.month || month == Month.NOV.month)
        {
            workingDays += 30;
        }
        return Math.round((workingDays * 5) / 90);
    }



    private static String getRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }

}

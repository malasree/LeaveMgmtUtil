package com.pramati.leave.parser;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 14/06/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.pramati.leave.service.impl.Leave;
import org.xml.sax.InputSource;

public class LeaveParserImpl implements ILeaveParser {


    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

    @Override
    public Leave getLeave(InputSource leaveXml) throws Exception {
        //create document builder
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        //parse document
        Document doc = docBuilder.parse (leaveXml);

        //TODO: Is this required
        doc.getDocumentElement().normalize();

        //get all leave records
        NodeList records = doc.getElementsByTagName("record");

        //initialize leaves object
        Leave leaves = new Leave();

        for(int i=0; i < records.getLength(); i++) {
            //get status
            Element record = (Element) records.item(i);
            NodeList status = record.getElementsByTagName("status");
            if(status == null || status.getLength() == 0) {
                throw new Exception("Missing status element under record element");
            }
            Element statusElement = (Element) status.item(0);
            String statusText = statusElement.getTextContent();

            //get from date string
            NodeList from = record.getElementsByTagName("from");
            if(from == null || from.getLength() == 0) {
                throw new Exception("Missing from element under record element");
            }
            Element fromElement = (Element) from.item(0);
            String fromText = fromElement.getTextContent();

            //get to date string
            NodeList to = record.getElementsByTagName("to");
            if(to == null || to.getLength() == 0) {
                throw new Exception("Missing to element under record element");
            }
            Element toElement = (Element) to.item(0);
            String toText = toElement.getTextContent();

            //get type of leave

            NodeList typeOfLeave = record.getElementsByTagName("leave_type");
            if(typeOfLeave == null || typeOfLeave.getLength() == 0) {
                throw new Exception("Missing leave_type element under record element");
            }
            Element typeOfLeaveElement = (Element) typeOfLeave.item(0);
            String typeOfLeaveText = typeOfLeaveElement.getTextContent();

            //update the leave object
            if(typeOfLeaveText.equals("Paid Time Off (PTO)"))
            {
                updateLeaves(leaves, statusText, fromText, toText);
            }

        }
        return leaves;
    }

    private void updateLeaves(Leave leaves, String statusText, String fromText, String toText) {

        /*//TODO: remove this block if canceled leave count is required
          if("Canceled".equalsIgnoreCase(statusText)) {
              return;
          }*/

        //find all the dates for which the leave has been applied
        List<Date> dates = findDatesBetween(fromText, toText);

        //increment appropriate leave count for appropriate quarter
        for(Date dt : dates) {
            incrementLeave(leaves, dt, statusText);
        }
    }

    private void incrementLeave(Leave leaves, Date dt, String statusText) {
        //find the quarter into which this date falls
        int quarter = getDateQuarter(dt);

        switch (quarter) {
            case 1:
                updateQ1LeaveCount(leaves, statusText);
                break;
            case 2:
                updateQ2LeaveCount(leaves, statusText);
                break;
            case 3:
                updateQ3LeaveCount(leaves, statusText);
                break;
            case 4:
                updateQ4LeaveCount(leaves, statusText);
                break;
        }
    }

    private void updateQ4LeaveCount(Leave leaves, String statusText) {
        if("Applied".equalsIgnoreCase(statusText)) {
            leaves.setQ4AppliedLeaves(leaves.getQ4AppliedLeaves() + 1);
        } else if("Utilized".equalsIgnoreCase(statusText)) {
            leaves.setQ4UtilizedLeaves(leaves.getQ4UtilizedLeaves() + 1);
        } else if("Approved".equalsIgnoreCase(statusText)) {
            leaves.setQ4ApprovedLeaves(leaves.getQ4ApprovedLeaves() + 1);
        } else if("Cancelled".equalsIgnoreCase(statusText)) {
            leaves.setQ4CanceledLeaves(leaves.getQ4CanceledLeaves() + 1);
        }
    }

    private void updateQ3LeaveCount(Leave leaves, String statusText) {
        if("Applied".equalsIgnoreCase(statusText)) {
            leaves.setQ3AppliedLeaves(leaves.getQ3AppliedLeaves() + 1);
        } else if("Utilized".equalsIgnoreCase(statusText)) {
            leaves.setQ3UtilizedLeaves(leaves.getQ3UtilizedLeaves() + 1);
        } else if("Approved".equalsIgnoreCase(statusText)) {
            leaves.setQ3ApprovedLeaves(leaves.getQ3ApprovedLeaves() + 1);
        } else if("Cancelled".equalsIgnoreCase(statusText)) {
            leaves.setQ3CanceledLeaves(leaves.getQ3CanceledLeaves() + 1);
        }
    }

    private void updateQ2LeaveCount(Leave leaves, String statusText) {
        if("Applied".equalsIgnoreCase(statusText)) {
            leaves.setQ2AppliedLeaves(leaves.getQ2AppliedLeaves() + 1);
        } else if("Utilized".equalsIgnoreCase(statusText)) {
            leaves.setQ2UtilizedLeaves(leaves.getQ2UtilizedLeaves() + 1);
        } else if("Approved".equalsIgnoreCase(statusText)) {
            leaves.setQ2ApprovedLeaves(leaves.getQ2ApprovedLeaves() + 1);
        } else if("Cancelled".equalsIgnoreCase(statusText)) {
            leaves.setQ2CanceledLeaves(leaves.getQ2CanceledLeaves() + 1);
        }
    }

    private void updateQ1LeaveCount(Leave leaves, String statusText) {
        if("Applied".equalsIgnoreCase(statusText)) {
            leaves.setQ1AppliedLeaves(leaves.getQ1AppliedLeaves() + 1);
        } else if("Utilized".equalsIgnoreCase(statusText)) {
            leaves.setQ1UtilizedLeaves(leaves.getQ1UtilizedLeaves() + 1);
        } else if("Approved".equalsIgnoreCase(statusText)) {
            leaves.setQ1ApprovedLeaves(leaves.getQ1ApprovedLeaves() + 1);
        } else if("Cancelled".equalsIgnoreCase(statusText)) {
            leaves.setQ1CanceledLeaves(leaves.getQ1CanceledLeaves() + 1);
        }
    }

    private int getDateQuarter(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int month = cal.get(Calendar.MONTH);
        if(month>=Calendar.APRIL && month <=Calendar.JUNE) {
            return 1;
        } else if(month >=Calendar.JULY && month <=Calendar.SEPTEMBER) {
            return 2;
        } else if((month >=Calendar.OCTOBER && month <=Calendar.DECEMBER)) {
            return 3;
        } else if(month >=Calendar.JANUARY && month <=Calendar.MARCH) {
            return 4;
        } else {
            return 0;
        }
    }

    private List<Date> findDatesBetween(String fromText, String toText) {
        List<Date> dates = new ArrayList<Date>();

        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(Long.parseLong(fromText));
        fromCal.set(Calendar.HOUR, 0);
        fromCal.set(Calendar.MINUTE, 0);
        fromCal.set(Calendar.SECOND, 0);
        fromCal.set(Calendar.MILLISECOND, 0);

        Calendar toCal = Calendar.getInstance();
        toCal.setTimeInMillis(Long.parseLong(toText));
        toCal.set(Calendar.HOUR, 0);
        toCal.set(Calendar.MINUTE, 0);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);

        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(fromCal.getTime());

        while(tempCal.before(toCal) || tempCal.equals(toCal)) {
            if(tempCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && tempCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
            {
                dates.add(tempCal.getTime());
            }
            tempCal.add(Calendar.DATE, 1);
        }

        return dates;
    }
}

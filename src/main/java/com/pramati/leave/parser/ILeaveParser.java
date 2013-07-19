package com.pramati.leave.parser;


import com.pramati.leave.service.impl.Leave;
import org.xml.sax.InputSource;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 14/06/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */



public interface ILeaveParser {

    Leave getLeave(InputSource leaveXml) throws Exception;
}
package com.pramati.leave.service.impl;

import com.pramati.leave.service.EmployeeAuthenticationService;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 18/06/13
 * Time: 6:15 PM
 * To change this template use File | Settings | File Templates.
 */

@Service(value = "employeeAuthenticationService")
public class EmployeeAuthenticationServiceImpl implements EmployeeAuthenticationService{

    @Override
    public String getUserMailID(String username, String pwd)
    {
        String email = null;

        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://ldap.pramati.com:389");

        // Authenticate as S. User and password "mysecret"
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL,
                "uid="+username+",ou=Employees,dc=pramati,dc=com");
        env.put(Context.SECURITY_CREDENTIALS, pwd);

        try {
            // Create initial context
            DirContext ctx = new InitialDirContext(env);
            System.out.println("&&&& ctx &&&&&&&" + ctx);
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"mail"};
            constraints.setReturningAttributes(attrIDs);
            NamingEnumeration<SearchResult> answer = ctx.search("ou=Employees,dc=pramati,dc=com", "uid="
                    + username, constraints);
            if (answer.hasMore()) {
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                email =   attrs.get("mail").toString() ;
                email = email.substring(6,attrs.get("mail").toString().length());
            }else{
                throw new Exception("Invalid User");
            }
            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return email;
    }
}

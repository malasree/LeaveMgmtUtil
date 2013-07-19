package com.pramati.leave.service.impl;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 18/06/13
 * Time: 6:01 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LDAPClient {

    public String getEmail(String user, String pwd){
        System.out.println("start ::: ");
        String email = null;

        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://ldap.pramati.com:389");

        // Authenticate as S. User and password "mysecret"
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL,
                "uid="+user+",ou=Employees,dc=pramati,dc=com");
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
                    + user, constraints);
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

    public static void main(String[] args) {
        LDAPClient lClient = new LDAPClient();
        //bmtrac
        System.out.println(" &&&&&& "+lClient.getEmail("malasreeb", ""));
    }

}

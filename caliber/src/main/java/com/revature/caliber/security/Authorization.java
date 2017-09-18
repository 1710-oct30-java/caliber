package com.revature.caliber.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Martino on 1/25/2017.
 */
public interface Authorization {

    /**
     * Generates authURI with provided parameters
     * @return view of the generated URI
     */
    ModelAndView openAuthURI();
    /**
     * Creates salesforce token and saves it as a session cookie
     * @param code the string returned from the authURI required for getting token from salesforce
     * @param
     * @return back to the application
     * @throws IOException
     */
    ModelAndView generateSalesforceToken(String code, HttpServletRequest request) throws IOException;
}

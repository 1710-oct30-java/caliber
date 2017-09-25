package com.revature.caliber.security.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.revature.caliber.security.Authorization;
import com.revature.caliber.security.models.SalesforceUser;

/**
 * Created by louislopez on 1/18/17.
 */

@Controller
public class AuthorizationImpl extends AbstractSalesforceSecurityHelper implements Authorization {
	@Value("/caliber/")
	private String forwardUrl;
	private static final Logger log = Logger.getLogger(AuthorizationImpl.class);

	@Value("#{systemEnvironment['CALIBER_DEV_MODE']}")
	private boolean debug;
	private static final String REDIRECT = "redirect:";

	private static final String REVATURE = "http://www.revature.com/";

	public AuthorizationImpl() {
		super();
	}

	/**
	 * Redirects the request to perform authentication.
	 * 
	 */
	@RequestMapping("/")
	public ModelAndView openAuthURI() {
		if (debug) {
			return new ModelAndView(REDIRECT + redirectUrl);
		}
		log.debug("redirecting to salesforce authorization");
		return new ModelAndView(REDIRECT + loginURL + authURL + "?response_type=code&client_id=" + clientId
				+ "&redirect_uri=" + redirectUri);
	}

	/**
	 * Retrieves Salesforce authentication token from Salesforce REST API
	 * 
	 * @param code
	 */
	@RequestMapping("/authenticated")
	public ModelAndView generateSalesforceToken(@RequestParam(value = "code") String code, Model model)
			throws IOException {
		log.debug("in authenticated method");
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(loginURL + accessTokenURL);
		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
		parameters.add(new BasicNameValuePair("client_secret", clientSecret));
		parameters.add(new BasicNameValuePair("client_id", clientId));
		parameters.add(new BasicNameValuePair("redirect_uri", redirectUri));
		parameters.add(new BasicNameValuePair("code", code));
		post.setEntity(new UrlEncodedFormEntity(parameters));
		log.debug("Generating Salesforce token");
		HttpResponse response = httpClient.execute(post);
		model.addAttribute("salestoken", toJsonString(response.getEntity().getContent()));
		log.debug("Forwarding to : " + redirectUrl);
		return new ModelAndView(forwardUrl);
	}

	/**
	 * Clears session information and logout the user.
	 * 
	 * Note: Still retrieving 302 on access-token and null refresh-token
	 * 
	 * @param auth
	 * @param session
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	@RequestMapping(value = "/revoke", method = RequestMethod.GET)
	public ModelAndView revoke(Authentication auth, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException, ServletException {
		if (auth == null)
			return new ModelAndView(REDIRECT + REVATURE);
		if (!debug) {
			// revoke all tokens from the Salesforce
			String accessToken = ((SalesforceUser) auth.getPrincipal()).getSalesforceToken().getAccessToken();
			revokeToken(accessToken);
		}

		// logout and clear Spring Security Context
		servletRequest.logout();
		SecurityContextHolder.clearContext();

		log.info("User has logged out");
		return new ModelAndView(REDIRECT + REVATURE);
	}

	private void revokeToken(String token) throws ClientProtocolException, IOException {
		log.debug("POST " + loginURL + revokeUrl);
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(loginURL + revokeUrl);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("token", token));
		post.setEntity(new UrlEncodedFormEntity(parameters));
		HttpResponse response = httpClient.execute(post);
		log.debug("Revoke token : " + response.getStatusLine().getStatusCode() + " "
				+ response.getStatusLine().getReasonPhrase());
	}

	public void setAuthURL(String authURL) {
		this.authURL = authURL;
	}

	public void setAccessTokenURL(String accessTokenURL) {
		this.accessTokenURL = accessTokenURL;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
}

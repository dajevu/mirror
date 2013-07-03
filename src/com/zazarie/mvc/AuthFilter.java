package com.zazarie.mvc;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


@Service
public class AuthFilter implements Filter {

	private static final Logger LOG = Logger.getLogger(AuthFilter.class.getSimpleName());
	
	private AuthUtil authUtil;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		
		if (response instanceof HttpServletResponse
				&& request instanceof HttpServletRequest) {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			// Are we in the middle of a task?
			if (httpRequest.getRequestURI().indexOf("task") > 0) {
				LOG.info("Skipping task filter");
				filterChain.doFilter(request, response);
				return;
			}

			// Redirect to https when on App Engine since subscriptions only
			// work over https
			if (httpRequest.getServerName().contains("appspot.com")
					&& httpRequest.getScheme().equals("http")) {

				httpResponse.sendRedirect(httpRequest.getRequestURL()
						.toString().replaceFirst("http", "https"));
				return;
			}

			// Are we in the middle of an google auth flow? IF so skip check.
			if (httpRequest.getRequestURI().equals("/authorize-google")) {
				LOG.info("Skipping google auth check during auth flow");
				filterChain.doFilter(request, response);
				return;
			}

			// Are we in the middle of a reddit auth flow? IF so skip check.
			if (httpRequest.getRequestURI().equals("/authorize-reddit")) {
				LOG.info("Skipping reddit auth check during auth flow");
				filterChain.doFilter(request, response);
				return;
			}

			// Is this a robot visit to the notify servlet? If so skip check
			if (httpRequest.getRequestURI().equals("/notify")) {
				LOG.info("Skipping auth check for notify servlet");
				filterChain.doFilter(request, response);
				return;
			}
			
			// Is this a robot visit to the notify servlet? If so skip check
			if (httpRequest.getRequestURI().contains("/_ah")) {
				LOG.info("Skipping auth check for admin servlet : " + httpRequest.getRequestURI());
				filterChain.doFilter(request, response);
				return;
			}
			
			
			UserService userService = UserServiceFactory.getUserService();

			// determine if user is logged into Google 
			String thisURL = httpRequest.getRequestURI();

	        response.setContentType("text/html");
	        
	        if (httpRequest.getUserPrincipal() != null) {
	            
	        	request.setAttribute("googleLoggedin", true);
	        	
	        	httpRequest.getSession().setAttribute("googleEmail",  userService.getCurrentUser().getEmail());
	        	
	        	request.setAttribute("googleLogoutRedirect",  userService.createLogoutURL(thisURL));
	            
	        } else {

	        	request.setAttribute("googleLoggedin", false);
	        	
	        	httpRequest.getSession().setAttribute("googleOauthSession", null);
	        	
	        	httpRequest.getSession().setAttribute("redditOauthSession", null);
	        	
	        	httpRequest.getSession().setAttribute("googleEmail",  null);
	        	
	        	request.setAttribute("googleLoginRedirect",  userService.createLoginURL(thisURL));
	        }


			if (AuthUtil.getUserId(httpRequest) == null
					|| authUtil.getCredential(AuthUtil.getUserId(httpRequest)) == null
					|| authUtil.getCredential(AuthUtil.getUserId(httpRequest)).getAccessToken() == null) {

				// redirect to auth flow
				// httpResponse.sendRedirect(WebUtil.buildUrl(httpRequest,
				// "/oauth2callback"));
				request.setAttribute("loggedin", false);

				if (AuthUtil.isRedditLoggedIn(httpRequest)) {
					request.setAttribute("reddit-loggedin", true);
				} else
					request.setAttribute("reddit-loggedin", false);

				filterChain.doFilter(request, response);
				return;

			} else {
				request.setAttribute("loggedin", true);

				if (AuthUtil.isRedditLoggedIn(httpRequest)) {
					request.setAttribute("reddit-loggedin", true);
				} else
					request.setAttribute("reddit-loggedin", false);
			}

			// Things checked out OK :)
			filterChain.doFilter(request, response);
		} else {
			LOG.warning("Unexpected non HTTP servlet response. Proceeding anyway.");
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	public AuthUtil getAuthUtil() {
		return authUtil;
	}

	@Autowired(required = true)
	public void setAuthUtil(AuthUtil authUtil) {
		this.authUtil = authUtil;
	}
}

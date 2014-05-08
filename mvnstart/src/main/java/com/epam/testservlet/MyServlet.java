package com.epam.testservlet;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 
public class MyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
 
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
 
    @Override
    public void destroy() {
    }

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}

	 
    
}

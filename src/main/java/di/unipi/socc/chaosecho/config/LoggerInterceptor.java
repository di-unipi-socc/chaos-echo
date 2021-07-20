package di.unipi.socc.chaosecho.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoggerInterceptor implements HandlerInterceptor {

    private static Logger log = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Received " + request.getMethod() + " request from " + request.getRemoteAddr() + " (request_id: " + request.getHeader("X-Request-ID") + ")");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("Handled " + request.getMethod() + " request from " + request.getRemoteAddr() + " (request_id: " + request.getHeader("X-Request-ID") + ")");
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,Object handler, Exception ex) throws Exception {
        if (ex == null) 
            log.info("Answered to " + request.getMethod() + " request from " + request.getRemoteAddr() + " with code: " + response.getStatus() + " (request_id: " + request.getHeader("X-Request-ID") + ")");
    }
}

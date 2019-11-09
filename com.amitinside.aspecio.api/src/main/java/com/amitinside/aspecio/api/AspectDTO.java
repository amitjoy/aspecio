package com.amitinside.aspecio.api;

import java.util.List;
import org.osgi.dto.DTO;

/**
 * The data transfer object of an aspect and the interceptors providing it.
 */
public final class AspectDTO extends DTO {

    /**
     * The aspect name.
     */
    public String aspectName;

    /**
     * The currently chosen interceptor for that aspect
     */
    public InterceptorDTO interceptor;

    /**
     * The sorted list of interceptors that will replace the current interceptor if the chosen
     * interceptor is unregistered.<br>
     * Interceptors are chosen using by comparing their ServiceReference, so a higher service ranking
     * is preferred, and in the case of equal service rankings a lower service id will be chosen.
     */
    public List<InterceptorDTO> backupInterceptors;

}

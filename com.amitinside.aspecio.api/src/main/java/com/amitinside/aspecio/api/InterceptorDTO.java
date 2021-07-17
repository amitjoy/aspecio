package com.amitinside.aspecio.api;

import java.util.Set;

import org.osgi.dto.DTO;

/**
 * The data transfer object describing an interceptor as seen by Aspecio.
 *
 * @NotThreadSafe
 */
public class InterceptorDTO extends DTO {

    public long serviceId;
    public long bundleId;
    public int serviceRanking;
    public Class<?> interceptorClass;
    public Set<String> extraProperties;

}

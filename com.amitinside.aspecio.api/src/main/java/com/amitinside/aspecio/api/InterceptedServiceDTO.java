package com.amitinside.aspecio.api;

import java.util.List;
import java.util.Set;
import org.osgi.dto.DTO;

/**
 * The data transfer object describing a service candidate to interception by Aspecio, along with
 * the Aspects it define and its status.
 */
public final class InterceptedServiceDTO extends DTO {

  public long         serviceId;
  public long         bundleId;
  public List<String> objectClass;
  public boolean      published;
  public Set<String>  satisfiedAspects;
  public Set<String>  unsatisfiedRequiredAspects;
  public Set<String>  requiredAspects;
  public Set<String>  optionalAspects;

}

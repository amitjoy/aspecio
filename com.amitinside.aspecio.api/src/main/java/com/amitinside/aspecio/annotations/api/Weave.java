package com.amitinside.aspecio.annotations.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.osgi.service.component.annotations.ComponentPropertyType;
import com.amitinside.aspecio.api.AspecioConstants;

/**
 * Component Property Type for Aspect Weaving.
 *
 * <ul>
 * <ul>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE}</li>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE_OPTIONAL}</li>
 * </ul>
 * </ul>
 *
 * <p>
 * This annotation can be used on a {@link Component} to request Aspects to be woven.
 *
 * @see "Component Property Types"
 */
@ComponentPropertyType
@Target(ElementType.TYPE)
public @interface Weave {

  String PREFIX_ = "service.aspect.weave.";

  /**
   * The required aspects to weave. The woven service will not be published unless all of the
   * required aspects are present. <br>
   * The deregistration of any required aspect will also cause the woven service to be unregistered.
   *
   * @return the required aspects.
   */
  Class<?>[] required() default {};

  /**
   * The optional aspects to weave. The woven service will be published even if the optional aspects
   * are absent. <br>
   * The registration of an optional aspect will allow these aspects to intercept the service
   * methods, even if the service was previously published without that aspect.
   *
   * @return the optional aspects.
   */
  Class<?>[] optional() default {};

}

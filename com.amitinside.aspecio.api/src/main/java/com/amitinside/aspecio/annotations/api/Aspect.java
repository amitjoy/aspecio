package com.amitinside.aspecio.annotations.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.osgi.service.component.annotations.ComponentPropertyType;
import com.amitinside.aspecio.api.AspecioConstants;

/**
 * Component Property Type for Aspect.
 *
 * <ul>
 * <li>{@link AspecioConstants#SERVICE_ASPECT}</li>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_EXTRAPROPERTIES}</li>
 * </ul>
 *
 * <p>
 * This annotation can be used on a {@link Component} to define aspects. The component class must be
 * assignable to Interceptor to be recognized as an Aspect.
 *
 * @see "Component Property Types"
 */
@ComponentPropertyType
@Retention(RUNTIME)
@Target({ TYPE, TYPE_USE })
public @interface Aspect {

    String PREFIX_ = "service.aspect.";

    /**
     * The name of the aspect to provide. A class is used here to piggyback on Java's namespacing and
     * avoid conflitcs in aspect names.
     *
     * @return The aspect class to provide
     */
    Class<?> name();

    /**
     * The extra properties that will be published, with value {@link Boolean#TRUE} to services woven
     * with this aspect. Defaults to the empty array.
     *
     * @return the extra properties
     */
    String[] extraProperties() default {};
}

package io.j13n.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MapArgumentResolver());
    }

    public static class MapArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            // Check if the parameter is a Map<String, String> or MultiValueMap<String, String>
            // and doesn't have a @RequestParam annotation with a specific name
            boolean isMapType = Map.class.isAssignableFrom(parameter.getParameterType()) ||
                   MultiValueMap.class.isAssignableFrom(parameter.getParameterType());

            boolean hasNoNameSpecified = false;
            if (parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestParam.class)) {
                // If it has @RequestParam annotation, check if name is not specified
                org.springframework.web.bind.annotation.RequestParam requestParam = 
                    parameter.getParameterAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
                hasNoNameSpecified = requestParam.name().isEmpty() && requestParam.value().isEmpty();
            } else {
                // If it doesn't have @RequestParam annotation, consider it as no name specified
                hasNoNameSpecified = true;
            }

            return isMapType && hasNoNameSpecified;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            // Get all parameter names and values from the request
            Map<String, String[]> parameterMap = webRequest.getParameterMap();

            if (MultiValueMap.class.isAssignableFrom(parameter.getParameterType())) {
                // Create a MultiValueMap and populate it with all request parameters
                org.springframework.util.MultiValueMap<String, String> multiValueMap = new org.springframework.util.LinkedMultiValueMap<>();

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();

                    for (String value : values) {
                        multiValueMap.add(key, value);
                    }
                }

                return multiValueMap;
            } else {
                // Create a Map<String, String> and populate it with all request parameters
                // If a parameter has multiple values, use the first one
                Map<String, String> map = new HashMap<>();

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();

                    if (values != null && values.length > 0) {
                        map.put(key, values[0]);
                    }
                }

                return map;
            }
        }
    }
}

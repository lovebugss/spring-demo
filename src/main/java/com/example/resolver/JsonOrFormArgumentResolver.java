package com.example.resolver;

import com.example.annotation.JsonOrFormBody;
import com.example.util.JsonUtils;
import com.example.util.RequestUtils;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义json/form 参数解析器
 *
 * @author renjp
 * @date 2021/8/20 11:13
 */
public class JsonOrFormArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 包含{@link JsonOrFormBody 注解}
     *
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JsonOrFormBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory binderFactory) throws Exception {


        // 获取request
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        parameter = parameter.nestedIfOptional();
        // type
        Class<?> clazz = parameter.getParameterType();
        Object arg = null;
        if (HttpMethod.GET.matches(request.getMethod())) {
            // Get 请求, 从url中获取
            arg = this.resolveArgumentFromQueryStr(request, clazz);
        } else {
            String contentType = request.getContentType();
            if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                // json 转对象
                arg = resolveArgumentFromJson(request, clazz);

            } else {
                arg = resolveArgumentFromParameter(request, clazz);
            }
        }
        String name = Conventions.getVariableNameForParameter(parameter);

        // 参数验证
        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(nativeWebRequest, arg, name);
            if (arg != null) {
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors()) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }

        }
        return arg;
    }

    private void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation ann : annotations) {
            Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
            if (validationHints != null) {
                binder.validate(validationHints);
                break;
            }
        }
    }

    /**
     *  从request body中获取
     * @param request
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object resolveArgumentFromJson(HttpServletRequest request, Class<?> clazz) throws InstantiationException, IllegalAccessException {
        BufferedReader reader = null;
        try {
            reader = request.getReader();

            String jsonStr = reader.lines().collect(Collectors.joining());
            return JsonUtils.parser(jsonStr, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clazz.newInstance();
    }

    /**
     * 从parameter参数中解析
     *
     * @param request
     * @param clazz
     * @return
     */
    private Object resolveArgumentFromParameter(HttpServletRequest request, Class<?> clazz) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> collect = parameterMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]));
        return JsonUtils.mapToObj(collect, clazz);
    }

    /**
     * 从QueryString 中获取参数
     *
     * @param clazz
     * @param request
     * @return
     */
    private Object resolveArgumentFromQueryStr(HttpServletRequest request, Class<?> clazz) {
        String queryString = request.getQueryString();
        Map<String, Object> queryStrToMap = RequestUtils.queryStrToMap(queryString);
        return JsonUtils.mapToObj(queryStrToMap, clazz);
    }
}

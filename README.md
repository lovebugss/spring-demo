# Spring 同时支持application/json, application/x-www-form-urlencoded

在重构php项目中, 发现php中使用的框架是同时支持json 和form 请求参数. 为了兼容原有请求方式, 需要支持这写法.

在网上找到几种方式, 但是决定自己写一个.

## 自定义注解

```jsx
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER})
public @interface JsonOrFormBody {
}
```

自定义参数解析

```jsx
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
```

## 注册到spring

```jsx
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new GenderEnumConverter());
    }

    /**
     * 添加自定义参数解析器
     *
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new JsonOrFormArgumentResolver());
    }

}
```
git: https://github.com/lovebugss/spring-demo
保底方案:

自己解析参数,

```jsx
@RequestMapping("/call")
public String callGet(HttpServletRequest request) throws IOException {

      Map<String, Object> param = new HashMap<>();
      String method = request.getMethod();
      if (HttpMethod.GET.matches(method)) {
          String queryString = request.getQueryString();
          RequestUtils.queryStringToMap(queryString).forEach(param::put);
      } else {
          String contentType = request.getContentType();
          // json
          if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
              BufferedReader reader = request.getReader();
              String jsonStr = IoUtils.toString(reader);
              // 检查是否为合法json
              boolean validJson = JsonUtils.isValidJson(jsonStr);
              if (validJson) {
                  Map<String, Object> jsonObj = JsonUtils.parseToMap(jsonStr, false);
                  if (!Objects.isNull(jsonObj)) {
                      jsonObj.forEach(param::put);
                  }
              }

          } else {
              Map<String, String[]> parameterMap = request.getParameterMap();
              parameterMap.forEach((k, v) -> param.put(k, v[0]));
          }

      }
```

参考:

[https://jiacyer.com/2019/01/23/Java-Spring-form-json-compatibility/](https://jiacyer.com/2019/01/23/Java-Spring-form-json-compatibility/)

[https://www.cnblogs.com/java-zhao/p/9119258.html](https://www.cnblogs.com/java-zhao/p/9119258.html)

[https://programmer.group/spring-mvc-custom-parameter-resolver.html](https://programmer.group/spring-mvc-custom-parameter-resolver.html)

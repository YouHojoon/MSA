package com.thoughtmechanix.zuulserver.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.thoughtmechanix.zuulserver.model.AbtestingRoute;
import com.thoughtmechanix.zuulserver.util.FilterUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpecialRoutesFilter extends ZuulFilter {
    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;
    @Autowired
    private FilterUtils filterUtils;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ProxyRequestHelper helper;

    @Override
    public String filterType() {
        return FilterUtils.ROUTE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        AbtestingRoute abtestingRoute = getAbRoutingInfo(filterUtils.getServiceId());
        if (abtestingRoute != null && useSpecialRoute(abtestingRoute)) {
            String route = buildRouteString(ctx.getRequest().getRequestURI(), abtestingRoute.getEndpoint()
                    , ctx.get("serviceId").toString());
            forwardToSpecialRoute(route);
        }
        return null;
    }

    private AbtestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbtestingRoute> restExchange = null;
        try {
            restExchange = restTemplate.exchange("http://specialRouteService/route/abtesting/{serviceName}",
                    HttpMethod.GET, null, AbtestingRoute.class, serviceName);
        } catch (HttpClientErrorException ex) {
            if (restExchange.getStatusCode() == HttpStatus.NOT_FOUND)
                return null;
            throw ex;
        }
        return restExchange.getBody();
    }

    public boolean useSpecialRoute(AbtestingRoute testRoute) {
        Random random = new Random();
        if (testRoute.getActive().equals("N"))
            return false;
        int value = random.nextInt(10) + 1;
        if (testRoute.getWeight() < value)
            return true;
        return false;
    }

    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
        int index = oldEndpoint.indexOf(serviceName);
        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        System.out.println("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    private void forwardToSpecialRoute(String route) {
        RequestContext context = RequestContext.getCurrentContext();
        ProxyRequestHelper helper = new ProxyRequestHelper(new ZuulProperties());
        HttpServletRequest request = context.getRequest();
        MultiValueMap<String, String> headers = helper.buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = helper.buildZuulRequestQueryParams(request);
        String verb = request.getMethod().toUpperCase();
        HttpResponse response = null;
        try {
            InputStream requestEntity = request.getInputStream();
            if (request.getContentLength() < 0)
                context.setChunkedRequestBody();
            this.helper.addIgnoredHeaders();
            CloseableHttpClient httpClient = HttpClients.createDefault();
            response = forward(httpClient, verb, route, request, headers, params, requestEntity);
            helper.setResponse(response.getStatusLine().getStatusCode(), response.getEntity()==null ? null :
                    response.getEntity().getContent(),revertHeaders(response.getAllHeaders()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private HttpResponse forward(HttpClient httpclient, String verb, String uri,
                                 HttpServletRequest request, MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
                requestEntity);
        URL host = new URL(uri);
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());

        HttpRequest httpRequest;
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null
                        ? ContentType.create(request.getContentType()) : null);
        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);

        }
        try {
            httpRequest.setHeaders(convertHeaders(headers));
            HttpResponse zuulResponse = httpclient.execute(httpHost, httpRequest);

            return zuulResponse;
        } finally {
        }
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }
    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }
}

package com.markdown.editor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IpLocationUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpLocationUtil.class);
    
    private static final Map<String, String> ipLocationMap = new ConcurrentHashMap<>();
    
    static {
        initIpLocationData();
    }
    
    public static String getLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length >= 3) {
            String prefix2 = parts[0] + "." + parts[1] + ".";
            String prefix3 = parts[0] + "." + parts[1] + "." + parts[2] + ".";
            
            if (ipLocationMap.containsKey(prefix3)) {
                return ipLocationMap.get(prefix3);
            }
            
            if (ipLocationMap.containsKey(prefix2)) {
                return ipLocationMap.get(prefix2);
            }
        }
        
        return null;
    }
    
    private static void initIpLocationData() {
        ipLocationMap.put("117.9.", "天津市");
        ipLocationMap.put("117.10.", "天津市");
        ipLocationMap.put("117.11.", "天津市");
        ipLocationMap.put("117.12.", "天津市");
        ipLocationMap.put("117.13.", "天津市");
        ipLocationMap.put("117.14.", "天津市");
        ipLocationMap.put("117.15.", "天津市");
        ipLocationMap.put("117.16.", "天津市");
        ipLocationMap.put("117.17.", "天津市");
        ipLocationMap.put("117.18.", "天津市");
        ipLocationMap.put("117.19.", "天津市");
        ipLocationMap.put("117.20.", "天津市");
        ipLocationMap.put("117.21.", "天津市");
        ipLocationMap.put("117.22.", "天津市");
        ipLocationMap.put("117.23.", "天津市");
        ipLocationMap.put("117.24.", "天津市");
        ipLocationMap.put("117.25.", "天津市");
        ipLocationMap.put("117.26.", "天津市");
        ipLocationMap.put("117.27.", "天津市");
        ipLocationMap.put("117.28.", "天津市");
        ipLocationMap.put("117.29.", "天津市");
        ipLocationMap.put("117.30.", "天津市");
        ipLocationMap.put("117.31.", "天津市");
        ipLocationMap.put("117.32.", "天津市");
        ipLocationMap.put("117.92.", "天津市");
        ipLocationMap.put("117.93.", "天津市");
        ipLocationMap.put("117.94.", "天津市");
        ipLocationMap.put("117.95.", "天津市");
        ipLocationMap.put("117.96.", "天津市");
        
        ipLocationMap.put("124.65.", "北京市");
        ipLocationMap.put("124.66.", "北京市");
        ipLocationMap.put("124.67.", "北京市");
        ipLocationMap.put("124.68.", "北京市");
        ipLocationMap.put("124.69.", "北京市");
        ipLocationMap.put("202.106.", "北京市");
        ipLocationMap.put("202.96.", "北京市");
        ipLocationMap.put("114.114.", "北京市");
        ipLocationMap.put("106.12.", "北京市");
        
        ipLocationMap.put("116.2.", "上海市");
        ipLocationMap.put("116.3.", "上海市");
        ipLocationMap.put("116.4.", "上海市");
        ipLocationMap.put("116.5.", "上海市");
        ipLocationMap.put("116.6.", "上海市");
        ipLocationMap.put("116.7.", "上海市");
        ipLocationMap.put("116.8.", "上海市");
        ipLocationMap.put("116.9.", "上海市");
        ipLocationMap.put("116.10.", "上海市");
        ipLocationMap.put("116.11.", "上海市");
        ipLocationMap.put("116.12.", "上海市");
        ipLocationMap.put("116.13.", "上海市");
        ipLocationMap.put("116.14.", "上海市");
        ipLocationMap.put("116.15.", "上海市");
        ipLocationMap.put("116.16.", "上海市");
        ipLocationMap.put("116.17.", "上海市");
        ipLocationMap.put("116.18.", "上海市");
        ipLocationMap.put("116.19.", "上海市");
        ipLocationMap.put("116.20.", "上海市");
        ipLocationMap.put("202.96.", "上海市");
        ipLocationMap.put("180.168.", "上海市");
        
        ipLocationMap.put("119.147.", "广东省");
        ipLocationMap.put("119.148.", "广东省");
        ipLocationMap.put("119.149.", "广东省");
        ipLocationMap.put("119.150.", "广东省");
        ipLocationMap.put("119.151.", "广东省");
        ipLocationMap.put("119.152.", "广东省");
        ipLocationMap.put("119.153.", "广东省");
        ipLocationMap.put("119.154.", "广东省");
        ipLocationMap.put("119.155.", "广东省");
        ipLocationMap.put("119.156.", "广东省");
        ipLocationMap.put("119.157.", "广东省");
        ipLocationMap.put("119.158.", "广东省");
        ipLocationMap.put("119.159.", "广东省");
        ipLocationMap.put("119.160.", "广东省");
        ipLocationMap.put("120.23.", "广东省");
        ipLocationMap.put("120.24.", "广东省");
        ipLocationMap.put("120.25.", "广东省");
        ipLocationMap.put("120.26.", "广东省");
    }
}
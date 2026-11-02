package com.xinyi.rongli.integration.config;
import java.util.Map;
import java.util.HashMap;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix="vi.integration.keys")
public class IntegrationKeyProperties {
 private String currentVersion="";private Map<String,String> versions=new HashMap<>();
 public String getCurrentVersion(){return currentVersion;}public void setCurrentVersion(String value){currentVersion=value;}
 public Map<String,String> getVersions(){return versions;}public void setVersions(Map<String,String> value){versions=value;}
 @Override public String toString(){return "IntegrationKeyProperties[REDACTED]";}
}

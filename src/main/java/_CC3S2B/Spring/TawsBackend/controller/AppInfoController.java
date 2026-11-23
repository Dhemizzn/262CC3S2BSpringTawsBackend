package _CC3S2B.Spring.TawsBackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AppInfoController {

    @Value("${spring.application.name:TawsBackend}")
    private String application;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${app.description:Backend Peru TOURS}")
    private String description;

    @GetMapping("/")
    public Map<String, Object> getAppInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", application);
        info.put("description", description);
        info.put("version", version);
        info.put("status", "UP");
        info.put("message", "Backend running successfully");
        return info;
    }
}

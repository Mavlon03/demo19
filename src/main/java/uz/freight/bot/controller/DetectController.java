package uz.freight.bot.controller;

import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.freight.bot.service.RegionDetectorService;

@RestController
@RequestMapping("/api")
public class DetectController {

    private final RegionDetectorService detectorService;

    public DetectController(RegionDetectorService detectorService) {
        this.detectorService = detectorService;
    }

    @PostMapping("/detect")
    public ResponseEntity<Set<Long>> detect(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        Set<Long> groupIds = detectorService.detect(text);
        return ResponseEntity.ok(groupIds);
    }
}

package cn.zy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class OrderController {

//  @Value("${name}")
  private String name;

  @GetMapping("/finderchina/api/v2/listing/vehicle")
  public String test(@RequestParam("marketplace") String marketplace,
      @RequestParam("language") String language, @RequestParam("vin") String vin) {
    return marketplace + language + vin;
  }

}

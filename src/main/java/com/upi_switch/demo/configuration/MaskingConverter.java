package com.upi_switch.demo.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaskingConverter extends CompositeConverter<ILoggingEvent> {
    public String transform(ILoggingEvent event, String in) {
        in = in.replaceAll("\"mobileNo\":\"(\\d{5})(\\d+?)\"", "\"mobileNo\":\"XXXXX$2\"");
        in = in.replaceAll("\"mobile\":\"(\\d{5})(\\d+?)\"", "\"mobile\":\"XXXXX$2\"");
        in = in.replaceAll("\"phoneNumber\":\"(\\d{5})(\\d{5})\"", "\"phoneNumber\":\"XXXXX$2\"");
        in = in.replaceAll("\"email\":\"(\\w{2})[^\"]*(@[^\"]+)\"", "\"email\":\"$1****$2\"");

        return in;
    }
}

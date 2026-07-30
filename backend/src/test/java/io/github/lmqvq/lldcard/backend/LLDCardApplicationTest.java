package io.github.lmqvq.lldcard.backend;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LLDCardApplicationTest {

    @Test
    void initializesShanghaiTimezone() {
        TimeZone previous = TimeZone.getDefault();
        try {
            new LLDCardApplication().init();

            assertEquals("Asia/Shanghai", TimeZone.getDefault().getID());
        } finally {
            TimeZone.setDefault(previous);
        }
    }
}
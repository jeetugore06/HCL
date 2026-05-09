package com.hcl.hackathon.scoring.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaskingUtilTest {

    @Test
    void shouldMaskCardCorrectly() {

        String masked =
                MaskingUtil.maskCard(
                        "4567123412345678");

        assertEquals(
                "XXXX-XXXX-XXXX-5678",
                masked);
    }
}
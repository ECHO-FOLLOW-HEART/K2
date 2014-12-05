package com.taozilvxing.test;

import org.junit.Test;
import play.test.WithApplication;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/5/14.
 */
public class UserTest extends WithApplication {
    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

}

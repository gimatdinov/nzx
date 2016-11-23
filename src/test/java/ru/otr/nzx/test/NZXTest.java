package ru.otr.nzx.test;

import ru.otr.nzx.NZXApplication;

public class NZXTest {

    public static void main(String... args) {
        new NZXApplication().run(new String[]{"-n", "Test", "-c", "src/test/config/nzx_TEST.conf"});
    }

}

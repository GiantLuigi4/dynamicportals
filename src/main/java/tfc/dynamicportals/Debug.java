package tfc.dynamicportals;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class Debug {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void TheUnimplemented(String s) {
        LOGGER.error("TheUnimplemented::" + s);
    }
}

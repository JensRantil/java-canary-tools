/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.jensrantil.tools.canary.utilities;

import io.github.jensrantil.tools.canary.list.LinkedList;

public class StringUtils {
    public static String join(LinkedList source) {
        return JoinUtils.join(source);
    }

    public static LinkedList split(String source) {
        return SplitUtils.split(source);
    }
}

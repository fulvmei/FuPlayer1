package com.chengfu.fuplayer.text;

import java.util.List;

/**
 * Receives text output.
 */
public interface TextOutput {

    /**
     * Called when there is a change in the {@link String}s.
     *
     * @param cues The {@link String}s.
     */
    void onCues(List<String> cues);

}

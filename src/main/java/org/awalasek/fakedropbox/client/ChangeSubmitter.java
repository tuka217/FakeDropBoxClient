package org.awalasek.fakedropbox.client;

import org.awalasek.fakedropbox.common.FileChange;

public interface ChangeSubmitter {
    void submitFileChange(FileChange fileChange);
}

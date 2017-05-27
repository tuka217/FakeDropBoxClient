package org.awalasek.fakedropbox.client;

import org.awalasek.FakeDropBox.common.FileChange;

public interface ChangeSubmitter {
    void submitFileChange(FileChange fileChange);
}

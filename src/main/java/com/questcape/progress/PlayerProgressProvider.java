package com.questcape.progress;

import java.io.IOException;

public interface PlayerProgressProvider
{
    AccountProgress lookup(String username) throws IOException;
}

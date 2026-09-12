package com.optimalquestguide.progress;

import java.io.IOException;

public interface PlayerProgressProvider
{
    AccountProgress lookup(String username) throws IOException;
}

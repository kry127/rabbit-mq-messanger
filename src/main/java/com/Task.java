package com;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Task {
    public void run() throws IOException, TimeoutException;
}

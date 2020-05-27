package com.ifs;

import java.io.IOException;
import java.time.ZonedDateTime;

public interface Message {
    String getMessage();
    String getAuthor();
    ZonedDateTime getDateTime();
    byte[] getBytes() throws IOException;
    String toString();
}

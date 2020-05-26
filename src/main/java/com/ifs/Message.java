package com.ifs;

import java.io.IOException;
import java.time.ZonedDateTime;

public interface Message {
    public String getMessage();
    public String getAuthor();
    public ZonedDateTime getDateTime();
    public byte[] getBytes() throws IOException;
    public String toString();
}

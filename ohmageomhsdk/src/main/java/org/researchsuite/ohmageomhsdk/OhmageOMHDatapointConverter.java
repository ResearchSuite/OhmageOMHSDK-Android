package org.researchsuite.ohmageomhsdk;

import com.squareup.tape.FileObjectQueue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by jameskizer on 4/27/17.
 */

public class OhmageOMHDatapointConverter implements FileObjectQueue.Converter<String> {
    @Override
    public String from(byte[] bytes) throws IOException {
        return new String(bytes);
    }

    @Override
    public void toStream(String o, OutputStream bytes) throws IOException {
        Writer writer = new OutputStreamWriter(bytes);
        writer.append(o);
        writer.close();
    }
}

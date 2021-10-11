package ru.falsher.base64;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;

public class Base64EncodeInputStream extends InputStream {

    private final InputStream in;

    private int extra = 0;
    private boolean END_OF_INPUT = false;

    private int buffer = 0;
    private int bufferLen = 0;

    public Base64EncodeInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (END_OF_INPUT && extra == 0) return -1;
        if (END_OF_INPUT) {
            extra--;
            return '=';
        }
        if (bufferLen == 6) {
            bufferLen = 0;
            return getCharFromIndex(buffer);
        }
        int i;
        if (bufferLen > 0) i = buffer << (6 - bufferLen);
        else i = 0;
        int b = in.read();
        if (b == -1) {
            END_OF_INPUT = true;
            if (bufferLen == 0) return -1;
            else if (bufferLen == 2) {
                extra = 2;
                return getCharFromIndex(i);
            } else if (bufferLen == 4) {
                extra = 1;
                return getCharFromIndex(i);
            } else if (bufferLen == 6) return getCharFromIndex(i);
            else return -1;
        } else {
            i |= b >> (2 + bufferLen);
            buffer = b & ~(0xFF << (2 + bufferLen));
            bufferLen += 2;
            return getCharFromIndex(i);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private int getCharFromIndex(int i){
        if (i < 0) return 'A';
        else if (i < 26) return 'A' + i;
        else if (i < 52) return 'a' + (i - 26);
        else if (i < 62) return '0' + (i - 52);
        else if (i == 62) return '+';
        else if (i == 63) return '/';
        else return 'A';
    }

}